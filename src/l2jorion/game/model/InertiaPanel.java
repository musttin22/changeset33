package l2jorion.game.model;

import l2jorion.game.cache.HtmCache;

/**
 * Panel HTML para el sistema Inertia adaptado para L2JOrion changeset 33 rev 1161
 */
public class InertiaPanel
{
	private final Inertia _inertia;
	
	public InertiaPanel(Inertia inertia)
	{
		_inertia = inertia;
	}
	
	/**
	 * Genera el panel principal
	 * @return El HTML del panel principal
	 */
	public String generateMainPanel()
	{
		// Cargar el archivo HTML
		String html = HtmCache.getInstance().getHtm("data/html/inertia/main.htm");
		
		if (html == null)
		{
			// Si no encuentra el archivo, generar HTML básico
			return generateBasicMainPanel();
		}
		
		// Reemplazar variables dinámicas
		
		// Estado del sistema
		String status = _inertia.isActive() ? "<font color=\"00FF00\">ACTIVO</font>" : "<font color=\"FF0000\">INACTIVO</font>";
		html = html.replace("%status%", status);
		
		// Botón de control
		String controlButton = _inertia.isActive() ? "<button value=\"Detener\" action=\"bypass -h inertia stop\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" : "<button value=\"Iniciar\" action=\"bypass -h inertia start\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		html = html.replace("%control_button%", controlButton);
		
		// Estados de funciones
		String autoAttack = _inertia.isAutoAttack() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>";
		html = html.replace("%auto_attack%", autoAttack);
		
		String autoPickup = _inertia.isAutoPickup() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>";
		html = html.replace("%auto_pickup%", autoPickup);
		
		String autoHeal = _inertia.isAutoHeal() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>";
		html = html.replace("%auto_heal%", autoHeal);
		
		String followMode = _inertia.isFollowMode() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>";
		html = html.replace("%follow_mode%", followMode);
		
		// Estadísticas de farm
		String farmStats = _inertia.getFarmStats();
		html = html.replace("%farm_stats%", farmStats);
		
		return html;
	}
	
	/**
	 * Genera panel básico si no encuentra el archivo HTML
	 * @return El HTML del panel básico
	 */
	private String generateBasicMainPanel()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><head><title>Sistema Inertia</title></head><body>");
		sb.append("<center><font color=\"LEVEL\">Sistema Inertia v1.0</font></center><br>");
		
		// Estado actual
		sb.append("<table width=\"280\">");
		sb.append("<tr><td width=\"120\">Estado:</td><td>");
		if (_inertia.isActive())
		{
			sb.append("<font color=\"00FF00\">ACTIVO</font>");
		}
		else
		{
			sb.append("<font color=\"FF0000\">INACTIVO</font>");
		}
		sb.append("</td></tr>");
		sb.append("</table><br>");
		
		// Botones de control
		sb.append("<center>");
		if (_inertia.isActive())
		{
			sb.append("<button value=\"Detener\" action=\"bypass -h inertia stop\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			sb.append("<button value=\"Iniciar\" action=\"bypass -h inertia start\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		sb.append("<br><br>");
		sb.append("<button value=\"Configuración\" action=\"bypass -h inertia config\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("</center><br>");
		
		// Estado de funciones
		sb.append("<center><font color=\"LEVEL\">Funciones Activas</font></center><br>");
		sb.append("<table width=\"280\">");
		
		sb.append("<tr><td>Auto Attack:</td><td>");
		sb.append(_inertia.isAutoAttack() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>");
		sb.append("</td></tr>");
		
		sb.append("<tr><td>Auto Pickup:</td><td>");
		sb.append(_inertia.isAutoPickup() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>");
		sb.append("</td></tr>");
		
		sb.append("<tr><td>Auto Heal:</td><td>");
		sb.append(_inertia.isAutoHeal() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>");
		sb.append("</td></tr>");
		
		sb.append("<tr><td>Follow Mode:</td><td>");
		sb.append(_inertia.isFollowMode() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"FF0000\">OFF</font>");
		sb.append("</td></tr>");
		
		sb.append("<tr><td>Farm Stats:</td><td>");
		sb.append(_inertia.getFarmStats());
		sb.append("</td></tr>");
		
		sb.append("</table>");
		
		sb.append("</body></html>");
		
		return sb.toString();
	}
}