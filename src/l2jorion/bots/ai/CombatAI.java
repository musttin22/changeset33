package l2jorion.bots.ai;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import l2jorion.bots.FakePlayer;
import l2jorion.bots.model.BotSkill;
import l2jorion.bots.model.HealingSpell;
import l2jorion.bots.model.OffensiveSpell;
import l2jorion.bots.model.SupportSpell;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.enums.ShotType;
import l2jorion.game.geo.GeoData;
import l2jorion.game.managers.TownManager;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Skill;
import l2jorion.game.templates.L2Item;
import l2jorion.game.templates.L2Weapon;
import l2jorion.util.random.Rnd;

public abstract class CombatAI extends FakePlayerAI
{
	private int x, y, z;
	
	public CombatAI(FakePlayer character)
	{
		super(character);
	}
	
	protected void tryAttackingUsingMageOffensiveSkill()
	{
		if (_fakePlayer.getTarget() != null)
		{
			if (getOffensiveSpells() != null && !getOffensiveSpells().isEmpty())
			{
				BotSkill botSkill = getRandomAvailableMageSpellForTarget();
				if (botSkill == null)
				{
					return;
				}
				
				L2Skill skill = _fakePlayer.getSkill(botSkill.getSkillId());
				if (skill != null)
				{
					castSpell(skill);
				}
			}
		}
	}
	
	protected void tryAttackingUsingFighterOffensiveSkill()
	{
		if (_fakePlayer.getTarget() != null)
		{
			_fakePlayer.forceAutoAttack((L2Character) _fakePlayer.getTarget());
			
			if (Rnd.nextDouble() < changeOfUsingSkill())
			{
				if (getOffensiveSpells() != null && !getOffensiveSpells().isEmpty())
				{
					L2Skill skill = getRandomAvailableFighterSpellForTarget();
					
					if (skill != null)
					{
						castSpell(skill);
					}
				}
			}
		}
	}
	
	@Override
	public void thinkAndAct()
	{
		handleDeath();
		actionsInTown();
	}
	
	protected int getShotId()
	{
		L2Weapon currentWeapon = _fakePlayer.getActiveWeaponItem();
		int grade = currentWeapon.getCrystalType();
		
		if (grade == L2Item.CRYSTAL_NONE)
		{
			return getShotType() == ShotType.SOULSHOT ? 1835 : 3947;
		}
		
		if (grade == L2Item.CRYSTAL_D)
		{
			return getShotType() == ShotType.SOULSHOT ? 1463 : 3948;
		}
		
		if (grade == L2Item.CRYSTAL_C)
		{
			return getShotType() == ShotType.SOULSHOT ? 1464 : 3949;
		}
		
		if (grade == L2Item.CRYSTAL_B)
		{
			return getShotType() == ShotType.SOULSHOT ? 1465 : 3950;
		}
		
		if (grade == L2Item.CRYSTAL_A)
		{
			return getShotType() == ShotType.SOULSHOT ? 1466 : 3951;
		}
		
		if (grade == L2Item.CRYSTAL_S)
		{
			return getShotType() == ShotType.SOULSHOT ? 1467 : 3952;
		}
		
		return 0;
	}
	
	protected int getArrowId()
	{
		int playerLevel = _fakePlayer.getLevel();
		if (playerLevel < 20)
		{
			return 17; // wooden arrow
		}
		if (playerLevel >= 20 && playerLevel < 40)
		{
			return 1341; // bone arrow
		}
		if (playerLevel >= 40 && playerLevel < 52)
		{
			return 1342; // steel arrow
		}
		if (playerLevel >= 52 && playerLevel < 61)
		{
			return 1343; // Silver arrow
		}
		if (playerLevel >= 61 && playerLevel < 76)
		{
			return 1344; // Mithril Arrow
		}
		if (playerLevel >= 76)
		{
			return 1345; // shining
		}
		
		return 0;
	}
	
	protected void handleShots()
	{
		if (_fakePlayer.getInventory().getItemByItemId(getShotId()) != null)
		{
			if (_fakePlayer.getInventory().getItemByItemId(getShotId()).getCount() <= 20)
			{
				_fakePlayer.getInventory().addItem("", getShotId(), 1000, _fakePlayer, null);
			}
		}
		else
		{
			_fakePlayer.getInventory().addItem("", getShotId(), 1000, _fakePlayer, null);
		}
		
		// if (_fakePlayer.getAutoSoulShot().isEmpty())
		{
			_fakePlayer.addAutoSoulShot(getShotId());
			_fakePlayer.rechargeAutoSoulShot(true, true, false);
		}
	}
	
	protected boolean handleLevels()
	{
		// Newbie bot
		if (_fakePlayer.getBotMode() == 1)
		{
			if (_fakePlayer.isCastingNow() || _fakePlayer.isAttackingNow())
			{
				return true;
			}
			
			switch (_fakePlayer.getLevel())
			{
				case 1:
					if (_fakePlayer.getActionId() == 0)
					{
						_fakePlayer.rndWalk(_fakePlayer.getX(), _fakePlayer.getY(), _fakePlayer.getZ());
					}
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
					if (_fakePlayer.getActionId() == 0)
					{
						switch (_fakePlayer.getClassId())
						{
							case fighter:
							case mage:
								_fakePlayer.setMaxTargetRange(1000);
								break;
							case elvenFighter:
							case elvenMage:
								_fakePlayer.setMaxTargetRange(2000);
								break;
							case darkFighter:
							case darkMage:
								_fakePlayer.setMaxTargetRange(1000);
								break;
							case orcFighter:
							case orcMage:
								_fakePlayer.setMaxTargetRange(2000);
								break;
							case dwarvenFighter:
								_fakePlayer.setMaxTargetRange(2000);
								break;
						}
						
						if (100 > Rnd.get(1000))
						{
							_fakePlayer.setActionId(1);
						}
					}
					
					if (_fakePlayer.getActionId() == 1 && !_fakePlayer.isMoving())
					{
						_fakePlayer.getFakeAi().clientStopMoving(null);
						
						int rndX = Rnd.get(-150, 150);
						int rndY = Rnd.get(-150, 150);
						
						switch (_fakePlayer.getClassId())
						{
							case fighter:
								x = -72193 + rndX;
								y = 257377 + rndY;
								z = -3115;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
							case mage:
								x = -89657 + rndX;
								y = 248848 + rndY;
								z = -3576;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
							case elvenFighter:
							case elvenMage:
								x = 46009 + rndX;
								y = 42515 + rndY;
								z = -3404;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
							case darkFighter:
							case darkMage:
								x = 27535 + rndX;
								y = 11034 + rndY;
								z = -4104;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
							case orcFighter:
							case orcMage:
								x = -55285 + rndX;
								y = -113610 + rndY;
								z = -672;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
							case dwarvenFighter:
								x = 110307 + rndX;
								y = -173748 + rndY;
								z = -554;
								_fakePlayer.getFakeAi().moveTo(x, y, z);
								break;
						}
						_fakePlayer.setActionId(2);
						return true;
					}
					
					if (_fakePlayer.getActionId() == 2)
					{
						if (!_fakePlayer.isMoving() && _fakePlayer.getX() == x && _fakePlayer.getY() == y && 30 > Rnd.get(100))
						{
							_fakePlayer.getFakeAi().clientStopMoving(null);
							_fakePlayer.getFakeAi().doUnstuck(_fakePlayer);
							_fakePlayer.setActionId(3);
						}
						return true;
					}
				default:
				{
					// For all newbie bots in town
					if ((TownManager.getInstance().getTown(_fakePlayer.getX(), _fakePlayer.getY(), _fakePlayer.getZ()) != null))
					{
						if (!_fakePlayer.isInCombat() && 50 > Rnd.get(1000))
						{
							int rndLoc = Rnd.get(1, 3);
							_fakePlayer.setLocId(rndLoc);
							switch (_fakePlayer.getClassId())
							{
								case fighter:
								case mage:
									switch (rndLoc)
									{
										case 1:
											_fakePlayer.teleToLocation(48726, 248458, -6160, true);
											break;
										case 2:
											_fakePlayer.teleToLocation(-101348, 212601, -3088, true);
											break;
										case 3:
											_fakePlayer.teleToLocation(-99684, 237536, -3567, true);
											break;
									}
									break;
								case elvenFighter:
								case elvenMage:
									switch (rndLoc)
									{
										case 1:
											_fakePlayer.teleToLocation(29107, 74957, -3779, true); // Elven Fortress
											break;
										case 2:
											_fakePlayer.teleToLocation(-10881, 75800, -3596, true); // Neutral zone
											break;
										case 3:
											_fakePlayer.teleToLocation(21400, 51199, -3688, true); // Elven Forest
											break;
									}
									break;
								case darkFighter:
								case darkMage:
									switch (rndLoc)
									{
										case 1:
											_fakePlayer.teleToLocation(-30786, 49794, -3547, true); // Swampland
											break;
										case 2:
											_fakePlayer.teleToLocation(-22160, 14139, -3240, true); // Dark Forest
											break;
										case 3:
											_fakePlayer.teleToLocation(-56495, 78371, -2960, true); // Spider Nest
											break;
									}
									break;
								case orcFighter:
								case orcMage:
									switch (rndLoc)
									{
										case 1:
											_fakePlayer.teleToLocation(9581, -112524, -2530, true); // Cave
											break;
										case 2:
											_fakePlayer.teleToLocation(7631, -138919, -938, true); // Frozen Waterfall
											break;
										case 3:
											_fakePlayer.teleToLocation(-17912, -90934, -2536, true); // The Immortal Plateau
											break;
									}
									break;
								case dwarvenFighter:
									switch (rndLoc)
									{
										case 1:
											_fakePlayer.teleToLocation(172061, -173432, 3450, true); // Mithril Mines
											break;
										case 2:
											_fakePlayer.teleToLocation(139740, -177613, -1543, true); // Coal Mines
											break;
										case 3:
											_fakePlayer.teleToLocation(169040, -208251, -3504, true); // Eastern Mining Zone
											break;
									}
									break;
							}
							_fakePlayer.setActionId(4);
							_fakePlayer.setTargetClass(L2Character.class);
							_fakePlayer.setTargetRange(300);
							_fakePlayer.setMaxTargetRange(3500);
						}
						return true;
					}
					
					// Further actions (ID: 4 - 7)
					if (_fakePlayer.getActionId() == 4)
					{
						int rndX = Rnd.get(-150, 150);
						int rndY = Rnd.get(-150, 150);
						
						switch (_fakePlayer.getClassId())
						{
							case elvenFighter:
							case elvenMage:
								if (_fakePlayer.getLocId() == 1)
								{
									x = 25272 + rndX;
									y = 74965 + rndY;
									z = -4096;
									_fakePlayer.getFakeAi().moveTo(x, y, z);
									_fakePlayer.setActionId(7);
								}
								else if (_fakePlayer.getLocId() == 3)
								{
									switch (Rnd.get(1, 2))
									{
										case 1:
											x = 22949 + rndX;
											y = 45615 + rndY;
											z = -3664;
											_fakePlayer.getFakeAi().moveTo(x, y, z);
											_fakePlayer.setActionId(7);
											break;
										case 2:
											x = 17808 + rndX;
											y = 52719 + rndY;
											z = -3616;
											_fakePlayer.getFakeAi().moveTo(x, y, z);
											_fakePlayer.setActionId(7);
											break;
									}
								}
								else
								{
									_fakePlayer.setActionId(7); // last
								}
								break;
							case orcFighter:
							case orcMage:
								if (_fakePlayer.getLocId() == 1)
								{
									x = 12202 + rndX;
									y = -112451 + rndY;
									z = -2976;
									_fakePlayer.getFakeAi().moveTo(x, y, z);
									_fakePlayer.setActionId(5);
								}
								else
								{
									_fakePlayer.setActionId(7); // last
								}
								break;
							case dwarvenFighter:
								if (_fakePlayer.getLocId() == 2)
								{
									x = 139655 + rndX;
									y = -175037 + rndY;
									z = -1704;
									_fakePlayer.getFakeAi().moveTo(x, y, z);
									_fakePlayer.setActionId(5);
								}
								else
								{
									_fakePlayer.setActionId(7); // last
								}
								break;
							
							default:
								_fakePlayer.setActionId(5);
								break;
							
						}
						return true;
					}
					
					if (_fakePlayer.getActionId() == 5)
					{
						int rndX = Rnd.get(-150, 150);
						int rndY = Rnd.get(-150, 150);
						
						switch (_fakePlayer.getClassId())
						{
							case orcFighter:
							case orcMage:
								if (_fakePlayer.getLocId() == 1)
								{
									if (_fakePlayer.getX() == x && _fakePlayer.getY() == y)
									{
										x = 15844 + rndX;
										y = -112460 + rndY;
										z = -3136;
										_fakePlayer.getFakeAi().moveTo(x, y, z);
										_fakePlayer.setActionId(6);
									}
								}
								break;
							case dwarvenFighter:
								if (_fakePlayer.getLocId() == 2)
								{
									if (_fakePlayer.getX() == x && _fakePlayer.getY() == y)
									{
										x = 137385 + rndX;
										y = -173717 + rndY;
										z = -1781;
										_fakePlayer.getFakeAi().moveTo(x, y, z);
										_fakePlayer.setActionId(6);
									}
								}
								break;
							default:
								_fakePlayer.setActionId(6);
								break;
						}
						return true;
					}
					
					if (_fakePlayer.getActionId() == 6)
					{
						switch (_fakePlayer.getClassId())
						{
							case orcFighter:
							case orcMage:
							case dwarvenFighter:
								if (_fakePlayer.getX() == x && _fakePlayer.getY() == y)
								{
									_fakePlayer.setActionId(7);
								}
								break;
							default:
								_fakePlayer.setActionId(7);
								break;
						}
						return true;
					}
				}
			}
		}
		return false;
		
	}
	
	public HealingSpell getRandomAvaiableHealingSpellForTarget()
	{
		if (getHealingSpells().isEmpty())
		{
			return null;
		}
		
		List<HealingSpell> spellsOrdered = getHealingSpells().stream().sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		
		BotSkill skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);
		
		return (HealingSpell) skill;
	}
	
	protected BotSkill getRandomAvailableMageSpellForTarget()
	{
		List<OffensiveSpell> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		
		BotSkill skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);
		
		return skill;
	}
	
	private BotSkill waitAndPickAvailablePrioritisedSpell(List<? extends BotSkill> spellsOrdered, int skillListSize)
	{
		int skillIndex = 0;
		BotSkill botSkill = spellsOrdered.get(skillIndex);
		
		L2Skill skill = _fakePlayer.getSkill(botSkill.getSkillId());
		
		if (skill.getCastRange() > 0)
		{
			if (!GeoData.getInstance().canSeeTarget(_fakePlayer, _fakePlayer.getTarget()))
			{
				moveToPawn(_fakePlayer.getTarget(), 100);// skill.getCastRange()
				return null;
			}
		}
		
		while (!_fakePlayer.checkUseMagicConditions(skill, true, false))
		{
			_isBusyThinking = true;
			if (_fakePlayer.isDead() || _fakePlayer.isOutOfControl())
			{
				return null;
			}
			
			if ((skillIndex < 0) || (skillIndex >= skillListSize))
			{
				return null;
			}
			
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			
			botSkill = spellsOrdered.get(skillIndex);
			skillIndex++;
		}
		return botSkill;
	}
	
	protected L2Skill getRandomAvailableFighterSpellForTarget()
	{
		List<BotSkill> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillIndex = 0;
		int skillListSize = spellsOrdered.size();
		
		L2Skill skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
		
		while (!_fakePlayer.checkUseMagicConditions(skill, true, false))
		{
			_isBusyThinking = true;
			if (_fakePlayer.isDead() || _fakePlayer.isOutOfControl())
			{
				return null;
			}
			
			if ((skillIndex < 0) || (skillIndex >= skillListSize))
			{
				return null;
			}
			
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			skillIndex++;
		}
		
		if (!_fakePlayer.checkUseMagicConditions(skill, true, false))
		{
			_fakePlayer.forceAutoAttack((L2Character) _fakePlayer.getTarget());
			return null;
		}
		
		return skill;
	}
	
	protected void selfSupportBuffs()
	{
		List<Integer> activeEffects = Arrays.stream(_fakePlayer.getAllEffects()).map(x -> x.getSkill().getId()).collect(Collectors.toList());
		
		for (SupportSpell selfBuff : getSelfSupportSpells())
		{
			if (activeEffects.contains(selfBuff.getSkillId()) && selfBuff.getSkillId() != 8 && selfBuff.getSkillId() != 50) // Let's allow to use force skills again
			{
				continue;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(selfBuff.getSkillId(), _fakePlayer.getSkillLevel(selfBuff.getSkillId()));
			
			if (!_fakePlayer.checkUseMagicConditions(skill, true, false))
			{
				continue;
			}
			
			switch (selfBuff.getCondition())
			{
				case LESSHPPERCENT:
					if (Math.round(100.0 / _fakePlayer.getMaxHp() * _fakePlayer.getCurrentHp()) <= selfBuff.getConditionValue())
					{
						castSelfSpell(skill);
					}
					break;
				case MISSINGCP:
					if (getMissingHealth() >= selfBuff.getConditionValue())
					{
						castSelfSpell(skill);
					}
					break;
				case SELFBUFFS:
					if (selfBuff.getConditionValue() > Rnd.get(100)) // if condition SELFBUFFS - it means a chance for activation of self buff for walkers in town
					{
						castSelfSpell(skill);
					}
					break;
				case NONE:
					castSelfSpell(skill);
					break;
				default:
					break;
			}
		}
	}
	
	private double getMissingHealth()
	{
		return _fakePlayer.getMaxCp() - _fakePlayer.getCurrentCp();
	}
	
	protected double changeOfUsingSkill()
	{
		return 1.0;
	}
	
	protected abstract ShotType getShotType();
	
	protected abstract List<OffensiveSpell> getOffensiveSpells();
	
	protected abstract List<HealingSpell> getHealingSpells();
	
	protected abstract List<SupportSpell> getSelfSupportSpells();
}
