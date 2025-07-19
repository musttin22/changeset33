/**
 * @Author    DenArt Designs
 * @Developer Nightwolf
 *
 * Documentation: https://shop.denart-designs.com/documentation/
 * Purchased: https://shop.denart-designs.com/
 * License: https://shop.denart-designs.com/activate.php?license
 * License Activation: https://shop.denart-designs.com/activate.php ( ^ first you must purchase the license)
 *
 * Created for DenArt Designs that holds the ownership of this files
 * Removing this constitutes violation of the agreement.
 */
package l2jorion.game.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.ItemList;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;

public class ItemDeliveryManager
{
	protected static Logger LOG = LoggerFactory.getLogger(ItemDeliveryManager.class);
	
	private final static String UPDATE = "UPDATE user_item_delivery SET status=1 WHERE id=?;";
	private final static String SELECT = "SELECT id, item_id, item_count, char_name FROM user_item_delivery WHERE status=0;";
	
	@SuppressWarnings("unused")
	private ScheduledFuture<?> _autoCheck;
	
	public static ItemDeliveryManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemDeliveryManager _instance = new ItemDeliveryManager();
	}
	
	protected ItemDeliveryManager()
	{
		_autoCheck = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckTask(), 5000, 5000);
		LOG.info("Item Delivery Manager: started.");
	}
	
	protected class CheckTask implements Runnable
	{
		@Override
		public void run()
		{
			int id = 0;
			int item_id = 0;
			int item_count = 0;
			String char_name = "";
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				
				final PreparedStatement statement = con.prepareStatement(SELECT);
				final ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					id = rset.getInt("id");
					item_id = rset.getInt("item_id");
					item_count = rset.getInt("item_count");
					char_name = rset.getString("char_name");
					if (item_id > 0 && item_count > 0 && char_name != "")
					{
						for (L2PcInstance activeChar : L2World.getInstance().getAllPlayers().values())
						{
							if (activeChar == null || activeChar.isOnline() == 0)
							{
								continue;
							}
							if (activeChar.getName().toLowerCase().equals(char_name.toLowerCase()))
							{
								activeChar.getInventory().addItem("Delivery", item_id, item_count, activeChar, null);
								activeChar.getInventory().updateDatabase();
								activeChar.sendPacket(new ItemList(activeChar, true));
								activeChar.sendMessage("Delivery of " + item_count + " coins.");
								UpdateDelivery(id);
								activeChar.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
					}
				}
				DatabaseUtils.close(rset);
				DatabaseUtils.close(statement);
			}
			catch (final SQLException e)
			{
				LOG.error("Check delivery items failed. " + e.getMessage());
			}
			finally
			{
				CloseUtil.close(con);
			}
		}
	}
	
	static void UpdateDelivery(int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(UPDATE);
			statement.setInt(1, id);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOG.error("Failed to update item delivery id: " + id);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}