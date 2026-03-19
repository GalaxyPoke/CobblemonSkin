## [1.2.0 - The Customization Update (January 1st, 2023)](#1-2-0)
#### "There are going to be so many fakemon..."
### Additions
- Added models for Natu and Xatu, Murkrow and Honchkrow, Wailmer and Wailord.
- Added new PC interface and it is beautiful.
- Reworked the battle system so that battles load faster, cause fewer bugs, and can run on shared server hosts. This is a very big change that also reduced the size of the mod by 50mb!
- Added full resource pack and data pack customization of models, textures, animations, spawning, and spawn file presets to make custom Pokémon species and variations very easy to create. You can find the guide for creating custom Pokémon on [our website](https://cobblemon.com/guides/custompokemon.html)!
- Added water surface spawning for Pokémon like Lapras.
- Added emissive texture support to Pokémon render layers.
- Added compatibility for Mod Menu ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu), [Modrinth](https://modrinth.com/mod/modmenu)).
- Added blank ambient Pokémon cries so cries can be added via resource packs.
- Added new sounds for GUIs and item uses.
- Added `nature` and `pokeball` options to commands such as `/spawnpokemon` and `/givepokemon`.

### Changes
- Reinforced party and PC saving to make data corruption from crashes less bad.
- Added a config option for whether the starter config will be exported, making it more maintainable as we add starter Pokémon.
- Battles now start with the options menu open for convenience.
- Doubled the default charge rate of healers. You'd need to reset your config under `./config/cobblemon/main.json` to see this change!
- Changed the default Apricorn seed chance config value from 6% to 10%.
- The mod now correctly reports our dependency on Architectury API so people don't get super confused when things don't work.
- Pokémon now look at their opponents during battle.
- Updated Sableye's animations to be super freaky.
- Changed the healer advancements to make the healing machine's mechanics more obvious.

### Fixes
- Fixed an incompatibility with [Porting Lib](https://github.com/Fabricators-of-Create/Porting-Lib) used by Fabric ports of mods such as Create or Twilight Forest.
- Fixed HP and max HP values in the battle GUI not being correct.
- Fixed some animations on snake-type Pokémon being super slow.
- Fixed a typo in the English name for Calcium. Calcuim.
- Fixed Pokémon gradually becoming rarer around you if you move long distances.
- Fixed a shoulder mount crash on Fabric.
- Fixed a rare issue where chunks would take a really long time to generate.
- Fixed a singleplayer bug where battles wouldn't work after leaving then rejoining a world.
- Fixed stat calculations for everything except HP. HP was fine though :).
- Fixed a randomized Pokémon spawning in mountains that corrupted the data of whatever party or PC it got into. Yikes.
- Fixed a rare crash involving literally random number generation. A random crash involving random numbers.
- Fixed all regular Mewtwo having the stats and types of Mega Mewtwo X - same deal with many other Pokémon.
- Fixed the framing of many Pokémon in GUIs.
- Fixed texture glitches with Rattata and Nidoqueen (again!).
- Fixed dropped item forms of all Pokéballs and the Healing Machine, and slightly adjusted all other display settings
- Fixed issues with forms not showing the correct evolution in the evolution menu.
- Fixed some alternate forms not having the alternate stats and types.
- Fixed moves that only work in double battles not being selectable at all (such as Helping Hand and Aromatic Mist).
- Fixed abilities not remaining legal in some forms.
- Fixed Poké Ball capture effects not triggering after a successful capture, such as the Heal Ball's healing effect.
- Fixed multiple-hit moves sending gibberish into the battle chat.
- Fixed Pyukumuku not being appropriately scaled.
- Fixed shiny and other variations of Pokémon not showing in the battle GUI.
- Fixed Eevee being poorly positioned and un-animated on shoulders.
- Fixed a Pokémon's hitbox not updating when it evolves while sent out.
- Fixed a Pokémon's PP going from zero to above the maximum when entering another battle.