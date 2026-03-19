## Calculating Seat Positions for Riding

The idea of the /calculateseatpositions command is that it ask for a pose type and it will simulate the animations
for the Pokémon in whatever the first pose is for that type. That will help it find the average position that each seat
locator was for the pose, allowing the user to click each seat index to get the JSON of the offset data onto the 
clipboard for easy pasting into the species data.

Usage: `/calculateseatpositions <species> <aspects> <pose>`

Example 1: `/calculateseatpositions araquanid . STAND`

Example 2: `/calculateseatpositions charizard megax,shiny FLY`

The aspects are optional and can be a comma separated list of aspects to apply to the Pokémon. The pose is required. If
there are no seats for the pose, it will just list out no seats.

The seat data for a species is a little bit complicated, because you can register a seat that applies regardless of poses
or you can specify for specific sets of pose types what the offset will be.

Here is an example of a seat object that has one offset used in general but a specific one for when they are swimming.

```json
{
  "offset": {
    "x": 0.0,
    "y": 0.4,
    "z": 0.0
  },
  "poseOffsets": [
    {
      "poseTypes": ["FLOAT", "SWIM"],
      "offset": {
        "x": 0.0,
        "y": 0.7,
        "z": 0.2
      }
    }
  ]
}
```

The command will copy to clipboard just the `x`, `y`, `z` part, e.g. `{ "x": 0.0, "y": 0.4, "z": 0.0 }`. How you choose
to implement this data in the seat data is up to you based on how close you think different poses are to having the
player in the same position.