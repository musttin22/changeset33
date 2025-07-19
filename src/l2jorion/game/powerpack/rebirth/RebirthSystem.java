package l2jorion.game.powerpack.rebirth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import l2jorion.Config;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.datatables.xml.ExperienceData;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.managers.RebirthManager;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.RebirthHolder;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.StatsSet;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;

public class RebirthSystem implements ICustomByPassHandler
{
	// private static Logger LOG = LoggerFactory.getLogger(RebirthSystem.class);
	
	protected static final Map<Integer, StatsSet> _playersRebirthData = new ConcurrentHashMap<>();
	
	private static int _pageLimit = 6;
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"rebirth"
		};
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		final StringTokenizer st = new StringTokenizer(parameters);
		st.nextToken();
		
		if (player == null)
		{
			return;
		}
		
		if (parameters.startsWith("perform"))
		{
			String[] cmd = parameters.split(" ");
			int pageId = Integer.valueOf(cmd[1]);
			
			showPerformWindow(player, pageId);
		}
		else if (parameters.startsWith("rebirthMain"))
		{
			showChatWindow(player);
		}
		else if (parameters.startsWith("requestRebirth"))
		{
			String[] cmd = parameters.split(" ");
			int skillId = Integer.valueOf(cmd[1]);
			int id = Integer.valueOf(cmd[2]);
			int itemId = Integer.valueOf(cmd[3]);
			int itemAmount = Integer.valueOf(cmd[4]);
			
			requestRebirth(player, skillId, id, itemId, itemAmount);
			
			if (!player.isSwitchingRebirthSkill())
			{
				showChatWindow(player);
			}
			else
			{
				showPerformWindow(player, 0);
			}
		}
		else if (parameters.startsWith("requestSwitchRebirth"))
		{
			if (player.isSwitchingRebirthSkill())
			{
				player.sendMessage("The switch is activated already.");
				player.sendPacket(new ExShowScreenMessage("The switch is activated already.", 2000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
			
			StatsSet rebirthData = getRebirthData(player);
			int currentRebirthLevel = 0;
			int rebirthSkillId = 0;
			
			if (!rebirthData.getSet().isEmpty())
			{
				currentRebirthLevel = rebirthData.getInteger("rebirthLevel");
				rebirthSkillId = rebirthData.getInteger("rebirthSkillId");
			}
			
			if (player.getLevel() < Config.REBIRTH_MIN_LEVEL)
			{
				player.sendMessage("You do not meet the level requirement for the rebirth: " + Config.REBIRTH_MIN_LEVEL);
				player.sendPacket(new ExShowScreenMessage("You do not meet the level requirement for the rebirth: " + Config.REBIRTH_MIN_LEVEL, 2000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
			
			if (currentRebirthLevel < Config.REBIRTH_MAX)
			{
				player.sendMessage("You need to have the maximum rebirth count.");
				player.sendPacket(new ExShowScreenMessage("You need to have the maximum rebirth count.", 2000, 2, false));
				return;
			}
			
			if (currentRebirthLevel == Config.REBIRTH_MAX)
			{
				player.sendMessage("You can choose your new rebirth now.");
				player.sendPacket(new ExShowScreenMessage("You can choose your new rebirth now.", 2000, 2, false));
				player.setSwitchingRebirthSkill(true);
				
				final L2Skill rebirthSkill = SkillTable.getInstance().getInfo(rebirthSkillId, 1);
				player.removeSkill(rebirthSkill);
				player.sendSkillList();
				
				updatePlayerRebirthData(player, -1, true);
				_playersRebirthData.remove(player.getObjectId());
			}
			showChatWindow(player);
		}
	}
	
	public void showPerformWindow(L2PcInstance player, int val)
	{
		StatsSet rebirthData = getRebirthData(player);
		int currentRebirthLevel = 0;
		
		if (!rebirthData.getSet().isEmpty())
		{
			currentRebirthLevel = rebirthData.getInteger("rebirthLevel");
		}
		
		if (currentRebirthLevel >= Config.REBIRTH_MAX && !player.isSwitchingRebirthSkill())
		{
			player.sendMessage("You are currently at your maximum rebirth count.");
			player.sendPacket(new ExShowScreenMessage("You are currently at your maximum rebirth count.", 2000, 2, false));
			return;
		}
		
		if (player.getLevel() < Config.REBIRTH_MIN_LEVEL && !player.isSwitchingRebirthSkill())
		{
			player.sendMessage("You do not meet the level requirement for the rebirth: " + Config.REBIRTH_MIN_LEVEL);
			player.sendPacket(new ExShowScreenMessage("You do not meet the level requirement for the rebirth: " + Config.REBIRTH_MIN_LEVEL, 2000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		String text = HtmCache.getInstance().getHtm("data/html/mods/rebirth/perform.htm");
		html.setHtml(text);
		html.replace("%list%", getRebirthList(player, val));
		player.sendPacket(html);
	}
	
	public void showChatWindow(L2PcInstance player)
	{
		// _playersRebirthData.clear();
		// player.setSwitchingRebirthSkill(false);
		
		String img = "<img height=115><img height=-67>";
		String rebirthIcon = "icon.skill0000";
		String rebirthSkillName = "-";
		int rebirthLevel = 0;
		String rebirthDescription = "-";
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		String htm = HtmCache.getInstance().getHtm("data/html/mods/rebirth/index.htm");
		
		StatsSet rebirthData = getRebirthData(player);
		if (rebirthData != null && !rebirthData.getSet().isEmpty())
		{
			rebirthLevel = rebirthData.getInteger("rebirthLevel");
			RebirthHolder data = RebirthManager.getInstance().getSkills(rebirthLevel).get(rebirthData.getInteger("rebirthId"));
			rebirthIcon = data.getIcon();
			L2Skill rebirthSkill = SkillTable.getInstance().getInfo(rebirthData.getInteger("rebirthSkillId"), 1);
			rebirthSkillName = rebirthSkill.getName();
			rebirthDescription = data.getDescription();
			img = "<img src=\"L2UI_CH3.refinegrade3_21\" width=120 height=115><img height=-67>";
		}
		
		htm = htm.replaceAll("%img%", img);
		htm = htm.replaceAll("%rebirthIcon%", rebirthIcon);
		htm = htm.replaceAll("%rebirthSkillName%", rebirthSkillName);
		htm = htm.replaceAll("%rebirthLevel%", String.valueOf(rebirthLevel));
		htm = htm.replaceAll("%rebirthMaxLevel%", String.valueOf(Config.REBIRTH_MAX));
		htm = htm.replaceAll("%rebirthDescription%", rebirthDescription);
		
		html.setHtml(htm);
		player.sendPacket(html);
	}
	
	public static int countPages(int objectsSize, int pageSize)
	{
		return objectsSize / pageSize + (objectsSize % pageSize == 0 ? 0 : 1);
	}
	
	private String getRebirthList(L2PcInstance player, int page)
	{
		int rebirthLevel = 1;
		int rebirthId = -1;
		
		final StatsSet rebirthData = getRebirthData(player);
		
		if (rebirthData != null && !rebirthData.getSet().isEmpty())
		{
			rebirthLevel = rebirthData.getInteger("rebirthLevel") + 1;
			rebirthId = rebirthData.getInteger("rebirthId");
		}
		
		List<RebirthHolder> list = RebirthManager.getInstance().getSkills(rebirthLevel);
		
		final int max = countPages(list.size(), _pageLimit);
		page = page > max ? max : page < 1 ? 1 : page;
		
		list = list.subList((page - 1) * _pageLimit, Math.min(page * _pageLimit, list.size()));
		
		final StringBuilder sb = new StringBuilder();
		
		int id = -1;
		L2Skill skillName = null;
		boolean changeColor = true;
		String color = "";
		
		sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		
		for (RebirthHolder rebirth : list)
		{
			id++;
			
			if (rebirthId != -1 && id != rebirthId)
			{
				continue;
			}
			
			color = "";
			changeColor = !changeColor;
			if (changeColor)
			{
				color = " bgcolor=000000";
			}
			
			skillName = SkillTable.getInstance().getInfo(rebirth.getSkillId(), 1);
			
			sb.append("<table border=0 cellspacing=\"1\" cellpadding=\"1\" width=\"300\"" + color + ">");
			sb.append("<tr>");
			sb.append("<td><img src=\"" + rebirth.getIcon() + "\" width=32 height=32></td>");
			sb.append("<td width=260>"//
				+ "<a action=\"bypass custom_rebirth requestRebirth " + rebirth.getSkillId() + " " + id + " " + rebirth.getRequiredItemId() + " " + rebirth.getRequiredItemAmount() + "\">"//
				+ "" + skillName.getName() + "</a> &nbsp; Lv: <font color=\"00FF00\">" + rebirthLevel + "</font><br1>Description: <font color=\"LEVEL\">" + rebirth.getDescription() + "</font>" //
				+ "<br1>Price: " + L2Item.getItemNameById(rebirth.getRequiredItemId()) + " (" + (player.isSwitchingRebirthSkill() ? "0" : rebirth.getRequiredItemAmount()) + ")</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		}
		
		if (rebirthId == -1)
		{
			sb.append("<table width=300><tr>");
			sb.append("<td align=left width=100>" + (page > 1 ? "<button value=\"Prev\" action=\"bypass custom_rebirth perform " + (page - 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
			sb.append("<td align=center width=100>Page: " + page + " / " + max + "</td>");
			sb.append("<td align=right width=100>" + (page < max ? "<button value=\"Next\" action=\"bypass custom_rebirth perform " + (page + 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
			sb.append("</tr></table>");
		}
		
		return sb.toString();
	}
	
	public void requestRebirth(L2PcInstance player, int skillId, int id, int itemId, int itemAmount)
	{
		boolean isSwitching = player.isSwitchingRebirthSkill();
		
		StatsSet rebirthData = getRebirthData(player);
		int currentRebirthLevel = 1;
		
		if (!rebirthData.getSet().isEmpty())
		{
			if (currentRebirthLevel >= Config.REBIRTH_MAX || player.getLevel() < Config.REBIRTH_MIN_LEVEL)
			{
				return;
			}
			
			currentRebirthLevel = rebirthData.getInteger("rebirthLevel") + 1;
		}
		
		if (!isSwitching)
		{
			if (!player.destroyItemByItemId("RebirthSystem", itemId, itemAmount, null, true))
			{
				return;
			}
			
			int max_level = ExperienceData.getInstance().getMaxLevel();
			
			if (player.isSubClassActive())
			{
				max_level = Config.MAX_SUBCLASS_LEVEL;
			}
			
			int returnToLevel = Config.REBIRTH_RETURN_TO_LEVEL;
			if (returnToLevel < 1)
			{
				returnToLevel = 1;
			}
			if (returnToLevel > max_level)
			{
				returnToLevel = max_level;
			}
			
			final byte lvl = Byte.parseByte(String.valueOf(returnToLevel));
			final long pXp = player.getStat().getExp();
			final long tXp = ExperienceData.getInstance().getExpForLevel(lvl);
			
			if (pXp > tXp)
			{
				player.getStat().removeExpAndSp(pXp - tXp, 0);
			}
			else if (pXp < tXp)
			{
				player.getStat().addExpAndSp(tXp - pXp, 0);
			}
			
			// Restore Hp-Mp-Cp
			final double actual_hp = player.getCurrentHp();
			final double actual_cp = player.getCurrentCp();
			
			player.setCurrentCp(actual_cp);
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentHp(actual_hp);
			player.broadcastStatusUpdate();
			player.store();
		}
		
		if (isSwitching && currentRebirthLevel == 3)
		{
			player.setSwitchingRebirthSkill(false);
			isSwitching = false;
		}
		
		if (currentRebirthLevel == 1)
		{
			if (isSwitching)
			{
				// Delete old data if player is switching
				_playersRebirthData.remove(player.getObjectId());
				deletePlayerRebirthData(player);
			}
			storePlayerRebirthData(player, id, skillId, isSwitching);
		}
		else
		{
			updatePlayerRebirthData(player, currentRebirthLevel, isSwitching);
		}
		
		final L2Skill rebirthSkill = SkillTable.getInstance().getInfo(skillId, currentRebirthLevel);
		player.removeSkill(rebirthSkill);
		player.addSkill(rebirthSkill, false);
		player.sendSkillList();
		
		player.broadcastPacket(new MagicSkillUser(player, player, 1410, 1, 1000, 1));
		player.sendMessage("Congratulations " + player.getName() + "! You've been reborn.");
		player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + "! You've been reborn.", 3000, 2, false));
	}
	
	public void restoreRebirthSkills(L2PcInstance player)
	{
		final StatsSet rebirthData = getRebirthData(player);
		if (rebirthData != null && !rebirthData.getSet().isEmpty())
		{
			int rebirthLevel = rebirthData.getInteger("rebirthLevel");
			int rebirthSkillId = rebirthData.getInteger("rebirthSkillId");
			boolean switching = rebirthData.getBool("switching");
			
			if (switching)
			{
				player.setSwitchingRebirthSkill(true);
			}
			
			if (rebirthLevel != -1)
			{
				final L2Skill rebirthSkill = SkillTable.getInstance().getInfo(rebirthSkillId, rebirthLevel);
				player.removeSkill(rebirthSkill);
				player.addSkill(rebirthSkill, false);
			}
		}
	}
	
	public StatsSet getRebirthData(final L2PcInstance player)
	{
		final int playerId = player.getObjectId();
		
		if (_playersRebirthData.get(playerId) == null)
		{
			getRebirthDataFromDB(player);
		}
		
		return _playersRebirthData.get(playerId);
	}
	
	public void getRebirthDataFromDB(final L2PcInstance player)
	{
		final int playerId = player.getObjectId();
		
		int rebirthLevel = -1;
		int rebirthId = 0;
		int rebirthSkillId = 0;
		boolean switching = false;
		
		Connection con = null;
		try
		{
			ResultSet rset;
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `orion_rebirth_manager` WHERE playerId = ?");
			statement.setInt(1, playerId);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				rebirthLevel = rset.getInt("rebirthLevel");
				rebirthId = rset.getInt("rebirthId");
				rebirthSkillId = rset.getInt("rebirthSkillId");
				switching = (rset.getInt("switching") == 1 ? true : false);
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (SQLException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		final StatsSet playerInfo = new StatsSet();
		
		if (switching)
		{
			player.setSwitchingRebirthSkill(switching);
		}
		
		if (rebirthLevel != -1)
		{
			playerInfo.set("rebirthLevel", rebirthLevel);
			playerInfo.set("rebirthId", rebirthId);
			playerInfo.set("rebirthSkillId", rebirthSkillId);
			playerInfo.set("switching", switching);
		}
		
		_playersRebirthData.put(playerId, playerInfo);
	}
	
	public void storePlayerRebirthData(final L2PcInstance player, int rebirthId, int skillId, boolean switching)
	{
		final StatsSet playerInfo = new StatsSet();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `orion_rebirth_manager` (playerId, rebirthLevel, rebirthId, rebirthSkillId, switching) VALUES (?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, 1);
			statement.setInt(3, rebirthId);
			statement.setInt(4, skillId);
			statement.setBoolean(5, switching);
			statement.execute();
			
			playerInfo.set("rebirthLevel", 1);
			playerInfo.set("rebirthId", rebirthId);
			playerInfo.set("rebirthSkillId", skillId);
			playerInfo.set("switching", switching);
			
			_playersRebirthData.put(player.getObjectId(), playerInfo);
		}
		catch (SQLException e)
		{
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
	
	public void updatePlayerRebirthData(final L2PcInstance player, final int newRebirthLevel, boolean switching)
	{
		Connection con = null;
		try
		{
			final int playerId = player.getObjectId();
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE `orion_rebirth_manager` SET rebirthLevel = ?, switching = ? WHERE playerId = ?");
			statement.setInt(1, newRebirthLevel);
			statement.setBoolean(2, switching);
			statement.setInt(3, playerId);
			statement.execute();
			statement.close();
			
			_playersRebirthData.get(playerId).set("rebirthLevel", newRebirthLevel);
			_playersRebirthData.get(playerId).set("switching", switching);
		}
		catch (SQLException e)
		{
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
	
	public void deletePlayerRebirthData(final L2PcInstance player)
	{
		Connection con = null;
		try
		{
			final int playerId = player.getObjectId();
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM orion_rebirth_manager WHERE playerId = ?");
			statement.setInt(1, playerId);
			statement.execute();
		}
		catch (SQLException e)
		{
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
	
	public static RebirthSystem getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RebirthSystem INSTANCE = new RebirthSystem();
	}
}