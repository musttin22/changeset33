package l2jorion.game.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BiMap - Mapa bidimensional adaptado para L2JOrion changeset 33 rev 1161 Compatible con Java 8 (sin Supplier ni var)
 * @param <K> Tipo de la clave primaria
 * @param <VK> Tipo de la clave secundaria
 * @param <V> Tipo del valor
 */
public class BiMap<K, VK, V> implements Map<K, Map<VK, V>>
{
	private final Map<K, Map<VK, V>> _mapImpl;
	private final boolean _concurrent;
	
	/**
	 * Constructor con implementación específica
	 * @param mapImpl La implementación del mapa a usar
	 * @param concurrent Si usar mapas concurrentes para valores
	 */
	public BiMap(Map<K, Map<VK, V>> mapImpl, boolean concurrent)
	{
		_mapImpl = mapImpl;
		_concurrent = concurrent;
	}
	
	/**
	 * Constructor por defecto (HashMap)
	 */
	public BiMap()
	{
		this(new HashMap<K, Map<VK, V>>(), false);
	}
	
	/**
	 * Constructor con opción de concurrencia
	 * @param concurrent Si usar ConcurrentHashMap
	 */
	public BiMap(final boolean concurrent)
	{
		this(concurrent ? new ConcurrentHashMap<K, Map<VK, V>>() : new HashMap<K, Map<VK, V>>(), concurrent);
	}
	
	/**
	 * Inserta un valor en el mapa bidimensional
	 * @param key1 Clave primaria
	 * @param key2 Clave secundaria
	 * @param value Valor a insertar
	 */
	public void put(K key1, VK key2, V value)
	{
		Map<VK, V> map = _mapImpl.get(key1);
		if (map == null)
		{
			map = createInnerMap();
			map.put(key2, value);
			_mapImpl.put(key1, map);
		}
		else
		{
			map.put(key2, value);
		}
	}
	
	/**
	 * Obtiene un valor del mapa bidimensional
	 * @param key1 Clave primaria
	 * @param key2 Clave secundaria
	 * @return El valor o null si no existe
	 */
	public V get(K key1, VK key2)
	{
		final Map<VK, V> map1 = _mapImpl.get(key1);
		if (map1 == null)
		{
			return null;
		}
		return map1.get(key2);
	}
	
	/**
	 * Verifica si existe una combinación de claves
	 * @param key1 Clave primaria
	 * @param key2 Clave secundaria
	 * @return true si existe la combinación
	 */
	public boolean containsKeys(K key1, VK key2)
	{
		final Map<VK, V> map1 = _mapImpl.get(key1);
		if (map1 == null)
		{
			return false;
		}
		return map1.containsKey(key2);
	}
	
	/**
	 * Remueve un valor específico
	 * @param key1 Clave primaria
	 * @param key2 Clave secundaria
	 * @return El valor removido o null
	 */
	public V removeValue(K key1, VK key2)
	{
		final Map<VK, V> map1 = _mapImpl.get(key1);
		if (map1 == null)
		{
			return null;
		}
		
		V removed = map1.remove(key2);
		
		// Si el mapa interno queda vacío, removerlo también
		if (map1.isEmpty())
		{
			_mapImpl.remove(key1);
		}
		
		return removed;
	}
	
	/**
	 * Obtiene el tamaño del mapa interno para una clave
	 * @param key1 Clave primaria
	 * @return Tamaño del mapa interno o 0 si no existe
	 */
	public int getInnerSize(K key1)
	{
		final Map<VK, V> map1 = _mapImpl.get(key1);
		return map1 == null ? 0 : map1.size();
	}
	
	/**
	 * Crea un nuevo mapa interno
	 * @return Nuevo mapa interno
	 */
	private Map<VK, V> createInnerMap()
	{
		return _concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
	}
	
	// Implementación de Map<K, Map<VK, V>>
	
	@Override
	public int size()
	{
		return _mapImpl.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return _mapImpl.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return _mapImpl.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		return _mapImpl.containsValue(value);
	}
	
	@Override
	public Map<VK, V> get(Object key)
	{
		return _mapImpl.get(key);
	}
	
	@Override
	public Map<VK, V> put(K key, Map<VK, V> value)
	{
		return _mapImpl.put(key, value);
	}
	
	@Override
	public Map<VK, V> remove(Object key)
	{
		return _mapImpl.remove(key);
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends Map<VK, V>> m)
	{
		_mapImpl.putAll(m);
	}
	
	@Override
	public void clear()
	{
		_mapImpl.clear();
	}
	
	@Override
	public Set<K> keySet()
	{
		return _mapImpl.keySet();
	}
	
	@Override
	public Collection<Map<VK, V>> values()
	{
		return _mapImpl.values();
	}
	
	@Override
	public Set<Entry<K, Map<VK, V>>> entrySet()
	{
		return _mapImpl.entrySet();
	}
	
	@Override
	public String toString()
	{
		return _mapImpl.toString();
	}
}