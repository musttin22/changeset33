package l2jorion.game.model;

import l2jorion.game.model.actor.instance.L2PcInstance;

/**
 * Interfaz para comportamientos de Inertia adaptada para L2JOrion changeset 33 rev 1161
 */
public interface IInertiaBehave
{
	/**
	 * Expande el comportamiento con otro comportamiento
	 * @param behave El comportamiento a expandir
	 */
	public default void expand(IInertiaBehave behave)
	{
		// Implementación por defecto vacía
	}
	
	/**
	 * Se ejecuta al inicio del ciclo de pensamiento
	 */
	void onThinkStart();
	
	/**
	 * Se ejecuta al final del ciclo de pensamiento
	 */
	void onThinkEnd();
	
	/**
	 * Se ejecuta cuando el jugador muere
	 * @param killer El asesino
	 */
	void onDeath(L2Character killer);
	
	/**
	 * Se ejecuta cuando el jugador mata a alguien
	 * @param victim La víctima
	 */
	void onKill(L2Character victim);
	
	/**
	 * Se ejecuta cuando el jugador ataca
	 * @param target El objetivo atacado
	 */
	void onAttack(L2Character target);
	
	/**
	 * Se ejecuta cuando el jugador usa una skill
	 * @param skill La skill usada
	 */
	void onSkillCast(L2Skill skill);
	
	/**
	 * Se ejecuta cuando cambia el objetivo
	 * @param oldTarget El objetivo anterior
	 * @param newTarget El nuevo objetivo
	 */
	void onNewTarget(L2Character oldTarget, L2Character newTarget);
	
	/**
	 * Filtra si una skill debe ser usada
	 * @param skill La skill a filtrar
	 * @return true si la skill debe ser usada
	 */
	boolean filterSkill(L2Skill skill);
	
	/**
	 * Filtra si un objetivo es válido
	 * @param target El objetivo a filtrar
	 * @return true si el objetivo es válido
	 */
	boolean filterTarget(L2Character target);
	
	/**
	 * Se ejecuta mientras el jugador está muerto
	 */
	void whileDead();
	
	/**
	 * Se ejecuta mientras el objetivo está muerto
	 */
	void whileTargetDead();
	
	/**
	 * Se ejecuta cuando se acaban los créditos
	 */
	void onCreditsEnd();
	
	/**
	 * Multiplicador de lag
	 * @return El multiplicador de lag
	 */
	float lagMultiplier();
	
	/**
	 * Se ejecuta cuando se quita el objetivo
	 */
	void onUntarget();
	
	/**
	 * Se ejecuta cuando el seguimiento está cerca
	 * @param assistPlayer El jugador a seguir
	 */
	void onFollowClose(L2PcInstance assistPlayer);
	
	/**
	 * Se ejecuta cuando el seguimiento está lejos
	 * @param assistPlayer El jugador a seguir
	 */
	void onFollowFar(L2PcInstance assistPlayer);
	
	/**
	 * Se ejecuta cuando no hay objetivo para asistir
	 * @param assistPlayer El jugador a asistir
	 */
	void onAssistNoTarget(L2PcInstance assistPlayer);
	
	/**
	 * Se ejecuta cuando inicia auto attack
	 * @param actualTarget El objetivo actual
	 */
	void onStartAutoAttack(L2Character actualTarget);
	
	/**
	 * Establece la instancia de Inertia
	 * @param inertia La instancia de Inertia
	 */
	public void setInertia(final Inertia inertia);
	
	/**
	 * Obtiene la instancia de Inertia
	 * @return La instancia de Inertia
	 */
	public Inertia getInertia();
	
	/**
	 * Obtiene la prioridad del comportamiento
	 * @return La prioridad (por defecto 0)
	 */
	public default int getPriority()
	{
		return 0;
	}
	
	/**
	 * Busca un objetivo
	 * @return El objetivo encontrado o null
	 */
	public L2Character searchTarget();
	
	/**
	 * Se ejecuta cuando no se encuentra objetivo
	 */
	public void onNoTargetFound();
}