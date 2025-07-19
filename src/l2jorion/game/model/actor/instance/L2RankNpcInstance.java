package l2jorion.game.model.actor.instance;

import java.util.StringTokenizer;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.L2Skill;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.L2NpcTemplate;

public final class L2RankNpcInstance extends L2FolkInstance
{
	public L2RankNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();
		
		if (currentCommand.startsWith("RankNpcIncreaseLevel"))
		{
			increasingMenu(player);
		}
	}
	
	public void increasingMenu(L2PcInstance player)
	{
		int itemId = 4385;
		int amount = 1;
		
		L2Skill bonus1 = player.getSkill(9002);
		L2Skill bonus2 = player.getSkill(9003);
		L2Skill bonus3 = player.getSkill(9004);
		
		if (bonus3 != null && bonus3.getLevel() == 5)
		{
			player.sendMessage("You've reached max rank.");
			player.sendPacket(new ExShowScreenMessage("You've reached max rank.", 2000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return;
		}
		
		if (player.destroyItemByItemId("Consume", itemId, amount, this, true))
		{
			if (bonus1 == null)
			{
				player.addSkill(SkillTable.getInstance().getInfo(9002, 1), true);
			}
			
			// Bonus 1
			if (bonus1 != null && bonus1.getLevel() < 5)
			{
				if (bonus1.getLevel() == 4)
				{
					player.getAppearance().setTitleColor(Integer.decode("0x00CCCC"));
					player.broadcastUserInfo();
				}
				
				player.addSkill(SkillTable.getInstance().getInfo(9002, bonus1.getLevel() + 1), true);
			}
			else if (bonus1 != null && bonus1.getLevel() == 5 && bonus2 == null)
			{
				player.addSkill(SkillTable.getInstance().getInfo(9003, 1), true);
			}
			
			// Bonus 2
			else if (bonus2 != null && bonus2.getLevel() < 5)
			{
				if (bonus2.getLevel() == 4)
				{
					player.getAppearance().setTitleColor(Integer.decode("0x00FF00"));
					player.broadcastUserInfo();
				}
				
				player.addSkill(SkillTable.getInstance().getInfo(9003, bonus2.getLevel() + 1), true);
			}
			else if (bonus2 != null && bonus2.getLevel() == 5 && bonus3 == null)
			{
				player.addSkill(SkillTable.getInstance().getInfo(9004, 1), true);
			}
			
			// Bonus 3
			else if (bonus3 != null && bonus3.getLevel() < 5)
			{
				if (bonus3.getLevel() == 4)
				{
					player.getAppearance().setTitleColor(Integer.decode("0x0000FF"));
					player.broadcastUserInfo();
				}
				player.addSkill(SkillTable.getInstance().getInfo(9004, bonus3.getLevel() + 1), true);
			}
			
			player.sendSkillList();
		}
		else
		{
			player.sendMessage("You don't have enough " + L2Item.getItemNameById(itemId) + ".");
			player.sendPacket(new ExShowScreenMessage("You don't have enough " + L2Item.getItemNameById(itemId) + ".", 2000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
		}
		
		showChatWindow(player, 0);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(getHtmlPath(player, getNpcId(), val));
		html.replace("%objectId%", getObjectId());
		html.replace("%menu%", getMenu(player, val));
		player.sendPacket(html);
	}
	
	public String generateBar(int width, int height, int current, int max, boolean newColor)
	{
		final StringBuilder sb = new StringBuilder();
		current = current > max ? max : current;
		int bar = Math.max((width * (current * 100 / max) / 100), 0);
		sb.append("<table width=" + width + " cellspacing=0 cellpadding=0><tr><td width=" + bar + " align=center><img src=L2UI_CH3.BR_BAR1_" + (newColor ? "HP" : "CP") + " width=" + bar + " height=" + height + "/></td>");
		sb.append("<td width=" + (width - bar) + " align=center><img src=L2UI_CH3.BR_BAR1_" + (newColor ? "HP" : "CP") + "1 width=" + (width - bar) + " height=" + height + "/></td></tr></table>");
		return sb.toString();
	}
	
	private String getMenu(L2PcInstance player, int page)
	{
		int rank = 0;
		int stage = 0;
		int bar = 0;
		
		boolean completed = false;
		
		// Bonus 1
		if (player.getSkill(9002) != null && player.getSkill(9002).getLevel() < 5)
		{
			rank = 0;
			stage = player.getSkill(9002).getLevel();
			bar = player.getSkill(9002).getLevel();
		}
		else if (player.getSkill(9002) != null && player.getSkill(9002).getLevel() == 5)
		{
			rank = 1;
			stage = player.getSkill(9002).getLevel();
			bar = player.getSkill(9002).getLevel();
		}
		
		// Bonus 2
		if (player.getSkill(9003) != null && player.getSkill(9003).getLevel() < 5)
		{
			rank = 1;
			stage = player.getSkill(9003).getLevel();
			bar = 5 + player.getSkill(9003).getLevel();
		}
		else if (player.getSkill(9003) != null && player.getSkill(9003).getLevel() == 5)
		{
			rank = 2;
			stage = player.getSkill(9003).getLevel();
			bar = 5 + player.getSkill(9003).getLevel();
		}
		
		// Bonus 3
		if (player.getSkill(9004) != null && player.getSkill(9004).getLevel() < 5)
		{
			rank = 2;
			stage = player.getSkill(9004).getLevel();
			bar = 10 + player.getSkill(9004).getLevel();
		}
		else if (player.getSkill(9004) != null && player.getSkill(9004).getLevel() == 5)
		{
			rank = 3;
			stage = player.getSkill(9004).getLevel();
			bar = 10 + player.getSkill(9004).getLevel();
		}
		
		if (rank == 3 && stage == 5)
		{
			completed = true;
			bar = 15;
		}
		
		final StringBuilder sb = new StringBuilder();
		
		String color = "00ff00";
		if (rank == 3 || stage == 5)
		{
			color = "ff0000";
		}
		
		sb.append("<table width=200><tr>");
		sb.append("<td align=left width=100>Rank: <font color=" + color + ">" + rank + "</font> / <font color=ff0000>3</font></td>");
		sb.append("<td align=right width=100>Stage: <font color=" + color + ">" + stage + "</font> / <font color=ff0000>5</font></td>");
		sb.append("</tr></table>");
		sb.append(generateBar(300, 10, completed ? 15 : bar, 15, true));
		sb.append("<br><table width=300><tr>");
		sb.append("<td align=left width=100></td>");
		sb.append("<td align=center width=100><button value=\"Increase\" action=\"bypass npc_" + getObjectId() + "_RankNpcIncreaseLevel\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2></td>");
		sb.append("<td align=right width=100></td>");
		sb.append("</tr></table>");
		
		return sb.toString();
	}
	
	@Override
	public String getHtmlPath(L2PcInstance player, final int npcId, final int val)
	{
		return "data/html/rankNPC/" + npcId + "" + (val == 0 ? "" : "-" + val) + ".htm";
	}
}