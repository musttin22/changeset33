package l2jorion.bots.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.bots.model.FarmLocation;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.xml.IXmlReader;

public class botFarm implements IXmlReader
{
	private static final Logger LOG = LoggerFactory.getLogger(botFarm.class);
	
	protected int lastZoneId = 1;
	
	private final static Map<Integer, List<FarmLocation>> _botFarmLocations = new HashMap<>();
	
	public botFarm()
	{
		load();
	}
	
	public void reload()
	{
		_botFarmLocations.clear();
		
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("./config/bots/botFarm.xml");
		
		LOG.info("BotEngine: Loaded " + _botFarmLocations.size() + " bot farm location set(s) ");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
		{
			if ("list".equalsIgnoreCase(list.getNodeName()))
			{
				for (Node skin = list.getFirstChild(); skin != null; skin = skin.getNextSibling())
				{
					if ("bot".equalsIgnoreCase(skin.getNodeName()))
					{
						final NamedNodeMap attrs = skin.getAttributes();
						
						int townId = parseInteger(attrs, "zoneId");
						String type = parseString(attrs, "type");
						if (type == null)
						{
							type = "default";
						}
						
						List<FarmLocation> data = new ArrayList<>();
						
						for (Node typeN = skin.getFirstChild(); typeN != null; typeN = typeN.getNextSibling())
						{
							if ("node".equalsIgnoreCase(typeN.getNodeName()))
							{
								final NamedNodeMap attrs2 = typeN.getAttributes();
								
								int x = parseInteger(attrs2, "X");
								int y = parseInteger(attrs2, "Y");
								int z = parseInteger(attrs2, "Z");
								data.add(new FarmLocation(x, y, z, type));
							}
						}
						
						lastZoneId = townId;
						_botFarmLocations.put(townId, data);
					}
				}
			}
		}
		
	}
	
	public List<FarmLocation> getFarmNode(int townId)
	{
		if (!_botFarmLocations.containsKey(townId))
		{
			return null;
		}
		
		return _botFarmLocations.get(townId);
	}
	
	public Map<Integer, List<FarmLocation>> getFarmNodeOptions()
	{
		return _botFarmLocations;
	}
	
	public int getLastZoneId()
	{
		return lastZoneId;
	}
	
	public static botFarm getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final botFarm _instance = new botFarm();
	}
}