package l2jorion.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import l2jorion.util.StringUtil;

public class AuditFormatter extends Formatter
{
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = StringUtil.startAppend(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10), "[", dateFmt.format(new Date(record.getMillis())), "] ", record.getMessage());
		
		if (params != null)
		{
			for (Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				StringUtil.append(output, ", ", p.toString());
			}
		}
		
		output.append(System.lineSeparator());
		return output.toString();
	}
}
