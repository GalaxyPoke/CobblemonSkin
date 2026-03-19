# Task Configuration

A task configuration is responsible for producing one or multiple tasks as part of a
[Behaviour Configuration](../README.md). Tasks are the smallest unit of behaviour in a brain and are executed
as part of an activity.

A task configuration can be a simple string that represents the type of task to create, or a JSON object
which a `type` property that describes what type of task is being created and other properties that configure it.

### one_of
The 'one_of' type creates a task that randomly selects one of the tasks in the list to execute each tick.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `options`: A list of objects representing the different tasks that could run for a tick. Each object must have a `task`
    which is a task configuration. Optionally, you can also specify a `weight` which is a number that determines the 
    probability of the task being selected. If no weight is specified, the task will have a weight of 1.

### all_of
The 'all_of' type creates a task that executes all of the tasks in the list each tick.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `tasks`: A list of task configurations to execute each tick.

### do_nothing
The 'do_nothing' type creates a task that does nothing for some period of time.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `minDurationTicks`: A MoLang expression that determines the minimum number of ticks to do nothing. Defaults to 40. Has `q.entity` as the entity.
- `maxDurationTicks`: A MoLang expression that determines the maximum number of ticks to do nothing. Defaults to 80. Has `q.entity` as the entity.

### random
The 'random' type creates a task that chooses one of the contained tasks when the entity is spawned. Note that this does not save forever,
so if the entity is reloaded, it will choose a new task. To make persisting randomization you have to use MoLang conditions on other task
configurations that use `q.entity.id_modulo(some_number)` to apply randomization based on the entity UUID.
For example, `q.entity.id_modulo(2)` will return 0 or 1 for an entity but will always be the same for the same entity,
so using `q.entity.id_modulo(2) == 0` as a task condition will make it occur for 50% of entities and remain after reloads.
`q.entity.id_modulo(3)` will return 0 or 1 or 2, and so on.

### follow_walk_target
The 'follow_walk_target' type creates a task that makes the entity follow a walk target. This does not choose the walk target,
it is only responsible for ensuring the entity will follow a path to whatever the current walk target is. Note that the target
does not necessarily need to be a land target.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `minRunTicks`: A MoLang expression that determines the minimum number of ticks to run to the target before stopping. Defaults to 150.
- `maxRunTicks`: A MoLang expression that determines the maximum number of ticks to run to the target before giving up. Defaults to 250.

### look_at_target
The 'look_at_target' type creates a task that makes the entity look at a target. This does not choose the target, it is only
responsible for ensuring the entity will look at the target position.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `minDurationTicks`: A MoLang expression that determines the minimum number of ticks that the entity will look at the target. Defaults to 40.
- `maxDurationTicks`: A MoLang expression that determines the maximum number of ticks that the entity will look at the target. Defaults to 80.

### stay_afloat
The 'stay_afloat' type creates a task that makes the entity stay afloat in water instead of quietly sinking and drowning.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `chance`: A MoLang expression that determines the chance of the entity trying to stay afloat. The higher it is, the more desperately it stays above the water. Defaults to 0.01. You don't need to change this, honestly.

### wander
The 'wander' type creates a task that makes the entity wander around randomly.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `shouldWander`: A MoLang expression that determines if the entity should wander. Defaults to `math.random(0, 20*8) <= 1`, which makes it wander approximately once every 8 seconds.
- `horizontalRange`: A MoLang expression that determines the maximum distance the entity will wander in a single path. Defaults to 10.
- `verticalRange`: A MoLang expression that determines the maximum vertical distance the entity will wander from its starting position. Defaults to 5.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for wandering. Defaults to 0.35 which is a standard walk speed.
- `completionRange`: A MoLang expression that determines the distance from the target location that the entity needs to be for the path to be considered complete. Defaults to 1.
- `lookTargetYOffset`: A MoLang expression that determines the vertical offset of the look target from the target walk position. Defaults to 1.5. You probably don't need to change this.

### look_at_entities
The 'look_at_entities' type creates a task that will randomly set the entity's look target to a nearby entity.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `maxDistance`: A MoLang expression that determines the maximum distance for an entity to be from this entity and be looked at.

### go_to_healing_machine
The 'go_to_healing_machine' type creates a task that makes the entity go to a nearby healing machine when they have a static NPC party that could be healed.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `horizontalSearchRange`: A MoLang expression that determines the maximum distance to search for a healing machine. Defaults to 10.
- `verticalSearchRange`: A MoLang expression that determines the maximum vertical distance to search for a healing machine. Defaults to 5.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for walking to the healing machine. Defaults to 0.35 which is a standard walk speed.
- `completionRange`: A MoLang expression that determines the distance from the target location that the entity needs to be for the path to be considered complete. Defaults to 1.

### heal_using_healing_machine
The 'heal_using_healing_machine' type creates a task that makes the entity heal its static NPC party using a healing machine if one is right next to the NPC.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `horizontalUseRange`: A MoLang expression that determines the maximum distance to a healing machine from which the NPC can use it. Defaults to 3.
- `verticalUseRange`: A MoLang expression that determines the maximum vertical distance to a healing machine from which the NPC can use it. Defaults to 2.

### switch_npc_to_battle
The 'switch_npc_to_battle' type creates a task that will switch NPC entities to the battle activity when it is in a battle. You'd generally put this 
on the idle activity so that it automatically transitions activities when a battle starts.

### switch_npc_from_battle
The 'switch_npc_from_battle' type creates a task that will switch NPC entities from the battle activity when it is no longer in a battle. You should
put this in the battle activity so that it automatically transitions activities when a battle ends. If the NPC is in multiple battles, this
will only activate when no battles remain. It will rely on the brain schedule to decide what activity to switch back to (usually idle).

### look_at_battling_pokemon
The 'look_at_battling_pokemon' type creates a task that will set the entity's look target to switch between the battling Pokémon.
- `minDurationTicks`: A MoLang expression that determines the minimum number of ticks that the entity will look at a specific battling Pokémon. Defaults to 40.
- `maxDurationTicks`: A MoLang expression that determines the maximum number of ticks that the entity will look at a specific battling Pokémon. Defaults to 80.

### exit_battle_when_hurt
The 'exit_battle_when_hurt' type creates a task that will make the entity exit the battle when it is hurt.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `includePassiveDamage`: A boolean that determines if passive damage (like poison or cacti) should trigger this task. Defaults to true.

### switch_to_chatting
The 'switch_to_chatting' type creates a task that will switch the entity to the chatting activity when a dialogue has been opened with that NPC.

### switch_from_chatting
The 'switch_from_chatting' type creates a task that will switch the entity from the chatting activity when the dialogue has been closed with that NPC.
It will rely on the brain schedule to decide what activity to switch back to (usually idle).

### look_at_speaker
The 'look_at_speaker' type creates a task that will set the entity's look target to the speaker of the dialogue.

### switch_to_action_effect
The 'switch_to_action_effect' type creates a task that will switch the entity to the action effect activity when it is performing an action effect.

### switch_from_action_effect
The 'switch_from_action_effect' type creates a task that will switch the entity from the action effect activity when it is no longer performing an action effect.
It will rely on the brain schedule to decide what activity to switch back to (usually idle).

### switch_to_fight
The 'switch_to_fight' type creates a task that will switch the entity to the fight activity when it has an attack target.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `activity`: The activity to switch to when the NPC is in a fight. Defaults to `minecraft:fight`.

### switch_from_fight
The 'switch_from_fight' type creates a task that will switch the entity from the fight activity when it no longer has an attack target. It will rely on the brain schedule to decide what activity to switch back to (usually idle).

### get_angry_at_attacker
The 'get_angry_at_attacker' type creates a task that will make the entity get angry at the attacker when it is attacked.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.

### stop_being_angry_if_attacker_dead
The 'stop_being_angry_if_attacker_dead' type creates a task that will make the entity stop being angry at the attacker when the attacker is dead.

### attack_angry_at
The 'attack_angry_at' type creates a task that will make the entity choose the entity that it is angry at to be its attack target.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.

### move_to_attack_target
The 'move_to_attack_target' type creates a task that will make the entity move to its attack target.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for moving to the attack target. Defaults to 0.5 which is an elevated speed to usual.
- `closeEnoughDistance`: A MoLang expression that determines the distance from the attack target that the entity needs to be for the path to be considered complete. Defaults to 1.

### melee_attack
The 'melee_attack' type creates a task that will make the entity perform a melee attack on its attack target when it is close enough. The damage dealt is determined by entity properties, which cannot be easily configured at time of writing.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `range`: A MoLang expression that determines the maximum distance from the attack target that the entity can be to perform the attack. Defaults to 1.5.
- `cooldownTicks`: A MoLang expression that determines the number of ticks to wait before performing another attack. Defaults to 30.

### switch_to_panic_when_hurt
The 'switch_to_panic_when_hurt' type creates a task that will switch the entity to the panic activity when it is hurt.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `includePassiveDamage`: A boolean that determines if passive damage (like poison or cacti) should trigger this task. Defaults to false.

### switch_to_panic_when_hostiles_nearby
The 'switch_to_panic_when_hostiles_nearby' type creates a task that will switch the entity to the panic activity when hostiles are nearby. The definition of nearby depends
on the entity type and this piggy-backs off the Minecraft list of aggressive mobs and the right distances.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.

### calm_down
The 'calm_down' type creates a task that will make the entity calm down when it is no longer being hurt and is far from hostiles and its attacker.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.

### walk_away_from_avoid_target
The 'flee_attacker' type creates a task that will make the entity flee from its avoid target.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for fleeing from the avoid target. Defaults to 0.5 which is an elevated speed to usual.
- `desiredDistance`: A MoLang expression that determines the distance from the target that the entity should try to maintain. Defaults to 9.

### flee_nearest_hostile
The 'flee_nearest_hostile' type creates a task that will make the entity flee from the nearest hostile entity when it is hurt.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if this entire task should exist on the entity or not. This runs at the time of entity creation, not on tick.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for fleeing from the nearest hostile. Defaults to 0.5 which is an elevated speed to usual.
- `desiredDistance`: A MoLang expression that determines the distance from the nearest hostile that the entity should try to maintain. Defaults to 9.

### run_script
The 'run_script' type creates a task that will run a MoLang script every tick on this entity. This has the risk of being very laggy
if the script is doing expensive things. Remember to use `q.entity.world.game_time` and `math.mod` to do intermittent actions inside
the script to avoid lagging the game.
- `script`: A resource location identifying the script to run. This should be in the `molang` datapack folder.

### look_in_direction
The 'look_in_direction' type creates a task that will make the entity look in a specific direction at all times where they aren't being told to look elsewhere.
- `yaw`: A MoLang expression that determines the yaw angle (degrees) of the entity. Defaults to `0`.
- `pitch`: A MoLang expression that determines the pitch angle (degrees) of the entity. Defaults to `0`.

### move_to_owner
The 'move_to_owner' type creates a task that will make the entity move to its owner. This will only work for entities that
can be owned, like Pokémon.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if the entity should move to its owner this tick. Defaults to true.
- `completionRange`: A MoLang expression that determines the distance from the owner that the entity needs to be for the path to be considered complete. Defaults to 4.
- `speedMultiplier`: A MoLang expression that determines the speed multiplier for moving to the owner. Defaults to 0.4 which is slightly above the normal walk speed.
- `teleportDistance`: A MoLang expression that determines the distance from the owner the entity must be before it resorts to teleporting. A value of -1 will cause it to never teleport. Defaults to 24.
- `maxDistance`: A MoLang expression that determines the maximum distance from the owner that the entity can be to move to them. Defaults to 14.

### point_to_spawn
The 'point_to_spawn' type creates a task that will make the entity point to the spawn point of the world whenever they
are not moving and are idling. This is basically only here for Nosepass.