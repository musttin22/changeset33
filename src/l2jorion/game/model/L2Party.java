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
package l2jorion.game.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import javolution.util.FastList;
import l2jorion.Config;
import l2jorion.game.controllers.GameTimeController;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.managers.DuelManager;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2PetInstance;
import l2jorion.game.model.actor.instance.L2PlayableInstance;
import l2jorion.game.model.actor.instance.L2SummonInstance;
import l2jorion.game.model.entity.DimensionalRift;
import l2jorion.game.model.entity.sevensigns.SevenSignsFestival;
import l2jorion.game.network.PacketServer;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.network.serverpackets.ExCloseMPCC;
import l2jorion.game.network.serverpackets.ExOpenMPCC;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.PartyMemberPosition;
import l2jorion.game.network.serverpackets.PartySmallWindowAdd;
import l2jorion.game.network.serverpackets.PartySmallWindowAll;
import l2jorion.game.network.serverpackets.PartySmallWindowDelete;
import l2jorion.game.network.serverpackets.PartySmallWindowDeleteAll;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.skills.Stats;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.game.util.Util;
import l2jorion.util.random.Rnd;

public class L2Party
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	private static final int PARTY_POSITION_BROADCAST = 10000;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	private final List<L2PcInstance> _members = new CopyOnWriteArrayList<>();
	
	private boolean _pendingInvitation = false;
	private long _pendingInviteTimeout;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	
	private L2CommandChannel _commandChannel = null;
	private DimensionalRift _dr;
	
	private Future<?> _positionBroadcastTask = null;
	protected PartyMemberPosition _positionPacket;
	
	public L2Party(L2PcInstance leader, int itemDistribution)
	{
		_members.add(leader);
		_itemDistribution = itemDistribution;
		_partyLvl = leader.getLevel();
	}
	
	public int getMemberCount()
	{
		return getPartyMembers().size();
	}
	
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}
	
	public void setPendingInvitation(boolean val)
	{
		_pendingInvitation = val;
		_pendingInviteTimeout = GameTimeController.getInstance().getGameTicks() + L2PcInstance.REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
	}
	
	public boolean isInvitationRequestExpired()
	{
		return !(_pendingInviteTimeout > GameTimeController.getInstance().getGameTicks());
	}
	
	public List<L2PcInstance> getPartyMembers()
	{
		return _members;
	}
	
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new FastList<>();
		
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				availableMembers.add(member);
			}
		}
		
		if (!availableMembers.isEmpty())
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		
		return null;
	}
	
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++_itemLastLoot >= getMemberCount())
			{
				_itemLastLoot = 0;
			}
			
			L2PcInstance member;
			try
			{
				member = getPartyMembers().get(_itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				{
					return member;
				}
			}
			catch (Exception e)
			{
				// continue, take another member if this just logged off
			}
		}
		
		return null;
	}
	
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;
		
		switch (_itemDistribution)
		{
			case ITEM_RANDOM:
				if (!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if (!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}
		
		if (looter == null)
		{
			looter = player;
		}
		
		return looter;
	}
	
	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player)
	{
		return getLeader().equals(player);
	}
	
	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 * @return int
	 */
	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}
	
	/**
	 * Broadcasts packet to every party member
	 * @param msg
	 */
	public void broadcastToPartyMembers(PacketServer msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void broadcastToPartyMembers(String msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendMessage(msg);
			}
		}
	}
	
	public void broadcastToPartyMembersNewLeader()
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}
	
	public void broadcastCSToPartyMembers(CreatureSay msg, L2PcInstance broadcaster)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !BlockList.isBlocked(member, broadcaster))
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void broadcastToPartyMembers(L2PcInstance player, PacketServer msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if ((member != null) && (member.getObjectId() != player.getObjectId()))
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void addPartyMember(L2PcInstance player)
	{
		if (getPartyMembers().contains(player))
		{
			return;
		}
		
		if (Config.ALPHA_CUSTOM)
		{
			List<Integer> healersList1 = new ArrayList<>(Arrays.asList(16, 97)); // Bishop, Cardinal
			int healersList1Count = 0;
			
			List<Integer> healersList2 = new ArrayList<>(Arrays.asList(30, 105)); // Elven Elder, Eva Saint
			int healersList2Count = 0;
			
			for (L2PcInstance member : getPartyMembers())
			{
				if (healersList1.contains(member.getClassId().getId()))
				{
					healersList1Count++;
				}
				
				if (healersList2.contains(member.getClassId().getId()))
				{
					healersList2Count++;
				}
				
				if (healersList1.contains(player.getClassId().getId()) || healersList2.contains(player.getClassId().getId()))
				{
					if (healersList1Count == 2 || healersList2Count == 2 || healersList1Count == 1 && healersList2Count == 1)
					{
						String msg = "The party has been reached maximum number of healers (limit: 2).";
						player.sendMessage(msg);
						broadcastToPartyMembers(msg);
						
						ExShowScreenMessage screenMsg = new ExShowScreenMessage(msg, 2000, 2, false);
						player.sendPacket(screenMsg);
						broadcastToPartyMembers(screenMsg);
						
						PlaySound errorSound = new PlaySound("ItemSound3.sys_impossible");
						player.sendPacket(errorSound);
						broadcastToPartyMembers(errorSound);
						return;
					}
				}
			}
		}
		
		// sends new member party window for all members
		player.sendPacket(new PartySmallWindowAll(player, this));
		
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessageId.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player, this));
		
		// add player to party, adjust party level
		getPartyMembers().add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}
		
		// update partySpelled
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				if (member.isSellingBuff())
				{
					member.leaveParty();
				}
				
				member.updateEffectIcons(true); // update party icons only
				member.broadcastUserInfo();
			}
		}
		
		if (isInDimensionalRift())
		{
			_dr.partyMemberInvited();
		}
		
		// open the CCInformationwindow
		if (isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}
		
		// activate position task
		if (_positionBroadcastTask == null)
		{
			_positionBroadcastTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2, PARTY_POSITION_BROADCAST);
		}
		
	}
	
	public void removePartyMember(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null)
		{
			removePartyMember(player);
		}
	}
	
	public void removePartyMember(L2PcInstance player)
	{
		removePartyMember(player, true);
	}
	
	public synchronized void removePartyMember(L2PcInstance player, boolean sendMessage)
	{
		if (getPartyMembers().contains(player))
		{
			boolean isLeader = isLeader(player);
			getPartyMembers().remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			}
			
			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}
			
			if (sendMessage)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_LEFT_PARTY));
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_LEFT_PARTY);
				msg.addString(player.getName());
				broadcastToPartyMembers(msg);
			}
			
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (isInDimensionalRift())
			{
				_dr.partyMemberExited(player);
			}
			
			// Close the CCInfoWindow
			if (isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}
			
			if (isLeader && getPartyMembers().size() > 1)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
				msg.addString(getLeader().getName());
				broadcastToPartyMembers(msg);
				broadcastToPartyMembersNewLeader();
			}
			
			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if (getCommandChannel().getChannelLeader().equals(getLeader()))
					{
						getCommandChannel().disbandChannel();
					}
					else
					{
						getCommandChannel().removeParty(this);
					}
				}
				
				L2PcInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(leader);
					}
				}
				
				if (_positionBroadcastTask != null)
				{
					_positionBroadcastTask.cancel(false);
					_positionBroadcastTask = null;
				}
				
				_members.clear();
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null && !player.isInDuel())
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					// Swap party members
					L2PcInstance temp;
					int p1 = getPartyMembers().indexOf(player);
					temp = getLeader();
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, temp);
					
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembersNewLeader();
					
					if (isInCommandChannel() && temp.equals(_commandChannel.getChannelLeader()))
					{
						_commandChannel.setChannelLeader(getLeader());
						msg = new SystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1);
						msg.addString(_commandChannel.getChannelLeader().getName());
						_commandChannel.broadcastToChannelMembers(msg);
					}
					
					if (player.isInPartyMatchRoom())
					{
						PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
	}
	
	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equalsIgnoreCase(name))
			{
				return member;
			}
		}
		return null;
	}
	
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		if (item.getItemId() == 6393)
		{
			distributeMedals(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
		}
		else
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, msg);
		}
	}
	
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null)
		{
			return;
		}
		
		if (Config.L2JMOD_CHAMPION_REWARD_SPOIL)
		{
			if (item.getItemId() == 6393)
			{
				distributeMedals(player, item.getCount(), target);
				return;
			}
		}
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		
		looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), item.getEnchantLevel(), player, true);
		
		L2ItemInstance itemData = null;
		
		if (ItemTable.getInstance().getTemplate(item.getItemId()) != null)
		{
			itemData = ItemTable.getInstance().createDummyItem(item.getItemId());
		}
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2_S3) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
			msg.addString(looter.getName());
			
			if (itemData == null)
			{
				msg.addItemName(item.getItemId());
			}
			else
			{
				msg.addString(item.getEnchantLevel() > 0 ? "a +" + item.getEnchantLevel() + " " + itemData.getItemName() : itemData.getItemName()); // fix for enchanted items
			}
			
			broadcastToPartyMembers(looter, msg);
		}
	}
	
	/**
	 * distribute adena to party members
	 * @param player
	 * @param adena
	 * @param target
	 */
	public void distributeAdena(L2PcInstance player, int adena, L2Character target)
	{
		// Get all the party members
		List<L2PcInstance> membersList = getPartyMembers();
		
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> ToReward = FastList.newInstance();
		
		for (L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			ToReward.add(member);
		}
		
		// Avoid null exceptions, if any
		if (ToReward.isEmpty())
		{
			return;
		}
		
		// Now we can actually distribute the adena reward
		// (Total adena split by the number of party members that are in range and must be rewarded)
		int count = adena / ToReward.size();
		
		for (L2PcInstance member : ToReward)
		{
			member.addAdena("Party", count, player, true);
		}
		FastList.recycle((FastList<?>) ToReward);
	}
	
	public void distributeMedals(L2PcInstance player, int adena, L2Character target)
	{
		// Get all the party members
		List<L2PcInstance> membersList = getPartyMembers();
		
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> ToReward = FastList.newInstance();
		
		for (L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			ToReward.add(member);
		}
		
		// Avoid null exceptions, if any
		if (ToReward.isEmpty())
		{
			return;
		}
		
		// Now we can actually distribute the adena reward
		// (Total adena split by the number of party members that are in range and must be rewarded)
		int count = adena / ToReward.size();
		
		for (L2PcInstance member : ToReward)
		{
			member.addMedal("Party", count, player, true);
		}
		FastList.recycle((FastList<?>) ToReward);
	}
	
	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary)</li>
	 * <li>Calculate the Experience and SP reward distribution rate</li>
	 * <li>Add Experience and SP to the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR>
	 * <BR>
	 * Exception are L2PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain<BR>
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 * @param topLvl
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<L2PlayableInstance> rewardedMembers, int topLvl)
	{
		L2SummonInstance summon = null;
		List<L2PlayableInstance> validMembers = getValidMembers(rewardedMembers, topLvl);
		
		float penalty;
		double sqLevel;
		double preCalculation;
		
		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());
		
		double sqLevelSum = 0;
		
		for (L2PlayableInstance character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}
		
		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (L2Character member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}
				
				penalty = 0;
				
				// The L2SummonInstance penalty
				if (member.getPet() instanceof L2SummonInstance)
				{
					summon = (L2SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}
				
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetData().getOwnerExpTaken() > 0)
					{
						continue;
					}
					penalty = (float) 0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
					
					double exp = (xpReward * preCalculation);
					double sp = (spReward * preCalculation);
					
					if (member.isPlayer() && ((L2PcInstance) member).getPremiumService() >= 1)
					{
						exp *= Config.PREMIUM_XPSP_RATE;
						sp *= Config.PREMIUM_XPSP_RATE;
					}
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, exp, null, null)), (int) member.calcStat(Stats.EXPSP_RATE, sp, null, null));
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
	}
	
	/**
	 * Calculates and gives final XP and SP rewards to the party member.<BR>
	 * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.<BR>
	 * <BR>
	 * @param member is the L2Character to be rewarded
	 * @param xpReward is the total amount of XP to be "splited" and given to the member
	 * @param spReward is the total amount of SP to be "splited" and given to the member
	 * @param penalty is the penalty that must be applied to the XP rewards of the requested member
	 */
	
	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}
			
			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		
		_partyLvl = newLevel;
	}
	
	private List<L2PlayableInstance> getValidMembers(List<L2PlayableInstance> members, int topLvl)
	{
		List<L2PlayableInstance> validMembers = new FastList<>();
		
		// Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (L2PlayableInstance member : members)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
			}
		}
		
		// Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			
			for (L2PlayableInstance member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			for (L2PlayableInstance member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
				{
					validMembers.add(member);
				}
			}
		}
		// Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			
			for (L2PlayableInstance member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			int i = members.size() - 1;
			
			if (i < 1)
			{
				return members;
			}
			
			if (i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}
			
			for (L2PlayableInstance member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
				{
					validMembers.add(member);
				}
			}
		}
		return validMembers;
	}
	
	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		
		if (i < 1)
		{
			return 1;
		}
		
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}
		
		return BONUS_EXP_SP[i];
	}
	
	private double getExpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			return getBaseExpSpBonus(membersCount);
		}
		
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private double getSpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel()
	{
		return _partyLvl;
	}
	
	public int getLootDistribution()
	{
		return _itemDistribution;
	}
	
	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}
	
	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}
	
	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return _dr != null;
	}
	
	public void setDimensionalRift(DimensionalRift dr)
	{
		_dr = dr;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return _dr;
	}
	
	public L2PcInstance getLeader()
	{
		return _members.get(0);
	}
	
	protected class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (_positionPacket == null)
			{
				_positionPacket = new PartyMemberPosition(L2Party.this);
			}
			else
			{
				_positionPacket.reuse(L2Party.this);
			}
			
			broadcastToPartyMembers(L2Party.this._positionPacket);
		}
	}
}
