## [1.5.1 (May 27th, 2024)](#1-5-1)

### Additions
- Added unique send out particles for Cherish, Dream, Beast, and Ancient Origin balls.
- Made Wooloo and Dubwool dye-able like sheep. So cute!
- Added stat up and down particles.
- Most status effects now have particles! These include: Paralysis, Poison, Sleep, Confusion, Infatuation.
#### Move Particle Effects
- Confusion
- Cotton Guard
- Growl
- Ice Punch
- Fire Punch
- Thunder Punch
- Minimize
- Quick Attack
- Protect
- Swords Dance
- Sand Attack
- Poison Powder
- Sleep Powder
- Stun Spore
- Powder
- Rage Powder
- Spore
- Thunder Wave

### Changes
- Sounds for Relic Coin Sacks have been correctly renamed. Relic Coin Pouches received new sounds for breaking and placing.
- Readjusted Petilil portraits so they fit a bit better.
- Improved handling of Pokémon taken from the Restoration Tank block to be a bit more stable.
- Made Mulch cheaper to craft.

### Fixes
- Fixed a bug in which adding organic material to the restoration tank via right click was adding the full count of the stack currently in hand - but only taking 1 of the item.
- Fixed a niche issue where some properties of entities were not initialized correctly, causing Pokémon that appeared to be level 1 until you battle them.
- Fixed Fossilized Drake being missing from the Fossils item tag.
- Fixed Gilded Chest block entity not being cleared on block break, creating spooky ghost blocks. Old ones can be fixed by placing something like a furnace where it was, then breaking the furnace.
- Fixed sherd brokenness on Forge.
- Fixed Supplementaries incompatibility.
- Fixed Fossil Compartment crash with Jade / WAILA forks.
- Fixed pasture block PC lookups when the player is offline.
- Fixed an untranslated battle message that occurs when using a move that just ran out of PP (e.g. Fire Blast that just got spited mid-turn).
- Fixed held items being eaten even when the held item evolutions are already unlocked.
- Fixed Hisuian Decidueye not being Grass/Fighting.
- Fixed both Decidueye forms learning both Triple Arrows and Spirit Shackle.
- Fixed Pineco being unable to evolve into Shulker Forretress.
- Fixed Kabutops T-posing when underwater. It doesn't have proper swimming animations yet, though.
- Fixed Pidgey's missing walk animation.
- Fixed Cyndaquil's hidden flames clipping if it was swimming.
- Fixed Chimecho and Chingling being unable to spawn near bells. They are meant to!
- Fixed Tyrantrum and Wailord Party Overlay models peeking through the chat box. It was kinda funny though.
- Fixed hitbox sizes for Seedot, Nuzleaf, and Shiftry.
- Fixed Budew and Lechonk sliding if they walked for too long.
- Fixed Shedinja T-posing in battle.
- Fixed recoil evolution condition not working, making things like Basculegion unobtainable.
- Fixed issue where poser debug tools didn't work on JSON posers.
- Fixed issue where gilded chests don't close when going far away.
- Fixed issue where the restoration tank's renderer was reading old data, making it appear wrong.
- Fixed issue where the lights on the restoration tank would not animate if it was facing east. Very specific.
- Fixed client crash with the fossil machine when updating block state on a chunk that is unloaded in the client. I don't understand this but the devs are sure that all of those are real words.
- Fixed Restoration Tank crash with Create upon the tank block's destruction.
- Fixed Restoration Tank over consuming items when interacting with Create blocks.
- Fixed addons that add very many moves to a learn-set causing disappearing Pokémon (visually) issues on servers.
- Fixed Hyper Cutter and Big Pecks incorrectly stating that it prevented accuracy from being lowered in battle.
- Fixed missing messages for Rough Skin and Iron Barbs in battle.
- Fixed a bug where sometimes Pokémon sendouts wouldn't create an entity, or the entity would spawn at 0 0 0 which is not a good place for a Pokémon to be. Or any of us, really.
- Fixed issue in which a locked gilded chest would animate to the open state when the client fails to open it, such as when it is locked.
- Fixed a bug where aspects of a form would not be properly reflected on form changes (eg. Normal -> Hisui).
- Fixed generic battle effect sounds not sounding the way they were intended to.
- Fixed particle effects often not having access to some specific entity functions from MoLang.
- Fixed particles sometimes lasting a single tick too long, causing (very quick) visual glitches.
- Fixed particle rotations being inverted.
- Fixed particle events not spawning at the instigating particle's location.
- Fixed a bunch of spam during world generation.
- Fixed a bug in which throwing a Poké Ball at a player owned Pokémon with the ability Illusion would reveal its true species. Hilarious meta strategy.
- Fixed root-part animations not working for JSON posed Pokémon. You didn't notice this but if we didn't fix this in this update then if you use Quick Attack a lot you'd have seen a whole lot of [this](https://cdn.discordapp.com/attachments/1076993968803434627/1242660506783715369/Minecraft__1.20.1_-_Singleplayer_2024-05-21_22-08-17.mp4?ex=66549408&is=66534288&hm=ff95ee293eb15634fd63e6546534ea279540a1c892605e8d561593ca2c5600c5&) which is damn funny but very unintended.

### Developer
- Changed SpawnAction#complete to return a nullable generic R (the spawn action result) instead of a boolean. Provides more information this way.
- Added an event that fires when a fossil is revived, with or without a player.
- Added IVS and EVS property extractors.
- Fixed PCStore#resize not allowing PC boxes size reduction.

### Data Pack & Resource Pack Creators
- Added support for MoLang conditions for quirks and poses.
- Changed the AttackDefenceRatio requirement to StatCompare and StatEqual. There is some backwards compatibility for AttackDefenceRatio, though.
- Changed "dimensions" spawn condition to check with dimension IDs instead of effects, so custom dimension IDs can be used.
- Added parametric motion and rotation support to particle effects.
- Added entity_scale as a molang var for particles (likely only applicable to Pokemon)
- Added support for primary quirk animations using the following format:
  `JSON
  {
    "quirks": [
      "q.bedrock_primary_quirk('<pokemon>', '<animation>', <minSeconds>, <maxSeconds>, <loopTimes>, '<excludedLabels>', q.curve('<waveFunction>'))"
    ]
  }
  `
- Added support for custom stashes, similar to Gimmighoul's coin and scrap stashes.
- Added the ability to create custom brewing stand recipes.

### Localization
- Updated translations for:
    - Simplified and Traditional Chinese.
    - Spanish.
