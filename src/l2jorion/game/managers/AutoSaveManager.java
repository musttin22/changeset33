package l2jorion.game.managers;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import l2jorion.Config;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class AutoSaveManager
{
	protected static final Logger LOG = LoggerFactory.getLogger(AutoSaveManager.class);
	
	private ScheduledFuture<?> _autoSaveInDB;
	
	public static final AutoSaveManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public AutoSaveManager()
	{
		LOG.info("Initializing: Auto Save Manager");
	}
	
	public void stopAutoSaveManager()
	{
		if (_autoSaveInDB != null)
		{
			_autoSaveInDB.cancel(true);
			_autoSaveInDB = null;
		}
	}
	
	public void startAutoSaveManager()
	{
		stopAutoSaveManager();
		
		if (Config.AUTOSAVE_INITIAL_TIME > 0)
		{
			_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		}
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values();
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					try
					{
						player.store(true, false);
					}
					catch (Exception e)
					{
						LOG.info("Auto Save Manager: Error saving player: " + player.getName(), e);
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSaveManager _instance = new AutoSaveManager();
	}
}