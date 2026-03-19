## [1.4.1 (December 23rd, 2023)](#1-4-1)

### Additions
- Added battle spectating. Press R on a player in a battle and you can spectate and bully them for their tactics.
- Added the Litwick and Drifloon lines.
- Cobblemon now has compatibility with [Adorn](https://modrinth.com/mod/adorn), allowing you to craft Apricorn wood furniture.
- Berries can now be used in recipes from [Farmer's Delight](https://modrinth.com/mod/farmers-delight) and [Farmer's Delight (Fabric)](https://modrinth.com/mod/farmers-delight-fabric), as well as any other mods using the same berry tags.
- Boats, signs and hanging signs are now craftable with Apricorn wood.
- Added the Fairy Feather, Iron Ball, Cleanse Tag, Flame Orb, Life Orb, Smoke Ball, and Toxic Orb held items.
- Added the Inferno, Void, and Forsaken patterns for Vivillon. These can be obtained by evolving a Spewpa in the Nether, End, or Deep Dark respectively.
- Bees can now be fed using Pep-Up Flowers.
- Mooshtank can now be milked with a bowl for Mushroom Stew.
- Updated Showdown version to use generation 9 battle data.
- Added cries to Beldum, Metang and Metagross.
- Added a `/bedrockparticle` command to run Snowstorm-format particle effects.
- Added data for Dipplin, Fezandipiti, Munkidori, Ogerpon, Okidogi, Poltchageist and Sinistcha.
- Added additional nickname trigger "Grumm" for Inkay's evolution.

### Changes
- Using Potions, Status Heals, Ethers, and Antidotes will now return a glass bottle
- Using a Remedy, Fine Remedy, or Superb Remedy will no longer lower friendship with a Pokémon.
- The Healing Machine now has a [much more difficult recipe](https://wiki.cobblemon.com/index.php/Healing_Machine), placing it later game.
- Made the EXP. Share recipe cheaper.
- Turtwig can now be put on your shoulder.
- Updated Zubat line model, texture, and animations.
- Updated Geodude line models and textures.
- Added animations for Hitmontop, Tyrogue, and Mightyena.
- Tweaked animations for Dusknoir, Ratatta, Bewear, Exeggutor, and Alolan Exeggutor.
- Sized Kantonian Exeggutor down. Still big, but not TOO big.
- Tweaked cries for Pikachu, Raichu and Alolan Raichu.
- Fixed Swimming behaviors for Wimpod line, Oshawott line, Quaxly line, and Clodsire
- Changed the way level scaling works in spawning. By default, anything with a spawn range of up to 5 either side of the party highest level and everything else will spawn per its specified ranges.
- The nature of Pokémon will now be displayed italicized when a mint has been applied. Hovering over the nature will display the mint that was applied.
- Slightly lowered the volume of all cries.
- Giving Pokémon items now plays a sound
- Updated the Poké Ball model and animations.
- Pasture blocks will now also connect their bottom left and right sides to walls, iron bars, glass panes, and any other modded block that follows the same connection rules.
- The config option `consumeHeldItems` has been removed, please see the Datapack & Resourcepack Creators section for instructions on the updated method.
- Heal Powder can now be composted with a 75% chance of adding a layer
- Mental, Power, White, and Mirror Herbs can now be composted with a 100% chance of adding a layer.
- Added glowing eyes to Hoothoot and Noctowl.
- Mining Evolution Stone Ores with a Fortune pickaxe will now increase the amount of items received.
- Black Augurite can now be used to craft stone axes and obsidian.
- Using Experience Candies brings up the Party Pokémon Select screen when not targeting a Pokémon.
- Added tab completion for statuses to commands.
- Remedies can now be cooked in a Smoker and on a Campfire.
- Vertically flipped the Destiny Knot recipe.

### Fixes
- Fixed Raticate, Onix, Unfezant, Bergmite, Avalugg, Boltund and Revavroom cries not playing.
- Fixed Alolan Ratticate animations causing a crash.
- Fixed Quaxwell not doing its cry.
- Fixed Shroomish not using its idle.
- Fixed how Weight and Height is calculated for Pokémon, fixing the damage from moves like Low Kick.
- Fixed a staggering number of battle messages.
- Fixed various stone related blocks not being valid for Big Roots to spread onto on the Fabric version.
- Updated the registration of compostable items to improve compatibility with Fabric forks such as Quilt. Please note this does not mean we officially support Quilt, this change was only done since it was possible by correcting the registration to use the new intended way in the Fabric API.
- Fixed Dispensers being unable to shear grown Apricorns.
- Fixed Bowl not being given back to player after using Berry Juice
- Fixed missing text for attempting to catch an uncatchable Pokémon
- Fixed Moonphases for Clefairy line
- Fixed issue where Potions, Super Potions, and Hyper Potions did not work during battle
- Fixed the compatibility patch with the Forge version of [Carry On](https://modrinth.com/mod/carry-on) due to a bug on the mod, the Fabric version was unchanged and is still compatible.
- Added the ability to place Berries on modded Farmland blocks.
- Shouldered Pokémon now hop off when selected in team and R is pressed. This also is in effect in battles leading to shouldered Pokémon jumping of the shoulder of the trainer when it is their turn.
- Made more items compostable and changed the process for making items compostable.
- Added the ability for Hoppers to fill Brewing Stands with Medicinal Brews and Potions.
- Apricorn blocks are now flammable. Probably should have started that way, but we got there.
- The default pose for Pokémon being passengers is now "standing".
- Fixed issue where some IVs were changing every time a player logged back in.
- Fixed advancement crash from bad datapack evolution data.
- Fixed global influences being applied to TickingSpawners twice.
- Reverted the default SpawningSelector back to FlatContextWeightedSelector. This fixes multiple weight related issues, including weights with SpawningInfluences.
- Apricorn Planting advancement should work again.
- Advancement "Vivillonaire" should now allow High Plains and Icy Snow Vivillon to register.
- Fixed the last battle critical hits evolution requirement not working.
- Fixed the damage taken evolution requirement not saving progress.
- Fixed the defeated Pokémon evolution requirement not saving progress.
- Fixed potion brewing recipes not showing up JEI and similar mods on the Forge version.
- Fixed an exploit that could convert a single piece of Blaze Powder into an extra Medicinal Brew on the Forge version.
- Fixed an issue where health percentages would show incorrectly after healing
- Fixed the move Revival Blessing not allowing you to select from fainted party members.
- Fixed villagers not being able to pick up and plant mint seeds, vivichoke seeds, and revival herbs.
- Fixed Exeggcute faint.
- Fixed various spawn configuration issues across the board.
- Fixed a possible visual duplication of sent out Pokémon.
- Fixed battle text for Trace, Receiver, and Power of Alchemy.
- Fixed tooltips being appended too late in items.
- Fixed battles ending background music when battle music is not present.
- Fixed battles ending background music, instead of pausing, when battle music is played.
- Fixed a bunch of regionals to actually be obtainable, namely the unmodelled ones
- Fixed battle text for moves that were missing.
- Fixed a formatting error that affected Pokémon nicknames when the storage type is JSON.
- Fixed a crash that could occur on some servers relating to chunk loading and teleporting.
- Fixed an issue with Inkay's evolution requirement.
- Fixed conflicting evolution requirements that would cause the Ocean, River, Sun, and Tundra variants of Vivillon to be unobtainable through evolution.
- Fixed the Modern variant of Vivillon not being obtainable through evolution.
- Fixed Pokémon pathing through berry bushes, harming themselves in the process.

### Developer
- Fixed the `SpawnEvent` not respecting usage of `Cancelable#cancel`.
- Added the `EvolutionTestedEvent`, this allows listening and overriding the final result of evolution requirement tests.
- Rebuilt the scheduling API to more clearly force side choices and allow more local temporal frames of reference for tasks.
- Added utility script that can be used to generate all Spawn JSONS for all pokemon from the spawning spreadsheet in 1 click ([cobblemon_spawn_csv_to_json.py](utilityscripts%2Fcobblemon_spawn_csv_to_json.py)).
- The `HeldItemManager` has a new method `shouldConsumeItem`, this will return false by default to prevent breaking changes, see the documentation and update your implementations as needed.
- Added and implemented minSkyLight and maxSkyLight as config options for SpawnConditions
- Player specific battle themes can now be assigned to `PlayerData#battleTheme`.
- Changed design of `BattleStartedPreEvent`. Will now expose the `PokemonBattle`.

### Datapack & Resourcepack Creators
- Added 3 new item tags: `cobblemon:held/consumed_in_npc_battle`, `cobblemon:held/consumed_in_pvp_battle` & `cobblemon:held/consumed_in_wild_battle` these will determine which items get consumed in the implied battle types by Cobblemon, keep in mind the controller for this behaviour can be overriden by 3rd party.
- Unique wild encounter themes can now be associated with a specific species (or form) by assigning a SoundEvent identifier to the `battleTheme` field in the species' data configuration.
- Added a `structure` evolution condition, used to check if a Pokémon is in a given structure.

### Localization
- Updated translations for:
    - French and Canadian French
    - Simplified and Traditional Chinese
    - Spanish and Mexican Spanish
    - Pirate English
    - German
    - Thai
    - Portuguese and Brazilian Portuguese
    - Polish
    - Italian
    - Dutch
    - Ukrainian
    - Russian

Thank you so much to all of our community translators that bring the mod to the rest of the world!