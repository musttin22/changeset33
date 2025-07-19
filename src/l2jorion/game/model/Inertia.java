package l2jorion.game.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;

/**
 * Clase principal del sistema Inertia adaptada para L2JOrion changeset 33 rev 1161
 */
public class Inertia implements Runnable
{
	private static final Logger _log = Logger.getLogger(Inertia.class.getName());
	
	private final L2PcInstance _player;
	
	private AutoFarmManager _farmManager;
	
	private boolean _active = false;
	private boolean _autoAttack = false;
	private boolean _autoPickup = false;
	private boolean _autoHeal = false;
	private boolean _followMode = false;
	
	private L2PcInstance _followTarget = null;
	
	private int _healHpPercent = 70;
	private int _healMpPercent = 50;
	
	private long _lastAction = 0;
	private long _lastHeal = 0;
	private long _lastPickup = 0;
	
	private final List<Integer> _pickupItems = new CopyOnWriteArrayList<>();
	
	public Inertia(L2PcInstance player)
	{
		_player = player;
		_farmManager = new AutoFarmManager(player, this);
		initDefaultSettings();
	}
	
	private void initDefaultSettings()
	{
		// Items por defecto para pickup (adena)
		_pickupItems.add(57); // Adena
	}
	
	@Override
	public void run()
	{
		if (!_active || _player == null || !_player.isOnline())
		{
			return;
		}
		
		try
		{
			// Verificar si el jugador está muerto
			if (_player.isDead())
			{
				return;
			}
			
			// Usar el farm manager avanzado
			_farmManager.process();
			
			// Follow mode
			if (_followMode && _followTarget != null)
			{
				processFollow();
			}
			
		}
		catch (Exception e)
		{
			_log.warning("Error en Inertia para jugador " + _player.getName() + ": " + e.getMessage());
		}
	}
	
	/**
	 * Procesa follow mode
	 */
	private void processFollow()
	{
		if (_followTarget == null || _followTarget.isOnline() == false)
		{
			_followMode = false;
			return;
		}
		
		// Calcular distancia usando coordenadas
		double distance = Math.sqrt(Math.pow(_player.getX() - _followTarget.getX(), 2) + Math.pow(_player.getY() - _followTarget.getY(), 2));
		
		if (distance > 100)
		{
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _followTarget);
		}
	}
	
	/**
	 * Callback cuando mata un mob
	 */
	public void onKill(L2MonsterInstance victim)
	{
		if (_farmManager != null)
		{
			_farmManager.onKill(victim);
		}
	}
	
	/**
	 * Obtiene estadísticas de farm
	 */
	public String getFarmStats()
	{
		if (_farmManager != null)
		{
			return _farmManager.getStats();
		}
		return "No disponible";
	}
	
	// Getters y Setters
	public boolean isActive()
	{
		return _active;
	}
	
	public void setActive(boolean active)
	{
		_active = active;
	}
	
	public boolean isAutoAttack()
	{
		return _autoAttack;
	}
	
	public void setAutoAttack(boolean autoAttack)
	{
		_autoAttack = autoAttack;
	}
	
	public boolean isAutoPickup()
	{
		return _autoPickup;
	}
	
	public void setAutoPickup(boolean autoPickup)
	{
		_autoPickup = autoPickup;
	}
	
	public boolean isAutoHeal()
	{
		return _autoHeal;
	}
	
	public void setAutoHeal(boolean autoHeal)
	{
		_autoHeal = autoHeal;
	}
	
	public boolean isFollowMode()
	{
		return _followMode;
	}
	
	public void setFollowMode(boolean followMode)
	{
		_followMode = followMode;
	}
	
	public L2PcInstance getFollowTarget()
	{
		return _followTarget;
	}
	
	public void setFollowTarget(L2PcInstance followTarget)
	{
		_followTarget = followTarget;
	}
	
	public int getHealHpPercent()
	{
		return _healHpPercent;
	}
	
	public void setHealHpPercent(int healHpPercent)
	{
		_healHpPercent = healHpPercent;
	}
	
	public int getHealMpPercent()
	{
		return _healMpPercent;
	}
	
	public void setHealMpPercent(int healMpPercent)
	{
		_healMpPercent = healMpPercent;
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public AutoFarmManager getFarmManager()
	{
		return _farmManager;
	}
}