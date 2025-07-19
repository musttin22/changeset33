package l2jorion.game.datatables.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import l2jorion.Config;
import l2jorion.game.model.L2ExtractableItem;
import l2jorion.game.model.L2ExtractableProductItem;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class ExtractableItemsData
{
	protected static final Logger LOG = LoggerFactory.getLogger(ExtractableItemsData.class);
	
	private Map<Integer, L2ExtractableItem> _items;
	
	private static ExtractableItemsData _instance = null;
	
	public static ExtractableItemsData getInstance()
	{
		if (_instance == null)
		{
			_instance = new ExtractableItemsData();
		}
		
		return _instance;
	}
	
	public ExtractableItemsData()
	{
		_items = new HashMap<>();
		
		Scanner s = null;
		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT + "/data/csv/extractable_items.csv"));
			
			int lineCount = 0;
			while (s.hasNextLine())
			{
				lineCount++;
				
				String line = s.nextLine();
				
				if (line.startsWith("#"))
				{
					continue;
				}
				else if (line.equals(""))
				{
					continue;
				}
				
				String[] lineSplit = line.split(";");
				String[] lineSplit2 = lineSplit[1].split(":");
				
				int itemID = 0;
				int itemChance = 0;
				
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
					if (lineSplit2.length > 1)
					{
						itemChance = Integer.parseInt(lineSplit2[0]);
					}
				}
				catch (Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					LOG.error("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
					continue;
				}
				
				List<L2ExtractableProductItem> products = new ArrayList<>(lineSplit.length);
				String[] lineReader = null;
				if (itemChance != 0)
				{
					lineReader = lineSplit2;
				}
				else
				{
					lineReader = lineSplit;
				}
				
				for (int i = 0; i < lineReader.length - 1; i++)
				{
					String[] lineSplit3 = lineReader[i + 1].split(",");
					if (lineSplit3.length < 2)
					{
						LOG.error("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
						continue;
					}
					
					int production = 0, amount = 0, chance = 0;
					
					try
					{
						production = Integer.parseInt(lineSplit3[0]);
						amount = Integer.parseInt(lineSplit3[1]);
						if (itemChance == 0)
						{
							chance = Integer.parseInt(lineSplit3[2]);
						}
					}
					catch (Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOG.error("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
						continue;
					}
					
					products.add(new L2ExtractableProductItem(production, amount, chance));
				}
				
				int fullChances = 0;
				for (L2ExtractableProductItem Pi : products)
				{
					fullChances += Pi.getChance();
				}
				
				if (fullChances > 100)
				{
					LOG.error("Extractable items data: Error in line " + lineCount + " -> all chances together are more than 100!");
					continue;
				}
				
				_items.put(itemID, new L2ExtractableItem(itemID, itemChance, products));
			}
			
			LOG.info("ExtractableItemsData: Loaded " + _items.size() + " extractable items");
		}
		catch (Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.error("Extractable items data: Can not find './data/extractable_items.csv'");
			
		}
		finally
		{
			
			if (s != null)
			{
				try
				{
					s.close();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	
	public L2ExtractableItem getExtractableItem(int itemID)
	{
		return _items.get(itemID);
	}
	
	public int[] itemIDs()
	{
		int size = _items.size();
		int[] result = new int[size];
		int i = 0;
		for (L2ExtractableItem ei : _items.values())
		{
			result[i] = ei.getItemId();
			i++;
		}
		return result;
	}
}