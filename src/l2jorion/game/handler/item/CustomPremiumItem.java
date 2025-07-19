package l2jorion.game.handler.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import l2jorion.Config;
import l2jorion.game.handler.IItemHandler;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2PlayableInstance;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public class CustomPremiumItem implements IItemHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(CustomPremiumItem.class);
	
	private String INSERT_DATA = "REPLACE INTO account_premium (account_name, premium_service, enddate) VALUES (?,?,?)";
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (Config.PREMIUM_CUSTOM_ITEMS)
		{
			if (!(playable instanceof L2PcInstance))
			{
				return;
			}
			
			L2PcInstance player = (L2PcInstance) playable;
			
			if (player.isInOlympiadMode())
			{
				player.sendMessage("This item cannot be used on Olympiad Games.");
				return;
			}
			
			if (player.getPremiumService() >= 1)
			{
				player.sendMessage("You're already The Premium account!");
				return;
			}
			
			// Old Method
			/*
			 * int days = Config.PREMIUM_CUSTOM_DAY; int status = 1; switch (item.getItemId()) { case 9999: days = Config.PREMIUM_CUSTOM_DAY; status = 1; break; case 10000: days = 10; status = 2; break; case 10001: days = 30; status = 3; break; } player.setPremiumService(status);
			 * updateDatabase(player, status, days * 24L * 60L * 60L * 1000L);
			 */
			
			// New method
			int itemId = 0;
			int timeInHours = 0;
			for (int[] premItem : Config.PREMIUM_CUSTOM_ITEM)
			{
				if (item.getItemId() == premItem[0])
				{
					itemId = premItem[0];
					timeInHours = premItem[1];
				}
			}
			
			if (itemId == 0)
			{
				player.sendMessage("Something went wrong.");
				return;
			}
			
			player.setPremiumService(1);
			updateDatabase(player, 1, timeInHours * 60L * 60L * 1000L);
			
			player.sendMessage("Congratulations! You're The Premium account.");
			player.sendPacket(new ExShowScreenMessage("Congratulations! You're The Premium account.", 4000, 0x02, false));
			player.sendPacket(new PlaySound("ItemSound.quest_fanfare_1"));
			
			if (Config.PREMIUM_NAME_COLOR_ENABLED && player.getPremiumService() >= 1)
			{
				player.getAppearance().setTitleColor(Config.PREMIUM_TITLE_COLOR);
			}
			
			if (Config.PREMIUM_BUFF_MULTIPLIER > 0)
			{
				player.restoreEffects();
			}
			
			player.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		List<int[]> list = new ArrayList<>();
		for (int[] premItemId : Config.PREMIUM_CUSTOM_ITEM)
		{
			list.add(premItemId);
		}
		
		return list.stream().mapToInt(i -> i[0]).toArray();
	}
	
	private void updateDatabase(L2PcInstance player, int status, long premiumTime)
	{
		Connection con = null;
		try
		{
			if (player == null)
			{
				return;
			}
			
			player.setPremiumExpire(System.currentTimeMillis() + premiumTime);
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_DATA);
			
			stmt.setString(1, player.getAccountName());
			stmt.setInt(2, status);
			stmt.setLong(3, premiumTime == 0 ? 0 : System.currentTimeMillis() + premiumTime);
			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			LOG.error(getClass().getSimpleName() + ": could not update database ", e);
			
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
}
