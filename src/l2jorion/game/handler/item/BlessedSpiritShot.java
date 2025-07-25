/*
 * L2jOrion Project - www.l2jorion.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.handler.item;

import l2jorion.Config;
import l2jorion.game.handler.IItemHandler;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2PlayableInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ExAutoSoulShot;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.L2Weapon;

public class BlessedSpiritShot implements IItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS =
	{
		3947,
		3948,
		3949,
		3950,
		3951,
		3952
	};
	private static final int[] SKILL_IDS =
	{
		2061,
		2160,
		2161,
		2162,
		2163,
		2164
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		
		final int itemId = item.getItemId();
		
		if (activeChar.isInOlympiadMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			sm.addString(item.getItemName());
			activeChar.sendPacket(sm);
			return;
		}
		
		// Check if Blessed Spiritshot can be used
		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
			}
			
			return;
		}
		
		// Check if Blessed Spiritshot is already active (it can be charged over Spiritshot)
		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
		{
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (weaponGrade == L2Item.CRYSTAL_NONE && (itemId != 3947 && itemId != 10011) || weaponGrade == L2Item.CRYSTAL_D && (itemId != 3948 && itemId != 10005) || weaponGrade == L2Item.CRYSTAL_C && (itemId != 3949 && itemId != 10006)
			|| weaponGrade == L2Item.CRYSTAL_B && (itemId != 3950 && itemId != 10007) || weaponGrade == L2Item.CRYSTAL_A && (itemId != 3951 && itemId != 10008) || weaponGrade == L2Item.CRYSTAL_S && (itemId != 3952 && itemId != 10009))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
			}
			return;
		}
		
		// Consume Blessed Spiritshot if player has enough of them
		if (!activeChar.isBot())
		{
			if ((!Config.DONT_DESTROY_SS) && (itemId != 10005 && itemId != 10006 && itemId != 10007 && itemId != 10008 && itemId != 10009 && itemId != 10011))
			{
				if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
				{
					if (activeChar.getAutoSoulShot().contains(itemId))
					{
						activeChar.removeAutoSoulShot(itemId);
						activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
						SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
						sm.addString(item.getItem().getName());
						activeChar.sendPacket(sm);
						sm = null;
						
						return;
					}
					
					activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
					return;
				}
			}
		}
		
		// Charge Blessed Spiritshot
		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
		
		// Send message to client
		activeChar.sendPacket(new SystemMessage(SystemMessageId.ENABLED_SPIRITSHOT));
		if (!activeChar.isHidingEffects())
		{
			activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0), 500);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
