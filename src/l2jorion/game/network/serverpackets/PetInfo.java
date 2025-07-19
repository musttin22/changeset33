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
package l2jorion.game.network.serverpackets;

import l2jorion.game.model.L2Summon;
import l2jorion.game.model.actor.instance.L2PetInstance;
import l2jorion.game.model.actor.instance.L2SummonInstance;
import l2jorion.game.network.PacketServer;

public class PetInfo extends PacketServer
{
	private static final String _S__CA_PETINFO = "[S] b1 PetInfo";
	
	private L2Summon _summon;
	private int _maxFed, _curFed;
	
	public PetInfo(L2Summon summon)
	{
		_summon = summon;
		
		if (_summon instanceof L2PetInstance)
		{
			final L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (_summon instanceof L2SummonInstance)
		{
			final L2SummonInstance sum = (L2SummonInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb1);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getTemplateId() + 1000000);
		
		writeD(0); // 1=attackable
		
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeD(_summon.getHeading());
		
		writeD(0);
		
		writeD(_summon.getMAtkSpd());
		writeD(_summon.getPAtkSpd());
		
		final int runSpd = _summon.getRunSpeed();
		final int walkSpd = _summon.getWalkSpeed();
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		
		writeF(_summon.getMovementSpeedMultiplier());
		writeF(_summon.getAttackSpeedMultiplier());
		writeF(_summon.getTemplate().getCollisionRadius());
		writeF(_summon.getTemplate().getCollisionHeight());
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // chest
		writeD(0); // left hand weapon
		writeC((_summon.getOwner() != null) ? 1 : 0); // name above char 1=true
		writeC(_summon.isRunning() ? 1 : 0); // running=1
		writeC(_summon.isInCombat() ? 1 : 0); // attacking 1=true
		writeC(_summon.isAlikeDead() ? 1 : 0); // dead 1=true
		writeC(_summon.isShowSummonAnimation() ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(_summon.getName());
		writeS(_summon.getTitle());
		writeD(1);
		writeD(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() : 0); // 0 = white,2= purpleblink, if its greater then karma = purple
		writeD(_summon.getOwner() != null ? _summon.getOwner().getKarma() : 0); // karma
		writeD(_curFed); // how fed it is
		writeD(_maxFed); // max fed it can be
		writeD((int) _summon.getCurrentHp());// current hp
		writeD(_summon.getMaxHp());// max hp
		writeD((int) _summon.getCurrentMp());// current mp
		writeD(_summon.getMaxMp());// max mp
		writeD(_summon.getStat().getSp()); // sp
		writeD(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());// 0% absolute value
		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		writeD(_summon instanceof L2PetInstance ? _summon.getInventory().getTotalWeight() : 0);// weight
		writeD(_summon.getMaxLoad());// max weight it can carry
		writeD(_summon.getPAtk(null));// patk
		writeD(_summon.getPDef(null));// pdef
		writeD(_summon.getMAtk(null, null));// matk
		writeD(_summon.getMDef(null, null));// mdef
		writeD(_summon.getAccuracy());// accuracy
		writeD(_summon.getEvasionRate(null));// evasion
		writeD(_summon.getCriticalHit(null, null));// critical
		writeD(_summon.getRunSpeed());// speed
		writeD(_summon.getPAtkSpd());// atkspeed
		writeD(_summon.getMAtkSpd());// casting speed
		
		writeD(_summon.getAbnormalEffect());// c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		writeH((_summon.isMountable()) ? 1 : 0);
		
		writeC(0); // c2
		
		// Following all added in C4.
		writeH(0); // ??
		writeC(0); // team aura (1 = blue, 2 = red)
		writeD(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeD(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
	}
	
	@Override
	public String getType()
	{
		return _S__CA_PETINFO;
	}
	
}
