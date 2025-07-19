package l2jorion.game.managers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.game.model.RebirthHolder;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.templates.StatsSet;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.xml.IXmlReader;

public class RebirthManager implements IXmlReader
{
	private static Logger LOG = LoggerFactory.getLogger(RebirthManager.class);
	
	private final Map<Integer, List<RebirthHolder>> _rebirths = new LinkedHashMap<>();
	private List<RebirthHolder> _skills = new ArrayList<>();
	
	public RebirthManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("./data/xml/rebirth.xml");
		LOG.info(getClass().getSimpleName() + ": Loaded {} rebirth levels and {} skills ", _rebirths.size(), _skills.size());
	}
	
	public void reload()
	{
		_rebirths.clear();
		load();
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node a = doc.getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("rebirth".equalsIgnoreCase(b.getNodeName()))
					{
						final NamedNodeMap attrs = b.getAttributes();
						
						int rebirthLevel = parseInteger(attrs, "level");
						
						final StatsSet set = new StatsSet();
						
						_skills = new ArrayList<>();
						
						for (Node c = b.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("skill".equalsIgnoreCase(c.getNodeName()))
							{
								set.add(parseAttributes(c));
								_skills.add(new RebirthHolder(set));
							}
						}
						
						_rebirths.put(rebirthLevel, _skills);
					}
				}
			}
		}
		
	}
	
	public List<Integer> getRebirthList(L2PcInstance player)
	{
		List<Integer> list = new ArrayList<>();
		for (Integer level : _rebirths.keySet())
		{
			list.add(level);
		}
		return list;
	}
	
	public List<RebirthHolder> getSkills(int rebirthLevel)
	{
		return _rebirths.get(rebirthLevel);
	}
	
	public Map<Integer, List<RebirthHolder>> getRebirths()
	{
		return _rebirths;
	}
	
	public static RebirthManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RebirthManager INSTANCE = new RebirthManager();
	}
}