package l2jorion.game.model.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Tabla de drops de items adaptada para L2JOrion changeset 33 rev 1161 Versión mejorada con configuraciones avanzadas
 */
public class ItemDropTable
{
	private static final Logger _log = Logger.getLogger(ItemDropTable.class.getName());
	private static ItemDropTable _instance;
	
	private final Map<Integer, Integer> _itemValues = new ConcurrentHashMap<>();
	private final Map<Integer, String> _itemCategories = new ConcurrentHashMap<>();
	
	public static ItemDropTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemDropTable();
		}
		return _instance;
	}
	
	private ItemDropTable()
	{
		loadDefaultDrops();
		loadItemCategories();
	}
	
	/**
	 * Carga drops por defecto para mobs comunes
	 */
	private void loadDefaultDrops()
	{
		// Items valiosos comunes
		_itemValues.put(57, 1); // Adena
		
		// Crystals
		_itemValues.put(1458, 100); // Crystal D
		_itemValues.put(1459, 200); // Crystal C
		_itemValues.put(1460, 400); // Crystal B
		_itemValues.put(1461, 800); // Crystal A
		_itemValues.put(1462, 1600); // Crystal S
		
		// Herbs
		_itemValues.put(8600, 50); // Herb of Life
		_itemValues.put(8601, 100); // Herb of Mana
		_itemValues.put(8602, 150); // Herb of Strength
		_itemValues.put(8603, 200); // Herb of Dexterity
		_itemValues.put(8604, 200); // Herb of Constitution
		_itemValues.put(8605, 200); // Herb of Intelligence
		_itemValues.put(8606, 200); // Herb of Wisdom
		_itemValues.put(8607, 200); // Herb of Wit
		_itemValues.put(8608, 200); // Herb of Mental Shield
		_itemValues.put(8609, 200); // Herb of Cleansing
		
		// Recipes y materials
		_itemValues.put(1865, 500); // Varnish of Purity
		_itemValues.put(1866, 300); // Synthetic Cokes
		_itemValues.put(1867, 200); // Oriharukon Ore
		_itemValues.put(1868, 100); // Braided Hemp
		
		// Gemstones
		_itemValues.put(2130, 1000); // Gemstone D
		_itemValues.put(2131, 2000); // Gemstone C
		_itemValues.put(2132, 4000); // Gemstone B
		_itemValues.put(2133, 8000); // Gemstone A
		_itemValues.put(2134, 16000); // Gemstone S
		
		// Scrolls
		_itemValues.put(3936, 300); // Blessed Scroll of Escape
		_itemValues.put(736, 200); // Scroll of Escape
		
		_log.info("ItemDropTable: Cargados " + _itemValues.size() + " items valiosos por defecto");
	}
	
	/**
	 * Carga categorías de items
	 */
	private void loadItemCategories()
	{
		// Adena
		_itemCategories.put(57, "currency");
		
		// Crystals
		for (int i = 1458; i <= 1462; i++)
		{
			_itemCategories.put(i, "crystal");
		}
		
		// Herbs
		for (int i = 8600; i <= 8609; i++)
		{
			_itemCategories.put(i, "herb");
		}
		
		// Gemstones
		for (int i = 2130; i <= 2134; i++)
		{
			_itemCategories.put(i, "gemstone");
		}
		
		// Materials
		_itemCategories.put(1865, "material");
		_itemCategories.put(1866, "material");
		_itemCategories.put(1867, "material");
		_itemCategories.put(1868, "material");
		
		// Scrolls
		_itemCategories.put(3936, "scroll");
		_itemCategories.put(736, "scroll");
	}
	
	/**
	 * Obtiene el valor de un item
	 * @param itemId El ID del item
	 * @return El valor del item o 0 si no es valioso
	 */
	public int getItemValue(int itemId)
	{
		return _itemValues.getOrDefault(itemId, 0);
	}
	
	/**
	 * Verifica si un item es valioso
	 * @param itemId El ID del item
	 * @return true si el item es valioso
	 */
	public boolean isValuableItem(int itemId)
	{
		return _itemValues.containsKey(itemId) || itemId == 57; // Siempre incluir adena
	}
	
	/**
	 * Verifica si un item es de una categoría específica
	 * @param itemId El ID del item
	 * @param category La categoría a verificar
	 * @return true si el item pertenece a la categoría
	 */
	public boolean isItemCategory(int itemId, String category)
	{
		return category.equals(_itemCategories.get(itemId));
	}
	
	/**
	 * Obtiene la categoría de un item
	 * @param itemId El ID del item
	 * @return La categoría del item o "unknown" si no se conoce
	 */
	public String getItemCategory(int itemId)
	{
		return _itemCategories.getOrDefault(itemId, "unknown");
	}
	
	/**
	 * Añade un item valioso
	 * @param itemId El ID del item
	 * @param value El valor del item
	 * @param category La categoría del item
	 */
	public void addValuableItem(int itemId, int value, String category)
	{
		_itemValues.put(itemId, value);
		if (category != null && !category.isEmpty())
		{
			_itemCategories.put(itemId, category);
		}
	}
	
	/**
	 * Remueve un item valioso
	 * @param itemId El ID del item a remover
	 */
	public void removeValuableItem(int itemId)
	{
		_itemValues.remove(itemId);
		_itemCategories.remove(itemId);
	}
	
	/**
	 * Obtiene todos los items valiosos
	 * @return Mapa con todos los items valiosos y sus valores
	 */
	public Map<Integer, Integer> getValuableItems()
	{
		return new ConcurrentHashMap<>(_itemValues);
	}
	
	/**
	 * Obtiene items por categoría
	 * @param category La categoría a buscar
	 * @return Lista de IDs de items de la categoría especificada
	 */
	public List<Integer> getItemsByCategory(String category)
	{
		List<Integer> items = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : _itemCategories.entrySet())
		{
			if (category.equals(entry.getValue()))
			{
				items.add(entry.getKey());
			}
		}
		return items;
	}
	
	/**
	 * Verifica si debe recoger items de una categoría
	 * @param category La categoría a verificar
	 * @return true si debe recoger items de esta categoría
	 */
	public boolean shouldPickupCategory(String category)
	{
		// Por defecto, recoger todas las categorías valiosas
		return category.equals("currency") || category.equals("crystal") || category.equals("herb") || category.equals("gemstone") || category.equals("material") || category.equals("scroll");
	}
	
	/**
	 * Obtiene el valor total de items por categoría
	 * @param category La categoría a evaluar
	 * @return El valor total de todos los items de la categoría
	 */
	public int getCategoryValue(String category)
	{
		int totalValue = 0;
		for (Map.Entry<Integer, String> entry : _itemCategories.entrySet())
		{
			if (category.equals(entry.getValue()))
			{
				totalValue += getItemValue(entry.getKey());
			}
		}
		return totalValue;
	}
}