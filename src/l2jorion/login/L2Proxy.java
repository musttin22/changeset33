package l2jorion.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class L2Proxy
{
	private final int _gameserverId;
	private final int _proxyServerId;
	private final InetAddress _proxyAddress;
	private final int _proxyPort;
	private final boolean _hidesGameserver;
	
	public L2Proxy(int gameserverId, int proxyServerId, String proxyHost, int proxyPort, boolean hidesGameserver) throws UnknownHostException
	{
		_gameserverId = gameserverId;
		_proxyServerId = proxyServerId;
		_proxyAddress = InetAddress.getByName(proxyHost);
		_proxyPort = proxyPort;
		_hidesGameserver = hidesGameserver;
	}
	
	public int getGameserverId()
	{
		return _gameserverId;
	}
	
	public int getProxyServerId()
	{
		return _proxyServerId;
	}
	
	public InetAddress getProxyAddress()
	{
		return _proxyAddress;
	}
	
	public int getProxyPort()
	{
		return _proxyPort;
	}
	
	public boolean hidesGameserver()
	{
		return _hidesGameserver;
	}
}