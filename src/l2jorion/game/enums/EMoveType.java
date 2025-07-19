package l2jorion.game.enums;

/**
 * Enumeración para tipos de movimiento
 */
public enum EMoveType
{
	WALK("Caminar"),
	RUN("Correr"),
	FOLLOW("Seguir");
	
	private final String _description;
	
	EMoveType(String description)
	{
		_description = description;
	}
	
	public String getDescription()
	{
		return _description;
	}
}