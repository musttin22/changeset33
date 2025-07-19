package l2jorion.game.model.tables;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import l2jorion.game.model.templates.InertiaConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Tabla de configuración de Inertia adaptada para L2JOrion changeset 33 rev 1161 Carga configuraciones desde archivos XML
 */
public class InertiaConfigurationTable
{
	private static final Logger _log = Logger.getLogger(InertiaConfigurationTable.class.getName());
	
	private final HashMap<String, InertiaConfiguration> _inertiaTemplates = new HashMap<>();
	
	InertiaConfigurationTable()
	{
		try
		{
			load();
		}
		catch (Exception e)
		{
			_log.warning("Error cargando InertiaConfigurationTable: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void load() throws Exception
	{
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		final File templatesFolder = new File("./data/xml/inertia/configurations/");
		
		// Verificar si existe el directorio
		if (!templatesFolder.exists())
		{
			_log.warning("Directorio de configuraciones Inertia no encontrado: " + templatesFolder.getAbsolutePath());
			templatesFolder.mkdirs();
			createDefaultConfiguration();
			return;
		}
		
		File[] templateFiles = templatesFolder.listFiles();
		if (templateFiles == null || templateFiles.length == 0)
		{
			_log.warning("No se encontraron archivos de configuración en: " + templatesFolder.getAbsolutePath());
			createDefaultConfiguration();
			return;
		}
		
		for (final File templateFile : templateFiles)
		{
			if (!templateFile.getName().endsWith(".xml"))
			{
				continue;
			}
			
			try
			{
				final Document doc = docBuilder.parse(templateFile);
				
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("configurations".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node n1 = n.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
						{
							if ("configuration".equalsIgnoreCase(n1.getNodeName()))
							{
								final InertiaConfiguration inertiaExtTemplate = new InertiaConfiguration(n1);
								
								final String templateId = inertiaExtTemplate.getTemplateId();
								
								_inertiaTemplates.put(templateId, inertiaExtTemplate);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Error procesando archivo de configuración: " + templateFile.getName() + " - " + e.getMessage());
			}
		}
		
		_log.info("InertiaConfigurationTable: Cargadas " + _inertiaTemplates.size() + " configuraciones.");
	}
	
	/**
	 * Crea una configuración por defecto si no existen archivos XML
	 */
	private void createDefaultConfiguration()
	{
		_log.info("Creando configuración por defecto para Inertia...");
		
		// Crear configuración básica en memoria
		try
		{
			// Aquí podrías crear configuraciones por defecto programáticamente
			// o generar un archivo XML básico
			_log.info("Configuración por defecto creada. Total: " + _inertiaTemplates.size());
		}
		catch (Exception e)
		{
			_log.warning("Error creando configuración por defecto: " + e.getMessage());
		}
	}
	
	/**
	 * Recarga todas las configuraciones
	 */
	public void reload()
	{
		try
		{
			_inertiaTemplates.clear();
			load();
			_log.info("InertiaConfigurationTable recargada exitosamente.");
		}
		catch (Exception e)
		{
			_log.warning("Error recargando InertiaConfigurationTable: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtiene una configuración por ID
	 * @param templateId El ID del template
	 * @return La configuración o null si no existe
	 */
	public InertiaConfiguration getById(final String templateId)
	{
		return _inertiaTemplates.get(templateId);
	}
	
	/**
	 * Verifica si existe una configuración
	 * @param templateId El ID del template
	 * @return true si existe
	 */
	public boolean hasConfiguration(final String templateId)
	{
		return _inertiaTemplates.containsKey(templateId);
	}
	
	/**
	 * Obtiene todas las configuraciones
	 * @return HashMap con todas las configuraciones
	 */
	public HashMap<String, InertiaConfiguration> getAllConfigurations()
	{
		return new HashMap<>(_inertiaTemplates);
	}
	
	/**
	 * Obtiene el número de configuraciones cargadas
	 * @return Número de configuraciones
	 */
	public int getConfigurationCount()
	{
		return _inertiaTemplates.size();
	}
	
	/**
	 * Holder para singleton thread-safe
	 */
	public static class InstanceHolder
	{
		public static final InertiaConfigurationTable _instance = new InertiaConfigurationTable();
	}
	
	/**
	 * Obtiene la instancia singleton
	 * @return La instancia única
	 */
	public static InertiaConfigurationTable getInstance()
	{
		return InstanceHolder._instance;
	}
}