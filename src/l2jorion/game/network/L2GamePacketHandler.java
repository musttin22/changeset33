/* This program is free software; you can redistribute it and/or modify
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
package l2jorion.game.network;

import java.nio.ByteBuffer;

import l2jorion.Config;
import l2jorion.game.network.L2GameClient.GameClientState;
import l2jorion.game.network.clientpackets.*;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.mmocore.ClientFactory;
import l2jorion.mmocore.MMOExecutor;
import l2jorion.mmocore.PacketHandler;
import l2jorion.mmocore.MMOConnection;
import l2jorion.mmocore.ReceivablePacket;
import l2jorion.util.Util;

public final class L2GamePacketHandler implements PacketHandler<L2GameClient>, ClientFactory<L2GameClient>, MMOExecutor<L2GameClient>
{
	protected static final Logger LOG = LoggerFactory.getLogger(L2GamePacketHandler.class);
	
	@Override
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buff, L2GameClient client)
	{
		if (client.dropPacket())
		{
			return null;
		}
		
		int opcode = buff.get() & 0xFF;
		
		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0x00:
						msg = new ProtocolVersion();
						break;
					case 0x08:
						msg = new AuthLogin();
						break;
					default:
						printDebug(opcode, buff, state, client);
						break;
				}
				break;
			case AUTHED:
				switch (opcode)
				{
					case 0x09:
						msg = new Logout();
						break;
					case 0x0b:
						msg = new CharacterCreate();
						break;
					case 0x0c:
						msg = new CharacterDelete();
						break;
					case 0x0d:
						msg = new CharacterSelected();
						break;
					case 0x0e:
						msg = new NewCharacter();
						break;
					case 0x62:
						msg = new CharacterRestore();
						break;
					case 0x68:
						msg = new RequestPledgeCrest();
						break;
					default:
						printDebug(opcode, buff, state, client);
						break;
				}
				break;
			case ENTERING:
			{
				switch (opcode)
				{
					case 0x03:
						msg = new EnterWorld();
						break;
					case 0xd0:
						int id2 = -1;
						if (buff.remaining() >= 2)
						{
							id2 = buff.getShort() & 0xffff;
						}
						else
						{
							LOG.warn("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
							break;
						}
						
						switch (id2)
						{
							case 8:
								msg = new RequestManorList();
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buff, state, client);
								break;
						}
						break;
					default:
						printDebug(opcode, buff, state, client);
						break;
				}
				break;
			}
			case IN_GAME:
			{
				switch (opcode)
				{
					case 0x01:
						msg = new MoveBackwardToLocation();
						break;
					case 0x04:
						msg = new Action();
						break;
					case 0x09:
						msg = new Logout();
						break;
					case 0x0a:
						msg = new AttackRequest();
						break;
					case 0x0f:
						msg = new RequestItemList();
						break;
					case 0x11:
						msg = new RequestUnEquipItem();
						break;
					case 0x12:
						msg = new RequestDropItem();
						break;
					case 0x14:
						msg = new UseItem();
						break;
					case 0x15:
						msg = new TradeRequest();
						break;
					case 0x16:
						msg = new AddTradeItem();
						break;
					case 0x17:
						msg = new TradeDone();
						break;
					case 0x1a:
						msg = new DummyPacket();
						break;
					case 0x1b:
						msg = new RequestSocialAction();
						break;
					case 0x1c:
						msg = new ChangeMoveType2();
						break;
					case 0x1d:
						msg = new ChangeWaitType2();
						break;
					case 0x1e:
						msg = new RequestSellItem();
						break;
					case 0x1f:
						msg = new RequestBuyItem();
						break;
					case 0x20:
						msg = new RequestLinkHtml();
						break;
					case 0x21:
						msg = new RequestBypassToServer();
						break;
					case 0x22:
						msg = new RequestBBSwrite();
						break;
					case 0x23:
						msg = new DummyPacket();
						break;
					case 0x24:
						msg = new RequestJoinPledge();
						break;
					case 0x25:
						msg = new RequestAnswerJoinPledge();
						break;
					case 0x26:
						msg = new RequestWithdrawalPledge();
						break;
					case 0x27:
						msg = new RequestOustPledgeMember();
						break;
					case 0x28:
						// RequestDismissPledge
						break;
					case 0x29:
						msg = new RequestJoinParty();
						break;
					case 0x2a:
						msg = new RequestAnswerJoinParty();
						break;
					case 0x2b:
						msg = new RequestWithDrawalParty();
						break;
					case 0x2c:
						msg = new RequestOustPartyMember();
						break;
					case 0x2d:
						// RequestDismissParty
						break;
					case 0x2e:
						msg = new DummyPacket();
						break;
					case 0x2f:
						msg = new RequestMagicSkillUse();
						break;
					case 0x30:
						msg = new Appearing();
						break;
					case 0x31:
						if (Config.ALLOW_WAREHOUSE)
						{
							msg = new SendWareHouseDepositList();
						}
						break;
					case 0x32:
						msg = new SendWareHouseWithDrawList();
						break;
					case 0x33:
						msg = new RequestShortCutReg();
						break;
					case 0x34:
						msg = new DummyPacket();
						break;
					case 0x35:
						msg = new RequestShortCutDel();
						break;
					case 0x36:
						msg = new CannotMoveAnymore();
						break;
					case 0x37:
						msg = new RequestTargetCanceld();
						break;
					case 0x38:
						msg = new Say2();
						break;
					case 0x3c:
						msg = new RequestPledgeMemberList();
						break;
					case 0x3e:
						msg = new DummyPacket();
						break;
					case 0x3f:
						msg = new RequestSkillList();
						break;
					case 0x41:
						msg = new MoveWithDelta();
						break;
					case 0x42:
						msg = new RequestGetOnVehicle();
						break;
					case 0x43:
						msg = new RequestGetOffVehicle();
						break;
					case 0x44:
						msg = new AnswerTradeRequest();
						break;
					case 0x45:
						msg = new RequestActionUse();
						break;
					case 0x46:
						msg = new RequestRestart();
						break;
					case 0x47:
						msg = new RequestSiegeInfo();
						break;
					case 0x48:
						msg = new ValidatePosition();
						break;
					case 0x49:
						// RequestSEKCustom
						break;
					case 0x4a:
						msg = new StartRotating();
						break;
					case 0x4b:
						msg = new FinishRotating();
						break;
					case 0x4d:
						msg = new RequestStartPledgeWar();
						break;
					case 0x4e:
						msg = new RequestReplyStartPledgeWar();
						break;
					case 0x4f:
						msg = new RequestStopPledgeWar();
						break;
					case 0x50:
						msg = new RequestReplyStopPledgeWar();
						break;
					case 0x51:
						msg = new RequestSurrenderPledgeWar();
						break;
					case 0x52:
						msg = new RequestReplySurrenderPledgeWar();
						break;
					case 0x53:
						msg = new RequestSetPledgeCrest();
						break;
					case 0x55:
						msg = new RequestGiveNickName();
						break;
					case 0x57:
						msg = new RequestShowBoard();
						break;
					case 0x58:
						msg = new RequestEnchantItem();
						break;
					case 0x59:
						msg = new RequestDestroyItem();
						break;
					case 0x5b:
						msg = new SendBypassBuildCmd();
						break;
					case 0x5c:
						msg = new RequestMoveToLocationInVehicle();
						break;
					case 0x5d:
						msg = new CannotMoveAnymoreInVehicle();
						break;
					case 0x5e:
						msg = new RequestFriendInvite();
						break;
					case 0x5f:
						msg = new RequestAnswerFriendInvite();
						break;
					case 0x60:
						msg = new RequestFriendList();
						break;
					case 0x61:
						msg = new RequestFriendDel();
						break;
					case 0x63:
						msg = new RequestQuestList();
						break;
					case 0x64:
						msg = new RequestQuestAbort();
						break;
					case 0x66:
						msg = new RequestPledgeInfo();
						break;
					case 0x67:
						msg = new RequestPledgeExtendedInfo();
						break;
					case 0x68:
						msg = new RequestPledgeCrest();
						break;
					case 0x69:
						msg = new RequestSurrenderPersonally();
						break;
					case 0x6a:
						// Ride
						break;
					case 0x6b:
						msg = new RequestAquireSkillInfo();
						break;
					case 0x6c:
						msg = new RequestAquireSkill();
						break;
					case 0x6d:
						msg = new RequestRestartPoint();
						break;
					case 0x6e:
						msg = new RequestGMCommand();
						break;
					case 0x6f:
						msg = new RequestPartyMatchConfig();
						break;
					case 0x70:
						msg = new RequestPartyMatchList();
						break;
					case 0x71:
						msg = new RequestPartyMatchDetail();
						break;
					case 0x72:
						msg = new RequestCrystallizeItem();
						break;
					case 0x73:
						msg = new RequestPrivateStoreManageSell();
						break;
					case 0x74:
						msg = new SetPrivateStoreListSell();
						break;
					case 0x75:
						// msg = new RequestPrivateStoreManageCancel(data, _client);
						break;
					case 0x76:
						msg = new RequestPrivateStoreQuitSell();
						break;
					case 0x77:
						msg = new SetPrivateStoreMsgSell();
						break;
					case 0x78:
						// RequestPrivateStoreList
						break;
					case 0x79:
						msg = new RequestPrivateStoreBuy();
						break;
					case 0x7a:
						// ReviveReply
						break;
					case 0x7b:
						msg = new RequestTutorialLinkHtml();
						break;
					case 0x7c:
						msg = new RequestTutorialPassCmdToServer();
						break;
					case 0x7d:
						msg = new RequestTutorialQuestionMark();
						break;
					case 0x7e:
						msg = new RequestTutorialClientEvent();
						break;
					case 0x7f:
						msg = new RequestPetition();
						break;
					case 0x80:
						msg = new RequestPetitionCancel();
						break;
					case 0x81:
						msg = new RequestGmList();
						break;
					case 0x82:
						msg = new RequestJoinAlly();
						break;
					case 0x83:
						msg = new RequestAnswerJoinAlly();
						break;
					case 0x84:
						msg = new AllyLeave();
						break;
					case 0x85:
						msg = new AllyDismiss();
						break;
					case 0x86:
						msg = new RequestDismissAlly();
						break;
					case 0x87:
						msg = new RequestSetAllyCrest();
						break;
					case 0x88:
						msg = new RequestAllyCrest();
						break;
					case 0x89:
						msg = new RequestChangePetName();
						break;
					case 0x8a:
						msg = new RequestPetUseItem();
						break;
					case 0x8b:
						msg = new RequestGiveItemToPet();
						break;
					case 0x8c:
						msg = new RequestGetItemFromPet();
						break;
					case 0x8e:
						msg = new RequestAllyInfo();
						break;
					case 0x8f:
						msg = new RequestPetGetItem();
						break;
					case 0x90:
						msg = new RequestPrivateStoreManageBuy();
						break;
					case 0x91:
						msg = new SetPrivateStoreListBuy();
						break;
					case 0x92:
						// RequestPrivateStoreBuyManageCancel
						break;
					case 0x93:
						msg = new RequestPrivateStoreQuitBuy();
						break;
					case 0x94:
						msg = new SetPrivateStoreMsgBuy();
						break;
					case 0x95:
						// RequestPrivateStoreBuyList
						break;
					case 0x96:
						msg = new RequestPrivateStoreSell();
						break;
					case 0x97:
						// SendTimeCheckPacket
						break;
					case 0x98:
						// RequestStartAllianceWar
						break;
					case 0x99:
						// ReplyStartAllianceWar
						break;
					case 0x9a:
						// RequestStopAllianceWar
						break;
					case 0x9b:
						// ReplyStopAllianceWar
						break;
					case 0x9c:
						// RequestSurrenderAllianceWar
						break;
					case 0x9d:
						msg = new RequestSkillCoolTime();
						break;
					case 0x9e:
						msg = new RequestPackageSendableItemList();
						break;
					case 0x9f:
						msg = new RequestPackageSend();
						break;
					case 0xa0:
						msg = new RequestBlock();
						break;
					case 0xa1:
						// RequestCastleSiegeInfo
						break;
					case 0xa2:
						msg = new RequestSiegeAttackerList();
						break;
					case 0xa3:
						msg = new RequestSiegeDefenderList();
						break;
					case 0xa4:
						msg = new RequestJoinSiege();
						break;
					case 0xa5:
						msg = new RequestConfirmSiegeWaitingList();
						break;
					case 0xa6:
						// RequestSetCastleSiegeTime
						break;
					case 0xa7:
						msg = new MultiSellChoose();
						break;
					case 0xa8:
						// NetPing
						break;
					case 0xaa:
						msg = new RequestUserCommand();
						break;
					case 0xab:
						msg = new SnoopQuit();
						break;
					case 0xac: // we still need this packet to handle BACK button of craft dialog
						msg = new RequestRecipeBookOpen();
						break;
					case 0xad:
						msg = new RequestRecipeBookDestroy();
						break;
					case 0xae:
						msg = new RequestRecipeItemMakeInfo();
						break;
					case 0xaf:
						msg = new RequestRecipeItemMakeSelf();
						break;
					case 0xb0:
						// msg = new RequestRecipeShopManageList(data, client);
						break;
					case 0xb1:
						msg = new RequestRecipeShopMessageSet();
						break;
					case 0xb2:
						msg = new RequestRecipeShopListSet();
						break;
					case 0xb3:
						msg = new RequestRecipeShopManageQuit();
						break;
					case 0xb4:
						// msg = new SnoopQuit();
						break;
					case 0xb5:
						msg = new RequestRecipeShopMakeInfo();
						break;
					case 0xb6:
						msg = new RequestRecipeShopMakeItem();
						break;
					case 0xb7:
						msg = new RequestRecipeShopManagePrev();
						break;
					case 0xb8:
						msg = new ObserverReturn();
						break;
					case 0xb9:
						msg = new RequestEvaluate();
						break;
					case 0xba:
						msg = new RequestHennaList();
						break;
					case 0xbb:
						msg = new RequestHennaItemInfo();
						break;
					case 0xbc:
						msg = new RequestHennaEquip();
						break;
					case 0xc0:
						msg = new RequestPledgePower();
						break;
					case 0xc1:
						msg = new RequestMakeMacro();
						break;
					case 0xc2:
						msg = new RequestDeleteMacro();
						break;
					case 0xc3:
						msg = new RequestBuyProcure();
						break;
					case 0xc4:
						msg = new RequestBuySeed();
						break;
					case 0xc5:
						msg = new DlgAnswer();
						break;
					case 0xc6:
						msg = new RequestWearItem();
						break;
					case 0xc7:
						msg = new RequestSSQStatus();
						break;
					case 0xCA:
						msg = new GameGuardReply();
						break;
					case 0xcc:
						msg = new RequestSendFriendMsg();
						break;
					case 0xcd:
						msg = new RequestShowMiniMap();
						break;
					case 0xce: // MSN dialogs so that you dont see them in the console.
						break;
					case 0xcf:
						msg = new RequestRecordInfo();
						break;
					case 0xd0:
						int id2 = -1;
						if (buff.remaining() >= 2)
						{
							id2 = buff.getShort() & 0xffff;
						}
						else
						{
							LOG.warn("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
							break;
						}
						
						switch (id2)
						{
							case 1:
								msg = new RequestOustFromPartyRoom();
								break;
							case 2:
								msg = new RequestDismissPartyRoom();
								break;
							case 3:
								msg = new RequestWithdrawPartyRoom();
								break;
							case 4:
								msg = new RequestChangePartyLeader();
								break;
							case 5:
								msg = new RequestAutoSoulShot();
								break;
							case 6:
								msg = new RequestExEnchantSkillInfo();
								break;
							case 7:
								msg = new RequestExEnchantSkill();
								break;
							case 8:
								msg = new RequestManorList();
								break;
							case 9:
								msg = new RequestProcureCropList();
								break;
							case 0x0a:
								msg = new RequestSetSeed();
								break;
							case 0x0b:
								msg = new RequestSetCrop();
								break;
							case 0x0c:
								msg = new RequestWriteHeroWords();
								break;
							case 0x0d:
								msg = new RequestExAskJoinMPCC();
								break;
							case 0x0e:
								msg = new RequestExAcceptJoinMPCC();
								break;
							case 0x0f:
								msg = new RequestExOustFromMPCC();
								break;
							case 0x10:
								msg = new RequestExPledgeCrestLarge();
								break;
							case 0x11:
								msg = new RequestExSetPledgeCrestLarge();
								break;
							case 0x12:
								msg = new RequestOlympiadObserverEnd();
								break;
							case 0x13:
								msg = new RequestOlympiadMatchList();
								break;
							case 0x14:
								msg = new RequestAskJoinPartyRoom();
								break;
							case 0x15:
								msg = new AnswerJoinPartyRoom();
								break;
							case 0x16:
								msg = new RequestListPartyMatchingWaitingRoom();
								break;
							case 0x17:
								msg = new RequestExitPartyMatchingWaitingRoom();
								break;
							case 0x18:
								msg = new RequestGetBossRecord();
								break;
							case 0x19:
								msg = new RequestPledgeSetAcademyMaster();
								break;
							case 0x1a:
								msg = new RequestPledgePowerGradeList();
								break;
							case 0x1b:
								msg = new RequestPledgeMemberPowerInfo();
								break;
							case 0x1c:
								msg = new RequestPledgeSetMemberPowerGrade();
								break;
							case 0x1d:
								msg = new RequestPledgeMemberInfo();
								break;
							case 0x1e:
								msg = new RequestPledgeWarList();
								break;
							case 0x1f:
								msg = new RequestExFishRanking();
								break;
							case 0x20:
								msg = new RequestPCCafeCouponUse();
								break;
							case 0x22:
								msg = new RequestCursedWeaponList();
								break;
							case 0x23:
								msg = new RequestCursedWeaponLocation();
								break;
							case 0x24:
								msg = new RequestPledgeReorganizeMember();
								break;
							case 0x26:
								msg = new RequestExMPCCShowPartyMembersInfo();
								break;
							case 0x27:
								msg = new RequestDuelStart();
								break;
							case 0x28:
								msg = new RequestDuelAnswerStart();
								break;
							case 0x29:
								msg = new RequestConfirmTargetItem();
								break;
							case 0x2a:
								msg = new RequestConfirmRefinerItem();
								break;
							case 0x2b:
								msg = new RequestConfirmGemStone();
								break;
							case 0x2c:
								msg = new RequestRefine();
								break;
							case 0x2d:
								msg = new RequestConfirmCancelItem();
								break;
							case 0x2e:
								msg = new RequestRefineCancel();
								break;
							case 0x2f:
								msg = new RequestExMagicSkillUseGround();
								break;
							case 0x30:
								msg = new RequestDuelSurrender();
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buff, state, client);
								break;
						}
						break;
					default:
						printDebugDoubleOpcode(opcode, 0, buff, state, client);
						break;
				}
				break;
			}
		}
		return msg;
	}
	
	private void printDebug(int opcode, ByteBuffer buff, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
		{
			return;
		}
		
		int size = buff.remaining();
		LOG.warn("Unknown Packet: 0x" + Integer.toHexString(opcode) + " on State: " + state.name() + " Client: " + client.toString());
		byte[] array = new byte[size];
		buff.get(array);
		LOG.warn(Util.printData(array, size));
	}
	
	private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buff, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
		{
			return;
		}
		
		int size = buff.remaining();
		LOG.warn("Unknown Packet: 0x" + Integer.toHexString(opcode) + ":0x" + Integer.toHexString(id2) + " on State: " + state.name() + " Client: " + client.toString());
		byte[] array = new byte[size];
		buff.get(array);
		LOG.warn(Util.printData(array, size));
	}
	
	@Override
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}
	
	@Override
	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		if (Config.CLIENT_FLOOD_PROTECTION)
		{
			rp.getClient().execute(rp);
			return;
		}
		
		ThreadPoolManager.getInstance().executeIOPacket(rp);
	}
}
