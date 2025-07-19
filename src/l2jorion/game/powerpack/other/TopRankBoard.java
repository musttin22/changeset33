package l2jorion.game.powerpack.other;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.text.TextBuilder;
import l2jorion.game.datatables.sql.ClanTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public class TopRankBoard implements ICustomByPassHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(TopRankBoard.class);
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (parameters.startsWith("get"))
		{
			final String index = parameters.substring(3).trim();
			
			NpcHtmlMessage htmFile = new NpcHtmlMessage(1);
			TextBuilder htm = new TextBuilder();
			
			switch (index)
			{
				case "pvp": // pvp
				{
					htm.append("<html><head><title>TOP 10</title></head><body>");
					htm.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32/>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					htm.append("<table width=300 height=20 bgcolor=000000>");
					htm.append("<tr>");
					htm.append("<td width=15 align=right><font color =0066CC>#</font></td><td width=160><font color =0066CC>Name</font></td><td align=right><font color =0066CC>PvP's</font></td>");
					htm.append("</tr></table>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					
					htm.append("<table width=300>");
					int pos = 0;
					
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement stm = con.prepareStatement("SELECT char_name, pvpkills FROM characters WHERE pvpkills > 0 order by pvpkills desc limit 10");
						ResultSet rs = stm.executeQuery();
						
						while (rs.next())
						{
							pos++;
							
							String char_name = rs.getString("char_name");
							String char_pvpkills = rs.getString("pvpkills");
							
							htm.append("<tr><td width=15 align=right>" + pos + ".</td><td width=160>" + char_name + "</td><td align=right><font color=FF33AA>" + char_pvpkills + "</font></td></tr>");
						}
						rs.close();
						stm.close();
					}
					catch (Exception e)
					{
						LOG.info("topRankBoard: ", e);
					}
					finally
					{
						CloseUtil.close(con);
					}
					break;
				}
				case "pk": // pk
				{
					htm.append("<html><head><title>TOP 10</title></head><body>");
					htm.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32/>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					htm.append("<table width=300 height=20 bgcolor=000000>");
					htm.append("<tr>");
					htm.append("<td width=15 align=right><font color =0066CC>#</font></td><td width=160><font color =0066CC>Name</font></td><td align=right><font color =0066CC>Pk's</font></td>");
					htm.append("</tr></table>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					
					htm.append("<table width=300>");
					int pos = 0;
					
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement stm = con.prepareStatement("SELECT char_name, pkkills FROM characters WHERE pkkills > 0 order by pkkills desc limit 10");
						ResultSet rs = stm.executeQuery();
						
						while (rs.next())
						{
							pos++;
							
							String char_name = rs.getString("char_name");
							String char_pkkills = rs.getString("pkkills");
							
							htm.append("<tr><td width=15 align=right>" + pos + ".</td><td width=160>" + char_name + "</td><td align=right><font color=FF0000>" + char_pkkills + "</font></td></tr>");
						}
						rs.close();
						stm.close();
					}
					catch (Exception e)
					{
						LOG.info("topRankBoard: ", e);
					}
					finally
					{
						CloseUtil.close(con);
					}
					break;
				}
				case "clan": // clan
				{
					htm.append("<html><head><title>TOP 10</title></head><body>");
					htm.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32/>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					htm.append("<table width=300 height=20 bgcolor=000000>");
					htm.append("<tr>");
					htm.append("<td width=15 align=right><font color =0066CC>#</font></td><td width=160><font color =0066CC>Clan Name</font></td><td align=right><font color =0066CC>PvP's</font></td>");
					htm.append("</tr></table>");
					htm.append("<img src=\"L2UI.SquareGray\" width=\"300\" height=\"1\"/>");
					
					htm.append("<table width=300>");
					int pos = 0;
					
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement stm = con.prepareStatement("SELECT clanid, SUM(pvpkills) AS totalPvP FROM characters WHERE pvpkills > 0 GROUP BY clanid ORDER BY totalPvP DESC LIMIT 10");
						ResultSet rs = stm.executeQuery();
						
						while (rs.next())
						{
							int clanid = rs.getInt("clanId");
							
							if (clanid == 0)
							{
								continue;
							}
							
							pos++;
							
							String clanName = ClanTable.getInstance().getClan(clanid).getName();
							String points = rs.getString("totalPvP");
							
							htm.append("<tr><td width=15 align=right>" + pos + ".</td><td width=160>" + clanName + "</td><td align=right><font color=FF33AA>" + points + "</font></td></tr>");
						}
						rs.close();
						stm.close();
					}
					catch (Exception e)
					{
						LOG.info("topRankBoard: ", e);
					}
					finally
					{
						CloseUtil.close(con);
					}
					break;
				}
			}
			
			htm.append("</table><br1><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32><br1><center><button value=\"Back\" action=\"bypass -h custom_gmshop Chat toprank\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></body></html>");
			htmFile.setHtml(htm.toString());
			player.sendPacket(htmFile);
		}
	}
	
	@SuppressWarnings("unused")
	private String loadAndFixClanCrest(String clanId)
	{
		String crest = "<img src=\"Crest.Crest_1_" + clanId + "\" width=\"16\" height=\"16\">";
		
		return crest;
	}
	
	private static String[] _topboard =
	{
		"toprankboard"
	};
	
	@Override
	public String[] getByPassCommands()
	{
		return _topboard;
	}
}