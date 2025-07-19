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
package l2jorion.game.model.entity.event.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import l2jorion.game.model.entity.event.CTF;
import l2jorion.game.model.entity.event.DM;
import l2jorion.game.model.entity.event.TvT;
import l2jorion.game.model.entity.event.custom.Dungeon;
import l2jorion.game.model.entity.event.custom.HighRatesEvent;
import l2jorion.game.model.entity.event.custom.MiniRbEvent;
import l2jorion.game.model.entity.event.custom.PartyZone;
import l2jorion.game.model.entity.event.custom.SoloZone;
import l2jorion.game.model.entity.event.tournament.Tournament;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class EventManager
{
	protected static final Logger LOG = LoggerFactory.getLogger(EventManager.class);
	
	private final static String EVENT_MANAGER_CONFIGURATION_FILE = "./config/events/eventmanager.ini";
	
	public static boolean TVT_EVENT_ENABLED;
	public static ArrayList<String> TVT_TIMES_LIST;
	
	public static boolean CTF_EVENT_ENABLED;
	public static ArrayList<String> CTF_TIMES_LIST;
	
	public static boolean DM_EVENT_ENABLED;
	public static ArrayList<String> DM_TIMES_LIST;
	
	public static boolean TM_EVENT_ENABLED;
	public static ArrayList<String> TM_TIMES_LIST;
	
	public static boolean DG_EVENT_ENABLED;
	public static ArrayList<String> DG_TIMES_LIST;
	
	public static boolean PZ_EVENT_ENABLED;
	public static ArrayList<String> PZ_TIMES_LIST;
	
	public static boolean MINIRB_EVENT_ENABLED;
	public static ArrayList<String> MINIRB_TIMES_LIST;
	
	public static boolean HIGHRATES_EVENT_ENABLED;
	public static ArrayList<String> HIGHRATES_TIMES_LIST;
	
	public static boolean SOLO_ZONE_ENABLED;
	public static ArrayList<String> SOLO_ZONE_TIMES_LIST;
	
	public static boolean POLL_ENABLED;
	
	private static EventManager instance = null;
	
	private EventManager()
	{
		loadConfiguration();
	}
	
	public static EventManager getInstance()
	{
		if (instance == null)
		{
			instance = new EventManager();
		}
		
		return instance;
	}
	
	public static void loadConfiguration()
	{
		InputStream is = null;
		try
		{
			Properties eventSettings = new Properties();
			is = new FileInputStream(new File(EVENT_MANAGER_CONFIGURATION_FILE));
			eventSettings.load(is);
			
			TVT_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TVTEventEnabled", "false"));
			TVT_TIMES_LIST = new ArrayList<>();
			String[] propertySplit;
			propertySplit = eventSettings.getProperty("TVTStartTime", "").split(";");
			for (String time : propertySplit)
			{
				TVT_TIMES_LIST.add(time);
			}
			
			CTF_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("CTFEventEnabled", "false"));
			CTF_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("CTFStartTime", "").split(";");
			for (String time : propertySplit)
			{
				CTF_TIMES_LIST.add(time);
			}
			
			DM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("DMEventEnabled", "false"));
			DM_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("DMStartTime", "").split(";");
			for (String time : propertySplit)
			{
				DM_TIMES_LIST.add(time);
			}
			
			TM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TournamentEventEnabled", "false"));
			TM_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("TournamentStartTime", "").split(";");
			for (String time : propertySplit)
			{
				TM_TIMES_LIST.add(time);
			}
			
			DG_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("DungeonEventEnabled", "false"));
			DG_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("DungeonStartTime", "").split(";");
			for (String time : propertySplit)
			{
				DG_TIMES_LIST.add(time);
			}
			
			PZ_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("PartyZoneEventEnabled", "false"));
			PZ_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("PartyZoneStartTime", "").split(";");
			for (String time : propertySplit)
			{
				PZ_TIMES_LIST.add(time);
			}
			
			MINIRB_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("MiniRbEventEnabled", "false"));
			MINIRB_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("MiniRbEventStartTime", "").split(";");
			for (String time : propertySplit)
			{
				MINIRB_TIMES_LIST.add(time);
			}
			
			HIGHRATES_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("HighRatesEventEnabled", "false"));
			HIGHRATES_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("HighRatesEventStartTime", "").split(";");
			for (String time : propertySplit)
			{
				HIGHRATES_TIMES_LIST.add(time);
			}
			
			SOLO_ZONE_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("SoloZoneEnabled", "false"));
			SOLO_ZONE_TIMES_LIST = new ArrayList<>();
			propertySplit = eventSettings.getProperty("SoloZoneStartTime", "").split(";");
			for (String time : propertySplit)
			{
				SOLO_ZONE_TIMES_LIST.add(time);
			}
			
			POLL_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("PollEnabled", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void startEventRegistration()
	{
		if (TVT_EVENT_ENABLED)
		{
			registerTvT();
		}
		
		if (CTF_EVENT_ENABLED)
		{
			registerCTF();
		}
		
		if (DM_EVENT_ENABLED)
		{
			registerDM();
		}
		
		if (DG_EVENT_ENABLED)
		{
			registerDG();
		}
		
		if (PZ_EVENT_ENABLED)
		{
			registerPZ();
		}
		
		if (TM_EVENT_ENABLED)
		{
			registerTM();
		}
		
		if (MINIRB_EVENT_ENABLED)
		{
			registerMiniRbEvent();
		}
		
		if (HIGHRATES_EVENT_ENABLED)
		{
			registerHighRatesEvent();
		}
		
		if (SOLO_ZONE_ENABLED)
		{
			registerSoloZoneEvent();
		}
	}
	
	public static void registerTvT()
	{
		TvT.loadData();
		
		if (!TvT.checkStartJoinOk())
		{
			LOG.warn("registerTvT: TvT Event is not setted Properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(TvT.get_eventName());
		
		for (String time : TVT_TIMES_LIST)
		{
			TvT newInstance = TvT.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerCTF()
	{
		CTF.loadData();
		
		if (!CTF.checkStartJoinOk())
		{
			LOG.warn("registerCTF: CTF Event is not setted Properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(CTF.get_eventName());
		
		for (String time : CTF_TIMES_LIST)
		{
			
			CTF newInstance = CTF.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
		
	}
	
	public static void registerDM()
	{
		DM.loadData();
		
		if (!DM.checkStartJoinOk())
		{
			LOG.error("registerDM: DM Event is not established properly");
		}
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(DM.get_eventName());
		
		for (String time : DM_TIMES_LIST)
		{
			DM newInstance = DM.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerDG()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(Dungeon.getNewInstance().get_eventName());
		
		for (String time : DG_TIMES_LIST)
		{
			Dungeon newInstance = Dungeon.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerPZ()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(PartyZone.getNewInstance().get_eventName());
		
		for (String time : PZ_TIMES_LIST)
		{
			PartyZone newInstance = PartyZone.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerTM()
	{
		EventsGlobalTask.getInstance().clearEventTasksByEventName(Tournament.get_eventName());
		
		for (String time : TM_TIMES_LIST)
		{
			Tournament newInstance = Tournament.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerMiniRbEvent()
	{
		MiniRbEvent.loadData();
		MiniRbEvent.loadEventMobsData();
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(MiniRbEvent.get_eventName());
		
		for (String time : MINIRB_TIMES_LIST)
		{
			MiniRbEvent newInstance = MiniRbEvent.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerHighRatesEvent()
	{
		HighRatesEvent.loadData();
		HighRatesEvent.loadEventMobsData();
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(HighRatesEvent.get_eventName());
		
		for (String time : HIGHRATES_TIMES_LIST)
		{
			HighRatesEvent newInstance = HighRatesEvent.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	public static void registerSoloZoneEvent()
	{
		SoloZone.loadData();
		SoloZone.loadEventMobsData();
		
		EventsGlobalTask.getInstance().clearEventTasksByEventName(SoloZone.get_eventName());
		
		for (String time : SOLO_ZONE_TIMES_LIST)
		{
			SoloZone newInstance = SoloZone.getNewInstance();
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
}