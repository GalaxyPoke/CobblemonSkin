import json
import copy
from pathlib import Path

# Traverses bones and inserts the data of the original keyframe into the scaled keyframe, popping the original if it is not 0.
def scale_keyframes(property, old_map, new_map):
    if property in old_map[animationName]['bones'][bone].keys():
        for frame in old_map[animationName]['bones'][bone][property]:
            scaled_keyframe = float(frame) * speed_scalar
            new_map[animationName]['bones'][bone][property][str(scaled_keyframe)] = new_map[animationName]['bones'][bone][property][frame]
            if scaled_keyframe != 0:
                new_map[animationName]['bones'][bone][property].pop(frame)

file_path = input('Enter file path: ')
speed_scalar = float(input('Enter speed scalar: '))
destination = input('Enter file destination: ')

# Parse file into json
file_data = Path(file_path).read_text()
file_json = json.loads(file_data)

# Makes deep copy of animations json, and traverses each keyframe in the bones, appending to the copy and popping the original frame (unless it is 0).
animations = file_json['animations']
animations_copy = copy.deepcopy(animations)
for animationName in animations:
    animations_copy[animationName]['animation_length'] = animations_copy[animationName]['animation_length'] * speed_scalar
    for bone in animations[animationName]['bones']:
        scale_keyframes('position', animations, animations_copy)
        scale_keyframes('rotation', animations, animations_copy)
# Replaces the animations with the new, mutated copy and writes it to a file at destination
file_json['animations'] = animations_copy
with open(destination, 'w') as outfile:
    json.dump(file_json, outfile, indent=4)
print("Parsing complete.")