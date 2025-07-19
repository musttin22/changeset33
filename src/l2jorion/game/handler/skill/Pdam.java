/*
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
package l2jorion.game.handler.skill;

import java.util.ArrayList;
import java.util.List;

import l2jorion.Config;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.handler.ISkillHandler;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Effect;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.L2Skill.SkillType;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2RaidBossInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.EtcStatusUpdate;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.skills.BaseStats;
import l2jorion.game.skills.Formulas;
import l2jorion.game.skills.effects.EffectCharge;
import l2jorion.game.templates.L2WeaponType;
import l2jorion.log.Log;

public class Pdam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.PDAM,
		SkillType.FATALCOUNTER
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		int damage = 0;
		
		// Calculate targets based on vegeance
		List<L2Object> target_s = new ArrayList<>();
		for (L2Object _target : targets)
		{
			target_s.add(_target);
			L2Character target = (L2Character) _target;
			
			if (target.vengeanceSkill(skill))
			{
				target_s.add(activeChar);
			}
		}
		
		boolean bss = activeChar.checkBss();
		boolean sps = activeChar.checkSps();
		boolean ss = activeChar.checkSs();
		
		for (L2Object target2 : target_s)
		{
			L2Character target = (L2Character) target2;
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}
			
			// Calculate skill evasion
			if (Formulas.calcPhysicalSkillEvasion(target, skill))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
				continue;
			}
			
			boolean dual = activeChar.isUsingDualWeapon();
			byte shld = Formulas.calcShldUse(activeChar, target);
			
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar));
			}
			
			boolean soul = false;
			if (weapon != null)
			{
				soul = (ss && weapon.getItemType() != L2WeaponType.DAGGER);
			}
			
			if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				damage = 0;
			}
			else
			{
				damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
			}
			
			if (crit)
			{
				damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
			}
			
			if (damage > 5000 && Config.LOG_HIGH_DAMAGES && activeChar instanceof L2PcInstance)
			{
				String name = "";
				if (target instanceof L2RaidBossInstance)
				{
					name = "RaidBoss ";
				}
				if (target instanceof L2NpcInstance)
				{
					name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
				}
				if (target instanceof L2PcInstance)
				{
					name = target.getName() + "(" + target.getObjectId() + ") ";
				}
				name += target.getLevel() + " lvl";
				Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
			}
			
			if (damage > 0)
			{
				activeChar.sendDamageMessage(target, damage, false, crit, false);
				
				// Possibility of a lethal strike
				Formulas.calcLethalHit(activeChar, target, skill);
				
				target.reduceCurrentHp(damage, activeChar);
				
				if (!target.isInvul())
				{
					if (skill.hasEffects())
					{
						if (target.reflectSkill(skill))
						{
							activeChar.stopSkillEffects(skill.getId());
							skill.getEffects(target, activeChar, ss, sps, bss);
						}
						else
						{
							// Like L2OFF must remove the first effect if the second effect lands
							target.stopSkillEffects(skill.getId());
							skill.getEffects(activeChar, target, ss, sps, bss);
						}
					}
				}
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
			
			if (skill.getId() == 345 || skill.getId() == 346) // Sonic Rage or Raging Force
			{
				EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
				if (effect != null)
				{
					int effectcharge = effect.getLevel();
					if (effectcharge < 7)
					{
						effectcharge++;
						effect.addNumCharges(1);
						
						activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
						SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
						sm.addNumber(effectcharge);
						activeChar.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED);
						activeChar.sendPacket(sm);
					}
				}
				else
				{
					if (skill.getId() == 345) // Sonic Rage
					{
						L2Skill dummy = SkillTable.getInstance().getInfo(8, 7); // Lv7 Sonic Focus
						dummy.getEffects(activeChar, activeChar, ss, sps, bss);
						dummy = null;
					}
					else if (skill.getId() == 346) // Raging Force
					{
						L2Skill dummy = SkillTable.getInstance().getInfo(50, 7); // Lv7 Focused Force
						dummy.getEffects(activeChar, activeChar, ss, sps, bss);
					}
				}
			}
			
			if (skill.hasSelfEffects())
			{
				L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if (effect != null && effect.isSelfEffect())
				{
					// Replace old effect with new one.
					effect.exit(false);
				}
				skill.getEffectsSelf(activeChar);
			}
		}
		
		if (skill.isMagic())
		{
			if (bss)
			{
				activeChar.removeBss();
			}
			else if (sps)
			{
				activeChar.removeSps();
			}
		}
		else
		{
			activeChar.removeSs();
		}
		
		if (skill.isSuicideAttack())
		{
			activeChar.reduceCurrentHp(activeChar.getMaxHp() + activeChar.getMaxCp() + 1, activeChar);
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}