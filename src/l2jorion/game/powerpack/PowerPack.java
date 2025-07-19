/*
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
package l2jorion.game.powerpack;

import l2jorion.Config;
import l2jorion.game.autofarm.AutofarmCommandHandler;
import l2jorion.game.community.CommunityBoardManager;
import l2jorion.game.datatables.sql.ItemMarketTable;
import l2jorion.game.handler.VoicedCommandHandler;
import l2jorion.game.handler.custom.CustomBypassHandler;
import l2jorion.game.handler.voice.Bank;
import l2jorion.game.handler.voice.ClassManager;
import l2jorion.game.handler.voice.DressMe;
import l2jorion.game.handler.voice.LevelUp;
import l2jorion.game.handler.voice.Menu;
import l2jorion.game.handler.voice.PremiumMenu;
import l2jorion.game.handler.voice.Sub;
import l2jorion.game.handler.voice.Vote;
import l2jorion.game.handler.voice.VoteForEvent;
import l2jorion.game.handler.voice.Watch;
import l2jorion.game.model.entity.event.custom.Dungeon;
import l2jorion.game.model.entity.event.custom.PartyZone;
import l2jorion.game.powerpack.bossInfo.RaidInfoHandler;
import l2jorion.game.powerpack.buffer.Buffer;
import l2jorion.game.powerpack.buffer.BuffsTable;
import l2jorion.game.powerpack.gatekeeper.Gatekeeper;
import l2jorion.game.powerpack.other.CastlesMenu;
import l2jorion.game.powerpack.other.ClassMaster;
import l2jorion.game.powerpack.other.CustomAugmentation;
import l2jorion.game.powerpack.other.Market;
import l2jorion.game.powerpack.other.NewUnstuck;
import l2jorion.game.powerpack.other.TopBoardOfL2RaptorServer;
import l2jorion.game.powerpack.other.TopRankBoard;
import l2jorion.game.powerpack.other.WeddingPanel;
import l2jorion.game.powerpack.other.WeeklyBoard;
import l2jorion.game.powerpack.other.AcademyManager;
import l2jorion.game.powerpack.rebirth.RebirthSystem;
import l2jorion.game.powerpack.shop.Shop;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class PowerPack
{
	private static Logger LOG = LoggerFactory.getLogger(PowerPack.class);
	
	private static PowerPack _instance = null;
	
	public static PowerPack getInstance()
	{
		if (_instance == null)
		{
			_instance = new PowerPack();
		}
		
		return _instance;
	}
	
	private PowerPack()
	{
		PowerPackConfig.load();
		
		if (PowerPackConfig.BUFFER_ENABLED)
		{
			if ((PowerPackConfig.BUFFER_COMMAND != null && PowerPackConfig.BUFFER_COMMAND.length() > 0) || PowerPackConfig.BUFFER_USEBBS)
			{
				Buffer handler = new Buffer();
				if (PowerPackConfig.BUFFER_USECOMMAND && PowerPackConfig.BUFFER_COMMAND != null && PowerPackConfig.BUFFER_COMMAND.length() > 0)
				{
					VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handler);
				}
				
				if (PowerPackConfig.BUFFER_USEBBS)
				{
					CommunityBoardManager.getInstance().registerBBSHandler(handler);
				}
				
				CustomBypassHandler.getInstance().registerCustomBypassHandler(handler);
			}
			
			LOG.info("Buffer - Enabled");
			BuffsTable.getInstance();
		}
		
		if (PowerPackConfig.GLOBALGK_ENABLED)
		{
			Gatekeeper handler = new Gatekeeper();
			if (PowerPackConfig.GLOBALGK_USECOMMAND && PowerPackConfig.GLOBALGK_COMMAND != null && PowerPackConfig.GLOBALGK_COMMAND.length() > 0)
			{
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handler);
			}
			
			if (PowerPackConfig.GLOBALGK_USEBBS)
			{
				CommunityBoardManager.getInstance().registerBBSHandler(handler);
			}
			
			CustomBypassHandler.getInstance().registerCustomBypassHandler(handler);
			LOG.info("Gatekeeper - Enabled");
		}
		
		if (PowerPackConfig.GMSHOP_ENABLED)
		{
			Shop gs = new Shop();
			CustomBypassHandler.getInstance().registerCustomBypassHandler(gs);
			
			if (PowerPackConfig.GMSHOP_USECOMMAND && PowerPackConfig.GMSHOP_COMMAND != null && PowerPackConfig.GMSHOP_COMMAND.length() > 0)
			{
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(gs);
			}
			
			if (PowerPackConfig.GMSHOP_USEBBS)
			{
				CommunityBoardManager.getInstance().registerBBSHandler(gs);
			}
			
			LOG.info("Shop - Enabled");
		}
		
		if (PowerPackConfig.MARKET_ENABLED)
		{
			Market gm = new Market();
			CustomBypassHandler.getInstance().registerCustomBypassHandler(gm);
			
			if (PowerPackConfig.MARKET_COMMAND != null && PowerPackConfig.MARKET_COMMAND.length() > 0)
			{
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(gm);
			}
			
			if (PowerPackConfig.MARKET_USEBBS)
			{
				CommunityBoardManager.getInstance().registerBBSHandler(gm);
			}
			
			LOG.info("Market - Enabled");
			ItemMarketTable.getInstance().load();
		}
		
		Menu mn = new Menu();
		CustomBypassHandler.getInstance().registerCustomBypassHandler(mn);
		if (PowerPackConfig.COMMAND_MENU_ENABLED)
		{
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(mn);
		}
		CommunityBoardManager.getInstance().registerBBSHandler(mn);
		
		CustomBypassHandler.getInstance().registerCustomBypassHandler(new PremiumMenu());
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new PremiumMenu());
		LOG.info("Command: .menu - Enabled");
		
		if (PowerPackConfig.VOTEREWARD)
		{
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new Vote());
			LOG.info("Command: .votereward - Enabled");
		}
		
		if (Config.CUSTOM_SUB_CLASS_COMMAND)
		{
			Sub handle4 = new Sub();
			CustomBypassHandler.getInstance().registerCustomBypassHandler(handle4);
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new Sub());
			LOG.info("Command: .sub - Enabled");
		}
		
		if (Config.ALLOW_COMMAND_LEVEL_UP)
		{
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new LevelUp());
			LOG.info("Command: .setlvl - Enabled");
		}
		
		if (PowerPackConfig.RESPAWN_BOSS)
		{
			RaidInfoHandler raidInfo = new RaidInfoHandler();
			CustomBypassHandler.getInstance().registerCustomBypassHandler(raidInfo);
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(raidInfo);
			CommunityBoardManager.getInstance().registerBBSHandler(raidInfo);
			LOG.info("Command: .boss  - Enabled");
		}
		
		if (PowerPackConfig.BANK_SYSTEM)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new Bank());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new Bank());
			LOG.info("Command: .bank  - Enabled");
		}
		
		if (Config.MENU_NEW_STYLE)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new Watch());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new Watch());
			LOG.info("Command: .watch  - Enabled");
			
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new VoteForEvent());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new VoteForEvent());
			LOG.info("Command: .event  - Enabled");
		}
		
		if (Config.ALLOW_CLASS_MASTERS)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new ClassManager());
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new ClassMaster());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new ClassManager());
		}
		
		if (Config.RON_CUSTOM)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new TopBoardOfL2RaptorServer());
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new WeddingPanel());
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new NewUnstuck());
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new AcademyManager());
		}
		
		CustomBypassHandler.getInstance().registerCustomBypassHandler(new CastlesMenu());
		
		if (Config.L2UNLIMITED_CUSTOM)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new Dungeon());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new Dungeon());
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new PartyZone());
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new PartyZone());
		}
		
		if (Config.L2LIMIT_CUSTOM)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new WeeklyBoard());
		}
		
		if (Config.AUTOFARM_ENABLED)
		{
			// Autofarm bot
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new AutofarmCommandHandler());
		}
		
		DressMe dm = new DressMe();
		CustomBypassHandler.getInstance().registerCustomBypassHandler(dm);
		// CommunityBoardManager.getInstance().registerBBSHandler(dm);
		
		if (Config.REBIRTH_ENABLE)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new RebirthSystem());
		}
		
		if (Config.ALPHA_CUSTOM)
		{
			CustomBypassHandler.getInstance().registerCustomBypassHandler(new TopRankBoard());
		}
		
		CustomBypassHandler.getInstance().registerCustomBypassHandler(new CustomAugmentation());
	}
}