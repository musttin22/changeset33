package l2jorion.game.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import l2jorion.Config;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.templates.StatsSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;

public class DressmeItems
{
	private static final Logger LOG = LoggerFactory.getLogger(DressmeItems.class);
	
	private final Map<String, StatsSet> _playerSkins = new ConcurrentHashMap<>();
	
	private static DressmeItems _instance = null;
	
	private DressmeItems()
	{
	}
	
	public void insertIntoDb(String unique_id, String skinType, int skinId, long sysTime)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO characters_dressme_items (unique_id,skin_type,skin_id,sys_time) VALUES (?,?,?,?)");
			statement.setString(1, unique_id);
			statement.setString(2, skinType);
			statement.setInt(3, skinId);
			statement.setLong(4, sysTime);
			
			final StatsSet data = new StatsSet();
			data.set("unique_id", unique_id);
			data.set("skin_type", skinType);
			data.set("skin_id", skinId);
			data.set("sys_time", sysTime);
			
			_playerSkins.put(unique_id, data);
			
			statement.executeUpdate();
			DatabaseUtils.close(statement);
		}
		catch (final SQLException e)
		{
			LOG.error(getClass().getSimpleName() + ": Error - It could not insert data: " + e.getMessage());
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
	
	public void restoreFromDb()
	{
		_playerSkins.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT unique_id,skin_type,skin_id,sys_time FROM characters_dressme_items");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				final String unique_id = rs.getString("unique_id");
				final String skin_type = rs.getString("skin_type");
				final int skin_id = rs.getInt("skin_id");
				final long sys_time = rs.getLong("sys_time");
				
				final StatsSet data = new StatsSet();
				data.set("unique_id", unique_id);
				data.set("skin_type", skin_type);
				data.set("skin_id", skin_id);
				data.set("sys_time", sys_time);
				
				_playerSkins.put(unique_id, data);
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			
			if (_playerSkins.size() > 0)
			{
				LOG.info("DressMeData: Restored " + _playerSkins.size() + " skins");
			}
		}
		catch (final SQLException e)
		{
			LOG.error(getClass().getSimpleName() + ": Error - It could not restore data: " + e.getMessage());
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
	
	private void removeFromDb(final String unique_id)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM characters_dressme_items WHERE unique_id=?");
			statement.setString(1, (unique_id));
			statement.executeUpdate();
			DatabaseUtils.close(statement);
			
		}
		catch (final Exception e)
		{
			LOG.error(getClass().getSimpleName() + ": Error - It could not delete data from DB: " + e.getMessage());
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
	
	public void checkDressMeItems(L2PcInstance player)
	{
		if (DressmeItems.getInstance().getAllData() != null && DressmeItems.getInstance().getAllData().entrySet() != null)
		{
			for (Entry<String, StatsSet> uniqueKey : DressmeItems.getInstance().getAllData().entrySet())
			{
				String unique_id = uniqueKey.getValue().getString("unique_id");
				String skin_type = uniqueKey.getValue().getString("skin_type");
				int skin_id = uniqueKey.getValue().getInteger("skin_id");
				
				long timeLeft = (uniqueKey.getValue().getLong("sys_time") - System.currentTimeMillis());
				if (timeLeft <= 0)
				{
					switch (skin_type.toLowerCase())
					{
						case "armor":
							if (player.getArmorSkinOption() == skin_id)
							{
								player.setArmorSkinOption(0);
							}
							player.deleteArmorSkin(skin_id);
							break;
						case "weapon":
							if (player.getWeaponSkinOption() == skin_id)
							{
								player.setWeaponSkinOption(0);
							}
							player.deleteWeaponSkin(skin_id);
							break;
						case "hair":
							if (player.getHairSkinOption() == skin_id)
							{
								player.setHairSkinOption(0);
							}
							player.deleteHairSkin(skin_id);
							break;
						case "face":
							if (player.getFaceSkinOption() == skin_id)
							{
								player.setFaceSkinOption(0);
							}
							player.deleteFaceSkin(skin_id);
							break;
						case "shield":
							if (player.getShieldSkinOption() == skin_id)
							{
								player.setShieldSkinOption(0);
							}
							player.deleteShieldSkin(skin_id);
							break;
					}
					
					DressmeItems.getInstance().deleteFromList(unique_id, true);
					player.broadcastUserInfo();
				}
			}
		}
	}
	
	public StatsSet getList(String uniqueId)
	{
		return _playerSkins.get(uniqueId);
	}
	
	public Map<String, StatsSet> getAllData()
	{
		return _playerSkins;
	}
	
	public StatsSet deleteFromList(String uniqueId, boolean removeFromDB)
	{
		if (removeFromDB)
		{
			removeFromDb(uniqueId);
		}
		
		return _playerSkins.remove(uniqueId);
	}
	
	public static DressmeItems getInstance()
	{
		return _instance == null ? (_instance = new DressmeItems()) : _instance;
	}
	
	public static void reload()
	{
		_instance = null;
		
		getInstance();
	}
	
}