package l2jorion.game.model.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Template de configuración de Inertia adaptado para L2JOrion changeset 33 rev 1161 Maneja configuraciones cargadas desde XML
 */
public class InertiaConfiguration
{
	private static final Logger _log = Logger.getLogger(InertiaConfiguration.class.getName());
	
	private final String _templateId;
	private final Map<String, String> _properties = new HashMap<>();
	
	/**
	 * Constructor que parsea un nodo XML
	 * @param node El nodo XML de configuración
	 */
	public InertiaConfiguration(Node node)
	{
		NamedNodeMap attrs = node.getAttributes();
		
		// Obtener ID del template
		Node idNode = attrs.getNamedItem("id");
		_templateId = idNode != null ? idNode.getNodeValue() : "default";
		
		// Parsear todas las propiedades del nodo
		parseProperties(node);
		
		_log.fine("Configuración Inertia cargada: " + _templateId + " con " + _properties.size() + " propiedades");
	}
	
	/**
	 * Parsea las propiedades del nodo XML
	 * @param node El nodo a parsear
	 */
	private void parseProperties(Node node)
	{
		// Parsear atributos del nodo principal
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null)
		{
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				_properties.put(attr.getNodeName(), attr.getNodeValue());
			}
		}
		
		// Parsear nodos hijos
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				String nodeValue = child.getTextContent();
				
				if (nodeValue != null && !nodeValue.trim().isEmpty())
				{
					_properties.put(nodeName, nodeValue.trim());
				}
				
				// También parsear atributos de nodos hijos
				NamedNodeMap childAttrs = child.getAttributes();
				if (childAttrs != null)
				{
					for (int i = 0; i < childAttrs.getLength(); i++)
					{
						Node attr = childAttrs.item(i);
						String key = nodeName + "." + attr.getNodeName();
						_properties.put(key, attr.getNodeValue());
					}
				}
			}
		}
	}
	
	/**
	 * Obtiene el ID del template
	 * @return El ID del template
	 */
	public String getTemplateId()
	{
		return _templateId;
	}
	
	/**
	 * Obtiene una propiedad como String
	 * @param key La clave de la propiedad
	 * @return El valor o null si no existe
	 */
	public String getProperty(String key)
	{
		return _properties.get(key);
	}
	
	/**
	 * Obtiene una propiedad como String con valor por defecto
	 * @param key La clave de la propiedad
	 * @param defaultValue Valor por defecto
	 * @return El valor o defaultValue si no existe
	 */
	public String getProperty(String key, String defaultValue)
	{
		return _properties.getOrDefault(key, defaultValue);
	}
	
	/**
	 * Obtiene una propiedad como int
	 * @param key La clave de la propiedad
	 * @param defaultValue Valor por defecto
	 * @return El valor como int o defaultValue si no existe o no es válido
	 */
	public int getIntProperty(String key, int defaultValue)
	{
		String value = _properties.get(key);
		if (value != null)
		{
			try
			{
				return Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				_log.warning("Error parseando propiedad int '" + key + "': " + value);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Obtiene una propiedad como boolean
	 * @param key La clave de la propiedad
	 * @param defaultValue Valor por defecto
	 * @return El valor como boolean o defaultValue si no existe
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		String value = _properties.get(key);
		if (value != null)
		{
			return Boolean.parseBoolean(value);
		}
		return defaultValue;
	}
	
	/**
	 * Obtiene una propiedad como double
	 * @param key La clave de la propiedad
	 * @param defaultValue Valor por defecto
	 * @return El valor como double o defaultValue si no existe o no es válido
	 */
	public double getDoubleProperty(String key, double defaultValue)
	{
		String value = _properties.get(key);
		if (value != null)
		{
			try
			{
				return Double.parseDouble(value);
			}
			catch (NumberFormatException e)
			{
				_log.warning("Error parseando propiedad double '" + key + "': " + value);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Verifica si existe una propiedad
	 * @param key La clave de la propiedad
	 * @return true si existe
	 */
	public boolean hasProperty(String key)
	{
		return _properties.containsKey(key);
	}
	
	/**
	 * Obtiene todas las propiedades
	 * @return Mapa con todas las propiedades
	 */
	public Map<String, String> getAllProperties()
	{
		return new HashMap<>(_properties);
	}
	
	/**
	 * Obtiene el número de propiedades
	 * @return Número de propiedades
	 */
	public int getPropertyCount()
	{
		return _properties.size();
	}
	
	@Override
	public String toString()
	{
		return "InertiaConfiguration{" + "templateId='" + _templateId + '\'' + ", properties=" + _properties.size() + '}';
	}
}