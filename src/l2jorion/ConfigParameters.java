package l2jorion;

public class ConfigParameters extends Config
{
	/**
	 * This function is for admin set config 1 by 1 from game side.
	 * @param pName
	 * @param pValue
	 * @return
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		if (pName.equalsIgnoreCase("GmLoginSpecialEffect"))
		{
			GM_SPECIAL_EFFECT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSp"))
		{
			RATE_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartyXp"))
		{
			RATE_PARTY_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartySp"))
		{
			RATE_PARTY_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestsReward"))
		{
			RATE_QUESTS_REWARD = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropAdena"))
		{
			RATE_DROP_ADENA = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateConsumableCost"))
		{
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropItems"))
		{
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropSealStones"))
		{
			RATE_DROP_SEAL_STONES = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropSpoil"))
		{
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropManor"))
		{
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropQuest"))
		{
			RATE_DROP_QUEST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateKarmaExpLost"))
		{
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
		{
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerDropLimit"))
		{
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDrop"))
		{
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropItem"))
		{
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip"))
		{
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
		{
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaDropLimit"))
		{
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDrop"))
		{
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropItem"))
		{
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip"))
		{
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
		{
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem"))
		{
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyEquipableItem"))
		{
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItem"))
		{
			SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad"))
		{
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItemInterval"))
		{
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClearDroppedItemTable"))
		{
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("PreciseDropCalculation"))
		{
			PRECISE_DROP_CALCULATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MultipleItemDrop"))
		{
			MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CoordSynchronize"))
		{
			COORD_SYNCHRONIZE = Integer.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays"))
		{
			DELETE_DAYS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowDiscardItem"))
		{
			ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowFreight"))
		{
			ALLOW_FREIGHT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWarehouse"))
		{
			ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWear"))
		{
			ALLOW_WEAR = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("WearDelay"))
		{
			WEAR_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WearPrice"))
		{
			WEAR_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWater"))
		{
			ALLOW_WATER = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRentPet"))
		{
			ALLOW_RENTPET = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowBoat"))
		{
			ALLOW_BOAT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowCursedWeapons"))
		{
			ALLOW_CURSED_WEAPONS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowManor"))
		{
			ALLOW_MANOR = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("BypassValidation"))
		{
			BYPASS_VALIDATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CommunityType"))
		{
			COMMUNITY_TYPE = pValue.toLowerCase();
		}
		else if (pName.equalsIgnoreCase("BBSDefault"))
		{
			BBS_DEFAULT = pValue;
		}
		else if (pName.equalsIgnoreCase("ShowNpcLevel"))
		{
			SHOW_NPC_LVL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ForceInventoryUpdate"))
		{
			FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
		{
			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumOnlineUsers"))
		{
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ZoneTown"))
		{
			ZONE_TOWN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules"))
		{
			DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowGuards"))
		{
			ALLOW_GUARDS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CancelLesserEffect"))
		{
			EFFECT_CANCELING = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("WyvernSpeed"))
		{
			WYVERN_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StriderSpeed"))
		{
			STRIDER_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
		{
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
		{
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
		{
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
		{
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
		{
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
		{
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumFreightSlots"))
		{
			FREIGHT_SLOTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationNGSkillChance"))
		{
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidSkillChance"))
		{
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighSkillChance"))
		{
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopSkillChance"))
		{
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationBaseStatChance"))
		{
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationNGGlowChance"))
		{
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidGlowChance"))
		{
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighGlowChance"))
		{
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopGlowChance"))
		{
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMax"))
		{
			ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMaxFull"))
		{
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GMOverEnchant"))
		{
			GM_OVER_ENCHANT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("HpRegenMultiplier"))
		{
			HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("MpRegenMultiplier"))
		{
			MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("CpRegenMultiplier"))
		{
			CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
		{
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
		{
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidPhysicalDefenceMultiplier"))
		{
			RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMagicalDefenceMultiplier"))
		{
			RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime"))
		{
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAdena"))
		{
			STARTING_ADENA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UnstuckInterval"))
		{
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerSpawnProtection"))
		{
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
		{
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod"))
		{
			PARTY_XP_CUTOFF_METHOD = pValue;
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent"))
		{
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel"))
		{
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreCP"))
		{
			RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreHP"))
		{
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreMP"))
		{
			RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf"))
		{
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther"))
		{
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StoreSkillCooltime"))
		{
			STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn"))
		{
			ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameTiredness"))
		{
			ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreation"))
		{
			ALT_GAME_CREATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed"))
		{
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate"))
		{
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate"))
		{
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltWeightLimit"))
		{
			ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes"))
		{
			ALT_BLACKSMITH_USE_RECIPES = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameSkillLearn"))
		{
			ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("RemoveCastleCirclets"))
		{
			REMOVE_CASTLE_CIRCLETS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		else if (pName.equalsIgnoreCase("AltShieldBlocks"))
		{
			ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
		{
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("Delevel"))
		{
			ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MagicFailures"))
		{
			ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameMobAttackAI"))
		{
			ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone"))
		{
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentXp"))
		{
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentSp"))
		{
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowClassMasters"))
		{
			ALLOW_CLASS_MASTERS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreights"))
		{
			ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreightPrice"))
		{
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange"))
		{
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange2"))
		{
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CraftingEnabled"))
		{
			IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("LifeCrystalNeeded"))
		{
			LIFE_CRYSTAL_NEEDED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("SpBookNeeded"))
		{
			SP_BOOK_NEEDED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLoot"))
		{
			AUTO_LOOT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLootHerbs"))
		{
			AUTO_LOOT_HERBS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaFlagPlayerCanUseBuffer"))
		{
			FLAGED_PLAYER_USE_BUFFER = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireCastleForDawn"))
		{
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireClanCastle"))
		{
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltFreeTeleporting"))
		{
			ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
		{
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRestoreEffectOnSub"))
		{
			ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie"))
		{
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH"))
		{
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit"))
		{
			DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CommonRecipeLimit"))
		{
			COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionEnable"))
		{
			L2JMOD_CHAMPION_ENABLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionFrequency"))
		{
			L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMinLevel"))
		{
			L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMaxLevel"))
		{
			L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHp"))
		{
			L2JMOD_CHAMPION_HP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHpRegen"))
		{
			L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewards"))
		{
			L2JMOD_CHAMPION_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards"))
		{
			L2JMOD_CHAMPION_ADENAS_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAtk"))
		{
			L2JMOD_CHAMPION_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionSpdAtk"))
		{
			L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItem"))
		{
			L2JMOD_CHAMPION_REWARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemID"))
		{
			L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty"))
		{
			L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWedding"))
		{
			L2JMOD_ALLOW_WEDDING = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPrice"))
		{
			L2JMOD_WEDDING_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPunishInfidelity"))
		{
			L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleport"))
		{
			L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportPrice"))
		{
			L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportDuration"))
		{
			L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingAllowSameSex"))
		{
			L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingFormalWear"))
		{
			L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingDivorceCosts"))
		{
			L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTEvenTeams"))
		{
			TVT_EVEN_TEAMS = pValue;
		}
		else if (pName.equalsIgnoreCase("TvTAllowInterference"))
		{
			TVT_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTAllowPotions"))
		{
			TVT_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTAllowSummon"))
		{
			TVT_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTOnStartRemoveAllEffects"))
		{
			TVT_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTOnStartUnsummonPet"))
		{
			TVT_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TVTReviveDelay"))
		{
			TVT_REVIVE_DELAY = Long.parseLong(pValue);
		}
		else if (pName.equalsIgnoreCase("MinKarma"))
		{
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxKarma"))
		{
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("XPDivider"))
		{
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BaseKarmaLost"))
		{
			KARMA_LOST_BASE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CanGMDropEquipment"))
		{
			KARMA_DROP_GM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
		{
			KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
		{
			KARMA_PK_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsNormalTime"))
		{
			PVP_NORMAL_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsPvPTime"))
		{
			PVP_PVP_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GlobalChat"))
		{
			DEFAULT_GLOBAL_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("TradeChat"))
		{
			DEFAULT_TRADE_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("MenuStyle"))
		{
			GM_ADMIN_MENU_STYLE = pValue;
		}
		else if (pName.equalsIgnoreCase("MaxPAtkSpeed"))
		{
			MAX_PATK_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxMAtkSpeed"))
		{
			MAX_MATK_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ServerNameEnabled"))
		{
			ALT_SERVER_NAME_ENABLED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("WelcomeText"))
		{
			ALT_SERVER_TEXT = String.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ServerCfgMenuName"))
		{
			ALT_Server_Menu_Name = String.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("FlagedPlayerCanUseGK"))
		{
			FLAGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AddExpAtPvp"))
		{
			ADD_EXP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AddSpAtPvp"))
		{
			ADD_SP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CastleShieldRestriction"))
		{
			CASTLE_SHIELD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ClanHallShieldRestriction"))
		{
			CLANHALL_SHIELD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ApellaArmorsRestriction"))
		{
			APELLA_ARMORS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("OathArmorsRestriction"))
		{
			OATH_ARMORS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CastleLordsCrownRestriction"))
		{
			CASTLE_CROWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CastleCircletsRestriction"))
		{
			CASTLE_CIRCLETS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRaidBossPetrified"))
		{
			ALLOW_RAID_BOSS_PETRIFIED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowLowLevelTrade"))
		{
			ALLOW_LOW_LEVEL_TRADE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowPotsInPvP"))
		{
			ALLOW_POTS_IN_PVP = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAncientAdena"))
		{
			STARTING_AA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnouncePvPKill"))
		{
			ANNOUNCE_PVP_KILL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnouncePkKill"))
		{
			ANNOUNCE_PK_KILL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DisableWeightPenalty"))
		{
			DISABLE_WEIGHT_PENALTY = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFEvenTeams"))
		{
			CTF_EVEN_TEAMS = pValue;
		}
		else if (pName.equalsIgnoreCase("CTFAllowInterference"))
		{
			CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFAllowPotions"))
		{
			CTF_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFAllowSummon"))
		{
			CTF_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFOnStartRemoveAllEffects"))
		{
			CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFOnStartUnsummonPet"))
		{
			CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMAllowInterference"))
		{
			DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMAllowPotions"))
		{
			DM_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMAllowSummon"))
		{
			DM_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMJoinWithCursedWeapon"))
		{
			DM_JOIN_CURSED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMOnStartRemoveAllEffects"))
		{
			DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMOnStartUnsummonPet"))
		{
			DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMReviveDelay"))
		{
			DM_REVIVE_DELAY = Long.parseLong(pValue);
		}
		else
		{
			return false;
		}
		return true;
	}
}
