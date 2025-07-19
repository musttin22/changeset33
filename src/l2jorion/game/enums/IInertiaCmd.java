package l2jorion.game.enums;

/**
 * Interface para comandos de Inertia
 */
public interface IInertiaCmd
{
	
	// Comandos principales
	String START = "start";
	String STOP = "stop";
	String PANEL = "panel";
	String CONFIG = "config";
	
	// Comandos de configuración
	String AUTO_ATTACK = "autoattack";
	String AUTO_PICKUP = "autopickup";
	String AUTO_HEAL = "autoheal";
	String FOLLOW_MODE = "follow";
	
	// Comandos de panel
	String SHOW_MAIN = "main";
	String SHOW_CONFIG = "configuration";
	String SHOW_SKILLS = "skills";
	String SHOW_ITEMS = "items";
}