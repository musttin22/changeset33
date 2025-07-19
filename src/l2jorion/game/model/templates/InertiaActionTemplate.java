package l2jorion.game.model.templates;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Template para acciones de Inertia adaptado para L2JOrion changeset 33 rev 1161 Define configuraciones de skills y acciones automáticas
 */
public class InertiaActionTemplate
{
	private final String _alias;
	private final int _skillId;
	private final int _priority;
	private final String _nextAction;
	private final int _selfHp;
	private final int _repeat;
	private final double _reuse;
	
	/**
	 * Constructor que parsea un nodo XML de acción
	 * @param n El nodo XML que contiene la configuración de la acción
	 * @param priority La prioridad de la acción
	 */
	public InertiaActionTemplate(final Node n, final int priority)
	{
		final NamedNodeMap nnm2 = n.getAttributes();
		
		// Parsear atributos requeridos
		_alias = nnm2.getNamedItem("alias").getNodeValue();
		_skillId = Integer.parseInt(nnm2.getNamedItem("id").getNodeValue());
		_priority = priority;
		
		// Parsear atributos opcionales con valores por defecto
		_repeat = nnm2.getNamedItem("repeat") == null ? -1 : Integer.parseInt(nnm2.getNamedItem("repeat").getNodeValue());
		_nextAction = nnm2.getNamedItem("nextAction") == null ? "EMPTY" : nnm2.getNamedItem("nextAction").getNodeValue();
		_selfHp = nnm2.getNamedItem("selfhp") == null ? 101 : Integer.parseInt(nnm2.getNamedItem("selfhp").getNodeValue());
		_reuse = nnm2.getNamedItem("reuse") == null ? -1 : Double.parseDouble(nnm2.getNamedItem("reuse").getNodeValue());
	}
	
	/**
	 * Obtiene el tiempo de reutilización de la acción
	 * @return El tiempo de reuse en segundos, -1 si no tiene límite
	 */
	public double getReuse()
	{
		return _reuse;
	}
	
	/**
	 * Obtiene el alias de la acción
	 * @return El alias identificador de la acción
	 */
	public String getAlias()
	{
		return _alias;
	}
	
	/**
	 * Obtiene el ID de la skill asociada
	 * @return El ID de la skill a ejecutar
	 */
	public int getSkillId()
	{
		return _skillId;
	}
	
	/**
	 * Obtiene la prioridad de la acción
	 * @return La prioridad (mayor número = mayor prioridad)
	 */
	public int getPriority()
	{
		return _priority;
	}
	
	/**
	 * Obtiene la siguiente acción a ejecutar
	 * @return El alias de la siguiente acción o "EMPTY" si no hay
	 */
	public String getNextAction()
	{
		return _nextAction;
	}
	
	/**
	 * Obtiene el porcentaje de HP requerido para ejecutar la acción
	 * @return El porcentaje de HP mínimo (101 = sin restricción)
	 */
	public int getSelfHp()
	{
		return _selfHp;
	}
	
	/**
	 * Obtiene el número de repeticiones de la acción
	 * @return El número de veces a repetir (-1 = infinito)
	 */
	public int getRepeat()
	{
		return _repeat;
	}
	
	/**
	 * Verifica si la acción puede ejecutarse con el HP actual
	 * @param currentHpPercent El porcentaje actual de HP del jugador
	 * @return true si puede ejecutarse
	 */
	public boolean canExecuteWithHp(int currentHpPercent)
	{
		return _selfHp >= 101 || currentHpPercent >= _selfHp;
	}
	
	/**
	 * Verifica si la acción tiene límite de repeticiones
	 * @return true si tiene límite de repeticiones
	 */
	public boolean hasRepeatLimit()
	{
		return _repeat > 0;
	}
	
	/**
	 * Verifica si la acción tiene tiempo de reutilización
	 * @return true si tiene cooldown
	 */
	public boolean hasReuse()
	{
		return _reuse > 0;
	}
	
	/**
	 * Verifica si hay una acción siguiente configurada
	 * @return true si hay siguiente acción
	 */
	public boolean hasNextAction()
	{
		return _nextAction != null && !_nextAction.equals("EMPTY");
	}
	
	@Override
	public String toString()
	{
		return "InertiaActionTemplate{" + "alias='" + _alias + '\'' + ", skillId=" + _skillId + ", priority=" + _priority + ", selfHp=" + _selfHp + ", repeat=" + _repeat + ", reuse=" + _reuse + ", nextAction='" + _nextAction + '\'' + '}';
	}
}