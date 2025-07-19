package l2jorion.log.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.templates.L2EtcItemType;

public class ItemFilter implements Filter
{
	private static final String EXCLUDE_PROCESS = "Consume";
	private static final Set<L2EtcItemType> EXCLUDED_ITEM_TYPES = new HashSet<>();
	
	static
	{
		EXCLUDED_ITEM_TYPES.add(L2EtcItemType.ARROW);
		EXCLUDED_ITEM_TYPES.add(L2EtcItemType.SHOT);
	}
	
	@Override
	public boolean isLoggable(LogRecord record)
	{
		if (!"item".equals(record.getLoggerName()))
		{
			return false;
		}
		
		String[] messageList = record.getMessage().split(":");
		if ((messageList.length < 2) || !EXCLUDE_PROCESS.contains(messageList[1]))
		{
			return true;
		}
		
		L2ItemInstance item = ((L2ItemInstance) record.getParameters()[0]);
		if (!EXCLUDED_ITEM_TYPES.contains(item.getItemType()))
		{
			return true;
		}
		
		return false;
	}
}
