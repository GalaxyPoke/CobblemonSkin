### Marks

Marks are special markings that Pokémon can have. 

#### Server Side
To add a custom mark, a JSON file will need to be created in the datapack folder `marks`. Cobblemon by default already has files for all title ribbons and marks from the mainline Pokémon games.

For a custom mark example, you can create a JSON file called `mark_example.json` with the following contents:
```
    {
    	"name": "cobblemon.mark.mark_example",
    	"title": "cobblemon.mark.mark_example.title",
    	"titleColor": "FFFFFF",
    	"description": "cobblemon.mark.mark_example.desc",
    	"texture": "cobblemon:textures/gui/mark/mark_example.png",
    	"replace": ["cobblemon:mark_sample"],
    	"group": "example",
    	"chance": 0.5,
    	"indexNumber": 0
    }
```

The `name`, `title`, and `description` fields should be the lang keys that you will need to create for the mark. The `title` should contain `%1$s`, as this will be the Pokémon's name. For example, a lang key of `"cobblemon.mark.mark_example.title": %1$s the Example"` will show as "Bulbasaur the Example" in game.

The `title` field is also optional, if you leave out the field then the mark will provide no title.

The `titleColor`/`titleColour` field is the hexadecimal color code for the title. This field is optional.

The `texture` field is the path to your texture.

The `replace` field is optional. It is a list of identifiers of existing marks that will be removed if this mark is given. This is mainly used for "upgrading" existing marks that a Pokémon may have. For example, if a Pokémon has an existing silver ribbon and you want to change it into a gold ribbon, you would put the identifier of the silver ribbon in this field for the JSON file of the gold ribbon.

The `group` and `chance` fields are optional. The chance field, if provided, should be a decimal from 0 to 1. This value will be used as the chance for the Pokémon obtaining this mark if the mark's condition is met. For example, putting `0.5` for the `chance` field will be equal to a 50% chance. If the `chance` field is omitted, it will default to 0. The `group` field can be any string, and is also related to the chance mechanic of obtaining the mark. Marks with the same group and chance will be grouped together during the calculation for obtaining potential marks.

The `indexNumber` field is optional and only used for cataloging official marks and ribbons from the Pokémon games. The index numbers used are identical to the listed index numbers for marks and ribbons from Generation IX.

#### Commands
If cheats are enabled, a mark can be given to a Pokémon with the command, `/givemark <player> <partySlot> <mark>`.

To take a mark from a Pokémon, use the `/takemark <player> <partySlot> <mark>` command.

`<mark>` in this case is the mark identifier, generated automatically from the JSON file name. For the example above, this will be `cobblemon:mark_example`.

To give all available marks to a Pokémon, use the `/giveallmarks <player> <partySlot>` command.

#### Mechanics
Cobblemon has obtainable marks from the mainline Pokémon games, though some may currently not be obtainable through regular gameplay. A Pokémon will spawn with a list containing potential marks if it has passed any of the marks' obtain conditions. Upon capture, a calculation is run for the Pokémon to obtain a mark from the list, depending on each mark's chance value.

The calculation uses the `chance` and `group` values to group the marks, while the chance value determines the order. If a mark does not have a `group` field, its chance field will be used for grouping. For example a Pokémon has a potential marks list containing the following:

```
Mark 0       - Chance: 0.4, Group: A
Mark 1       - Chance: 0.4, Group: A
Mark 2       - Chance: 0.4, Group: B
Mark 3       - Chance: 0.4, Group: A
Mark 4       - Chance: 0.1, Group: C
Mark 5       - Chance: 0.4
Mark 6       - Chance: 0.1, Group: C, 
Mark 7       - Chance: 0.6, Group: D, 
Mark 8       - Chance: 0.4, Group: A, 
Mark 9       - Chance: 0.4
Mark Example - Chance: 0.5, Group: example
Mark Sample  - Chance: 0.5, Group: example
```
Upon capturing the Pokémon, the calculation will sort the above using the chance value in ascending order. If marks have the same chance and group, they will be grouped together:
```
1. Chance: 0.1, Group: C       [ Mark 4, Mark 6 ]
2. Chance: 0.4                 [ Mark 5, Mark 9 ]
3. Chance: 0.4, Group: A       [ Mark 0, Mark 1, Mark 3, Mark 8 ]
4. Chance: 0.4, Group: B       [ Mark 2 ]
5. Chance: 0.5, Group: example [ Mark Example, Mark Sample ]
6. Chance: 0.6, Group: D       [ Mark 7 ]
```
Then it will go down the groups in order and roll the chance value for each group. If the chance fails, it will move on to the next group and roll again using the group's chance value. If it reaches the last group and the chance fails again, then no mark will be given. If a group's chance roll succeeds, then a mark from that group's list will be chosen at random to be given to the Pokémon, and the rest of the groups will no longer be rolled. After the calculation, the potential marks list is cleared.

#### Molang
Molang is used for giving marks to Pokémon if conditions are met. Molang conditions can be added in the `flows` datapack folder. For example, placing the following, `q.pokemon_entity.add_potential_marks('cobblemon:mark_example', 'cobblemon:mark_sample');`, in a molang file within the folder `pokemon_entity_spawn`, will automatically add example marks to the Pokémon's potential marks list upon spawning.

To give a mark to a Pokémon directly, bypassing the potential marks mechanic, `q.pokemon_entity.add_marks('cobblemon:mark_example');` can be used.

`q.pokemon_entity.add_marks_with_chance('cobblemon:mark_example');` can also be used to add marks directly, using the mark's chance value to roll the probablity of adding the mark.

All of the above functions can take any amount of marks as a parameter.

To run the calculation for the chance to apply a mark from the Pokémon's potential marks list, `q.pokemon.apply_potential_marks;` can be used.