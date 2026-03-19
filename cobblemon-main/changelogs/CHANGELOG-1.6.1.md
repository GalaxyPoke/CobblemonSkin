## [1.6.1 (January 26th, 2025)](#1-6-1)

### Additions
- Added crossover paintings from Close Combat: Premonition, Altar, Slumber, and Nomad.
- Added Galarica Nuts, used for crafting Galarica Cuffs and Wreaths. Dropped from certain Pokémon. Take a wild guess which.
- Added compatibility with Repurposed Structures. (Thank you, TelepathicGrunt!)
- Added an evolution method for Karrablast to evolve into Escavalier in singleplayer.
- Pokédexes can now be placed in Chiseled Bookshelves.
- Added optional box argument to the /pc command.
- Pokédex and Dialogue screens now close when the inventory keybind is pressed.
- Added config setting `maxPokedexScanningDetectionRange` to control from what distance the player can scan Pokémon using the Pokédex.
- Added config setting `hideUnimplementedPokemonInThePokedex` which hides unimplemented Pokémon from the Pokédex when set to true.
- Added debug renderer for posable entity locators.
- Added crossover paintings from Close Combat, Premonition, Altar, Slumber, and Nomad.
- Added optional box argument to the /pc command

### Changes
- Pokémon will now be dynamically revealed to the Pokédex as they're seen instead of revealing entire parties at the end of battle regardless.
- Unseen wild Pokémon will update their name from '???' to their real name as soon as a battle starts to reflect the battle UI showing the actual species name.
- Pokémon under the illusion effect will reveal their disguise to the Pokédex first and then the base Pokémon once the disguise is broken.
- Added more support for a variety of Fabric/NeoForge Convention tags.
- Reformatted some tags to be more consistent.
- Edited some recipes to utilize tags instead of direct item ids, for greater mod compatibility.
- Berries will drop if broken at age 0
- Improved Fortune drops on Mint Seeds
- New Slowpoke shiny texture.
- Updated drops for many Pokémon.
- Completely resynced Pokémon move and stat data based on later games. Learnsets have changed considerably to maximise available moves.
- Cobblemon save data now saves .old files where applicable as a means to recover from file corruption due to crashes or similar abrupt stops

### Pokémon Added

#### Gen 5
- Ducklett
- Swanna
- Shelmet
- Accelgor
- Karrablast
- Escavalier
- Rufflet
- Braviary
- Foongus
- Amoonguss

#### Gen 6
- Binacle
- Barbaracle

#### Gen 7
- Dewpider
- Araquanid
- Alolan Geodude
- Alolan Graveler
- Alolan Golem

#### Gen 8
- Galarian Slowpoke
- Galarian Slowbro
- Galarian Slowking

#### Gen 9
- Paldean Tauros

### Added cries to the following Pokémon
- All Nidorans
- Shellder, Cloyster
- Pinsir
- Tyrogue, Hitmontop
- Spinda

### Animation updates for the following Pokémon
- Primeape
- Munchlax
- Snorlax
- Poliwrath
- Goldeen
- Seaking
- Dondozo
- Wobbuffet
- Charcadet
- Armarouge
- Ceruledge
- Geodude
- Graveler
- Golem
- Sandile
- Krokorok
- Krookodile

### Model updates for the following Pokémon
- Slowpoke
- Slowbro
- Slowking
- Eiscue
- Tauros
- Goldeen
- Seaking
- Charcadet
- Armarouge
- Ceruledge
- Pinsir
- Geodude
- Graveler
- Golem
- Magnezone

### Cry updates for the following Pokémon
- Sceptile

### Changes
- Completely re-synced Pokémon move and stat data based on later games. Learnsets have changed considerably to maximise available moves.
- Pokémon will now be dynamically revealed to the Pokédex as they're seen in battle instead of revealing entire parties at the end of battle.
- Unseen wild Pokémon will update their name from '???' to their real name as soon as a battle starts to match how the battle UI shows the actual species name.
- Pokémon under the illusion effect will reveal their disguise to the Pokédex first and then the base Pokémon once the disguise is broken.
- Berries will now drop from berry trees if broken at age 0 so you aren't punished for mistaken planting.
- Increased Fortune drops on Mint Seeds.
- Updated Slowpoke's shiny texture.
- Updated drops for many Pokémon.
- Adjusted the evolution sound to match the timing of the particle effect.
- Made berry trees shear-able by dispenser blocks. I'm sure nobody will make unholy contraptions with this.
- Edited some recipes to utilize tags instead of direct item IDs, for better mod compatibility.
- Added more support for a variety of Fabric/NeoForge conventional tags.
- Reformatted some tags to be more consistent.

### Fixes
- Fixed Pokédex sometimes crashing when switching forms.
- Fixed Pokédex interface not transitioning out when closed.
- Fixed texture dimensions for the player and Pokémon interact interface.
- Fixed crash related to Tom's Simple Storage mod and the Fossil Machine.
- Fixed not being able to retrieve a fossil from the Fossil Machine with an empty hand.
- Fixed Pokémon being collidable (collidible? collissionable? kaleidoscopable?) while being captured by a Poké Ball.
- Fixed `full_party`, `own_zangoose_seviper`, `use_revive` and `use_candy` Advancement triggers.
- Fixed `healing_machine` Advancement by using the correct 1.21 trigger.
- Fix Display Cases not dropping items if destroyed through explosions.
- Fixed an issue where the first Pokémon in the pastured Pokémon list clipped into the interface.
- Fixed all Pokémon facing South on spawn.
- Fixed bait being consumed even when not reeling in any Pokémon.
- Fixed Miltank milk magically disappearing out of your bucket.
- Fixed Pokémon nicknames migrating from 1.5.2 not being displayed properly.
- Fixed capitalization in one of our config options. It was a very important fix. Very important. Old configs are fine.
- Fixed Poké Rods not working if Lure or Luck of the Sea enchantments get removed by other mods.
- Fixed crashes related to Pokémon when they are ready to evolve while holding an enchanted item. Very specific.
- Fixed a crash that sometimes occurred when evolving Nincada.
- Fixed Cobblemon plants not being compostable on NeoForge.
- Fixed hide UI (F1 key) not hiding the party overlay.
- Fixed NPC MoLang command `player_lose_command` not working.
- Fixed misaligned tooltips with edit boxes in the NPC editor screen.
- Fixed Pokémon riding two boats when attempting to deploy a platform on water.
- Fixed the summary screen showing there's experience to reach the next level when they are at the level cap.
- Fixed Pokémon forgetting moves when evolving on specific cases.
- Fixed Adorn compatibility, including improvements when using JEI/REI (Apricorn items now show up under the collapsed entries rather than standalone).
- Fixed error message appearing on battle log when using Solar Beam with Sunny Day.
- Fixed Pokémon Model offsets for larger species.
- Fixed `/pokedex grant all` command not giving male/female/shininess completion for some Pokémon.
- Fixed `/pokedex grant only` and `/pokedex remove only` not respecting the form parameter passed.
- Fixed variant forms appearing incorrectly in the Pokédex when the normal form had not been unlocked.
- Fixed Pokémon occasionally being shot into the sky during battle. No Pokémon were harmed by this bug, probably.
- Fixed NPC editing GUI not updating aspects until a game restart.
- Fixed some users being unable to open their PC if a Pokémon in it had a lot of PP raises beyond normal bounds. How did you get those, anyway? Tell me or the Bellossom gets it.
- Fixed some color variants (Dubwool, Conkeldurr and Undyed wooloo) being missing in the Pokédex.
- Fixed invalid species or held items causing Players to not be able to load into their world anymore (commonly happening after removing addons/mods).
- Fixed Wooloo variants not being automatically registered in the owner's Pokédex when dyed.
- Fixed Vivichoke Dip and Leek & Potato Stew not returning a bowl upon consumption.
- Fixed Fossil Restoration Tank not accepting Hay Bales as organic material.
- Fixed Potion items applying double their intended healing value.
- Fixed Fast Ball capture bonuses applying to all Pokémon, making it the Best Ball instead of the Mediocre Edge-Case Ball.
- Fixed "learned new move" messages appearing for already-known moves on Pokémon evolutions.
- Fixed Pokémon Item Models breaking shadows nearby when being placed in Display Cases or Item Frames.
- Fixed berries not giving bonus yields when planted in their preferred biomes. I'm sure we've fixed that 5 times now.
- Fixed the NeoForge version not supporting "SodiumDynamicLights".
- Fixed players disconnecting from servers if they made changes to certain config options.
- Fixed players with shouldered Pokémon not being able to rejoin their 1.5.2 worlds using 1.6.
- Fixed `PokemonProperties` utilizing `ability=<some ability>` being treated as a forced ability even when it is a legal ability for the Pokémon.
- Fixed type formatting in Pokédex scanner mode when dual types require two lines.
- Fixed trading sometimes crashing the game or server.
- Fixed Wild shiny sounds not respecting the `shinyNoticeParticlesDistance` config setting.
- Fixed Pokémon being able to evolve mid-battle.
- Fixed NPC held items being able to be stolen by players. Don't be a thief!
- Fixed evolutions that require a held item consuming it as soon as meeting requirements when it should only be consumed upon evolution.
- Fixed Pokémon showing only the default form when selecting them as a target in battle.
- Fixed a possible error coming out of reeling fishing rods in specific situations.
- Fixed incorrect weights being used when Poké Fishing with Luck of the Sea.
- Parametric particle motion now works.
- Event-spawned particles now work.
- Particles can now have independent coordinate spaces.
- Fixed suffocation of Pokémon when a pokeball breaks near a wall

### Developer
- Updated the Pokédex data updated events to always include a `Pokemon` instance, and optionally a `DisguiseData` instance.
- Updated fields in `SpawnNPCPacket` and `SpawnPokemonPacket` to be visible and mutable.
- Updated `UnvalidatedPlaySoundS2CPacket` to be public instead of internal and made its fields mutable.
- Added `hideNameTag` field and `HideNPCNameTag` nbt tag to `NPCEntity` to allow hiding the name tag of the NPC.
- Added the player to `PokerodReelEvent` so you know who is doing the reeling.

### MoLang & Datapacks
- Added flows for:
    - `forme_change`: Triggered when a Pokémon changes form in battle.
    - `mega_evolution`: Triggered when a Pokémon mega evolves in battle. (Note: Third-party mods are required for this feature currently)
    - `zpower_used`: Triggered when a Pokémon uses a Z-Power move in battle. (Note: Third-party mods are required for this feature currently)
    - `terastallization`: Triggered when a Pokémon terastallizes in battle. (Note: Third-party mods are required for this feature currently)
    - `battle_fainted`: Triggered when a Pokémon faints in battle.
    - `battle_fled`: Triggered when a player flees from battle.
    - `battle_started_pre`: Triggered when a battle starts. Cancelable!
    - `battle_started_post`: Triggered when a battle starts.
    - `apricorn_harvested`: Triggered when an Apricorn is harvested.
    - `thrown_pokeball_hit`: Triggered when a thrown Pokéball hits a Pokémon.
    - `level_up`: Triggered when a Pokémon levels up.
    - `pokemon_fainted`: Triggered when a Pokémon faints.
    - `pokemon_gained`: Triggered when a player gains a Pokémon.
- Added MoLang functions:
    - For Pokémon:
        - `pokemon.apply(PokemonProperties)`: Applies the given properties to the Pokémon.
        - `pokemon.owner`: Returns the owner of the Pokémon or 0.0 if there is no owner or they are not online.
    - For all entities:
        - `entity.is_standing_on_blocks(depth, blocks...)`: Returns whether the specified entity is standing on a specific block or set of blocks. Example usage: `q.is_standing_on_blocks(2, minecraft:sand)`
- Added NPC field:
    - `hideNameTag`: Hides the name tag of the NPC.
    - Added `baseScale` property to NPCs.
- Added MoLang particle queries for getting distance to targeted entities.
