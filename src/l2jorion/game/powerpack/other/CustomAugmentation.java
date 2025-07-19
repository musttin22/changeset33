package l2jorion.game.powerpack.other;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javolution.text.TextBuilder;
import l2jorion.Config;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.datatables.xml.AugmentationData;
import l2jorion.game.datatables.xml.AugmentationScrollData;
import l2jorion.game.datatables.xml.AugmentationScrollData.L2AugmentScroll;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.model.L2Augmentation;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.InventoryUpdate;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.StatusUpdate;
import l2jorion.game.templates.L2Item;
import l2jorion.game.util.Util;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class CustomAugmentation implements ICustomByPassHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(CustomAugmentation.class);
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"aug_index",
			"aug_item"
		};
	}
	
	private enum CommandEnum
	{
		aug_index,
		aug_item
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		CommandEnum comm = CommandEnum.valueOf(command);
		if (comm == null)
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(parameters, " ");
		
		switch (comm)
		{
			case aug_index:
			{
				String page = st.nextToken();
				String option = st.nextToken();
				
				showMainWindow(player, Integer.parseInt(page), Integer.parseInt(option));
				break;
			}
			case aug_item:
			{
				String scrolId = st.nextToken();
				String page = st.nextToken();
				String option = st.nextToken();
				
				L2ItemInstance currentWeapon = player.getActiveWeaponInstance();
				
				handleAugment(player, currentWeapon, Integer.parseInt(scrolId));
				showMainWindow(player, Integer.parseInt(page), Integer.parseInt(option));
				break;
			}
		}
	}
	
	public void showMainWindow(L2PcInstance player, int page, int option)
	{
		int pageLimit = 8;
		L2Skill augSkill = null;
		String itemIcon = "";
		List<L2AugmentScroll> list = AugmentationScrollData.getInstance().getScrolls().values().stream()//
			.filter(scroll -> (option == 1 ? SkillTable.getInstance().getInfo(scroll.getAugmentSkillId(), 1).isActive() : SkillTable.getInstance().getInfo(scroll.getAugmentSkillId(), 1).isPassive())).collect(Collectors.toList());
		
		if (list.isEmpty())
		{
			player.sendMessage("List is empty.");
			return;
		}
		
		final int max = list.size() / pageLimit + (list.size() % pageLimit == 0 ? 0 : 1);
		page = page > max ? max : page < 1 ? 1 : page;
		list = list.subList((page - 1) * pageLimit, Math.min(page * pageLimit, list.size()));
		
		String filename = "data/html/mods/augmentation/index.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		
		TextBuilder reply = new TextBuilder();
		
		reply.append("<table cellspacing=0 cellpadding=0 align=center>");
		reply.append("<tr>");
		
		if (option == 1)
		{
			reply.append("<td align=center><button value=\"Active\" width=95 height=25 back=\"L2UI_ch3.skill_tab1\" fore=\"L2UI_ch3.skill_tab1\"></td>");
			reply.append("<td align=center><button value=\"Passive\" action=\"bypass -h custom_aug_index " + page + " 2\" width=95 height=25 back=\"L2UI_ch3.skill_tab1\" fore=\"L2UI_ch3.skill_tab2\"></td>");
		}
		else
		{
			reply.append("<td align=center><button value=\"Active\" action=\"bypass -h custom_aug_index " + page + " 1\" width=95 height=25 back=\"L2UI_ch3.skill_tab1\" fore=\"L2UI_ch3.skill_tab2\"></td>");
			reply.append("<td align=center><button value=\"Passive\" width=95 height=25 back=\"L2UI_ch3.skill_tab1\" fore=\"L2UI_ch3.skill_tab1\"></td>");
		}
		
		reply.append("</tr>");
		reply.append("</table>");
		reply.append("<img height=-2><img src=\"l2ui.SquareGray\" width=\"298\" height=\"1\">");
		
		int color = 1;
		for (L2AugmentScroll scroll : list)
		{
			if (scroll == null)
			{
				continue;
			}
			
			augSkill = SkillTable.getInstance().getInfo(scroll.getAugmentSkillId(), 1);
			
			itemIcon = (augSkill.isPassive() ? "icon.skill3238" : "icon.skill3123");
			
			if (color == 1)
			{
				reply.append("<table width=300 border=0 bgcolor=000000><tr>");
				color = 2;
			}
			else
			{
				reply.append("<table width=300 border=0><tr>");
				color = 1;
			}
			
			reply.append("<td valign=top width=35><img src=\"" + itemIcon + "\" width=32 height=32/></td>");
			reply.append("<td valign=top width=235>");
			reply.append("<table border=0 width=100%>");
			
			reply.append("<tr><td width=235><a action=\"bypass -h custom_aug_item " + scroll.getAugmentScrollId() + " " + page + " " + option + "\">" + augSkill.getName() + " </a>&nbsp; Lv " + scroll.getAugmentSkillLv() + "</td></tr>");
			reply.append("<tr><td width=235>Price: <font color=LEVEL>" + Util.formatAdena(scroll.getPriceAmount()) + "</font> " + L2Item.getItemNameById(scroll.getPriceItemId()) + "</td></tr>");
			
			reply.append("</table></td>");
			reply.append("</tr></table>");
			
		}
		reply.append("<img src=\"l2ui.SquareGray\" width=\"298\" height=\"1\">");
		reply.append("<img height=-2><table width=300><tr>");
		reply.append("<td width=66>" + ((page == 1) ? "" : "<button value=\"Back\" action=\"bypass -h custom_aug_index " + (page - 1) + " " + option + "\"width=66 height=16 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">") + "</td>");
		reply.append("<td align=center width=138>Page: " + page + " / " + max + "</td>");
		reply.append("<td width=66>" + ((page < max) ? "<button value=\"Next\" action=\"bypass -h custom_aug_index " + (page + 1) + " " + option + "\" width=66 height=16 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">" : "") + "</td>");
		reply.append("</tr></table>");
		html.replace("%list%", reply.toString());
		player.sendPacket(html);
	}
	
	private void handleAugment(L2PcInstance player, L2ItemInstance weapon, int scrrollId)
	{
		L2AugmentScroll enchant = AugmentationScrollData.getInstance().getScrollById(scrrollId);
		
		if (enchant != null)
		{
			if (weapon == null)
			{
				player.sendMessage("Put on your weapon.");
				player.sendPacket(new ExShowScreenMessage("Put on your weapon.", 3000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
			
			if (weapon.getItem().getType2() != L2Item.TYPE2_WEAPON)
			{
				return;
			}
			
			if (weapon.getItem().getCrystalType() == L2Item.CRYSTAL_NONE || weapon.getItem().getCrystalType() == L2Item.CRYSTAL_D || weapon.getItem().getCrystalType() == L2Item.CRYSTAL_C)
			{
				player.sendMessage("You can't augment this grade weapon.");
				player.sendPacket(new ExShowScreenMessage("You can't augment this grade weapon.", 3000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
			
			if (!Config.ALPHA_CUSTOM)
			{
				if (weapon.isHeroItem())
				{
					player.sendMessage("You can't augment hero weapon: " + weapon.getItemName() + ".");
					player.sendPacket(new ExShowScreenMessage("You can't augment hero weapon: " + weapon.getItemName() + ".", 3000, 2, false));
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					return;
				}
			}
			
			if (weapon.isAugmented())
			{
				player.sendMessage("This weapon is already augmented.");
				player.sendPacket(new ExShowScreenMessage("This weapon is already augmented.", 3000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
			
			if (weapon.getOwnerId() != player.getObjectId())
			{
				player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				return;
			}
			
			if (!player.destroyItemByItemId("ConsumeItem", enchant.getPriceItemId(), enchant.getPriceAmount(), null, true))
			{
				return;
			}
			
			// unequip item
			if (weapon.isEquipped())
			{
				L2ItemInstance[] unequipped = player.getInventory().unEquipItemInSlotAndRecord(weapon.getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				
				for (L2ItemInstance itm : unequipped)
				{
					iu.addModifiedItem(itm);
				}
				
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			int skill = enchant.getAugmentSkillId();
			int level = enchant.getAugmentSkillLv();
			
			final L2Augmentation aug = AugmentationData.getInstance().generateAugmentationWithSkill(weapon, skill, level);
			weapon.setAugmentation(aug);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(weapon);
			player.sendPacket(iu);
			
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
			player.sendPacket(su);
			
			player.broadcastUserInfo();
			player.sendSkillList();
			player.sendMessage("You successfully augmented the weapon.");
			player.sendPacket(new ExShowScreenMessage("You successfully augmented the weapon.", 3000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_enchant_success"));
			return;
		}
	}
}