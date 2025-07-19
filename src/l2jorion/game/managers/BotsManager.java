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
package l2jorion.game.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2jorion.Config;
import l2jorion.game.datatables.GmListTable;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.templates.StatsSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.Util;
import l2jorion.util.database.L2DatabaseFactory;

public class BotsManager
{
	private final Logger LOG = LoggerFactory.getLogger(BotsManager.class);
	
	private final Map<Integer, BotDataHolder> _bots = new ConcurrentHashMap<>();
	private final Map<Integer, ReporterDataHolder> _reporters = new ConcurrentHashMap<>();
	private static final String INSERT_OR_UPDATE_BOT_REPORT_DATA = "INSERT INTO bot_report_data (botId, botName, reportCount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE botId=?, botName=?, reportCount=?";
	private static final String RESTORE_BOT_REPORT_DATA = "SELECT botId, botName, reportCount FROM bot_report_data";
	private static final String DELETE_ALL_BOT_REPORT_DATA = "TRUNCATE bot_report_data";
	
	public static final BotsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected BotsManager()
	{
		
	}
	
	public boolean isAlreadyRepored(L2PcInstance player, L2PcInstance bot)
	{
		if (_reporters.containsKey(player.getObjectId()))
		{
			for (Integer id : _reporters.get(player.getObjectId()).getAllReportedBots())
			{
				if (id.equals(bot.getObjectId()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int getPlayerReports(L2PcInstance player)
	{
		if (_reporters.containsKey(player.getObjectId()))
		{
			return _reporters.get(player.getObjectId()).getReportNumber();
		}
		
		return 0;
	}
	
	public void addReporterToList(L2PcInstance reporter, L2PcInstance bot)
	{
		final StatsSet set = new StatsSet();
		
		int reporterId = reporter.getObjectId();
		int botId = bot.getObjectId();
		
		set.set("reporterId", reporterId);
		set.set("botId", botId);
		set.set("reportNumber", 1);
		
		if (_reporters.containsKey(reporterId))
		{
			_reporters.get(reporterId).setReportNumber(_reporters.get(reporterId).getReportNumber() + 1);
			_reporters.get(reporterId).addReportedBot(botId);
			return;
		}
		
		_reporters.put(reporterId, new ReporterDataHolder(set));
	}
	
	public void addBotToList(L2PcInstance bot)
	{
		final StatsSet set = new StatsSet();
		
		int botId = bot.getObjectId();
		String botName = bot.getName();
		
		set.set("botId", botId);
		set.set("botName", botName);
		set.set("reportCount", 1);
		
		if (_bots.containsKey(botId))
		{
			_bots.get(botId).setReportCount(_bots.get(botId).getReportCount() + 1);
			return;
		}
		
		GmListTable.broadcastMessageToGMs("New bot has been added to the list: " + botName);
		_bots.put(botId, new BotDataHolder(set));
	}
	
	public Map<Integer, BotDataHolder> getBotsList()
	{
		return _bots;
	}
	
	public static class BotDataHolder
	{
		private final int _botId;
		private final String _botName;
		private int _reportCount;
		
		public BotDataHolder(StatsSet set)
		{
			_botId = set.getInteger("botId");
			_botName = set.getString("botName");
			_reportCount = set.getInteger("reportCount");
		}
		
		public final int getBotId()
		{
			return _botId;
		}
		
		public final String getBotName()
		{
			return _botName;
		}
		
		public final int getReportCount()
		{
			return _reportCount;
		}
		
		public void setReportCount(int number)
		{
			_reportCount = number;
		}
	}
	
	public static class ReporterDataHolder
	{
		private final int _reporterId;
		private int _reportNumber;
		private static ArrayList<Integer> _allReportedIds = new ArrayList<>();
		
		public ReporterDataHolder(StatsSet set)
		{
			_reporterId = set.getInteger("reporterId");
			_reportNumber = set.getInteger("reportNumber");
			_allReportedIds.add(set.getInteger("botId"));
		}
		
		public final int getReportedId()
		{
			return _reporterId;
		}
		
		public final void addReportedBot(int bodId)
		{
			_allReportedIds.add(bodId);
		}
		
		public final ArrayList<Integer> getAllReportedBots()
		{
			return _allReportedIds;
		}
		
		public final int getReportNumber()
		{
			return _reportNumber;
		}
		
		public void setReportNumber(int number)
		{
			_reportNumber = number;
		}
	}
	
	public void insertOrUpdateBotReportData()
	{
		Connection con = null;
		
		for (BotDataHolder bot : _bots.values())
		{
			int botId = bot.getBotId();
			String botName = bot.getBotName();
			int reportCount = bot.getReportCount();
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_OR_UPDATE_BOT_REPORT_DATA);
				
				// Insert
				statement.setInt(1, botId);
				statement.setString(2, botName);
				statement.setInt(3, reportCount);
				
				// Update
				statement.setInt(4, botId);
				statement.setString(5, botName);
				statement.setInt(6, reportCount);
				
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					LOG.error("Could not store insertOrUpdateBotReportData:");
					e.printStackTrace();
				}
			}
			finally
			{
				CloseUtil.close(con);
			}
		}
	}
	
	public void restoreBotReportData()
	{
		Connection con = null;
		int count = 0;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			ResultSet rset;
			
			statement = con.prepareStatement(RESTORE_BOT_REPORT_DATA);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final StatsSet set = new StatsSet();
				
				int botId = rset.getInt("botId");
				String botName = rset.getString("botName");
				long reportCount = rset.getLong("reportCount");
				
				set.set("botId", botId);
				set.set("botName", botName);
				set.set("reportCount", reportCount);
				
				_bots.put(botId, new BotDataHolder(set));
				
				count++;
			}
			
			rset.close();
			statement.close();
			
			if (count > 0)
			{
				Util.printSection("Bot Report Data");
				LOG.info(getClass().getSimpleName() + ": Restored bot report data with " + count + " record" + (count > 1 ? "s" : ""));
				deleteBotReportData();
			}
		}
		
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				LOG.error("Could not restore Bot Report Data:");
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void deleteBotReportData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(DELETE_ALL_BOT_REPORT_DATA);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				LOG.info(getClass().getSimpleName() + ": Couldn't delete bot_report_data table:");
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
		protected static final BotsManager _instance = new BotsManager();
	}
}
