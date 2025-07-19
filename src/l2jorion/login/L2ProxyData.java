package l2jorion.login;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.xml.IXmlReader;

public class L2ProxyData implements IXmlReader
{
	private static final Logger LOG = LoggerFactory.getLogger(L2ProxyData.class);
	private final Map<Integer, L2Proxy> _proxyServers = new HashMap<>();
	
	public L2Proxy getProxyById(int proxyId)
	{
		return _proxyServers.get(proxyId);
	}
	
	public Collection<L2Proxy> getProxies()
	{
		return _proxyServers.values();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("./config/network/proxy.xml");
		LOG.info("Loaded {} proxy servers", _proxyServers.size());
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
		{
			if ("list".equalsIgnoreCase(list.getNodeName()))
			{
				for (Node a = list.getFirstChild(); a != null; a = a.getNextSibling())
				{
					if ("gameserver".equalsIgnoreCase(a.getNodeName()))
					{
						final NamedNodeMap attrs = a.getAttributes();
						
						int serverId = parseInteger(attrs, "serverId");
						boolean hidesGameserver = parseBoolean(attrs, "hide");
						
						for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("proxy".equalsIgnoreCase(b.getNodeName()))
							{
								final NamedNodeMap attrs2 = b.getAttributes();
								
								int proxyServerId = parseInteger(attrs2, "proxyServerId");
								String proxyHost = parseString(attrs2, "proxyHost");
								int proxyPort = parseInteger(attrs2, "proxyPort");
								
								try
								{
									final L2Proxy proxy = new L2Proxy(serverId, proxyServerId, proxyHost, proxyPort, hidesGameserver);
									_proxyServers.put(proxyServerId, proxy);
								}
								catch (UnknownHostException ex)
								{
									LOG.warn("Failed to process proxy due to badly formatted proxy host", ex);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static L2ProxyData getInstance()
	{
		return L2ProxyData.SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final L2ProxyData INSTANCE = new L2ProxyData();
	}
}