import os
import json

# This is the entire list of "External Moves" as it is designated in official game data.
# Functionally, it is our entire list of potential base-mod TMs when the TMM is complete.
# Fans often split these into "TM moves" and "Tutor moves" depending on the game.
potential_tm_list = [
    "focuspunch", "dragonclaw", "waterpulse", "calmmind", "roar", "toxic", "hail", "bulkup", "bulletseed",
    "sunnyday", "taunt", "icebeam", "blizzard", "hyperbeam", "lightscreen", "protect", "raindance", "gigadrain",
    "safeguard", "solarbeam", "irontail", "thunderbolt", "thunder", "earthquake", "dig", "psychic", "shadowball",
    "brickbreak", "doubleteam", "reflect", "shockwave", "flamethrower", "sludgebomb", "sandstorm", "fireblast", "rocktomb",
    "aerialace", "torment", "facade", "secretpower", "rest", "attract", "thief", "steelwing", "skillswap",
    "snatch", "overheat", "roost", "focusblast", "energyball", "falseswipe", "brine", "fling", "chargebeam",
    "endure", "dragonpulse", "drainpunch", "willowisp", "silverwind", "embargo", "explosion", "shadowclaw", "payback",
    "recycle", "gigaimpact", "rockpolish", "flash", "stoneedge", "avalanche", "thunderwave", "gyroball", "swordsdance",
    "stealthrock", "psychup", "captivate", "darkpulse", "rockslide", "xscissor", "sleeptalk", "naturalgift", "poisonjab",
    "dreameater", "grassknot", "swagger", "pluck", "uturn", "substitute", "flashcannon", "trickroom", "dive",
    "mudslap", "furycutter", "icywind", "rollout", "thunderpunch", "firepunch", "superpower", "icepunch", "ironhead",
    "aquatail", "ominouswind", "gastroacid", "snore", "spite", "aircutter", "helpinghand", "endeavor", "outrage",
    "ancientpower", "synthesis", "signalbeam", "zenheadbutt", "vacuumwave", "earthpower", "gunkshot", "twister", "seedbomb",
    "irondefense", "magnetrise", "lastresort", "bounce", "trick", "heatwave", "knockoff", "suckerpunch", "swift",
    "uproar", "superfang", "painsplit", "stringshot", "tailwind", "gravity", "worryseed", "magiccoat", "roleplay",
    "healbell", "lowkick", "skyattack", "block", "bugbite", "headbutt", "cut", "fly", "surf",
    "strength", "whirlpool", "defog", "rocksmash", "waterfall", "rockclimb", "honeclaws", "psyshock", "venoshock",
    "telekinesis", "smackdown", "sludgewave", "flamecharge", "lowsweep", "round", "echoedvoice", "allyswitch", "scald",
    "skydrop", "incinerate", "quash", "acrobatics", "retaliate", "voltswitch", "strugglebug", "bulldoze", "frostbreath",
    "dragontail", "workup", "afteryou", "magicroom", "wonderroom", "bind", "foulplay", "electroweb", "hypervoice",
    "covet", "dualchop", "drillrun", "wildcharge", "snarl", "grasspledge", "firepledge", "waterpledge", "frenzyplant",
    "blastburn", "hydrocannon", "dracometeor", "infestation", "naturepower", "poweruppunch", "dazzlinggleam", "confide", "leechlife",
    "brutalswing", "smartstrike", "auroraveil", "liquidation", "stompingtantrum", "throatchop", "laserfocus", "megapunch", "megakick",
    "payday", "pinmissile", "magicalleaf", "solarblade", "firespin", "screech", "selfdestruct", "scaryface", "charm",
    "beatup", "revenge", "imprison", "weatherball", "faketears", "sandtomb", "iciclespear", "mudshot", "rockblast",
    "assurance", "powerswap", "guardswap", "speedswap", "thunderfang", "icefang", "firefang", "psychocut", "crosspoison",
    "hex", "razorshell", "tailslap", "phantomforce", "drainingkiss", "grassyterrain", "mistyterrain", "electricterrain", "psychicterrain",
    "mysticalfire", "eerieimpulse", "airslash", "breakingswipe", "bodyslam", "hydropump", "agility", "focusenergy", "metronome",
    "amnesia", "triattack", "reversal", "spikes", "megahorn", "batonpass", "encore", "crunch", "futuresight",
    "blazekick", "cosmicpower", "muddywater", "leafblade", "dragondance", "closecombat", "toxicspikes", "flareblitz", "aurasphere",
    "bugbuzz", "powergem", "bravebird", "nastyplot", "leafstorm", "powerwhip", "heavyslam", "electroball", "storedpower",
    "heatcrash", "hurricane", "playrough", "venomdrench", "darkestlariat", "highhorsepower", "pollenpuff", "psychicfangs", "bodypress",
    "steelbeam", "terrainpulse", "burningjealousy", "flipturn", "risingvoltage", "grassyglide", "tripleaxel", "coaching", "corrosivegas",
    "scorchingsands", "dualwingbeat", "expandingforce", "skittersmack", "meteorbeam", "poltergeist", "scaleshot", "lashout", "steelroller",
    "mistyexplosion", "iceball", "powershift", "babydolleyes", "takedown", "acidspray", "psybeam", "confuseray", "disarmingvoice",
    "trailblaze", "pounce", "chillingwater", "poisontail", "metalclaw", "nightshade", "snowscape", "icespinner", "terablast",
    "charge", "haze", "lunge", "doubleedge", "petalblizzard", "temperflare", "supercellslam", "featherdance", "metalsound",
    "curse", "hardpress", "dragoncheer", "alluringvoice", "psychicnoise", "upperhand"
]

# 99% of this is a subset of TMs since Tutors no longer exist in the official games.
# Since addons use the source anyways, we will provide some minor redundancy to maintain support. :)
# The assumption here is if we decide to have special tutor moves, this list won't just repeat TMs.
# This list will probably be useless then, because deviating from official sets would require manual edits to the species files.
tutor_list = [
    "aerialace", "afteryou", "aircutter", "allyswitch", "ancientpower", "aquatail", "babydolleyes",
    "bind", "blastburn", "block", "bounce", "bugbite", "bulkup", "bulldoze",
    "burningjealousy", "calmmind", "chargebeam", "coaching", "corrosivegas", "covet", "darkpulse",
    "dazzlinggleam", "defog", "dive", "dracometeor", "dragonascent", "dragonpulse", "drainpunch",
    "drillrun", "dualchop", "dualwingbeat", "earthpower", "electroweb", "endeavor", "energyball",
    "expandingforce", "falseswipe", "firefang", "firepledge", "firepunch", "flamethrower", "flashcannon",
    "flipturn", "focusenergy", "focuspunch", "foulplay", "frenzyplant", "furycutter", "gastroacid",
    "gigadrain", "gigaimpact", "grasspledge", "grassyglide", "gravity", "gunkshot", "healbell",
    "heatwave", "helpinghand", "highhorsepower", "hydrocannon", "hyperbeam", "hypervoice", "iceball",
    "icebeam", "icefang", "icepunch", "icywind", "irondefense", "ironhead", "irontail",
    "knockoff", "laserfocus", "lashout", "lastresort", "leechlife", "liquidation", "lowkick",
    "magiccoat", "magicroom", "magicalleaf", "magnetrise", "megahorn", "meteorbeam", "mistyexplosion",
    "mudslap", "mysticalfire", "ominouswind", "outrage", "painsplit", "playrough", "poisonjab",
    "poltergeist", "powershift", "psychic", "psychocut", "recycle", "relicsong", "rest",
    "risingvoltage", "rockslide", "rocksmash", "roleplay", "rollout", "roost", "scaleshot",
    "scorchingsands", "secretsword", "seedbomb", "shadowball", "shadowclaw", "shockwave", "signalbeam",
    "skillswap", "skittersmack", "skyattack", "sleeptalk", "sludgebomb", "snarl", "snatch",
    "snore", "spikes", "spite", "stealthrock", "steelbeam", "steelroller", "stompingtantrum",
    "stoneedge", "stringshot", "suckerpunch", "superfang", "superpower", "swift", "synthesis",
    "tailwind", "telekinesis", "terrainpulse", "throatchop", "thunderfang", "thunderpunch", "thunderbolt",
    "triaattack", "trick", "tripleaxel", "twister", "uproar", "vacuumwave", "volttackle",
    "waterpledge", "waterpulse", "wildcharge", "wonderroom", "worryseed", "xscissor", "zenheadbutt"
]

# These Pokemon are always allowed to learn every External Move in the games.
# By "these" we mean Mew. Mew is the only one, but even it isn't always in every game.
# Unless the list of TMs ONLY represents games where Mew is present, this is necessary.
always_tm_species = [
    "mew"
]

# These Pokemon are never allowed to learn any moves outside of their available set.
# Addons can do as they please, but "official" behavior says never-ever.
never_tm_species = [
    "magikarp", "ditto", "unown", "wobbuffet", "smeargle", "wynaut", "pyukumuku", "cosmog", "cosmoem"
]

# These Pokemon would be part of the "never_tm" list if not for Tera Blast.
# We don't know if there will be other universal gimmick moves like Tera Blast, better safe than sorry.
# We still won't teach these anything aside from Tera Blast.
sometimes_tm_species = [
    "caterpie", "metapod", "weedle", "kakuna", "wurmple", "silcoon", "cascoon", "beldum", "kricketot",
    "burmy", "combee", "tynamo", "scatterbug", "spewpa", "blipbug", "applin", "snom"
]

# List of desired moves that are learned by everything, where Pokemon haven't appeared in a game its featured in.
# We should ideally never add to this list, best to make no assumptions on who learns what unless game data suggests.
# Protect and Substitute are examples of this, but not necessary since they have consistently appeared every game.
universal_tms = [
]

# Only do this because it is such a pivotal component of Terastal.
# It's also not quite just a "universal tm" because even more Pokemon than those learn it.
super_universal_tms = [
    "terablast"
]

# List of moves that when available have been learnable by everyone, but haven't been TMs for some time.
# If desired, place these in universal_tms to replicate the behavior from the games these moves were TMs in.
# Otherwise, restrict them to only those who learn organically to avoid overcentralization. 
# (You don't really want everything to learn Double Team and Swagger, right? RIGHT?!)
ambiguously_present_tms = [
    "captivate", "attract", "naturalgift", "secretpower", "confide", "swagger",
    "doubleteam", "round", "snore"
]

# Though they aren't in recent TM availability, Captivate and Attract are reasonably learnable by all who have a gender.
# If you want to include them as TMs, include them here.
# Note: Nincada/Ninjask lost these due to an additional rule with genderless evos.
gender_only_tms = [
]

# These are moves that have been designated as "signature moves" for particular Pokemon.
# While they could be functional TMs, only being learnable by one line is a deterrent.
# Note: Hidden Power, Return, and Frustration are formerly universal.
# Keep them here if you want "latest" mechanics, remove them if you long for the past. (Beware HP Ice Regieleki!)
signature_move_tms = [
    "volttackle", "hiddenpower", "return", "frustration", "secretsword", "relicsong", "dragonascent"
]

# List of directories to process
base_directories = [
    'species/generation1', 'species/generation2', 'species/generation3', 'species/generation4', 
    'species/generation5', 'species/generation6', 'species/generation7', 'species/generation7b', 
    'species/generation8', 'species/generation8a', 'species/generation9'
]
output_directory = 'species'

def process_species_file(base_species_data, base_species_path, output_file_path):
    os.makedirs(os.path.dirname(output_file_path), exist_ok=True)
    
    def process_moves(moves):
        for move in tutor_list:
            tutor_move = f"tutor:{move}"
            if any(move in m for m in moves) and tutor_move not in moves:
                moves.append(tutor_move)
        
        # Curates TM source for "tighter" representation of what is actually an externally available move.
        # Note: This will break custom/datapackable TMs in the future and necessitates manual edits to get them to work.
        moves = [move for move in moves if not (move.startswith('tm:') and move.split(':')[1] not in potential_tm_list and any(
            move.split(':')[1] in m for m in moves if m.split(':')[0].isdigit() or m.startswith('egg:')
        ))]
        
        # Give em the ol' sort-a-roo.
        egg_moves = sorted([move for move in moves if move.startswith('egg:')])
        tm_moves = sorted([move for move in moves if move.startswith('tm:')])
        tutor_moves = sorted([move for move in moves if move.startswith('tutor:')])
        other_moves = [move for move in moves if not (move.startswith('tm:') or move.startswith('tutor:') or move.startswith('egg:'))]
        
        moves = other_moves + egg_moves + tm_moves + tutor_moves
        
        return moves

    # Mew? Mew. (Ensures Mew's functionality isn't compromised by the games' differing species/move availability.)
    if base_species_data['name'].lower() in always_tm_species:
        for move in potential_tm_list:
            tm_move = f"tm:{move}"
            if tm_move not in base_species_data['moves']:
                base_species_data['moves'].append(tm_move)

    # Process base species moves
    base_species_data['moves'] = process_moves(base_species_data.get('moves', []))

    # Process moves for each form
    for form in base_species_data.get('forms', []):
        form['moves'] = process_moves(form.get('moves', []))
    
    # Write the modified data to the output file
    with open(output_file_path, 'w') as output_file:
        json.dump(base_species_data, output_file, indent=2)

# Process each species file in the base directories and its subdirectories
for base_directory in base_directories:
    for root, _, files in os.walk(base_directory):
        for filename in files:
            if filename.endswith('.json'):
                base_species_path = os.path.join(root, filename)
                with open(base_species_path, 'r') as file:
                    base_species_data = json.load(file)
                
                relative_path = os.path.relpath(base_species_path, base_directory)
                output_file_path = os.path.join(output_directory, base_directory.split('/')[-1], relative_path)
                process_species_file(base_species_data, base_species_path, output_file_path)
