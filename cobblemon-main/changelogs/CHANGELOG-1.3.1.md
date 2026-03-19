## [1.3.1 (March 31st, 2023)](#1-3-1)

### Additions
- Added Slugma, Magcargo, Nosepass, and Probopass.
- Elgyem family now drops Chorus Fruit, Geodude family now drops Black Augurite.
- Added missing spawn files for Golett and Bergmite family.
- Apricorns can now be smelted into dyes.
- Added animations to Staryu line and Porygon line.
- Added faint animations to Klink line.
- Add lava surface spawn preset.
- Added an `any` evolution requirement allowing you to define `possibilities` of other evolution requirements, for example, this allows you to create an evolution that requires the Pokémon to be shiny or a female.
- Added the `/spawnpokemonfrompool [amount]` or `/forcespawn [amount]` command to spawn Pokémon(s) in the surrounding area using the natural spawn rates/pool of that area, this will be a cheat command in the Minecraft permission system or use the permission `cobblemon.command.spawnpokemon` if a permission mod is present. On a successful execution of the command, the amount of Pokémon spawned will be the output.
- Added the `/pokebox` and `/pokeboxall` commands to move Pokémon(s) to the PC from a Player's party, this will be a cheat command in the Minecraft permission system or use the permission `cobblemon.command.pokebox` if a permission mod is present. On a successful execution of the command the output will be the number of pokemon moved to the Player's PC.
- Added the `/pc` command which opens up the PC UI the same way interacting with the block would, this will be a cheat command in the Minecraft permission system or use the permission `cobblemon.command.pc` if a permission mod is present.

### Changes
- You can now click the portraits of other Pokémon in the starter selection screen to navigate directly to them.
- You can now click the right and left arrow keys to navigate PC boxes.
- Link Cables will now require Pokémon to hold any held item normally required for their evolution.
- After a battle, the last Pokémon used now becomes the selected one in your party.
- The `/teach` command can now only allow the Pokémon to be given moves in their learnset, this can be controlled with the permission `cobblemon.command.teach.bypass`, to account for that change the base command now requires the permission `cobblemon.command.teach.base`, this change is meant only for people using a mod capable of providing permissions such as [LuckPerms](https://luckperms.net/).
- Apricorns will no longer collide with their block form when picked, this should improve the experience in automatic farms.
- Increased spawn chances for many Pokémon requiring specific blocks to be nearby.
- Put Cryogonal in more snowy biomes.
- Ditto as well as the Eevee, Gible, and Riolu families have been made more common.
- Lowered spawn rate of Gyarados on the surface of water.
- Apricorn leaves can now be used in the [Composter](https://minecraft.fandom.com/wiki/Composter) block, these have the same chance to raise the compost pile the Minecraft leaves do.
- Updated Gengar's model and texture.
- Updated Swinub line model and animations.
- Tweaked portrait frames for the Pidgey line and for Walking Wake.
- Changed all buff shoulder effects to only give a level 1 buff instead of level 2.
- Made Weavile a little bigger.
- Changed the recipes for Mystic Water, Miracle Seed, and Charcoal Stick to utilise the evolution stones, as well as Never-Melt Ice having an alternate recipe using the Ice Stone.
- Replaced the `Failed to handle` battle messages to `Missing interpretation` to make it more clear that mechanics do work just still pending dedicated messages.
- Healing Machine and PC are now mine-able with pickaxes and Apricorn leaves are mine-able using hoes.

### Fixes
- Fixed killing a Dodrio killing your game. Dodrio will never look the same to us.
- Fixed non-Fire-type Pokémon being immune to lava.
- Fixed custom Pokémon not being usable in battle, properly. A last minute fix caused this to break again; what are these devs not paid for?
- Fixed being locked in an endless healing queue if you broke the healing machine during use.
- Fixed an issue with the experience calculation when the Exp. Share is held.
- Fixed Friendship-based attacks not using friendship values from your Pokémon.
- Fixed Link Cables consuming held items they shouldn't due to not validating the held item of a Pokémon.
- Fixed a crash when Aromatherapy cured the status of party members.
- Fixed moves learnt on evolution not being given when said evolution happens. If you were affected by this issue your existing Pokémon will now be able to relearn those moves.
- Fixed console spam when rendering Pokémon model items.
- Fixed battle messages for 50+ moves and abilities and items.
- Fixed the possible duplicate when capturing Pokémon (probably, this one's hard to reproduce to confirm it's fixed).
- Previously duplicated Pokémon are cleaned from PCs and parties on restart.
- Fixed an issue with some particle effects applying after a Pokémon has died or on top of the wrong Pokémon when using specific mods.
- Fixed Pokémon not looking at each other in battle.
- Fixed Experience Candy and Experience Share attempting to bring Pokémon above level cap causing crashes.
- Fixed level 100 Pokémon having experience go over the cap total amount they should have.
- Fixed `/pokemonspawnat` having the argument positions reverted making it impossible for Brigadier to understand when to suggest coordinates. It is now the intended `/spawnpokemonat <pos> <properties>`.
- Fixed performance issues with shouldered Pokémon in certain systems.
- Fixed learnset issues for Pokémon whose only modern debut was LGPE/BDSP/LA.
- Fixed shiny Zubat, Grimer, Omanyte, Elgyem, Delphox and Aegislash displaying their normal texture.
- Fixed sleeping in beds allowing fainted Pokémon to receive experience after a battle ends somehow.
- Fixed an issue where a Pokémon will claim to have learnt a new move they already have in their moveset when learnt at an earlier level in their previous evolution. I realize that's confusing.
- Fixed Dispensers not being able to shear Wooloo. This will also extend to other mods that check if an entity is valid to shear.
- Fixed the currently held item of your Pokémon not dropping to the ground when removing it if your inventory was full.
- Fixed creative mode allowing you to make your Pokémon hold more than 1 of the same item.
- Fixed a Pokémon duplication glitch when teleporting between worlds.
- Fixed dedicated servers being able to reload Cobblemon data with the vanilla `/reload` command causing unintended behavior for clients.
- Fixed underground Pokémon spawning above ground.
- Fixed Pokémon portrait not reverting back to the Pokémon after a failed capture during battle.
- Fixed edge texture artifacts on pane elements for Tentacool and Tentacruel models.
- Fixed crash caused by Pokémon pathing
- Fixed Pokémon not returning to their balls when being healed in a healing machine
- Fixed all Gen IX Pokémon as well as forms added in PLA and Wyrdeer, Kleavor, Ursaluna, Basculegion, Sneasler, Overqwil, and Enamorus having 0 exp yields.
- Fixed Irons Leaves having bluetooth back legs. If you saw it, you know what I mean.
- Fixed Golurk not having shoulder plates on its shoulders.
- Fixed some water Pokémon walking onto land from the water even though they are fish.
- Fixed Porygon2 and PorygonZ being too small.
- Fixed Snivy line head look animation.
- Fixed Staryu line not being able to swim.
- Fixed an incompatibility with [Thorium](https://modrinth.com/mod/thorium) patch for [MC-84873](https://bugs.mojang.com/browse/MC-84873).
- Fixed Pidgeotto wings when walking.
- Fixed Delphox walk animation.
- Fixed Froakie line sleep animations in battle.
- Fixed Pokémon missing the non-level up moves they could relearn when rejoining a world until a new move was added to their relearn list.
- Fixed instantly fleeing from Pokémon set to be unfleeable.
- Fixed Pumpkaboo line forms not working. (Currently sizes aren't visual but check base stats to see which size you have.)
- Fixed a bug that caused already interpreted messages for moves to be mistaken as uninterpreted.
- Fixed a Pokémon spawner bug that caused Pokémon to not spawn due to dropped item entities.
- Fixed a bug that causes Pokémon model items to be invisible.

### Developer
- Add events that are fired just before and after a Pokémon is released (ReleasePokemonEvent.Pre and .Post)

### Localization
- Added complete translations for Japanese, Thai, and Canadian French.
- Added partial translations for Russian, Ukrainian, Mexican Spanish, and Korean.
- Updated every existing language's translation.
- All the translators that contributed are amazing.