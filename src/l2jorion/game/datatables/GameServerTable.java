package l2jorion.game.datatables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javolution.io.UTF8StreamReader;
import javolution.util.FastMap;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;
import l2jorion.Config;
import l2jorion.ConfigLoader;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.login.GameServerThread;
import l2jorion.login.network.gameserverpackets.ServerStatus;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;
import l2jorion.util.random.Rnd;

public class GameServerTable
{
	private static Logger LOG = LoggerFactory.getLogger(GameServerTable.class);
	
	private static GameServerTable _instance;
	
	private static Map<Integer, String> _serverNames = new FastMap<>();
	
	private final Map<Integer, GameServerInfo> _gameServerTable = new FastMap<Integer, GameServerInfo>().shared();
	
	private static final int KEYS_SIZE = 10;
	private KeyPair[] _keyPairs;
	
	public static void load() throws GeneralSecurityException
	{
		if (_instance == null)
		{
			_instance = new GameServerTable();
		}
		else
		{
			throw new IllegalStateException("Load can only be invoked a single time.");
		}
	}
	
	public static GameServerTable getInstance()
	{
		return _instance;
	}
	
	public GameServerTable() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		loadServerNames();
		LOG.info("Loaded " + _serverNames.size() + " server names");
		
		loadRegisteredGameServers();
		LOG.info("Loaded " + _gameServerTable.size() + " registered Game Servers");
		
		loadRSAKeys();
		LOG.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication");
	}
	
	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);
		
		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < KEYS_SIZE; i++)
		{
			_keyPairs[i] = keyGen.genKeyPair();
		}
	}
	
	private void loadServerNames()
	{
		final XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
		final UTF8StreamReader reader = new UTF8StreamReader();
		
		InputStream in = null;
		try
		{
			File conf_file = new File(ConfigLoader.SERVER_NAME_FILE);
			in = new FileInputStream(conf_file);
			xpp.setInput(reader.setInput(in));
			
			for (int e = xpp.getEventType(); e != XMLStreamConstants.END_DOCUMENT; e = xpp.next())
			{
				if (e == XMLStreamConstants.START_ELEMENT)
				{
					if (xpp.getLocalName().toString().equals("server"))
					{
						Integer id = Integer.valueOf(xpp.getAttributeValue(null, "id").toString());
						String name = xpp.getAttributeValue(null, "name").toString();
						_serverNames.put(id, name);
					}
				}
			}
			
		}
		catch (final FileNotFoundException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.warn("servername.xml could not be loaded: file not found");
		}
		catch (final XMLStreamException xppe)
		{
			xppe.printStackTrace();
		}
		finally
		{
			try
			{
				xpp.close();
			}
			catch (final XMLStreamException e)
			{
				e.printStackTrace();
			}
			
			try
			{
				reader.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void loadRegisteredGameServers()
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			int id;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery();
			GameServerInfo gsi;
			
			while (rset.next())
			{
				id = rset.getInt("server_id");
				gsi = new GameServerInfo(id, stringToHex(rset.getString("hexid")));
				_gameServerTable.put(id, gsi);
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			
			e.printStackTrace();
			
		}
		finally
		{
			CloseUtil.close(con);
			
		}
		
	}
	
	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}
	
	public GameServerInfo getRegisteredGameServerById(final int id)
	{
		return _gameServerTable.get(id);
	}
	
	public boolean hasRegisteredGameServerOnId(final int id)
	{
		return _gameServerTable.containsKey(id);
	}
	
	public boolean registerWithFirstAvaliableId(final GameServerInfo gsi)
	{
		// avoid two servers registering with the same "free" id
		synchronized (_gameServerTable)
		{
			for (final Entry<Integer, String> entry : _serverNames.entrySet())
			{
				if (!_gameServerTable.containsKey(entry.getKey()))
				{
					_gameServerTable.put(entry.getKey(), gsi);
					gsi.setId(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean register(final int id, final GameServerInfo gsi)
	{
		synchronized (_gameServerTable)
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}
	
	public void registerServerOnDB(final GameServerInfo gsi)
	{
		this.registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}
	
	public void registerServerOnDB(final byte[] hexId, final int id, final String externalHost)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			statement.setString(1, hexToString(hexId));
			statement.setInt(2, id);
			statement.setString(3, externalHost);
			statement.executeUpdate();
			
			DatabaseUtils.close(statement);
		}
		catch (final SQLException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.warn("SQL error while saving gameserver: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public String getServerNameById(final int id)
	{
		return getServerNames().get(id);
	}
	
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}
	
	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}
	
	private byte[] stringToHex(final String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private String hexToString(final byte[] hex)
	{
		if (hex == null)
		{
			return "null";
		}
		
		return new BigInteger(hex).toString(16);
	}
	
	public static class GameServerInfo
	{
		// auth
		private int _id;
		private final byte[] _hexId;
		private boolean _isAuthed;
		
		// status
		private GameServerThread _gst;
		private int _status;
		
		// network
		private String _internalIpas;
		private String _externalIpas;
		private String _externalHostas;
		private int _port;
		
		// config
		private final boolean _isPvp = true;
		private boolean _isTestServer;
		private boolean _isShowingClock;
		private boolean _isShowingBrackets;
		private int _maxPlayers;
		
		public GameServerInfo(final int id, final byte[] hexId, final GameServerThread gst)
		{
			_id = id;
			_hexId = hexId;
			_gst = gst;
			_status = ServerStatus.STATUS_DOWN;
		}
		
		public GameServerInfo(final int id, final byte[] hexId)
		{
			this(id, hexId, null);
		}
		
		public void setId(final int id)
		{
			_id = id;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public byte[] getHexId()
		{
			return _hexId;
		}
		
		public void setAuthed(final boolean isAuthed)
		{
			_isAuthed = isAuthed;
		}
		
		public boolean isAuthed()
		{
			return _isAuthed;
		}
		
		public void setGameServerThread(final GameServerThread gst)
		{
			_gst = gst;
		}
		
		public GameServerThread getGameServerThread()
		{
			return _gst;
		}
		
		public void setStatus(final int status)
		{
			_status = status;
		}
		
		public int getStatus()
		{
			return _status;
		}
		
		public int getCurrentPlayerCount()
		{
			if (_gst == null)
			{
				return 0;
			}
			
			return _gst.getPlayerCount();
		}
		
		public void setInternalIp(final String internalIpas)
		{
			_internalIpas = internalIpas;
		}
		
		public String getInternalHost()
		{
			return _internalIpas;
		}
		
		public void setExternalIp(final String externalIpas)
		{
			_externalIpas = externalIpas;
		}
		
		public String getExternalIpas()
		{
			return _externalIpas;
		}
		
		public void setExternalHost(final String externalHostas)
		{
			_externalHostas = externalHostas;
		}
		
		public String getExternalHost()
		{
			return _externalHostas;
		}
		
		public int getPort()
		{
			return _port;
		}
		
		public void setPort(final int portas)
		{
			_port = portas;
		}
		
		public void setMaxPlayers(final int maxPlayers)
		{
			_maxPlayers = maxPlayers;
		}
		
		public int getMaxPlayers()
		{
			return _maxPlayers;
		}
		
		public boolean isPvp()
		{
			return _isPvp;
		}
		
		public void setTestServer(final boolean val)
		{
			_isTestServer = val;
		}
		
		public boolean isTestServer()
		{
			return _isTestServer;
		}
		
		public void setShowingClock(final boolean clock)
		{
			_isShowingClock = clock;
		}
		
		public boolean isShowingClock()
		{
			return _isShowingClock;
		}
		
		public void setShowingBrackets(final boolean val)
		{
			_isShowingBrackets = val;
		}
		
		public boolean isShowingBrackets()
		{
			return _isShowingBrackets;
		}
		
		public void setDown()
		{
			setAuthed(false);
			setPort(0);
			setGameServerThread(null);
			setStatus(ServerStatus.STATUS_DOWN);
		}
	}
}
