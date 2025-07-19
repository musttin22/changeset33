package l2jorion.game.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2jorion.game.model.Location;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public class DMSpawnLocations
{
	private static final Logger LOG = LoggerFactory.getLogger(DMSpawnLocations.class);
	
	private final static Map<Integer, List<Location>> _DMSpawnLocations = new HashMap<>();
	private static final String SELECT_LOCS = "SELECT * FROM `dm_spawn_locations` ORDER BY `date` DESC";
	
	public DMSpawnLocations()
	{
		loadSpawnLocations();
	}
	
	public void reload()
	{
		_DMSpawnLocations.clear();
		
		loadSpawnLocations();
	}
	
	public void loadSpawnLocations()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_LOCS);
			
			List<Location> data = new ArrayList<>();
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					String location = rs.getString("location");
					String[] coord = location.split(",");
					
					int x = Integer.parseInt(coord[0]);
					int y = Integer.parseInt(coord[1]);
					int z = Integer.parseInt(coord[2]);
					data.add(new Location(x, y, z));
				}
				_DMSpawnLocations.put(0, data);
			}
		}
		catch (Exception e)
		{
			LOG.warn(DMSpawnLocations.class.getSimpleName() + ": Couldn't load DM event spawn locations");
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		LOG.info(DMSpawnLocations.class.getSimpleName() + ": Loaded " + _DMSpawnLocations.get(0).size() + " DM event spawn locations");
	}
	
	public List<Location> getAllSpawnLocations()
	{
		return _DMSpawnLocations.get(0);
	}
	
	public static DMSpawnLocations getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DMSpawnLocations _instance = new DMSpawnLocations();
	}
}