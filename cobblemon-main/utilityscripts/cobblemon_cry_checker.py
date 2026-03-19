import json
import os
import re
from io import StringIO
import pandas as pd
from mutagen.oggvorbis import OggVorbis
from cobblemon_drops_csv_to_json import download_spreadsheet_data
from scriptutils import printCobblemonHeader, print_list_filtered, print_cobblemon_script_description, \
    print_cobblemon_script_footer, print_problems_and_paths, print_warning, sanitize_pokemon, print_separator

DEFAULT_POKEMON_MODELS_PATH = "../common/src/main/kotlin/com/cobblemon/mod/common/client/render/models/blockbench/pokemon/"
DEFAULT_POKEMON_POSER_PATH = "../common/src/main/resources/assets/cobblemon/bedrock/pokemon/posers/"

# Download the CSV file
ASSETS_CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vSTisDTkJvV0GzKV1zKjAPdMAQAO7znWxjEWXrM1gZPUVmsTU91oy54aJGMpbbvOqAOg03ER1wl7eeA/pub?gid=0&single=true&output=csv"


def main(print_missing_models=True, print_missing_animations=True):
    """
    cobblemon_cry_checker.py checks the consistency between the cry status of Pokémon in a CSV file and the actual
    presence of their cry audio files in a specific directory. It also checks for the presence of the corresponding
    model and animation files for each Pokémon. The function prints out any inconsistencies it finds.

    Parameters:
    print_missing_models (bool): If set to True, the function will print out the names of Pokémon for which model files are missing.
    print_missing_animations (bool): If set to True, the function will print out the names of Pokémon for which animation files are missing.

    Author: Waldleufer
    """

    printCobblemonHeader()

    scriptname = "Cobblemon Cry Checker"
    scriptdescription = "This script checks the consistency between the cry status of Pokémon in a CSV file and the actual presence of their cry audio files in a specific directory. It also checks for the presence of the corresponding model and animation files for each Pokémon. The function prints out any inconsistencies it finds. WARNING: G-Max and Mega Pokémon are not checked yet."

    print_cobblemon_script_description(scriptname, scriptdescription)
    # Load the CSV data into a DataFrame
    csv_data = download_spreadsheet_data(ASSETS_CSV_URL)
    csv_file = StringIO(csv_data)
    # Load the data into a DataFrame while specifying the types of the columns, skip NaN values
    df = pd.read_csv(csv_file, dtype={0: str, 1: str, 2: str, 53: str}, skip_blank_lines=True)

    # Extract the required columns
    gen_numbers = df.iloc[3:, 0]  # Column A
    dex_numbers = df.iloc[3:, 1]  # Column B
    pokemon_names = df.iloc[3:, 2]  # Column C
    pokemon_in_game = df.iloc[3:, 9]  # Column I
    cries_on_repo = df.iloc[3:, 54]  # Column BC
    cries_in_game = df.iloc[3:, 55]  # Column BD [Cry Audio | In-Game]

    # Initialize lists for false positives and negatives
    false_positives = []
    false_negatives = []
    implemented_and_not_marked = []
    invalid_model_files = []
    invalid_animation_files = []
    all_warnings_combined = []
    pokemon_ready_to_be_added_list = []

    # Iterate over the DataFrame rows
    for pokemon_name, gen_number, dex_number, cry_in_game, this_pokemon_in_game, this_cry_on_repo in zip(pokemon_names,
                                                                                                         gen_numbers,
                                                                                                         dex_numbers,
                                                                                                         cries_in_game,
                                                                                                         pokemon_in_game,
                                                                                                         cries_on_repo):
        if this_pokemon_in_game == "❌":
            continue
        cry_in_game = str(cry_in_game).strip()  # remove whitespace
        cry_on_repo = str(this_cry_on_repo).strip()  # remove whitespace
        # make the first letter of the Pokémon name uppercase and remove all spaces
        sanitized_pokemon_name_lower = sanitize_pokemon(pokemon_name)
        # if [ ] are present in the name, remove them and anything in between
        sanitized_pokemon_name_lower = sanitized_pokemon_name_lower.split("[")[0]
        sanitized_pokemon_name = sanitized_pokemon_name_lower.capitalize()
        pokemon_form = pokemon_name.split("[")[1].split("]")[0].lower() if "[" in pokemon_name else None

        # Construct the path to the audio file
        if pokemon_form:
            audio_file_path = f"../common/src/main/resources/assets/cobblemon/sounds/pokemon/{sanitized_pokemon_name_lower}/{sanitized_pokemon_name_lower}_{pokemon_form}_cry.ogg"
        else:
            audio_file_path = f"../common/src/main/resources/assets/cobblemon/sounds/pokemon/{sanitized_pokemon_name_lower}/{sanitized_pokemon_name_lower}_cry.ogg"

        checks = {
            'on_repo': False,
            'uses_poser': False,
            'poser_correct': False,
            'import_correct': False,
            'override_correct': False,
            'cry_in_game': False,
            'audio_file_exists': False,
            'sound_effects_and_keyframes': False,
        }

        # Hardcoded odd cases that should be ignored
        if pokemon_name == "Aegislash [Shield]" or pokemon_name == "Aegislash [Blade]" or pokemon_name == "Basculin [Red-Striped]" or pokemon_name == "Oinkologne":
            continue

        # Construct the path to the model file
        ## Hello, commented this out because it probably doesn't do anything, but Wald can scold me if it was important  - Yours truly, Sterr
        # if pokemon_name == "Rattata [Alolan]":
        #     print(pokemon_name)

        # Try to convert gen_number to an integer and handle ValueError
        try:
            if pokemon_form:
                # Check if the form is a regional form or F or M, if not, use the default model file
                model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}{pokemon_form.capitalize()}Model.kt"
                if pokemon_form == "F" or pokemon_form == "M":
                    if pokemon_form == "F":
                        model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}FemaleModel.kt"
                    elif pokemon_form == "M":
                        model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}MaleModel.kt"
                    if not os.path.isfile(model_file_path):
                        # Fallback for models that share one model file and all forms that are no regional forms
                        model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}Model.kt"
                elif pokemon_form in ["hisuian", "alolan", "galarian", "valencian", "paldean"]:
                    # search for the model file with the form name in all gen folders
                    model_pokemon_path = DEFAULT_POKEMON_MODELS_PATH
                    for gen_folder in os.listdir(model_pokemon_path):
                        if os.path.isdir(os.path.join(model_pokemon_path, gen_folder)):
                            model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}{gen_folder}/{sanitized_pokemon_name}{pokemon_form.capitalize()}Model.kt"
                            if not os.path.isfile(model_file_path):
                                model_file_path = ""
                                continue
                            break
                    if not model_file_path:
                        model_file_path = f"../gen*/{sanitized_pokemon_name}{pokemon_form.capitalize()}Model.kt"
                else:
                    model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}Model.kt"
            else:
                model_file_path = f"{DEFAULT_POKEMON_MODELS_PATH}gen{int(gen_number.strip())}/{sanitized_pokemon_name}Model.kt"
        except ValueError:
            all_warnings_combined.append(
                f"⚠️ Warning: Invalid gen_number for pokemon {pokemon_name}. Skipping this line.")
            continue  # Construct the path to the animation.json file
        dex_number = str(dex_number).zfill(4)  # prepend with 0s to make it 4 digits long
        animation_file_path = f"../common/src/main/resources/assets/cobblemon/bedrock/pokemon/animations/{dex_number}_{sanitized_pokemon_name_lower.lower()}/{sanitized_pokemon_name_lower.lower()}.animation.json"

        # Check if the audio file exists
        checks["audio_file_exists"] = os.path.isfile(audio_file_path)
        # if the length of the audio file is smaller or equals to 0.11725 seconds, it is considered empty
        if checks["audio_file_exists"]:
            audio = OggVorbis(audio_file_path)
            if audio.info.length <= 0.11725:
                checks["audio_file_exists"] = False

        # Check the cry status
        if cry_in_game == "✔":
            checks["cry_in_game"] = True

        # If the cry isn't supposed to exist whatsoever, it'll be skipped
        if cry_on_repo == "🛇":
            continue

        animations = None

        # Check if the Model.kt file exists and contains the required import and cryAnimation override
        json_poser_path = os.path.join(DEFAULT_POKEMON_POSER_PATH, dex_number + "_" + sanitized_pokemon_name_lower, sanitized_pokemon_name_lower + ".json")
        if os.path.isfile(model_file_path):
            content = read_file_ignore_comments(model_file_path)
            if "import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider" in content:
                checks["import_correct"] = True
            if f'override val cryAnimation = CryProvider {{ _, _ -> bedrockStateful("{sanitized_pokemon_name_lower}", "cry") }}' in content:
                checks["override_correct"] = True
            if f'animations["cry"] = "q.bedrock_stateful(\'{sanitized_pokemon_name_lower}\', \'cry\')".asExpressionLike()' in content:
                checks["override_correct"] = True
                checks["import_correct"] = True
            if not checks["override_correct"]:
                # it might be an elaborate cry with multiple animations
                # Search for the override and capture the content between the curly braces
                pattern = r'override val cryAnimation = CryProvider \{(.*?)\}'
                match = re.search(pattern, content, re.DOTALL)

                if match:
                    # Extract the content between the curly braces
                    content_between_braces = match.group(1)
                    # Extract the animations from the content
                    animations, all_warnings_combined, checks["override_correct"] = (
                        read_content_and_extract_animations(
                            content_between_braces,
                            pokemon_name,
                            all_warnings_combined))
        elif os.path.isfile(json_poser_path):
            checks["uses_poser"] = True
            try:
                with open(json_poser_path, 'r', encoding="utf-8-sig") as file:
                    data = json.load(file)
                    if "animations" in data and "cry" in data["animations"] and sanitized_pokemon_name_lower in data["animations"]["cry"]:
                        checks["poser_correct"] = True

            except json.decoder.JSONDecodeError:
                print_warning("Invalid JSON poser in " +  dex_number + "_" + sanitized_pokemon_name_lower + "/" +  sanitized_pokemon_name_lower + ".json")
        else:
            all_warnings_combined.append(
                (pokemon_name, f"⚠️ Warning: Model.kt file or JSON poser not found for: {sanitized_pokemon_name_lower}"))
            # Ignore the checks for this file
            checks["override_correct"] = True
            checks["import_correct"] = True

        # Check if the animation.json file exists and contains the correct effect
        if os.path.isfile(animation_file_path):
            try:
                with open(animation_file_path, 'r', encoding="utf-8-sig") as file:
                    data = json.load(file)
                    if not animations:
                        # Iterate through all animations and check if the cry effect is present
                        for animation_name, animation_data in data['animations'].items():
                            if animation_name == f"animation.{sanitized_pokemon_name_lower}.cry":
                                # Check if the "sound_effects" field is present
                                checks["sound_effects_and_keyframes"] = check_sound_effects(animation_data,
                                                                                            sanitized_pokemon_name_lower)
                    else:
                        for animation_name, animation_data in data['animations'].items():
                            for animation in animations:
                                if animation_name == f"animation.{sanitized_pokemon_name_lower}.{animation[1]}":
                                    # Check if the "sound_effects" field is present
                                    if check_sound_effects(animation_data, sanitized_pokemon_name_lower):
                                        animations.remove(animation)
                        if not animations:
                            checks["sound_effects_and_keyframes"] = True

            except json.decoder.JSONDecodeError:
                print_warning("Invalid JSON in " + animation_file_path.replace(
                    '../common/src/main/resources/assets/cobblemon/bedrock/pokemon/animations/', ""))
        else:
            all_warnings_combined.append(
                (pokemon_name,
                 f"⚠️ Warning: animation.json file not found at: {animation_file_path.replace('../common/src/main/resources/assets/cobblemon/bedrock/pokemon/animations/', '...')}"))
            # Ignore the checks for this file
            checks["sound_effects_and_keyframes"] = True

        # Check the condition
        if this_pokemon_in_game == "✔" and this_cry_on_repo == "✔":
            # check if all checks are true
            if not all(checks.values()):
                pokemon_ready_to_be_added_list.append(pokemon_name)

        # Construct results
        if not checks["cry_in_game"] and checks["audio_file_exists"]:
            false_negatives.append(f"{pokemon_name}")
        elif checks["cry_in_game"] and not checks["audio_file_exists"]:
            false_positives.append(f"{pokemon_name}")

        # Omit entries that are mega evolutions or g-max, they will be worked on at a later date
        if "Mega" in pokemon_name or "G-Max" in pokemon_name:
            continue

        # If any of the checks failed, add the Pokemon to the corresponding lists, but not if all checks are false
        if all(value is False for value in checks.values()) and checks["cry_in_game"] is False:
            continue

        # If both the Pokemon and audio file exist, add it to the list of implemented but unmarked Pokemon
        if not checks["cry_in_game"] and checks["audio_file_exists"]:
            implemented_and_not_marked.append(f"{pokemon_name}")
            continue

        if all(value is True for value in checks.values()):
            continue

        if checks["uses_poser"] and checks["cry_in_game"] and checks["audio_file_exists"]:
            if not checks["poser_correct"]:
                invalid_model_files.append((pokemon_name, " [Poser file has no cry specified]"))
        if checks["cry_in_game"] and checks["audio_file_exists"] and not checks["uses_poser"]:
            if not checks["override_correct"] and not checks["import_correct"]:
                invalid_model_files.append((pokemon_name, " [Model.kt import and override not correct]"))
                all_warnings_combined.append((pokemon_name, "⚠️ Warning: Model.kt import and override not correct"))
            elif not checks["override_correct"]:
                invalid_model_files.append((pokemon_name, " [Model.kt override not correct]"))
                all_warnings_combined.append((pokemon_name, "⚠️ Warning: Model.kt override not correct"))
            elif not checks["import_correct"]:
                invalid_model_files.append((pokemon_name, " [Model.kt import not correct]"))
                all_warnings_combined.append((pokemon_name, "⚠️ Warning: Model.kt import not correct"))

        # If any of the remaining checks failed, add the Pokemon to the all_warnings_combined list
        if not checks["sound_effects_and_keyframes"] and this_cry_on_repo == "✔":
            invalid_animation_files.append((pokemon_name, " [Sound effect keyframe missing or incorrect in animation.json]"))
            all_warnings_combined.append((pokemon_name, "⚠️ Warning: Sound effect in animation.json not correct"))

    # Check if lists are empty
    if not all_warnings_combined and not false_positives and not false_negatives and not invalid_model_files and not invalid_animation_files and not pokemon_ready_to_be_added_list:
        print("No issues found. All cries are in order! ♪♫")
    else:
        # Print out pokemon cries ready to be added to the game
        if pokemon_ready_to_be_added_list:
            print_separator()
            print("\nPokemon that have cries ready to be added to the game:")
            print_list_filtered(pokemon_ready_to_be_added_list)

        # Print out the lists of false positives and negatives
        if false_positives:
            print_separator()
            print("\nCry marked as in-game, but audio file is missing or incorrect:")
            print_list_filtered(false_positives)

        if false_negatives:
            print_separator()
            print(
                "\nAudio file found, but cry not marked as in-game:")
            # create a list of entries that do not contain G-Max or Mega
            false_negatives_filtered_no_gmax_mega = [x for x in false_negatives if "G-Max" not in x and "Mega" not in x]
            print_list_filtered(false_negatives_filtered_no_gmax_mega)

            ## Hello, commented this out to make output cleaner as we're not doing G-Max or Mega cries just yet  - Yours truly, Sterr
            # print("\nFalse negatives (G-Max and Mega are Not Implemented Yet):")
            ## Only print entries that contain G-Max or Mega
            # false_negatives_filtered = [x for x in false_negatives if "G-Max" in x or "Mega" in x]
            # print_list_filtered(false_negatives_filtered)

        if implemented_and_not_marked:
            print_separator()
            print("\nPokemon that are implemented in the game with audio, but not marked as cry ""in-game in the spreadsheet:")
            print_list_filtered(implemented_and_not_marked)

        # Print out the lists of invalid Model.kt and animation.json files
        if invalid_model_files:
            print_separator()
            print(
                "\nIssues with poser or model.kt files:")
            if print_missing_models:
                print_problems_and_paths(invalid_model_files)
            else:
                print_warning("WARNING: [[ main(print_missing_models=False) :: not showing missing Model.kt files]]")
                print_problems_and_paths(invalid_model_files, "Missing Model.kt")

        if invalid_animation_files:
            print_separator()
            print(
                "Invalid animation.json files: (For Pokémon that have a cry on the assets repository)")
            if print_missing_animations:
                print_problems_and_paths(invalid_animation_files)
            else:
                print_warning(
                    "WARNING: [[ main(print_missing_animations=False) :: not showing missing animation.json files]]")
                print_problems_and_paths(invalid_animation_files, "Missing animation.json")

        ## Hello, commented this out to make output cleaner for now  - Yours truly, Sterr
        # if all_warnings_combined:
        #     print_separator()
        #     print("\nAll warnings combined in one list:")
        #     print_problems_and_paths(all_warnings_combined)

    print_cobblemon_script_footer("Thanks for using the Cobblemon cry checker, provided to you by Waldleufer!")


def check_sound_effects(animation_data, sanitized_pokemon_name_lower):
    sound_effects = animation_data.get('sound_effects')
    if sound_effects is not None:
        # Iterate over the sound effects
        for _time_point, effect_data in sound_effects.items():
            # Check if the "effect" field is present and has the correct value
            effect = effect_data.get('effect')
            if effect == f"pokemon.{sanitized_pokemon_name_lower}.cry":
                return True
        return False
    else:
        return False


def read_file_ignore_comments(file_path):
    with open(file_path, 'r', encoding='utf-8-sig') as file:
        lines = file.readlines()
        content = ""
        multi_line_comment = False
        for line in lines:
            stripped_line = line.strip()
            if stripped_line.startswith("//"):
                continue
            elif stripped_line.startswith("/*"):
                multi_line_comment = True
            elif stripped_line.endswith("*/"):
                multi_line_comment = False
                continue
            if not multi_line_comment:
                content += line
    return content


def read_content_and_extract_animations(content, pokemon_name, all_warnings_combined):
    # Find all occurrences of bedrockStateful function call
    pattern = r'bedrockStateful\("(.*?)", "(.*?)"\)'
    matches = re.findall(pattern, content)

    # Extract pokemonName and cryAnimationName from each occurrence
    animations = [(match[0], match[1]) for match in matches]

    # Count the number of occurrences
    count = len(matches)

    # Count the number of bedrockStateful function calls
    bedrockStateful_count = content.count('bedrockStateful')

    # Check if the pokemonName was misspelled
    if count != bedrockStateful_count:
        all_warnings_combined.append(
            (pokemon_name, f"⚠️ Warning: The pokemonName might be misspelled in the Model.kt file."))

    return animations, all_warnings_combined, count == bedrockStateful_count


if __name__ == "__main__":
    main()
    input("Press Enter to exit...")
