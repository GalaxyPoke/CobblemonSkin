import os
import shutil
import sys
import json

if __name__ == "__main__":
	filepath = sys.argv[1]
	dirs = os.walk(filepath)
	print(f"Scanning {filepath}")
	for x in dirs:
		print(f"Scanning {x[0]}")
		for f in x[2]:
			if f.endswith("_berry.json"):
				print(f"Processing {x[0]}{f}")
				file = open(f"{x[0]}{f}", "r")
				blockstate = json.load(file)
				blockstate["variants"]["age=0"]={
					"model": "cobblemon:block/berries/planted"
				}
				blockstate["variants"]["age=1"]={
					"model": "cobblemon:block/berries/sprout"
				}
				file.close()
				file = open(f"{x[0]}/{f}", "w")
				file.write(json.dumps(blockstate, indent=2))
				file.close()