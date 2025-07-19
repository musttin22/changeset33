package l2jorion.game.controllers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l2jorion.game.cache.HtmCache;
import l2jorion.game.model.Inertia;
import l2jorion.game.model.InertiaPanel;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.thread.ThreadPoolManager;

/**
 * Controlador principal del sistema Inertia adaptado para L2JOrion changeset 33 rev 1161
 */
public class InertiaController
{
	private static final Logger _log = Logger.getLogger(InertiaController.class.getName());
	
	private static final ConcurrentHashMap<Integer, Inertia> _inertiaInstances = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, ScheduledFuture<?>> _inertiaThreads = new ConcurrentHashMap<>();
	
	/**
	 * Inicializa una instancia de Inertia para un jugador
	 * @param player El jugador para el cual inicializar Inertia
	 */
	public static void initInertia(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		int playerId = player.getObjectId();
		
		// Si ya existe una instancia, la detenemos
		if (_inertiaInstances.containsKey(playerId))
		{
			stopInertia(player);
		}
		
		// Creamos nueva instancia
		Inertia inertia = new Inertia(player);
		_inertiaInstances.put(playerId, inertia);
		
		_log.info("Inertia inicializado para jugador: " + player.getName());
	}
	
	/**
	 * Inicia el sistema Inertia para un jugador
	 * @param player El jugador para el cual iniciar el sistema
	 */
	public static void startInertia(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		int playerId = player.getObjectId();
		Inertia inertia = _inertiaInstances.get(playerId);
		
		if (inertia == null)
		{
			initInertia(player);
			inertia = _inertiaInstances.get(playerId);
		}
		
		if (inertia != null && !inertia.isActive())
		{
			inertia.setActive(true);
			
			// Iniciamos el hilo de ejecución usando ThreadPoolManager de L2JOrion
			ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(inertia, 100, 100);
			_inertiaThreads.put(playerId, task);
			
			player.sendMessage("Sistema Inertia activado.");
			_log.info("Inertia iniciado para jugador: " + player.getName());
		}
	}
	
	/**
	 * Detiene el sistema Inertia para un jugador
	 * @param player El jugador para el cual detener el sistema
	 */
	public static void stopInertia(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		int playerId = player.getObjectId();
		
		// Detenemos el hilo
		ScheduledFuture<?> task = _inertiaThreads.remove(playerId);
		if (task != null)
		{
			task.cancel(true);
		}
		
		// Desactivamos la instancia
		Inertia inertia = _inertiaInstances.get(playerId);
		if (inertia != null)
		{
			inertia.setActive(false);
		}
		
		player.sendMessage("Sistema Inertia desactivado.");
		_log.info("Inertia detenido para jugador: " + player.getName());
	}
	
	/**
	 * Remueve completamente la instancia de Inertia de un jugador
	 * @param player El jugador para el cual remover Inertia
	 */
	public static void removeInertia(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		int playerId = player.getObjectId();
		
		stopInertia(player);
		_inertiaInstances.remove(playerId);
		
		_log.info("Inertia removido para jugador: " + player.getName());
	}
	
	/**
	 * Obtiene la instancia de Inertia de un jugador
	 * @param player El jugador del cual obtener la instancia
	 * @return La instancia de Inertia o null si no existe
	 */
	public static Inertia getInertia(L2PcInstance player)
	{
		if (player == null)
		{
			return null;
		}
		return _inertiaInstances.get(player.getObjectId());
	}
	
	/**
	 * Procesa comandos de Inertia
	 * @param player El jugador que ejecuta el comando
	 * @param command El comando a procesar
	 */
	public static void processCommand(L2PcInstance player, String command)
	{
		if (player == null || command == null)
		{
			return;
		}
		
		String[] parts = command.split(" ");
		String cmd = parts[0].toLowerCase();
		
		switch (cmd)
		{
			case "start":
				startInertia(player);
				break;
			case "stop":
				stopInertia(player);
				break;
			case "panel":
				showPanel(player);
				break;
			case "config":
				showConfiguration(player);
				break;
			default:
				player.sendMessage("Comando no reconocido. Usa: start, stop, panel, config");
				break;
		}
	}
	
	/**
	 * Muestra el panel principal de Inertia
	 * @param player El jugador al cual mostrar el panel
	 */
	public static void showPanel(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		Inertia inertia = getInertia(player);
		if (inertia == null)
		{
			initInertia(player);
			inertia = getInertia(player);
		}
		
		InertiaPanel panel = new InertiaPanel(inertia);
		String html = panel.generateMainPanel();
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
	
	/**
	 * Muestra la configuración de Inertia
	 * @param player El jugador al cual mostrar la configuración
	 */
	public static void showConfiguration(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		String html = generateConfigPanel(player);
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
	
	/**
	 * Genera el HTML del panel de configuración
	 * @param player El jugador para el cual generar el panel
	 * @return El HTML del panel de configuración
	 */
	private static String generateConfigPanel(L2PcInstance player)
	{
		Inertia inertia = getInertia(player);
		if (inertia == null)
		{
			return "<html><body><center>Error: Sistema Inertia no inicializado</center></body></html>";
		}
		
		// Intentar cargar el archivo HTML
		String html = HtmCache.getInstance().getHtm("data/html/inertia/config.htm");
		
		if (html != null)
		{
			// Reemplazar variables dinámicas
			String autoAttackStatus = inertia.isAutoAttack() ? "ON" : "OFF";
			html = html.replace("%autoattack_status%", autoAttackStatus);
			
			String autoPickupStatus = inertia.isAutoPickup() ? "ON" : "OFF";
			html = html.replace("%autopickup_status%", autoPickupStatus);
			
			String autoHealStatus = inertia.isAutoHeal() ? "ON" : "OFF";
			html = html.replace("%autoheal_status%", autoHealStatus);
			
			String followStatus = inertia.isFollowMode() ? "ON" : "OFF";
			html = html.replace("%follow_status%", followStatus);
			
			return html;
		}
		
		// Si no encuentra el archivo, generar HTML básico
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Inertia - Configuración</title></head><body>");
		sb.append("<center><font color=\"LEVEL\">Sistema Inertia - Configuración</font></center><br>");
		sb.append("<table width=\"280\">");
		
		sb.append("<tr><td width=\"140\">Auto Attack:</td><td>");
		sb.append("<button value=\"").append(inertia.isAutoAttack() ? "ON" : "OFF").append("\" ");
		sb.append("action=\"bypass -h inertia config autoattack\" width=60 height=20 ");
		sb.append("back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		sb.append("<tr><td>Auto Pickup:</td><td>");
		sb.append("<button value=\"").append(inertia.isAutoPickup() ? "ON" : "OFF").append("\" ");
		sb.append("action=\"bypass -h inertia config autopickup\" width=60 height=20 ");
		sb.append("back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		sb.append("<tr><td>Auto Heal:</td><td>");
		sb.append("<button value=\"").append(inertia.isAutoHeal() ? "ON" : "OFF").append("\" ");
		sb.append("action=\"bypass -h inertia config autoheal\" width=60 height=20 ");
		sb.append("back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		sb.append("</table><br>");
		sb.append("<center><button value=\"Volver\" action=\"bypass -h inertia panel\" width=80 height=20 ");
		sb.append("back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	/**
	 * Procesa bypass de HTML
	 * @param player El jugador que ejecuta el bypass
	 * @param bypass El comando bypass a procesar
	 */
	public static void processBypass(L2PcInstance player, String bypass)
	{
		if (player == null || bypass == null)
		{
			return;
		}
		
		if (!bypass.startsWith("inertia"))
		{
			return;
		}
		
		String[] parts = bypass.split(" ");
		if (parts.length < 2)
		{
			return;
		}
		
		String action = parts[1];
		
		switch (action)
		{
			case "start":
				startInertia(player);
				showPanel(player);
				break;
			case "stop":
				stopInertia(player);
				showPanel(player);
				break;
			case "config":
				if (parts.length > 2)
				{
					processConfigCommand(player, parts[2]);
				}
				else
				{
					showConfiguration(player);
				}
				break;
			case "panel":
				showPanel(player);
				break;
		}
	}
	
	/**
	 * Procesa comandos de configuración
	 * @param player El jugador que ejecuta el comando
	 * @param configCmd El comando de configuración a procesar
	 */
	private static void processConfigCommand(L2PcInstance player, String configCmd)
	{
		Inertia inertia = getInertia(player);
		if (inertia == null)
		{
			return;
		}
		
		switch (configCmd)
		{
			case "autoattack":
				inertia.setAutoAttack(!inertia.isAutoAttack());
				player.sendMessage("Auto Attack: " + (inertia.isAutoAttack() ? "ON" : "OFF"));
				break;
			case "autopickup":
				inertia.setAutoPickup(!inertia.isAutoPickup());
				player.sendMessage("Auto Pickup: " + (inertia.isAutoPickup() ? "ON" : "OFF"));
				break;
			case "autoheal":
				inertia.setAutoHeal(!inertia.isAutoHeal());
				player.sendMessage("Auto Heal: " + (inertia.isAutoHeal() ? "ON" : "OFF"));
				break;
		}
		
		showConfiguration(player);
	}
	
	/**
	 * Limpia todas las instancias (para shutdown del servidor)
	 */
	public static void shutdown()
	{
		_log.info("Cerrando sistema Inertia...");
		
		for (ScheduledFuture<?> task : _inertiaThreads.values())
		{
			if (task != null)
			{
				task.cancel(true);
			}
		}
		
		_inertiaThreads.clear();
		_inertiaInstances.clear();
		
		_log.info("Sistema Inertia cerrado correctamente.");
	}
}