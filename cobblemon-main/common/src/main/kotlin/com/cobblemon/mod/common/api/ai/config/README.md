# Behaviour Configuration

A "Brain" is a Java Edition AI concept for more modern entities. It has tasks that are listed inside 'activities',
and in some cases a schedule which determines which activities will occur during different times of the day. There is
also a set of core activities, and a default activity. In Minecraft, the most complex usage of the Brain AI system 
is on villagers which have schedules that control their behaviour during the day and night as well as child villager play behaviours.

Cobblemon's Pokémon and NPCs use the Brain AI system. As part of that, Cobblemon added the concept of a Behaviour Configuration
that allows Java Edition brains to be configured in a more data-driven way, as well as to support the configuration of
other logical properties of entities that are configured at the same initialization step.

By default, without any brain configuration, the default activity will be `minecraft:idle` and the core activities will be `[minecraft:core]`.
The activities would not have any tasks though.

## Possible Activities
The activities that can be used in brains is determined by code and cannot be added to using datapacks. Note that except for
when used as defaults and core activities, these are purely names and what you use them for is entirely up to you.

The activities that exist in Minecraft are:
- `minecraft:core`: The core activity intended to be used for fundamental actions like floating in water and looking at look targets.
- `minecraft:idle`: The idle activity is used when the entity is not doing anything else. This is typically used for things like wandering and other random actions.
- `minecraft:work`: The work activity is used for actions that are related to the entity's job. For villagers, this is things like farming, smithing, and other job-related tasks.
- `minecraft:play`
- `minecraft:rest`: The rest activity is used in villagers for the activity that moves them to a bed and hops into it.
- `minecraft:meet`: The meet activity is used in villagers for going to the meeting place and gossipping with other NPCs.
- `minecraft:panic`: The panic activity is used in villagers for when they are panicking and fleeing, usually from an attacker.
- `minecraft:raid`
- `minecraft:pre_raid`: The raid activity is used in villagers for what they do when a raid is about to start, like running to a safe location during a raid.
- `minecraft:hide`
- `minecraft:fight`: Intended to be used when engaging in combat.
- `minecraft:celebrate`
- `minecraft:admire_item`
- `minecraft:avoid`
- `minecraft:ride`
- `minecraft:play_dead`
- `minecraft:long_jump`
- `minecraft:ram`
- `minecraft:tongue`
- `minecraft:swim`
- `minecraft:lay_spawn`
- `minecraft:sniff`
- `minecraft:investigate`
- `minecraft:roar`
- `minecraft:emerge`
- `minecraft:dig`

The activities that were added by Cobblemon are:
- `cobblemon:battling`: For when the entity is in a Pokémon battle.
- `cobblemon:action_effect`: For when the entity is performing an action effect. Usually registered as an almost empty activity that simply prevents the entity from doing other things.
- `cobblemon:npc_chatting`: Intended for NPC entities using dialogue so that they look at their chat partner and do nothing else.

## Behaviour Configuration Format
A Behaviour is configured using a series of "Behaviour Configurations". Each configuration is a JSON object that contains
a "type" field which determines the action to perform when configuring an entity. Many different types are available.

### script
The `script` type executes a MoLang script which will apply some changes to the entity or brain.
- `script`: The MoLang script to execute. This will be a resource location for the `molang` datapack folder. Something like `cobblemon:some_script`.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if the script should be executed. Defaults to `"true"`.

### add_tasks_to_activity
The 'add_tasks_to_activity' type adds a list of tasks to an activity.
- `activity`: The activity to add the tasks to. This must be a pre-existing activity - a behaviour configuration cannot
    create new activities. 
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if the tasks should be added. If this
    is not present, the tasks will always be added.
- `tasksByPriority`: A map of task priorities to lists of tasks to add to the activity. Each task is a JSON object with a "type" field that determines the
    task to add or in simple cases can just be the type as a simple JSON primitive to use default properties. Many different types of task config are available. See [Task Configuration](./task/README.md).
    The priority of the task is used to determine the execution order each tick. This usually does not matter, but in some cases
    where the execution of one task will move to a different activity or otherwise prevent another task, the priority matters a lot.
    As an example, a task that moves to nearby healing machines will set a walk target which prevents the use of the healing machine,
    so the use task would need to have a lower priority or be earlier in the list so that it runs sooner.

Example, making sure `heal_using_healing_machine` comes before `go_to_healing_machine`:
```json
{
  "type": "add_tasks_to_activity",
  "activity": "minecraft:idle",
  "tasksByPriority": {
    "1": [
      {
        "type": "cobblemon:heal_using_healing_machine",
        "horizontalUseRange": "2"
      },
      "cobblemon:go_to_healing_machine"
    ]
  }
}
```

Note that when specifying tasks, you can either use an object format with the "type" property, or if you want to use only
default parameters for the task you can simply state the type as a string. The above example uses both formats.

### set_core_activities
The `set_core_activities` type sets the core activities of the brain. It has only one property, `activities`, which is a list of activities.

### set_default_activity
The `set_default_activity` type sets the default activity of the brain. It has only one property, `activity`, which is the activity to set as the default.

### apply_behaviours
The `apply_behaviours` type applies some number of other Behaviours to the entity. These behaviours are JSONs under the `behaviours` datapack folder. 
This is a way to apply a set of configurations to the entity.
- `behaviours`: A list of behaviour resource locations to apply. This is the name of the file in the `behaviours` folder without the `.json` extension.
- `condition`: A MoLang expression (with `q.entity` as the entity) that determines if the behaviour should be applied. Defaults to `"true"`.
- `visible`: Whether the behaviour should be visible in the UI. Defaults to `true`. If it is not visible, name and description can be left out.
- `name`: The name of the behaviour. This is used in the UI to label the behaviour. This can be a translation key.
- `description`: A description of the behaviour. This is used in the UI to describe what the behaviour does. This can be a translation key.
- `entityType`: The entity type that the behaviour applies to. Defaults to null. This is used to determine if the behaviour should be applied to the entity type..

Example behaviour (`uses_healing_machine.json`):
```json
{
  "name": "cobblemon.behaviour.uses_healing_machine.name",
  "description": "cobblemon.behaviour.uses_healing_machine.desc",
  "entityType": "cobblemon:npc",
  "visible": true,
  "configurations": [
    {
      "type": "add_tasks_to_activity",
      "activity": "minecraft:idle",
      "tasksByPriority": {
        "1": [
          "heal_using_healing_machine",
          "go_to_healing_machine"
        ]
      }
    }
  ]
}
```

### add_variables
The `add_variables` type adds some number of MoLang variables to the entity that can be configured in-game or left with defaults.
- `variables`: A list of objects, each representing a variable that can be configured.

Each variable has the following properties:
- `variableName`: The name of the variable. This is the name that will be accessible from MoLang scripts.
- `defaultValue`: The default value of the variable. This is a MoLang expression that will be used if the variable is not set.
- `displayName`: The display name of the variable. This is used in the UI to label the variable. This can be a translation key.
- `description`: A description of the variable. This is used in the UI to describe what the variable does. This can be a translation key.
- `type`: The type of the variable. This can be `TEXT`, `NUMBER`, or `BOOLEAN`.

Example:
```json
{
  "type": "add_variables",
  "variables": [
    {
      "variableName": "can_heal_using_machine",
      "defaultValue": "false",
      "displayName": "Can Use Healing Machine",
      "description": "Whether or not the entity can use a healing machine.",
      "type": "BOOLEAN"
    }
  ]
}
```

### set_variables
The `set_variables` type sets some values for pre-established variables. This is useful in cases where a preset has defined a variable
but it's locked in by a specific entity using that preset. This strongly forces the variable to a specific value. In future this
probably should deregister the variable so that the client cannot edit them once they've been forced, as currently that will
be possible yet the value will get repeatedly overwritten.

The values inside the variables map can be MoLang expressions (with `q.entity` as the entity) or simple JSON primitives.

Example:
```json
{
  "type": "set_variables",
  "variableValues": {
    "aggressive": true,
    "attack_range": "10"
  }
}
```

### script
The `script` type executes a registered MoLang script which will apply some changes to the brain or entity. This can be 
useful for complex variable configuration. `q.entity` will refer to the entity.

- `executeEarly`: If true, the script will be executed in the variable phase which occurs before tasks are added to the brain. Use this when your script might influence which tasks are put onto the brain.
- `script`: Either a resource location for the `molang` datapack folder or a MoLang expression that will be executed.
- `variables`: A list of variables that can be configured in the UI. Each variable has the same properties as in `add_variables`.

Example:
```json
{
  "type": "script",
  "executeEarly": true,
  "script": "cobblemon:configure_npc_party",
  "variables": [
    {
      "variableName": "party_size",
      "defaultValue": "3",
      "displayName": "Party Size",
      "description": "The number of Pokémon in the NPC's party.",
      "type": "NUMBER"
    }
  ]
}
```