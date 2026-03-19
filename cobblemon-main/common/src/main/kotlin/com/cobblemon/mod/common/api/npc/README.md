# Cobbled NPCs

NPC's are AI-driven entities that are built on the same identifier-aspects rendering platform
as Cobblemon's Pokémon entities. Every NPC in the world is based on an [NPC class](#npc-classes).

## NPC Classes

An NPC class is a datapacked JSON (inside the <code>npcs</code> folder) that defines many properties of an NPC in a way that's reusable
across any number of individual NPC entities. The possible properties are explained in [NPC Properties](#npc-properties)

## NPC Class Presets

For many properties or sets of properties of an NPC class, there may be logical grouping of settings.
As an example, many types of trainer NPC classes could share the logic of challenging nearby players
that they are able to see. This could become tedious if the same AI script and range config needed to
be defined on every NPC class. Not only is it tedious, but if there was some tweak that was made in the
underlying logic of that AI, it would need to be updated in every single NPC class JSON that uses it.

The solution to this is NPC Class Presets, a datapack folder (<code>npc_presets</code>) with JSON files that have an almost identical
format to [NPC Classes](#npc-classes), but with every property being nullable. An NPC class can reference
any number of presets and the properties of those presets will be merged into the NPC class as it loads.

## NPC Properties
    {
      "hitbox": "player",
      "resourceIdentifier": "cobblemon:bug_catcher",
      "presets": [
        "cobblemon:npc-standard"
      ],
      "variations": {
        "dirt": [
          "clean",
          "slightly_dirty",
          "dirty",
          "filthy"
        ],
        "net": {
          "type": "weighted",
          "options": [
            {
              "aspect": "green-net",
              "weight": 10
            },
            {
              "aspect": "blue-net",
              "weight": 5
            },
            {
              "aspect": "red-net",
              "weight": 1
            }
          ]
        }
      },
      "config": [
        {
          "variableName": "challenge_distance",
          "displayName": "cobblemon.npc.challenge_distance",
          "description": "cobblemon.npc.challenge_distance.description",
          "type": "NUMBER",
          "defaultValue": "5"
        }
      ],
      "names": [
        "Red",
        "Green",
        "Blue",
        "Yellow"
      ],
      "interaction": {
        "type": "dialogue",
        "dialogue": "cobblemon:npc-example"
      },
      "battleConfiguration": {
        "canChallenge": true
      },
      "canDespawn": false,
      "skill": 5,
      "autoHealParty": true,
      "randomizePartyOrder": true,
      "hideNameTag": false,
      "party": {
        "type": "simple",
        "pokemon": [
          "spiritomb level=1 moves=splash,firepunch,flamethrower,swift",
          "porygonz level=1 speed_ev=252",
          "togekiss level=1 nature=timid",
          "lucario level=1 held_item=cobblemon:focus_sash",
          "milotic level=1 ability=marvelscale ",
          "garchomp level=1 hp_iv=31 attack_iv=31"
        ]
      }
    }

### hitbox
The hitbox of the NPC, relevant to combat and pathing. This can be defined as an object 
or as a shortcut string that is predefined in the code. The only currently hardcoded
hitbox name is "player", which is the standard Steve Minecraft hitbox and is the same as
this fully defined hitbox:
    
    "hitbox": {
      "width": 0.6,
      "height": 1.8
    }

### resourceIdentifier
An optional property representing the 'identifier' that will be used for rendering the NPC.
In a way, this is the same as the "species" of a Pokémon. It is this value combined with
whatever aspects have been defined for the NPC that completely handles the appearance of the
NPC. This identifier is referenced in the variation JSONs on the client.

If left blank, it will fallback to the identifier used for the NPC class itself. The reason why
this is useful is that it allows several NPC classes to appear the same despite having differences
in AI, parties, interactions, etc.

### presets
A list of strings representing the identifiers of [NPC Class Presets](#npc-class-presets) that form the foundation of
this NPC class. Any property defined in this class will override the same property in the presets.

### variations
A map of variations that generate the aspects that control the appearance of the NPC. The keys of this
map should represent the kind of variation that is being defined, and the values are some kind of [NPC Variation](#npc-variation).

Variations can be defined in presets and flow through to the NPC class, but they can also be defined
in the NPC class. If the same name is used in both a preset and an NPC class, that which is on the NPC
class will override that from the preset. If the variation names are different, they will be merged together.

#### NPC Variation
An NPC variation is some way in which aspects can be generated for the NPC. The types of variation can
be added using the API, but the type most anticipated is the weighted choice aspects.

This can be defined as a list of strings, in which case the aspect will be chosen randomly from the list
with uniform distribution.

    "dirt": [
      "clean",
      "slightly_dirty",
      "dirty",
      "filthy"
    ]

The weighted variation can also be defined more fully, which is necessary if you want some options to be
more likely than others.

    "net": {
      "type": "weighted",
      "options": [
        {
          "aspect": "green-net",
          "weight": 10
        },
        {
          "aspect": "blue-net",
          "weight": 5
        },
        {
          "aspect": "red-net",
          "weight": 1
        }
      ]
    }

In this example, the "net" value will be "green-net" most of the time, 10 times out of 16. The "blue-net"
aspect will be chosen 5 times out of 16, and the "red-net" aspect will be chosen 1 time out of 16. 

When using a type of NPC variation that is not "weighted", the value of "type" must specify it and the
other values (instead of "options") will be in whatever format is dictated by that type.

### config
A list of NPC config values that are predefined as being necessary for this class. An NPC config 
variable's value is used in the MoLang environment of the NPC and can be accessed in any MoLang 
environment that has the NPC. 

This class property is a way to present clear options to the user if configuring an NPC in-game using
the NPC Edit command. This does not mean that NPC variables can only be created when they were defined
in the class, but it makes it easier for the user to know what variables should be set and will set
the initial values when an NPC is initialized.

An example situation of using this is if there is an NPC class preset that defines an AI script for
challenging a player when they come within a certain range. Inside that class preset, the AI script may
need to know at what distance the player will be engaged. This can be defined in the NPC class preset as
a config variable, and therefore any NPC class that relies on this preset will have that config variable
and present it to users when they set up an NPC of that class.

    "config": [
      {
        "variableName": "challenge_distance",
        "displayName": "cobblemon.npc.challenge_distance.name",
        "description": "cobblemon.npc.challenge_distance.desc",
        "type": "NUMBER",
        "defaultValue": "5"
      }
    ]

The "type" of variable is one of: `STRING`, `NUMBER`, or `BOOLEAN`. The type is used to preset a more
appropriate input field for the user when they are setting up the NPC.

### names
A list of strings that represent the names that the NPC can have. The name of the NPC is chosen randomly
when initialized. These can either be simple names or translation keys so provide localization support.

### interaction
An object that defines how the NPC will handle being interacted with by players. The type of interaction
is defined by the "type" property, and different types of interaction have different properties.

#### dialogue
The "dialogue" interaction type is a simple way to have an NPC say something when interacted with. The
"dialogue" property should be the identifier of a datapacked dialogue JSON file. The dialogue that is
created will have `c.npc` as a contextual property in any script that occurs as part of that dialogue,
allowing the dialogue to work with the NPC (including the NPC config values, such as 
`c.npc.config.challenge_distance`).

    "interaction": {
      "type": "dialogue",
      "dialogue": "cobblemon:npc-example"
    }

#### script
The "script" interaction type is a way to have an NPC run a predefined script when interacted with. The
script field is an identifier of a datapacked MoLang script file (in the `scripts` folder).

    "interaction": {
      "type": "script", 
      "script": "cobblemon:npc-example"
    }

#### custom_script
The "custom_script" interaction type is a way to have an NPC run the MoLang script defined specifically
in this file. The script property can be a single string or an array of strings to do multiline MoLang scripts.

    "interaction": {
      "type": "custom_script",
      "script": "c.player.tell('My name is ' + c.npc.name);"
    }

### battleConfiguration
I don't really know whether this is going to stay tbh

### canDespawn
Whether or not regular entity despawning logic applies. By default, NPCs will despawn over time. If the value
of this property is set to false, the NPC will never despawn over time.

### skill
The skill level when this NPC is used in battle. This is a number from 1 to 5, with 5 being most difficult
to beat. This does not control their Pokémon's levels or moves, but it does control how well they use them.

### party
A party provider which generates the party of Pokémon that this NPC will have when in battle. The type of 
party is defined by the "type", and different types of party have different properties. An NPC party is
intended to be initialized when an NPC is spawned and uses a 'seed level'. When spawned using the world
spawning system, it will use a seed level that is based on the player's party's average level. When
spawned using the NPC command, the seed level can be defined in the command and is otherwise set to 1.

A party provider produces either 'static' or 'dynamic' parties. A static party is one that is generated
when the NPC is initialized then does not change. A dynamic party is one that may be configured using the
seed level but can be different any time the NPC is challenged.

A dynamic party prevents the NPC from doing several actions, such as using healing machines, having
persistent health of their party that changes between battles, and keeping their Pokémon out of their
balls. This is because a NPC's dynamic party is not stored in the world and is generated every time
they are challenged.

#### simple
The "simple" party type is a way to define a party of Pokémon in a simple list format. Each string in the
list should be a set of Pokémon properties, as used in commands and spawn files.

This party provider type can be static or dynamic depending on the value of `isStatic`. Any level that 
is not mentioned in the Pokémon properties will be set to the seed level.

    "party": {
      "type": "simple",
      "isStatic": true,
      "pokemon": [
        "spiritomb moves=splash,firepunch,flamethrower,swift",
        "porygonz speed_ev=252",
        "togekiss nature=timid",
        "lucario held_item=cobblemon:focus_sash",
        "milotic ability=marvelscale ",
        "garchomp level=100 hp_iv=31 attack_iv=31"
      ]
    }

#### pool
The "pool" party type is a way to define a party of Pokémon using a more complicated format that involves some
randomization and weighting to different Pokémon options.

A pool party provider can be static or dynamic depending on the value of `isStatic`. If `useFixedRandom` is true, the Pokémon it selects
from the pool will be constant for a single spawned NPC entity. This is slightly different to a static party because
the level of the Pokémon can still be modified dynamically using MoLang values inside each entry's `level` field. 

The pool specifies the minimum and maximum number of Pokémon that can be in the party using `minPokemon` and `maxPokemon`.

The `pool` property dictates all the possible Pokémon with each being an object with several properties.

- The `pokemon` property is the Pokémon properties as used in commands and spawn files that will generate the Pokémon. 
- The `weight` property is a decimal number that represents how likely that Pokémon is to be chosen. If the weight is -1, it will be guaranteed to be selected. Defaults to 1. The larger the value, the more likely it will be chosen.
- The `selectableTimes` property is the maximum number of times that the entry can be selected in the party. Defaults to 1.
- The `npcLevels` property is a range of seed levels that the Pokémon can be generated at. For example, if the NPC is spawned with seed level 10, and the range is 5-9, the Pokémon will not be selected. The actual level of the Pokémon will be the seed level. Defaults to "1-100".
- The `levelVariation` property is the amount of variation the Pokémon's level can have from the NPC seed level. For example, a value of 2 will mean that for an NPC generated with seed level 10, the Pokémon will be generated somewhere between level 8 and 12 inclusive. Defaults to zero.
- The `level` property can be used instead of `levelVariation` to force a specific level. This is a MoLang expression.

For `minPokemon`, `maxPokemon`, `weight`, `selectableTimes`, and `levelVariation`, the values can be a simple number or be full MoLang expressions. At the time
of seeding an NPC, the cosmetic variations have already been applied. Therefore, it's possible to make these properties
depend on visual aspects of the NPC. For example, if the NPC has a variation for "dirt", you could set a
certain Pokémon to be `"weight": "q.npc.has_aspect('dirty') ? -1 : 0"` which would make it a guaranteed party Pokémon for "dirty" NPCs
and impossible for all other variations of the NPC.

The MoLang environment used for the pool party provider contains q.level for the seed level, q.npc for the NPC, as well as q.players for
an array of all players that are challenging the NPC. The players list is only present for dynamic parties. If it is specifically
a single battle challenge and this is a dynamic party, there will also be a `q.player`, for convenience.

      "party": {
        "type": "pool",
        "minPokemon": "3",
        "maxPokemon": "6",
        "isStatic": false,
        "useFixedRandom": true,
        "pool": [
          {
            "pokemon": "weedle",
            "weight": "10",
            "selectableTimes": "2",
            "levelVariation": "2",
            "npcLevels": "1-8"
          },
          {
            "pokemon": "caterpie",
            "weight": "5",
            "npcLevels": "1-8",
            "selectableTimes": "2"
          },
          {
            "pokemon": "kakuna",
            "weight": "7",
            "selectableTimes": "2",
            "npcLevels": "8-10"
          },
          {
            "pokemon": "metapod",
            "weight": "7",
            "npcLevels": "8-10",
            "selectableTimes": "2"
          },
          {
            "pokemon": "beedrill",
            "weight": "5",
            "npcLevels": "10-15",
            "selectableTimes": "1"
          },
          {
            "pokemon": "butterfree",
            "weight": "5",
            "npcLevels": "10-15",
            "selectableTimes": "1",
            "level": "q.player.party.highest_level"

          }
        ]
      }

#### script
The "script" party type is a way to define a party of Pokémon using a MoLang script. The script can modify a q.party struct,
primarily using function calls such as `q.party.add_by_properties('pikachu shiny)`.

There is an `isStatic` property to control whether the party is static or dynamic. There is a `script` property which is a
resource location pointing to the script that should be located in the `molang` datapack folder.

The MoLang environment used for the script party provider contains q.level for the seed level, q.npc for the NPC, as well as q.players for
an array of all players that are challenging the NPC. The players list is only present for dynamic parties. If it is specifically
a single battle challenge and this is a dynamic party, there will also be a `q.player`, for convenience.

        "party": {
            "type": "script",
            "isStatic": true,
            "script": "cobblemon:npc_party"
        }

An example script might be:

        t.use_butterfree = math.random_integer(1, 2) == 1;
        t.use_butterfree ? {
            q.party.add_by_properties('butterfree level=' + (q.level + 3));
        } : {
            q.party.add_by_properties('beedrill level=' + (q.level + 3));
        };
        q.party.add_by_properties('pidgey');

### autoHealParty
Whether or not the NPC's party will be healed between battles. If the NPC has a dynamic party, this option will
do nothing. If the NPC has a static party and this option is false, their Pokémon will not be healed and the
NPC will have to use healing machines to heal them if they have that enabled in their AI.

### isMovable
Controls whether the NPC can be moved by players or other entities. If disabled, the NPC will remain 
stationary.

### isInvulnerable
Controls whether the NPC is immune to all damage sources.

### isLeashable
Controls whether players can attach a lead to a NPC. If disabled, players will be unable to leash 
the NPC and move them using leads.

### allowProjectileHits
Controls whether the NPC can be hit by projectiles. Disabling this option prevents unwanted 
interactions, such as players moving the NPC with fishing rods or other projectiles.

### randomizePartyOrder
Whether the NPC's party order will be randomized between battles. When true, this NPC's party (regardless of
whether it was dynamic or static) will choose its initial send-out Pokémon randomly instead of always choosing the first
Pokémon in the party.

### behaviours
The behaviours (AKA ai, behaviors) property is an array of behaviour configurations. This is used to configure the behaviours of the NPC. See
the documentation for brain configurations at [Behaviour Configuration](../ai/config/README.md).

### hideNameTag
Whether the NPC's nametag should be hidden or not. Defaults to false.