/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.permission

object CobblemonPermissions {

    private const val COMMAND_PREFIX = "command."
    private val permissions = arrayListOf<Permission>()

    @JvmStatic
    val CHANGE_EYE_HEIGHT = this.create("${COMMAND_PREFIX}changeeyeheight", PermissionLevel.ALL_COMMANDS)
    @JvmStatic
    val CHANGE_SCALE_AND_SIZE = this.create("${COMMAND_PREFIX}changescaleandsize", PermissionLevel.ALL_COMMANDS)
    @JvmStatic
    val CHANGE_WALK_SPEED = this.create("${COMMAND_PREFIX}changewalkspeed", PermissionLevel.ALL_COMMANDS)
    @JvmStatic
    val TRANSFORM_MODEL_PART = this.create("${COMMAND_PREFIX}transformmodelpart", PermissionLevel.ALL_COMMANDS)
    @JvmStatic
    val CHECKSPAWNS = this.create("${COMMAND_PREFIX}checkspawns", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val GET_NBT = this.create("${COMMAND_PREFIX}getnbt", PermissionLevel.ALL_COMMANDS)

    private const val GIVE_POKEMON_BASE = "${COMMAND_PREFIX}givepokemon"
    @JvmStatic
    val GIVE_POKEMON_SELF = this.create("${GIVE_POKEMON_BASE}.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val GIVE_POKEMON_OTHER = this.create("${GIVE_POKEMON_BASE}.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    private const val HEAL_POKEMON_BASE = "${COMMAND_PREFIX}healpokemon"
    @JvmStatic
    val HEAL_POKEMON_SELF = this.create("$HEAL_POKEMON_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val HEAL_POKEMON_OTHER = this.create("$HEAL_POKEMON_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    private const val LEVEL_UP_BASE = "${COMMAND_PREFIX}levelup"
    @JvmStatic
    val LEVEL_UP_SELF = this.create("$LEVEL_UP_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val LEVEL_UP_OTHER = this.create("$LEVEL_UP_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val OPEN_STARTER_SCREEN = this.create("${COMMAND_PREFIX}openstarterscreen", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val BEDROCK_PARTICLE = this.create("${COMMAND_PREFIX}bedrockparticle", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val OPEN_DIALOGUE = this.create("${COMMAND_PREFIX}opendialogue", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val UNLOCK_PC_BOX_WALLPAPER = this.create("${COMMAND_PREFIX}unlockpcboxwallpaper", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    private const val POKEMON_EDIT_BASE = "${COMMAND_PREFIX}pokemonedit"
    @JvmStatic
    val POKEMON_EDIT_SELF = this.create("$POKEMON_EDIT_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val POKEMON_EDIT_OTHER = this.create("$POKEMON_EDIT_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val SPAWN_ALL_POKEMON = this.create("${COMMAND_PREFIX}spawnallpokemon", PermissionLevel.ALL_COMMANDS)
    @JvmStatic
    val GIVE_ALL_POKEMON = this.create("${COMMAND_PREFIX}giveallpokemon", PermissionLevel.ALL_COMMANDS)

    @JvmStatic
    val SPAWN_POKEMON = this.create("${COMMAND_PREFIX}spawnpokemon", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val SPAWN_NPC = this.create("${COMMAND_PREFIX}spawnnpc", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    @JvmStatic
    val STOP_BATTLE = this.create("${COMMAND_PREFIX}stopbattle", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    @JvmStatic
    val TAKE_POKEMON = this.create("${COMMAND_PREFIX}takepokemon", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    @JvmStatic
    val TEACH = this.create("${COMMAND_PREFIX}teach.base", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val TEACH_BYPASS_LEARNSET = this.create("${COMMAND_PREFIX}teach.bypass", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    @JvmStatic
    val FRIENDSHIP = this.create("${COMMAND_PREFIX}friendship", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)


    @JvmStatic
    val HELD_ITEM = this.create("${COMMAND_PREFIX}helditem", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val CHANGE_MARK = this.create("${COMMAND_PREFIX}changemark", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val PC = this.create("${COMMAND_PREFIX}pc", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val PC_SEARCH = this.create("${COMMAND_PREFIX}pcsearch", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val POKEBOX = this.create("${COMMAND_PREFIX}pokebox", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val RENAMEBOX = this.create("${COMMAND_PREFIX}renamebox", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val CHANGE_WALLPAPER = this.create("${COMMAND_PREFIX}changewallpaper", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val TEST_STORE = this.create("${COMMAND_PREFIX}teststore", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val QUERY_LEARNSET = this.create("${COMMAND_PREFIX}querylearnset", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val TEST_PC_SLOT = this.create("${COMMAND_PREFIX}testpcslot", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val TEST_PARTY_SLOT = this.create("${COMMAND_PREFIX}testpartyslot", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val CLEAR_PARTY = this.create("${COMMAND_PREFIX}clearparty", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val CLEAR_PC = this.create("${COMMAND_PREFIX}clearpc", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val POKEDEX = this.create("${COMMAND_PREFIX}pokedex", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val NPC_EDIT = this.create("${COMMAND_PREFIX}npcedit", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val NPC_DELETE = this.create("${COMMAND_PREFIX}npcdelete", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val FREEZE_POKEMON = this.create("${COMMAND_PREFIX}freezepokemon", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val APPLY_PLAYER_TEXTURE = this.create("${COMMAND_PREFIX}applyplayertexture", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val BEHAVIOUR_EDIT = this.create("${COMMAND_PREFIX}behaviouredit", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val ABANDON_MULTITEAM = this.create("${COMMAND_PREFIX}abandonmultiteam", PermissionLevel.NONE)
    @JvmStatic
    val RUN_MOLANG = this.create("${COMMAND_PREFIX}runmolang", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val RUN_MOLANG_SCRIPT = this.create("${COMMAND_PREFIX}runmolangscript", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val COBBLEMON_CONFIG_RELOAD = this.create("${COMMAND_PREFIX}cobblemonconfig.reload", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val CALCULATE_SEAT_POSITIONS = this.create("${COMMAND_PREFIX}calculateseatpositions", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val CHANGE_BOX_COUNT = this.create("${COMMAND_PREFIX}boxcount", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    @JvmStatic
    val SPECTATE_BATTLE = this.create("${COMMAND_PREFIX}spectatebattle", PermissionLevel.ALL_COMMANDS)

    @JvmStatic
    val SEE_HIDDEN_NPCS = this.create("seehiddennpcs", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    val USE_RIDING_STATS_DEBUG = this.create("useridingstatsdebug", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    @JvmStatic
    fun all(): Iterable<Permission> = this.permissions

    private fun create(node: String, level: PermissionLevel): Permission {
        val permission = CobblemonPermission(node, level)
        this.permissions += permission
        return permission
    }

}