# Changelog
## [1.7.4 (MONTH Xth, 2026)](#1-7-4)

### Additions
- Party Overlay Exp Gained Animation will now display the amount of EXP the Pokémon gained
- Added a toast to notify players of how they can scroll through their party. Did you know you can hold R and scroll instead of using arrow keys??!
- Added new layer property that makes a layer scroll in a direction. "scrolling": {"speedU": 0.1, "speedV": 0.1}
- Added missing crops to Botany Pots integration

### New rideable Pokémon

### Pokémon Added

### Model updates for the following Pokémon

### Animation updates for the following Pokémon

### Changes
- Restructured Botany Pots integration folder structure
- Decrease default pastured mon wander radius to 32.
- Factor in the weight of pokemon when calculating pushing forces between hitboxes.
- Remade the Starter Selection Screen with new assets
- Refactored berry trees to be less impactful on TPS (more performant)

### Fixes
- Fixes https://gitlab.com/cable-mc/cobblemon/-/issues/1943
- Fix occasional crash on retrieving revived fossils
- Fixed a compatibility issue with World Game Rules which caused some log spam in specific cases.
- Fixed improper validation of Pokémon when performing a trade.
- Fixed issue where particle like beam can disappear when you don't see the beginning of the beam or you look away
 - Fixes https://gitlab.com/cable-mc/cobblemon/-/issues/1943
 - Fix occasional crash on retrieving revived fossils
 - Fix brushing not working on Neoforge
 - [Various Submarine Ride Fixes](https://gitlab.com/cable-mc/cobblemon/-/merge_requests/2157)
 - Fix Pastured mons despawning and running away.
 - Fix Apricorn trees crashing when other mods bonemeal it
 - [Various UFO Ride Improvements](https://gitlab.com/cable-mc/cobblemon/-/merge_requests/2156)
- Fixed NeoForge brewing stand unexpectedly crashing users when shift clicking items within it's menu
- Fixed NPCs switching pokemon very often during battles
- Fixed Campfire pot causing crashes under certain circumstances

### Developer
- Added `PokeSnackSpawnerFactory` which allows influence registration on PokeSnack encounters
- Added `FishingSpawnerFactory` which allows influence registration on fishing encounters

### Molang & Datapacks
- q.item.is_enchanted()
- q.item.has_enchantment(minecraft:sharpness, 3)
- Added `has_chosen_starter` & `get_starter_uuid` as available Molang functions
- Added `marks`, `has_mark`, & `remove_marks` as available Molang functions for pokemon

### Localization
- Updated translations for:
