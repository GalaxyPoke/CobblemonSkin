/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.particle

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.snowstorm.AttachedType
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.api.snowstorm.ParticleEmitterAction
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.particle.SnowstormParticleOptions
import com.cobblemon.mod.common.util.math.geometry.transformDirection
import com.mojang.blaze3d.vertex.PoseStack
import kotlin.random.Random
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.NoRenderParticle
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * An instance of a bedrock particle effect.
 *
 * @author Hiroku
 * @since January 2nd, 2022
 */
class ParticleStorm(
    val effect: BedrockParticleOptions,
    val emitterSpaceMatrix: MatrixWrapper,
    val attachedMatrix: MatrixWrapper, // Can be the same as emitterSpaceMatrix
    val world: ClientLevel,
    val sourceVelocity: () -> Vec3 = { Vec3.ZERO },
    val sourceAlive: () -> Boolean = { true },
    val sourceVisible: () -> Boolean = { true },
    val targetPos: (() -> Vec3)? = null,
    val onDespawn: () -> Unit = {},
    val getParticleColor: () -> Vector4f? = { null },
    val runtime: MoLangRuntime = MoLangRuntime(),
    val entity: Entity? = null,
): NoRenderParticle(world, emitterSpaceMatrix.getOrigin().x, emitterSpaceMatrix.getOrigin().y, emitterSpaceMatrix.getOrigin().z) {
    fun spawn() {
        if (entity != null) {
            runtime.environment.query
                .addFunction("entity_width") { DoubleValue(entity.boundingBox.xsize) }
                .addFunction("entity_height") { DoubleValue(entity.boundingBox.ysize) }
                .addFunction("entity_size") { DoubleValue(entity.boundingBox.run { if (xsize > ysize) xsize else ysize }) }
                .addFunction("entity_radius") { DoubleValue(entity.boundingBox.run { if (xsize > ysize) xsize else ysize } / 2) }
                .addFunction("entity_scale") {
                    val pokeEntity = entity as? PokemonEntity
                    val pokemon = pokeEntity?.pokemon
                    //Use form data if available, species as fall back
                    val baseScale = pokemon?.form?.baseScale ?: pokemon?.species?.baseScale ?: 1.0F
                    val pokemonScale = pokemon?.scaleModifier ?: 1.0F
                    val entityScale = pokeEntity?.scale ?: 1.0F
                    DoubleValue(baseScale * pokemonScale * entityScale)
                }
            if (entity is PosableEntity) {
                runtime.environment.query.addFunction("entity") { entity.struct }
                if (targetPos != null) {
                    addAttackingFunctions(targetPos)
                }
            } else if (entity is Player) {
                runtime.environment.query.addFunction("entity") { entity.asMoLangValue() }
            }
            // TODO replace with a generified call to if (entity is MoLangEntity) entity.applyVariables(env) or w/e
            runtime.environment.setSimpleVariable("entity_width", DoubleValue(entity.boundingBox.xsize))
            runtime.environment.setSimpleVariable("entity_height", DoubleValue(entity.boundingBox.ysize))
            val longerDiameter = entity.boundingBox.run { if (xsize > ysize) xsize else ysize }
            runtime.environment.setSimpleVariable("entity_size", DoubleValue(longerDiameter))
            runtime.environment.setSimpleVariable("entity_radius", DoubleValue(longerDiameter / 2))
            runtime.environment.setSimpleVariable("entity_scale", DoubleValue((entity as? PokemonEntity)?.scale ?: 1.0))
        }
        Minecraft.getInstance().particleEngine.add(this)
    }

    fun addAttackingFunctions(destinationPosSupplier: (() -> Vec3)) {
        runtime.environment.query.addFunction("target_deltax") { params ->
            DoubleValue(distanceTo(destinationPosSupplier.invoke()).x)
        }
        runtime.environment.query.addFunction("target_deltay") { params ->
            DoubleValue(distanceTo(destinationPosSupplier.invoke()).y * -1)
        }
        runtime.environment.query.addFunction("target_deltaz") { params ->
            DoubleValue(distanceTo(destinationPosSupplier.invoke()).z)
        }
        runtime.environment.query.addFunction("target_distance") { params ->
            DoubleValue(distanceTo(destinationPosSupplier.invoke()).length())
        }

        runtime.environment.setSimpleVariable("target_deltax",  DoubleValue(distanceTo(destinationPosSupplier.invoke()).x))
        runtime.environment.setSimpleVariable("target_deltay",  DoubleValue(distanceTo(destinationPosSupplier.invoke()).y * -1))
        runtime.environment.setSimpleVariable("target_deltaz",  DoubleValue(distanceTo(destinationPosSupplier.invoke()).z))
        runtime.environment.setSimpleVariable("target_distance",  DoubleValue(distanceTo(destinationPosSupplier.invoke()).length()))
    }

    //These are variables that need to be different per particle instance
    fun lockParticleVars(struct: VariableStruct) {
        val destinationPos = targetPos?.invoke() ?: return
        struct.setDirectly(
            "target_deltax", DoubleValue(distanceTo(destinationPos).x)
        )
        struct.setDirectly(
            "target_deltay", DoubleValue(distanceTo(destinationPos).y * -1)
        )
        struct.setDirectly(
            "target_deltaz", DoubleValue(distanceTo(destinationPos).z),
        )
        struct.setDirectly(
            "target_distance", DoubleValue(distanceTo(destinationPos).length()),
        )
    }

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    fun getPrevX() = xo
    fun getPrevY() = yo
    fun getPrevZ() = zo

    val particles = mutableListOf<SnowstormParticle>()
    var started = false
    var stopped = false
    var despawned = false
    // The idea is that some instantaneous particle effects could teeeechnically be over before they start.
    var hasPlayedOnce = false

    var distanceTravelled = 0F

    companion object {
        var contextStorm: ParticleStorm? = null

        fun createAtPosition(world: ClientLevel, effect: BedrockParticleOptions, position: Vec3): ParticleStorm {
            val wrapper = MatrixWrapper()
            val matrix = PoseStack()
            matrix.translate(position.x, position.y, position.z)
            wrapper.updateMatrix(matrix.last().pose())
            return ParticleStorm(effect, wrapper, wrapper, world)
        }

        /**
         * Creates multiple potentially, because in the case of posable entities if multiple locators match, it repeats the effect.
         */
        fun createAtEntity(world: ClientLevel, effect: BedrockParticleOptions, entity: LivingEntity, locator: Collection<String> = emptySet()): List<ParticleStorm> {
            if (entity is PosableEntity) {
                val state = entity.delegate as PosableState
                val locators = locator.firstNotNullOfOrNull { state.getMatchingLocators(it).takeIf { it.isNotEmpty() } } ?: return emptyList()
                val matrixWrappers = locators.mapNotNull { state.locatorStates[it] }
                return matrixWrappers.map { matrixWrapper ->
                    val particleRuntime = MoLangRuntime().setup().setupClient()
                    particleRuntime.environment.query.addFunction("entity") { state.runtime.environment.query }
                    ParticleStorm(
                        effect = effect,
                        emitterSpaceMatrix = matrixWrapper,
                        attachedMatrix = matrixWrapper,
                        world = world,
                        runtime = particleRuntime,
                        sourceVelocity = { entity.deltaMovement },
                        sourceAlive = { !entity.isRemoved },
                        sourceVisible = { !entity.isInvisible },
                        entity = entity
                    )
                }
            } else {
                val matrixWrapper = MatrixWrapper()
                matrixWrapper.updateFunction = { it.updatePosition(entity.position()) }
                val particleRuntime = MoLangRuntime().setup().setupClient()
                particleRuntime.environment.query.addFunction("entity") { params -> MoLangFunctions.livingEntityFunctions.flatMap { it(entity).map { it.key to it.value } } }
                return listOf(
                    ParticleStorm(
                        effect = effect,
                        emitterSpaceMatrix = matrixWrapper,
                        attachedMatrix = matrixWrapper,
                        world = world,
                        runtime = particleRuntime,
                        sourceVelocity = { entity.deltaMovement },
                        sourceAlive = { !entity.isRemoved },
                        sourceVisible = { !entity.isInvisible },
                        entity = entity
                    )
                )
            }
        }
    }

    val particleEffect = SnowstormParticleOptions(effect)

    init {
        runtime.execute(effect.emitter.startExpressions)
        effect.emitter.creationEvents.forEach { it.trigger(this, null) }
    }

    override fun getLifetime(): Int {
        return if (stopped) 0 else Int.MAX_VALUE
    }

    override fun remove() {
        super.remove()
        if (!despawned) {
            effect.emitter.expirationEvents.forEach { it.trigger(this, null) }
            despawned = true
            onDespawn()
        }
    }

    override fun tick() {
        setLifetime(getLifetime())
        super.tick()

        if (!hasPlayedOnce) {
            age = 0
            hasPlayedOnce = true
        }

        if (!sourceAlive() && !stopped) {
            stopped = true
            remove()
        }

        if (!sourceVisible()) {
            this.setInvisible()
            return
        }

        if (stopped) {
            return
        }

        val pos = attachedMatrix.getOrigin()
        xo = x
        yo = y
        zo = z

        x = pos.x
        y = pos.y
        z = pos.z

        if (emitterSpaceMatrix != attachedMatrix){
            val attachmentOptions = effect.emitter.shape.getAttachmentOptions()
            //Keeps emitter attached to locator based on the settings
            if (attachmentOptions[AttachedType.POSITION] == true){
                emitterSpaceMatrix.updatePosition(attachedMatrix.getOrigin())
            }

            val setRotation = attachmentOptions[AttachedType.ROTATION] == true
            val setScale = attachmentOptions[AttachedType.SCALE] == true
            if (setRotation || setScale){
                val translation = emitterSpaceMatrix.matrix.getTranslation(Vector3f())
                val rotation = if (setRotation){
                    attachedMatrix.matrix.getRotation(AxisAngle4f())
                } else {
                    emitterSpaceMatrix.matrix.getRotation(AxisAngle4f())
                }
                val scale = if (setScale) {
                    attachedMatrix.matrix.getScale(Vector3f())
                } else {
                    emitterSpaceMatrix.matrix.getScale(Vector3f())
                }
                // If any value is zero we get NaNs, so default to a large enough value to be safe
                scale.x.coerceAtLeast(0.01f)
                scale.y.coerceAtLeast(0.01f)
                scale.z.coerceAtLeast(0.01f)

                emitterSpaceMatrix.matrix.identity()
                    .translate(translation)
                    .rotate(rotation)
                    .scale(scale)
            }
        }

        val oldDistanceTravelled = distanceTravelled
        distanceTravelled += Vec3(x - xo, y - yo, z - zo).length().toFloat()

        effect.emitter.travelDistanceEvents.check(this, null, oldDistanceTravelled.toDouble(), distanceTravelled.toDouble())
        effect.emitter.loopingTravelDistanceEvents.forEach { it.check(this, null, oldDistanceTravelled.toDouble(), distanceTravelled.toDouble()) }
        effect.emitter.eventTimeline.check(this, null, (age - 1) / 20.0, age / 20.0)

        runtime.environment.setSimpleVariable("emitter_random_1", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_2", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_3", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_4", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_age", DoubleValue(age / 20.0))
        runtime.execute(effect.emitter.updateExpressions)

        when (effect.emitter.lifetime.getAction(runtime, started, age / 20.0)) {
            ParticleEmitterAction.GO -> {
                effect.curves.forEach { it.apply(runtime) }
                val toEmit = effect.emitter.rate.getEmitCount(runtime, started, particles.size)
                started = true
                repeat(times = toEmit) { spawnParticle() }
            }
            ParticleEmitterAction.NOTHING -> {}
            ParticleEmitterAction.STOP -> stopped = true
            ParticleEmitterAction.RESET -> started = false
        }
    }

    fun getNextParticleSpawnPosition(): Vec3 {
        runtime.environment.setSimpleVariable("particle_random_1", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_2", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_3", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_4", DoubleValue(Random.Default.nextDouble()))

        val pos = effect.emitter.shape.getNewParticlePosition(runtime, entity)
        val newPosition = if (effect.emitter.shape.getAttachmentOptions()[AttachedType.SCALE] == true) {
            transformPosition(pos)
        } else {
            transformPositionWithoutScale(pos)
        }
        return newPosition
    }

    fun getNextParticleVelocity(nextParticlePosition: Vec3): Vec3 {
        val pos = effect.emitter.shape.getCenter(runtime, entity)
        val center = if (effect.emitter.shape.getAttachmentOptions()[AttachedType.SCALE] == true) {
            transformPosition(pos)
        } else {
            transformPositionWithoutScale(pos)
        }
        val initialVelocity = effect.particle.motion.getInitialVelocity(runtime, storm = this, particlePos = nextParticlePosition, emitterPos = center)
        return initialVelocity
            .scale(1 / 20.0)
            .add(if (effect.space.localVelocity) sourceVelocity() else Vec3.ZERO)
    }

    fun spawnParticle() {
        val newPosition = getNextParticleSpawnPosition()
        val velocity = getNextParticleVelocity(newPosition)

        contextStorm = this
        world.addParticle(particleEffect, newPosition.x, newPosition.y, newPosition.z, velocity.x, velocity.y, velocity.z)
        contextStorm = null
    }

    // Sometimes we need to transform without scale, IE just rotation and position.
    fun transformPositionWithoutScale(position: Vec3): Vec3 {
        val matrixWrapper = emitterSpaceMatrix.clone()
        val scale = matrixWrapper.matrix.getScale(Vector3f())
        matrixWrapper.matrix.scale(1 / scale.x, 1 / scale.y, 1 / scale.z)
        return matrixWrapper.transformPosition(position)
    }

    fun transformPosition(position: Vec3): Vec3 = emitterSpaceMatrix.transformPosition(position)

    fun transformDirection(direction: Vec3): Vec3 = emitterSpaceMatrix.matrix.transformDirection(direction)

    //Gets distance between emitter pos and destination pos in emitter space
    fun distanceTo(destinationPos: Vec3): Vec3 {
        return emitterSpaceMatrix.transformWorldToParticle(Vec3(x, y, z)).subtract(emitterSpaceMatrix.transformWorldToParticle(destinationPos))
    }

    fun setInvisible() {
        particles.forEach { particle -> particle.invisible = true }
    }
}
