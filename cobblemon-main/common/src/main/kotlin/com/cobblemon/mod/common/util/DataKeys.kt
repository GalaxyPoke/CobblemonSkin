/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

object DataKeys {
    const val POKEMON = "Pokemon"
    const val POKEMON_UUID = "UUID"
    const val POKEMON_SPECIES_IDENTIFIER = "Species"
    const val POKEMON_NICKNAME = "Nickname"
    const val POKEMON_FORM_ID = "FormId"
    const val POKEMON_LEVEL = "Level"
    const val POKEMON_GENDER = "Gender"
    const val POKEMON_EXPERIENCE = "Experience"
    const val POKEMON_FRIENDSHIP = "Friendship"
    const val POKEMON_FULLNESS = "Fullness"
    const val POKEMON_INTERACTION_COOLDOWN = "InteractionCooldown"

    const val POKEMON_IVS = "IVs"
    const val POKEMON_IVS_BASE = "Base"
    const val POKEMON_IVS_HYPERTRAINED = "HyperTrained"
    const val POKEMON_EVS = "EVs"
    const val POKEMON_HEALTH = "Health"
    const val POKEMON_SCALE_MODIFIER = "ScaleModifier"
    const val POKEMON_MOVESET = "MoveSet"
    const val POKEMON_MOVESET_MOVENAME = "MoveName"
    const val POKEMON_MOVESET_MOVEPP = "MovePP"
    const val POKEMON_MOVESET_RAISED_PP_STAGES = "RaisedPPStages"
    const val POKEMON_ABILITY = "Ability"
    const val POKEMON_ABILITY_NAME = "AbilityName"
    const val POKEMON_ABILITY_FORCED = "AbilityForced"
    const val POKEMON_ABILITY_INDEX = "AbilityIndex"
    const val POKEMON_ABILITY_PRIORITY = "AbilityPriority"
    const val POKEMON_SHINY = "Shiny"
    const val POKEMON_STATUS = "Status"
    const val POKEMON_STATUS_NAME = "StatusName"
    const val POKEMON_STATUS_TIMER = "StatusTimer"
    const val POKEMON_CAUGHT_BALL = "CaughtBall"
    const val POKEMON_FAINTED_TIMER = "FaintedTimer"
    const val POKEMON_HEALING_TIMER = "HealingTimer"
    const val POKEMON_DATA = "PokemonData"
    const val POKEMON_NATURE = "Nature"
    const val POKEMON_MINTED_NATURE = "MintedNature"
    const val HELD_ITEM = "HeldItem"
    const val HELD_ITEM_VISIBLE = "HeldItemVisible"
    const val HELD_ITEM_AI_DROPPABLE = "HeldItemDroppableByAI"
    const val POKEMON_TERA_TYPE = "TeraType"
    const val POKEMON_DMAX_LEVEL = "DmaxLevel"
    const val POKEMON_GMAX_FACTOR = "GmaxFactor"
    const val POKEMON_TRADEABLE = "Tradeable"
    const val POKEMON_FORCED_ASPECTS = "ForcedAspects"
    const val POKEMON_COSMETIC_ITEM = "CosmeticItem"
    const val POKEMON_ACTIVE_MARK = "ActiveMark"
    const val POKEMON_MARKS = "Marks"
    const val POKEMON_POTENTIAL_MARKS = "Potential Marks"
    const val POKEMON_MARKINGS = "Markings"
    const val POKEMON_RIDE_BOOSTS = "RideBoosts"
    const val POKEMON_RIDE_STAMINA = "RideStamina"
    const val POKEMON_MIN_PERFECT_IVS = "MinPerfectIVs"

    const val POKEMON_STATE = "State"
    const val POKEMON_STATE_TYPE = "StateType"
    const val POKEMON_STATE_SHOULDER = "StateShoulder"
    const val POKEMON_STATE_ID = "StateId"
    const val POKEMON_STATE_PLAYER_UUID = "PlayerUUID"
    const val POKEMON_STATE_POKEMON_UUID = "PokemonUUID"

    const val POKEMON_BATTLE_ID = "BattleId"
    const val POKEMON_POSE_TYPE = "PoseType"
    const val POKEMON_BEHAVIOUR_FLAGS = "BehaviourFlags"
    const val POKEMON_OWNER_ID = "PokemonOwnerId"
    const val POKEMON_HIDE_LABEL = "HideLabel"
    const val POKEMON_UNBATTLEABLE = "Unbattleable"
    const val POKEMON_COUNTS_TOWARDS_SPAWN_CAP = "CountsTowardsSpawnCap"
    const val POKEMON_FREEZE_FRAME = "FreezeFrame"
    const val POKEMON_RECALCULATE_POSE = "RecalculatePose"
    const val POKEMON_PLATFORM_TYPE = "PlatformType"

    const val POKEMON_ORIGINAL_TRAINER = "PokemonOriginalTrainer"
    const val POKEMON_ORIGINAL_TRAINER_NAME = "PokemonOriginalTrainerName"
    const val POKEMON_ORIGINAL_TRAINER_TYPE = "PokemonOriginalTrainerType"

    const val POKEMON_PROPERTIES_MOVES = "Moves"
    const val POKEMON_PROPERTIES_HELDITEM = "HeldItem"

    // Entity effects
    const val ENTITY_EFFECTS = "EntityEffects"
    const val ENTITY_EFFECT_MOCK = "EntityEffectMock"
    const val ENTITY_EFFECT_ID = "EntityEffectID"

    const val POKEMON_ENTITY_MOCK = "PokemonEntityMock"
    const val POKEMON_ENTITY_SCALE = "PokemonEntityScale"

    // Evolution stuff
    const val POKEMON_EVOLUTIONS = "Evolutions"

    const val BENCHED_MOVES = "BenchedMoves"

    const val PC_ID = "PCId"
    const val STORE_SLOT = "Slot"
    const val STORE_SLOT_COUNT = "SlotCount"
    const val STORE_BOX = "Box"
    const val STORE_BOX_NAME = "BoxName"
    const val STORE_BOX_WALLPAPER = "BoxWallpaper"
    const val STORE_BOX_COUNT = "BoxCount"
    const val STORE_BOX_COUNT_LOCKED = "BoxCountLocked"
    const val STORE_BACKUP = "BackupStore"
    const val STORE_UNLOCKED_WALLPAPERS = "UnlockedWallpapers"
    const val STORE_UNSEEN_WALLPAPERS = "UnseenWallpapers"

    // Pokédex Keys
    const val POKEDEX_TYPE = "PokedexType"
    const val NUM_ENCOUNTED_WILD = "NumberEncounteredWild"
    const val NUM_ENCOUNTED_BATTLE = "NumberEncounteredBattle"
    const val NUM_CAUGHT = "NumberCaught"

    const val TETHER_OWNER_ID = "TetherOwnerId"
    const val TETHER_OWNER_NAME = "TetherOwnerName"
    const val TETHERING_ID = "TetheringId"
    const val TETHER_POKEMON = "TetherPokemon"
    const val TETHER_MIN_ROAM_POS = "TetherMinRoamPos"
    const val TETHER_MAX_ROAM_POS = "TetherMaxRoamPos"
    const val TETHER_COUNT = "TetherCount"
    const val TETHERING = "Tethering"
    const val TETHERING_POS = "Pos"
    const val TETHERING_PLAYER_ID = "PlayerId"
    const val TETHERING_ENTITY_ID = "EntityId"
    const val TETHER_PASTURE_POS = "TetherPasturePos"

    // Form stuff
    const val ALOLAN = "alolan"
    const val GALARIAN = "galarian"
    const val HISUIAN = "hisuian"
    const val VALENCIAN = "valencian"
    const val CRYSTAL = "crystal"

    const val POKEMON_PROPERTIES = "Properties"
    const val POKEMON_PROPERTIES_CUSTOM = "CustomProperties"
    const val POKEMON_PROPERTIES_ORIGINAL_TEXT = "OriginalText"
    const val POKEMON_SPECIES_TEXT = "SpeciesText"

    // Healer Block
    const val HEALER_MACHINE_USER = "MachineUser"
    const val HEALER_MACHINE_POKEBALLS = "MachinePokeBalls"
    const val HEALER_MACHINE_TIME_LEFT = "MachineTimeLeft"
    const val HEALER_MACHINE_CHARGE = "MachineCharge"
    const val HEALER_MACHINE_INFINITE = "MachineInfinite"

    // Cooking
    const val AMOUNT_SPAWNED = "AmountSpawned"
    const val FOOD_COLOUR = "FoodColour"
    const val BAIT_EFFECTS = "BaitEffects"
    const val INGREDIENTS = "Ingredients"

    const val PLACED_BY = "PlacedBy"

    const val TICKS_UNTIL_NEXT_SPAWN = "TicksUntilNextSpawn"

    // Pokémon Item
    const val POKEMON_ITEM_SPECIES = "species"
    const val POKEMON_ITEM_ASPECTS = "aspects"
    const val POKEMON_ITEM_TINT_RED = "TintRed"
    const val POKEMON_ITEM_TINT_GREEN = "TintGreen"
    const val POKEMON_ITEM_TINT_BLUE = "TintBlue"
    const val POKEMON_ITEM_TINT_ALPHA = "TintAlpha"

    // Features
    const val HAS_BEEN_SHEARED = "sheared"
    const val CAN_BE_COLORED = "color"
    const val TAIL_REGROWTH_SECONDS = "TailRegrowthSeconds"

    // Variants
    const val IS_MOOSHTANK = "mooshtank"

    // Persistent Data
    const val POKEMON_PERSISTENT_DATA = "PersistentData"

    // Item Tooltips
    const val HIDE_TOOLTIP = "HideTooltip"

    // Shoulder Mount
    const val SHOULDER_UUID = "shoulder_uuid"
    const val SHOULDER_SPECIES = "shoulder_species"
    const val SHOULDER_FORM = "shoulder_form"
    const val SHOULDER_ASPECTS = "shoulder_aspects"
    const val SHOULDER_SCALE_MODIFIER = "shoulder_scale"
    const val SHOULDER_ITEM = "shoulder_item"

    // Multi-Block
    const val MULTIBLOCK_STORAGE = "MultiblockStore"
    const val CONTROLLER_BLOCK = "ControllerBlock"

    // FossilMultiblockStructure Serialization
    const val MONITOR_POS = "MonitorPos"
    const val TANK_BASE_POS = "TankBasePos"
    const val ANALYZER_POS = "AnalyzerPos"
    const val ORGANIC_MATERIAL = "OrganicContent"
    const val INSERTED_FOSSIL = "InsertedFossil"
    const val FOSSIL_INVENTORY = "InsertedFossilStacks"
    const val TANK_FILL_LEVEL = "TankFillLevel"
    const val CONNECTOR_DIRECTION = "ConnectorDirection"
    const val TIME_LEFT = "TimeLeft"
    const val PROTECTED_TIME_LEFT = "ProtectedTimeLeft"
    const val FORMED = "Formed"
    const val CREATED_POKEMON = "CreatedPokemon"
    const val HAS_CREATED_POKEMON = "HasCreatedPokemon"
    const val FOSSIL_OWNER = "FossilOwner"
    const val FOSSIL_STATE = "FossilState"

    // Generic Block Entity
    const val BLOCK_ENTITY_USER_AMOUNT = "userAmount"

    // Generic Bedrock Entity
    const val GENERIC_BEDROCK_CATEGORY = "Category"
    const val GENERIC_BEDROCK_ASPECTS = "Aspects"
    const val GENERIC_BEDROCK_POSE_TYPE = "PoseType"
    const val GENERIC_BEDROCK_SCALE = "Scale"
    const val GENERIC_BEDROCK_COLLIDER_WIDTH = "Width"
    const val GENERIC_BEDROCK_COLLIDER_HEIGHT = "Height"
    const val GENERIC_BEDROCK_SYNC_AGE = "SyncAge"

    // NPCs
    const val NPC_CLASS = "NPCClass"
    const val NPC_BATTLE_CONFIGURATION = "NPCBattleConfiguration"
    const val NPC_CAN_CHALLENGE = "CanChallenge"
    const val NPC_SIMULTANEOUS_BATTLES = "SimultaneousBattles"
    const val NPC_HEAL_AFTERWARDS = "HealAfterwards"
    const val NPC_PARTY = "Party"
    const val NPC_PARTY_TYPE = "PartyType"
    const val NPC_ASPECTS = "AppliedAspects"
    const val NPC_VARIATION_ASPECTS = "VariationAspects"
    const val NPC_INTERACTION = "Interaction"
    const val NPC_INTERACT_TYPE = "Type"
    const val NPC_INTERACT_SCRIPT = "Script"
    const val NPC_INTERACT_CUSTOM_SCRIPT = "CustomScript"
    const val NPC_INTERACT_DIALOGUE = "Dialogue"
    const val NPC_PLAYER_TEXTURE = "NPCPlayerTexture"
    const val NPC_PLAYER_TEXTURE_MODEL = "Model"
    const val NPC_PLAYER_TEXTURE_TEXTURE = "Texture"
    const val NPC_LEVEL = "Level"
    const val NPC_FORCED_RESOURCE_IDENTIFIER = "ForcedResourceIdentifier"
    const val NPC_IS_MOVABLE = "IsMovable"
    const val NPC_SKILL = "Skill"
    const val NPC_IS_INVULNERABLE = "IsInvulnerable"
    const val NPC_IS_LEASHABLE = "IsLeashable"
    const val NPC_ALLOW_PROJECTILE_HITS = "AllowProjectileHits"
    @Deprecated("Divided into RENDER_SCALE and BOX_SCALE")
    const val NPC_BASE_SCALE = "BaseScale"
    const val NPC_BOX_SCALE = "BoxScale"
    const val NPC_RENDER_SCALE = "RenderScale"
    const val NPC_HITBOX = "Hitbox"
    const val NPC_HITBOX_WIDTH = "Width"
    const val NPC_HITBOX_HEIGHT = "Height"
    const val NPC_HITBOX_FIXED = "Fixed"
    const val NPC_HIDE_NAME_TAG = "HideNPCNameTag"

    const val SCRIPTED_BEHAVIOURS_ARE_CUSTOM = "BehavioursAreCustom"
    const val SCRIPTED_BEHAVIOURS = "Behaviours"
    const val SCRIPTED_DATA = "ScriptingData"
    const val SCRIPTED_CONFIG = "ScriptingConfig"

    // PokemonProperties
    const val ELEMENTAL_TYPE = "ElementalType"
}