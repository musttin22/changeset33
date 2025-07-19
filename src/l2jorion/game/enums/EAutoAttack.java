package l2jorion.game.enums;

/**
 * Enumeración para tipos de auto attack
 */
public enum EAutoAttack
{
	DISABLED("Deshabilitado"),
	MONSTERS_ONLY("Solo Monstruos"),
	PLAYERS_ONLY("Solo Jugadores"),
	ALL_TARGETS("Todos los Objetivos");
	
	private final String _description;
	
	EAutoAttack(String description)
	{
		_description = description;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	@Override
	public String toString()
	{
		return _description;
	}
}