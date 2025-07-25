/*
 * L2jOrion Project - www.l2jorion.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.model.actor.instance;

import javolution.text.TextBuilder;
import l2jorion.Config;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.datatables.sql.SkillTreeTable;
import l2jorion.game.managers.FishingChampionshipManager;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2SkillLearn;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.AquireSkillList;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.templates.L2NpcTemplate;

public class L2FishermanInstance extends L2MerchantInstance
{
	// private static Logger LOG = LoggerFactory.getLogger(L2FishermanInstance.class);
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2FishermanInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(L2PcInstance player, final int npcId, final int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/fisherman/" + pom + ".htm";
	}
	
	/*
	 * private void showBuyWindow(final L2PcInstance player, final int val) { double taxRate = 0; if (getIsInTown()) { taxRate = getCastle().getTaxRate(); } player.tempInvetoryDisable(); if (Config.DEBUG) { LOG.debug("Showing buylist"); } L2TradeList list =
	 * TradeController.getInstance().getBuyList(val); if (list != null && list.getNpcId().equals(String.valueOf(getNpcId()))) { BuyList bl = new BuyList(list, player.getAdena(), taxRate); player.sendPacket(bl); } else { LOG.error("possible client hacker: " + player.getName() +
	 * " attempting to buy from GM shop! (L2FishermanInstance)"); LOG.error("buylist id:" + val); } player.sendPacket(ActionFailed.STATIC_PACKET); } private void showSellWindow(final L2PcInstance player) { if (Config.DEBUG) { LOG.debug("Showing selllist"); } player.sendPacket(new SellList(player));
	 * if (Config.DEBUG) { LOG.debug("Showing sell window"); } player.sendPacket(ActionFailed.STATIC_PACKET); }
	 */
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showSkillList(player);
		}
		/*
		 * StringTokenizer st = new StringTokenizer(command, " "); String command2 = st.nextToken(); if (command2.equalsIgnoreCase("Buy")) { if (st.countTokens() < 1) { return; } player.setTempAccess(false); final int val = Integer.parseInt(st.nextToken()); showBuyWindow(player, val); } else if
		 * (command2.equalsIgnoreCase("Sell")) { showSellWindow(player); }
		 */
		else if (command.startsWith("FishingChampionship"))
		{
			if (!Config.ALLOW_FISH_CHAMPIONSHIP)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fisherman/championship/no_fish_event001.htm");
				player.sendPacket(html);
				return;
			}
			FishingChampionshipManager.getInstance().showChampScreen(player, getObjectId());
		}
		else if (command.startsWith("FishingReward"))
		{
			if (!Config.ALLOW_FISH_CHAMPIONSHIP)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fisherman/championship/no_fish_event001.htm");
				player.sendPacket(html);
				return;
			}
			
			if (!FishingChampionshipManager.getInstance().isWinner(player.getName()))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fisherman/championship/no_fish_event_reward001.htm");
				player.sendPacket(html);
				return;
			}
			FishingChampionshipManager.getInstance().getReward(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void showSkillList(final L2PcInstance player)
	{
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Fishing);
		
		int counts = 0;
		
		for (final L2SkillLearn s : skills)
		{
			final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			
			if (sk == null)
			{
				continue;
			}
			
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}
		
		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
			
			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
