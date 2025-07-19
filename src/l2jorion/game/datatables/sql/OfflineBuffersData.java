package l2jorion.game.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import l2jorion.Config;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.L2GameClient;
import l2jorion.game.network.L2GameClient.GameClientState;
import l2jorion.game.thread.LoginServerThread;
import l2jorion.log.Log;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.Util;
import l2jorion.util.database.L2DatabaseFactory;

public class OfflineBuffersData
{
	private static Logger LOG = LoggerFactory.getLogger(OfflineBuffersData.class);
	
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_buffers_data (`charId`,`name`,`time`,`buffPrice`) VALUES (?,?,?,?)";
	private static final String DELETE_OFFLINE_BUFFER = "DELETE FROM character_offline_buffers_data where charId=?";
	private static final String CLEAR_OFFLINE_BUFFERS_TABLE = "DELETE FROM character_offline_buffers_data";
	private static final String LOAD_OFFLINE_BUFFER_STATUS = "SELECT * FROM character_offline_buffers_data";
	
	public static final OfflineBuffersData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	// Save on server shutdown/restart
	public void storeOfflineBuffersOnShutDown()
	{
		int buffers = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement stm = con.prepareStatement(CLEAR_OFFLINE_BUFFERS_TABLE);
			stm.execute();
			stm.close();
			
			con.setAutoCommit(false);
			
			stm = con.prepareStatement(SAVE_OFFLINE_STATUS);
			
			for (L2PcInstance pc : L2World.getInstance().getAllPlayers().values())
			{
				try
				{
					if ((pc.isSellingBuff()))
					{
						stm.setInt(1, pc.getObjectId());
						stm.setString(2, pc.getName());
						stm.setLong(3, pc.getOfflineStartTime());
						stm.setInt(4, pc.getBuffPrice());
						stm.executeUpdate();
						stm.clearParameters();
						con.commit();
						buffers++;
					}
				}
				catch (SQLException e)
				{
					LOG.error(getClass().getSimpleName() + ": Error while saving offline buffer: " + pc.getObjectId(), e);
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
				}
			}
			
			stm.close();
			
			if (buffers > 0)
			{
				LOG.info(getClass().getSimpleName() + ": Saved " + buffers + " offline buffer" + (buffers > 1 ? "s" : ""));
			}
		}
		catch (SQLException e)
		{
			LOG.error(getClass().getSimpleName() + ": Error while saving offline buffers data: ", e);
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void restoreOfflineBuffers()
	{
		Connection con = null;
		int buffers = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement(LOAD_OFFLINE_BUFFER_STATUS);
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				long time = rs.getLong("time");
				
				if (Config.OFFLINE_BUFFER_MAX_DAYS > 0)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_BUFFER_MAX_DAYS);
					
					if (cal.getTimeInMillis() <= System.currentTimeMillis())
					{
						LOG.info(getClass().getSimpleName() + ": Offline buffer " + rs.getString("name") + " (" + rs.getInt("charId") + ") reached limit of offline time, kicked");
						continue;
					}
				}
				
				int buffPrice = rs.getInt("buffPrice");
				
				L2PcInstance player = null;
				
				try
				{
					L2GameClient client = new L2GameClient(null);
					player = L2PcInstance.load(rs.getInt("charId"));
					L2World.getInstance().addPlayerToWorld(player);
					
					client.setActiveChar(player);
					client.setAccountName(player.getAccountName());
					client.setState(GameClientState.IN_GAME);
					player.setOfflineMode(true);
					player.setOfflineStartTime(time);
					
					player.spawnMe(player.getX(), player.getY(), player.getZ());
					
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					
					player.setBuffPrice(buffPrice);
					player.sitDown();
					player.setTeam(1);
					player.setSellingBuff(true);
					
					player.setOnlineStatus(true);
					player.restoreEffects();
					
					if (Config.OFFLINE_SLEEP_EFFECT)
					{
						player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_SLEEP);
					}
					
					player.broadcastUserInfo();
					buffers++;
				}
				catch (Exception e)
				{
					LOG.error(getClass().getSimpleName() + ": Error loading buffer: ", e);
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					if (player != null)
					{
						player.logout();
					}
				}
			}
			
			rs.close();
			stm.close();
			
			if (buffers > 0)
			{
				Util.printSection("Offline buffers");
				LOG.info(getClass().getSimpleName() + ": Loaded " + buffers + " offline buffer" + (buffers > 1 ? "s" : ""));
			}
		}
		catch (Exception e)
		{
			LOG.error(getClass().getSimpleName() + ": Error while loading offline buffers: ", e);
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	// Store on exit
	public void storeOfflineBufferOnExit(L2PcInstance pc)
	{
		if (!pc.isSellingBuff())
		{
			return;
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement stm = con.prepareStatement(DELETE_OFFLINE_BUFFER);
			stm.setInt(1, pc.getObjectId());
			stm.execute();
			stm.clearParameters();
			stm.close();
			
			con.setAutoCommit(false);
			
			stm = con.prepareStatement(SAVE_OFFLINE_STATUS);
			
			try
			{
				stm.setInt(1, pc.getObjectId()); // Char Id
				stm.setString(2, pc.getName()); // char name
				stm.setLong(3, pc.getOfflineStartTime());
				stm.setInt(4, pc.getBuffPrice()); // buff price
			}
			catch (Exception e)
			{
				LOG.error(getClass().getSimpleName() + ": Error while saving offline buffer: " + pc.getObjectId(), e);
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
			
			stm.execute();
			stm.clearParameters();
			stm.close();
			
			String text = "Offline buffer " + pc.getName() + " stored.";
			Log.add(text, "Offline_buffer");
		}
		catch (Exception e)
		{
			LOG.error(getClass().getSimpleName() + ": Error while saving offline buffer data: ", e);
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineBuffersData _instance = new OfflineBuffersData();
	}
}