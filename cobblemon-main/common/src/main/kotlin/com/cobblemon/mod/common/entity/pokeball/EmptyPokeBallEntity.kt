/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokeball

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities.EMPTY_POKEBALL
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleCaptureAction
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleTypes
import com.cobblemon.mod.common.battles.ForcePassActionResponse
import com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokeballPacket
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.ai.PokemonBrain
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.cobblemon.mod.common.util.*
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.util.Mth.PI
import net.minecraft.world.entity.*
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

class EmptyPokeBallEntity : ThrowableItemProjectile, PosableEntity, WaterDragModifier, Schedulable {
    enum class CaptureState {
        NOT,
        HIT,
        FALL,
        CRITICAL,
        SHAKE,
        CAPTURED,
        BROKEN_FREE
    }

    companion object {
        val CAPTURE_STATE = SynchedEntityData.defineId(EmptyPokeBallEntity::class.java, EntityDataSerializers.BYTE)
        val HIT_TARGET_POSITION = SynchedEntityData.defineId(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        val HIT_VELOCITY = SynchedEntityData.defineId(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        val SHAKE = SynchedEntityData.defineId(EmptyPokeBallEntity::class.java, EntityDataSerializers.BOOLEAN)
        val ASPECTS = SynchedEntityData.defineId(EmptyPokeBallEntity::class.java, StringSetDataSerializer)

        const val SECONDS_BETWEEN_SHAKES = 1.25F
        const val SECONDS_BEFORE_SHAKE = 1F

        val DIMENSIONS = EntityDimensions.fixed(0.4F, 0.4F)
    }

    val dataTrackerEmitter = SimpleObservable<EntityDataAccessor<*>>()

    override val schedulingTracker = SchedulingTracker()
    var capturingPokemon: PokemonEntity? = null
    val captureFuture = CompletableFuture<Boolean>()
    var captureState: CaptureState
        get() = CaptureState.entries[entityData.get(CAPTURE_STATE).toInt()]
        set(value) { entityData.set(CAPTURE_STATE, value.ordinal.toByte()) }
    var aspects: Set<String>
        get() = entityData.get(ASPECTS)
        set(value) { entityData.set(ASPECTS, value) }

    override val delegate = if (level().isClientSide) {
        EmptyPokeBallClientDelegate()
    } else {
        EmptyPokeBallServerDelegate()
    }

    override val struct = QueryStruct(hashMapOf())
        .addFunction("capture_state") { StringValue(captureState.name) }
        .addFunction("ball_type") { StringValue(pokeBall.name.toString()) }
        .addFunction("aspects") { aspects.asArrayValue { StringValue(it) } }
        .addFunction("thrower") { owner?.asMostSpecificMoLangValue() ?: DoubleValue.ZERO }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        pokeBall = PokeBalls.POKE_BALL
        super.defineSynchedData(builder)
        builder.define(CAPTURE_STATE, CaptureState.NOT.ordinal.toByte())
        builder.define(ASPECTS, emptySet())
        builder.define(HIT_TARGET_POSITION, Vec3.ZERO)
        builder.define(HIT_VELOCITY, Vec3.ZERO)
        builder.define(SHAKE, false)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        // See what I said about this in PokemonEntity
        if (delegate != null) {
            delegate.onSyncedDataUpdated(data)
        }
        if (data == CAPTURE_STATE) {
            val newState = entityData[CAPTURE_STATE]
            when (CaptureState.entries[newState.toInt()]) {
                CaptureState.NOT, CaptureState.FALL -> isNoGravity = false
                CaptureState.CRITICAL, CaptureState.SHAKE -> isNoGravity = true
                CaptureState.HIT, CaptureState.CAPTURED, CaptureState.BROKEN_FREE -> {
                    // no op
                }
            }
        }
        dataTrackerEmitter.emit(data)
    }

    init {
        delegate.initialize(this)
        addPosableFunctions(struct)
    }

    constructor(world: Level) : this(pokeBall = PokeBalls.POKE_BALL, world = world)
    constructor(
        pokeBall: PokeBall,
        world: Level,
        entityType: EntityType<out EmptyPokeBallEntity> = EMPTY_POKEBALL
    ): super(entityType, world) {
        this.pokeBall = pokeBall
    }

    constructor(
        pokeBall: PokeBall,
        world: Level,
        ownerEntity: LivingEntity,
        entityType: EntityType<out EmptyPokeBallEntity> = EMPTY_POKEBALL
    ): super(entityType, ownerEntity, world) {
        this.pokeBall = pokeBall
    }

    var pokeBall: PokeBall = PokeBalls.POKE_BALL

    override fun onHitBlock(hitResult: BlockHitResult) {
        if (captureState == CaptureState.NOT) {
            if (level().isServerSide()) {
                super.onHitBlock(hitResult)
                level().sendParticlesServer(ParticleTypes.CLOUD, hitResult.location, 2, hitResult.location.subtract(position()).normalize().scale(-0.1), 0.0)
                level().playSoundServer(position(), SoundEvents.WOOD_PLACE, pitch = 2.5F)
                discard()
                val player = this.owner as? ServerPlayer
                if (player?.isCreative == false) {
                    spawnAtLocation(defaultItem)
                }
            }
        } else {
            setNoGravity(false)
            deltaMovement = Vec3.ZERO
        }
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        if (captureState == CaptureState.NOT) {
            if (hitResult.entity is PokemonEntity && level().isServerSide()) {
                val pokemonEntity = hitResult.entity as PokemonEntity

                val battle = (pokemonEntity.delegate as PokemonServerDelegate).getBattle()
                val owner = owner

                if (!pokemonEntity.pokemon.isWild()) {
                    owner?.sendSystemMessage(lang("capture.not_wild", pokemonEntity.exposedSpecies.translatedName).red())
                    return drop()
                }

                if (!UncatchableProperty.isCatchable(pokemonEntity)) {
                    owner?.sendSystemMessage(lang("capture.cannot_be_caught").red())
                    return drop()
                }

                if (battle != null && owner != null && owner is LivingEntity) {
                    val throwerActor = battle.getActor(owner.uuid)
                    val hitActor = battle.actors.find { it.isForPokemon(pokemonEntity) }
                    val hitBattlePokemon = hitActor?.activePokemon?.find { it.battlePokemon?.effectedPokemon?.entity == pokemonEntity }

                    if (throwerActor == null) {
                        owner.sendSystemMessage(lang("capture.in_battle", pokemonEntity.exposedSpecies.translatedName).red())
                        return drop()
                    }

                    if (hitActor == null || hitBattlePokemon == null) {
                        return drop() // Weird, shouldn't be possible
                    }

                    if (battle.format.battleType != BattleTypes.SINGLES || hitActor.pokemonList.count { it.health > 0 } > 1) {
                        owner.sendSystemMessage(lang("capture.not_single").red())
                        return drop()
                    }

                    val canFitForcedAction = throwerActor.canFitForcedAction()
                    if (!canFitForcedAction) {
                        owner.sendSystemMessage(lang("capture.not_your_turn").red())
                        return drop()
                    }

                    battle.captureActions.add(BattleCaptureAction(battle, hitBattlePokemon, this).also { it.attach() })

                    battle.broadcastChatMessage(
                        lang(
                            "capture.attempted_capture",
                            throwerActor.getName(),
                            pokeBall.item().description,
                            pokemonEntity.exposedSpecies.translatedName
                        ).yellow()
                    )
                    battle.sendUpdate(BattleCaptureStartPacket(pokeBall.name, aspects, hitBattlePokemon.getPNX()))
                    throwerActor.forceChoose(ForcePassActionResponse())
                } else if (pokemonEntity.isBusy) {
                    owner?.sendSystemMessage(lang("capture.busy", pokemonEntity.exposedSpecies.translatedName).red())
                    return drop()
                } else if (owner is ServerPlayer && BattleRegistry.getBattleByParticipatingPlayer(owner) != null) {
                    owner.sendSystemMessage(lang("you_in_battle").red())
                    return drop()
                }


                capturingPokemon = pokemonEntity
                entityData.set(HIT_VELOCITY, deltaMovement.normalize())
                entityData.set(HIT_TARGET_POSITION, hitResult.location)
                CobblemonEvents.THROWN_POKEBALL_HIT.postThen(
                    event = ThrownPokeballHitEvent(this, pokemonEntity),
                    ifSucceeded = {
                        attemptCatch(pokemonEntity)
                    },
                    ifCanceled = {
                        drop()
                        return
                    }
                )
                return
            }
        }
        super.onHitEntity(hitResult)
    }

    private fun drop() {
        val owner = owner
        discard()
        val player = owner?.takeIf { it is ServerPlayer } as? ServerPlayer
        if (player?.isCreative != true) spawnAtLocation(defaultItem)
        return
    }

    // Poké Balls don't save to the world.
    override fun shouldBeSaved() = false

    override fun tick() {
        super.tick()
        delegate.tick(this)

        if (level().isServerSide()) {
        

            if (this.tickCount > 600 && this.capturingPokemon == null) {
                this.remove(RemovalReason.DISCARDED)
            }

            if (owner == null || !owner!!.isAlive || (captureState != CaptureState.NOT && capturingPokemon?.isAlive != true)) {
                breakFree()
                discard()
                return
            }
        }

        // Look at the target, if the target is known.
        val hitTargetPosition = entityData.get(HIT_TARGET_POSITION)
        if (hitTargetPosition.length() != 0.0) {
            val diff = hitTargetPosition.subtract(position())
            yRot = ((Mth.atan2(diff.x, diff.z) * 180 / Math.PI).toFloat())
        }

        schedulingTracker.update(1/20F)
    }

    private fun ancientBallShake(animations: Set<String>) {
        val pktShakeAncientAnimation = PlayPosableAnimationPacket(this.id, animations, emptyList())
        pktShakeAncientAnimation.sendToPlayersAround(
            x = this.x,
            y = this.y,
            z = this.z,
            worldKey = this.level().dimension(),
            distance = 64.0
        )
    }

    private fun shakeBall(task: ScheduledTask, rollsRemaining: Int, captureResult: CaptureContext) {
        if (this.capturingPokemon?.isAlive != true || !this.isAlive || this.owner == null|| owner?.isAlive != true) {
            if (this.capturingPokemon?.isAlive == true) {
                this.breakFree()
            }
            this.discard()
            task.expire()
            return
        }

        if (rollsRemaining <= 0) {
            if (captureResult.isSuccessfulCapture) {
                captureState = CaptureState.CAPTURED
                // Do a capture
                val pokemon = capturingPokemon ?: return
                val player = this.owner as? ServerPlayer ?: return
                val captureTime = if (pokeBall.ancient == true) 1.8F else 1F
                after(seconds = captureTime) {
                    // Dupes occurred by double-adding Pokémon, this hopefully prevents it triple-condom style
                    // wtf did I just read above me
                    if (pokemon.pokemon.isWild() && pokemon.isAlive && !captureFuture.isDone) {
                        pokemon.discard()
                        discard()
                        captureFuture.complete(true)
                        val party = player.party()
                        pokemon.pokemon.caughtBall = pokeBall
                        pokeBall.effects.forEach { effect -> effect.apply(player, pokemon.pokemon) }
                        party.add(pokemon.pokemon)
                        val leashHolder = pokemon.leashHolder

                        if (leashHolder != null) {
                            val blockPos = leashHolder.blockPosition()
                            val blockState = level().getBlockState(blockPos)
                            val leashKnot = LeashFenceKnotEntity.getOrCreateKnot(level(), blockPos)
                            leashKnot?.discard()
                            pokemon.dropLeash(true, true)
                            level().sendBlockUpdated(blockPos, blockState, blockState, 3)
                        }

                        CobblemonEvents.POKEMON_CAPTURED.post(PokemonCapturedEvent(pokemon.pokemon, player, this))
                    }
                }
                return
            } else {
                breakFree()
            }
            return
        }
        // Emits a shake by changing the value to the opposite of what it currently is. Sends an update to the client basically.
        // We could replace this with a packet, but it feels awfully excessive when we already have 5 bajillion packets.
        if (this.pokeBall.ancient) {
            when (captureResult.numberOfShakes) {
                1 -> ancientBallShake(setOf("q.bedrock_stateful('ancient_poke_ball', 'weirdhop')"))
                2 -> ancientBallShake(setOf("q.bedrock_stateful('ancient_poke_ball', 'bighop')"))
                3 -> ancientBallShake(setOf("q.bedrock_stateful('ancient_poke_ball', 'midhop1')", "q.bedrock_stateful('ancient_poke_ball', 'midhop2')"))
                4 -> ancientBallShake(setOf("q.bedrock_stateful('ancient_poke_ball', 'smallhop1')", "q.bedrock_stateful('ancient_poke_ball', 'smallhop2')"))
            }
        } else {
            entityData.update(SHAKE) { !it }
        }
    }

    private fun breakFree() {
        val pokemon = capturingPokemon ?: return
        pokemon.setPositionSafely(position())
        
        pokemon.beamMode = 2
        pokemon.isInvisible = false
        pokemon.isSilent = false

        if (pokemon.battleId == null) {
            pokemon.pokemon.status?.takeIf { it.status == Statuses.SLEEP }?.let { pokemon.pokemon.status = null }
            owner?.let { PokemonBrain.onCaptureFailed(pokemon, it) }
        }

        captureState = CaptureState.BROKEN_FREE

        after(seconds = 0.5F) {
            if (pokemon.pokemon.shiny) {
                SpawnSnowstormEntityParticlePacket(cobblemonResource("shiny_ring"), pokemon.id, listOf("shiny_particles", "middle"))
                    .sendToPlayersAround(pokemon.x, pokemon.y, pokemon.z, 32.0, pokemon.level().dimension())
            }
        }

        after(seconds = 1.2F) {
            discard()
        }

        pokemon.after(seconds = 0.1F) {
            pokemon.beamMode = 0
            pokemon.busyLocks.remove(this)
        }

        after(seconds = 0.1F) {
            captureFuture.complete(false)
            val ballType =
                this.pokeBall.name.path.lowercase().replace("_", "")
            val mode = "casual"
            val sendflash =
                BedrockParticleOptionsRepository.getEffect(cobblemonResource("${ballType}/${mode}/sendflash"))
            sendflash?.let { effect ->
                val wrapper = MatrixWrapper()
                val matrix = PoseStack()
                matrix.translate(x, y, z)
                wrapper.updateMatrix(matrix.last().pose())
                val world = Minecraft.getInstance().level ?: return@let
                ParticleStorm(effect, wrapper, wrapper, world).spawn()
            }
        }
    }

    override fun getDefaultItem(): Item = pokeBall.item()

    override fun getDimensions(pPose: Pose) = DIMENSIONS

    private fun attemptCatch(pokemonEntity: PokemonEntity) {
        pokemonEntity.busyLocks.add(this)
        val displace = deltaMovement
        captureState = CaptureState.HIT
        val mul = if (random.nextBoolean()) 1 else -1
        level().playSoundServer(position(), CobblemonSounds.POKE_BALL_HIT, volume = 1F)

        // Hit Pokémon plays recoil animation
        pokemonEntity.playAnimation("recoil")

        // Bounce backwards away from the hit Pokémon
        deltaMovement = displace.multiply(-1.0, 0.0, -1.0).normalize().yRot(mul * PI/3).multiply(0.1, 0.0, 0.1).add(0.0, 1.0 / 3, 0.0)
        pokemonEntity.phasingTargetId = this.id
        after(seconds = 0.7F) {
            // Start beaming them up.
            deltaMovement = Vec3.ZERO
            setNoGravity(true)
            level().playSoundServer(position(), CobblemonSounds.POKE_BALL_RECALL, volume = 0.6F)
            pokemonEntity.beamMode = 3
        }

        after(seconds = 2.2F) {
            // Time to begin falling
            pokemonEntity.phasingTargetId = -1
            pokemonEntity.isInvisible = true
            pokemonEntity.isSilent = true
            captureState = CaptureState.FALL
            after(seconds = 1.5F) {
                // If it was still falling after a second and a half, just assume it's landed because we can't wait all day.
                if (captureState == CaptureState.FALL) {
                    deltaMovement = Vec3.ZERO
                    setNoGravity(true)
                    setOnGround(true)
                    beginCapture()
                }
            }
        }
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (captureState == CaptureState.FALL && hitResult.type == HitResult.Type.BLOCK) {
            if (level().isServerSide()) {
                beginCapture()
            }
        }
    }

    fun beginCapture() {
        // We have hit the ground, time to stop falling and start shaking! Calculate capture.
        val thrower = owner as LivingEntity
        val captureResult = Cobblemon.config.captureCalculator.processCapture(thrower, this, capturingPokemon!!).let {
            val event = PokeBallCaptureCalculatedEvent(thrower = thrower, pokemonEntity = capturingPokemon!!, pokeBallEntity = this, captureResult = it)
            CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.post(event)
            event.captureResult
        }

        var rollsRemaining = captureResult.numberOfShakes
        if (rollsRemaining == 4) {
            rollsRemaining--
        }
        var animatedCritical = false
        // Critical capture despite their name actually animate first
        // They can still fail and don't actually signify a success
        val iterations: Int = if (captureResult.isCriticalCapture) {
            captureState = CaptureState.CRITICAL
            captureResult.numberOfShakes + 2
        } else {
            captureState = CaptureState.SHAKE
            captureResult.numberOfShakes + 1
        }
        taskBuilder()
            .iterations(iterations)
            .delay(SECONDS_BEFORE_SHAKE)
            .interval(SECONDS_BETWEEN_SHAKES)
            .execute {
                if (captureResult.isCriticalCapture && !animatedCritical) {
                    captureState = CaptureState.SHAKE
                    animatedCritical = true
                    return@execute
                }
                if (pokeBall.ancient && rollsRemaining > 1) {
                    rollsRemaining = 1
                }
                shakeBall(it, rollsRemaining, captureResult)
                rollsRemaining--
            }
            .build()
    }
    override fun getCurrentPoseType(): PoseType {
        return PoseType.NONE
    }

    override fun canUsePortal(allowVehicles: Boolean) = false

    override fun getAddEntityPacket(entityTrackerEntry: ServerEntity): Packet<ClientGamePacketListener> =
        ClientboundCustomPayloadPacket(
            SpawnPokeballPacket(
                this.pokeBall,
                this.aspects,
                super.getAddEntityPacket(entityTrackerEntry) as ClientboundAddEntityPacket
            )
        ) as Packet<ClientGamePacketListener>

    override fun waterDrag(): Float = this.pokeBall.waterDragValue

}