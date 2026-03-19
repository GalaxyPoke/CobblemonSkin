import os
import time
import requests
import pandas as pd
import json
from io import StringIO
from sqlalchemy import create_engine
from tqdm import tqdm
from scriptutils import printCobblemonHeader, print_cobblemon_script_description, print_cobblemon_script_footer, \
    print_list_filtered, print_warning, sanitize_pokemon

# Initialize lists for report
no_riding_base_forms = []
removed_riding_pokemon = []
no_form_entry_files = []
no_riding_forms = []

form_entry_mapping = {
    "Alolan": "Alola",
    "Galarian": "Galar",
    "Hisuian": "Hisui",
    "Paldean": "Paldea",
}

flavours = [
    'SPEED',
    'ACCELERATION',
    'SKILL',
    'JUMP',
    'STAMINA'
]

controller_keys = {
    'Standard': 'cobblemon:land/generic',
    'Vehicle': 'cobblemon:land/vehicle',
    #'Submarine': 'cobblemon:water/submarine',
    #'Dolphin': 'cobblemon:water/dolphin',
    #'Burst': 'cobblemon:water/burst',
    'Boat': 'cobblemon:water/boat',
    #'Roll': 'cobblemon:land/roll',
    #'Jet': 'cobblemon:air/jet',
    'Bird': 'cobblemon:air/bird',
    #'UFO': 'cobblemon:air/ufo',
    'Glider': 'cobblemon:air/glider',
    #'Airship': 'cobblemon:air/airship',
    'Fall To Flight': 'cobblemon:composite/fall_to_flight',
    'Run Up To Flight': 'cobblemon:composite/run_up_to_flight'
}

def main(dex_range=None):
    """
    The riding_csv to json script does the following:
    1. Retrieves the DataFrame containing the riding data.
    2. Filters the filenames by Pokémon names contained in riding df.
    3. Modifies the files based on the riding data.
    4. Writes the modified data back to the files.
    """
    # Print header
    printCobblemonHeader()
    script_name = "Cobblemon Riding Generator"
    script_description = "This script generates the riding data for each Pokémon based on the data in a Google Spreadsheet. It also generates the riding properties for each form of a Pokémon, if the Pokémon has any forms and if the forms riding settings are different from the base form. It also generates a report, hinting at potential problems with the data in the Google Spreadsheet or in the JSON files."
    print_cobblemon_script_description(script_name, script_description)

    # Retrieve the DataFrame containing the riding data
    riding_df, pokemon_data_dir = get_riding_df(dex_range)

    # Add a forms column to the DataFrame, and sanitize the Pokémon names
    riding_df['forms'] = riding_df['Species'].apply(lambda x: x.split("[")[1].split("]")[0] if "[" in x else None)
    riding_df['Species'] = riding_df['Species'].apply(lambda san: sanitize_pokemon(san.split("[")[0].strip()))

    # Group the riding data by Pokémon names, then convert it to a dictionary
    riding_dict = riding_df.groupby('Species').apply(lambda x: x.to_dict(orient='records'),
                                                   include_groups=False).to_dict()
    # Filter the filenames by Pokémon names
    files_to_change = filter_filenames_by_pokemon_names(pokemon_data_dir, riding_df['Species'])

    # Modify the files based on the riding data
    print_warning("Modifying files...")
    for file in tqdm(files_to_change, bar_format='\033[92m' + '{l_bar}\033[0m{bar:58}\033[92m{r_bar}\033[0m',colour='blue'):
        try:
            modify_files(file, pokemon_data_dir, riding_dict)
        except Exception as e:
            print_warning(f"Error modifying {file}: {e}")
            raise e

    # Print report
    if no_riding_base_forms:
        print("\nNo riding specified for base forms in the sheet, but species files exists:")
        print_list_filtered(no_riding_base_forms)
    if no_riding_forms:
        print("\nNo riding specified for forms in the sheet, even though form entries exist in .json files:")
        print_list_filtered(no_riding_forms)
    if removed_riding_pokemon:
        print("\nRemoved riding specified for Pokémon in the sheet:")
        print_list_filtered(removed_riding_pokemon)
    if no_form_entry_files:
        print("\nNo form entry found in the respective .json file for the following pokemon, but form riding stats were specified in the sheet:")
        print_list_filtered(no_form_entry_files)
    print_cobblemon_script_footer("Thanks for using the Cobblemon Riding Generator, provided to you by Waldleufer and Hiroku!")



def get_riding_df(dex_range=range(0, 1111)):
    riding_spreadsheet_csv_url = 'https://docs.google.com/spreadsheets/d/e/2PACX-1vSxuI2nEgXfel-msRbUwezSxfhroiCJ_Ti_sZB9Ezk1c40fYu_TYdADQv_rXb0Zil3yRAdtysACLp_r/pub?gid=0&single=true&output=csv'
    pokemon_data_dir = '../common/src/main/resources/data/cobblemon/species'
    # Download the CSV from the Google Spreadsheet
    csv_data = download_spreadsheet_data(riding_spreadsheet_csv_url)
    # Load the data into a DataFrame
    riding_df = load_data_from_csv(csv_data)

    # Only include the specified range of dex numbers
    if dex_range is not None:
        riding_df = riding_df[riding_df['No.'].isin(dex_range)]

    return riding_df, pokemon_data_dir


def modify_files(file, pokemon_data_dir, riding_dict):
    # Open the file with 'utf-8-sig' encoding and read its contents
    with open(pokemon_data_dir + "/" + file, 'r', encoding="utf-8-sig") as f:
        data = json.load(f)

    # make sure to write the file with utf-8 encoding
    with open(pokemon_data_dir + "/" + file, 'r+', encoding="utf-8") as f:
        pokemon = sanitize_pokemon(data['name'])

        # compare all forms of the file with all forms of the riding_dict pokemon
        file_forms = set(form['name'] for form in data['forms']) if 'forms' in data else set()
        # always add None to the file_forms set, as the base form is always present in the files
        file_forms.add(None)
        riding_dict_forms = set(row['forms'] for row in riding_dict[pokemon] if 'forms' in row)
        # apply the mapping to al the riding_forms
        riding_dict_forms = set(form_entry_mapping.get(form, form) for form in riding_dict_forms)

        # Add forms that are in the file but not in riding_dict to no_riding_forms
        difference = file_forms - riding_dict_forms
        no_riding_forms.extend(f'{pokemon} [{form}]' for form in difference)

        # Add forms that are in riding_dict but not in the file to no_form_entry_files
        no_form_entry_files.extend(f'{pokemon} [{form}]' for form in riding_dict_forms - file_forms)

        for row in riding_dict[pokemon]:
            pokemon_form = row['forms'] if 'forms' in row else None
            blank_riding = has_riding(row) == False

            if pokemon in riding_dict:
                none_existed = False
                if 'forms' in data:
                    for form in data['forms']:
                        # There was an explicit 'None' form? Bet, won't need to do a later process
                        if form['name'] == 'None':
                            none_existed = True
                        # Apply the form entry mapping to the form name if applicable
                        if pokemon_form in form_entry_mapping:
                            pokemon_form = form_entry_mapping[pokemon_form]
                        if form['name'] == pokemon_form:
                            if blank_riding == True:
                                no_riding_forms.append(f'{pokemon} [{pokemon_form}]')
                                form.pop('riding', None)
                            else:
                                apply_riding(form, row)
                if none_existed == False and pokemon_form == None:
                    if blank_riding:
                        no_riding_base_forms.append(file)
                        data.pop('riding', None)
                    else:
                        apply_riding(data, row)
                # Clean duplicates from forms
                if 'forms' in data:
                    for form in data['forms']:
                        if 'riding' in form and 'riding' in data:
                            if form['riding'] == data['riding']:
                                form.pop('riding')
        # Write the modified data back to the file
        f.seek(0)
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.truncate()

def apply_riding(pokemon_form, riding_row):
    if has_riding(riding_row) == False:
        pokemon_form.pop('riding', None)
        return

    if 'riding' not in pokemon_form:
        pokemon_form['riding'] = {}

    riding = pokemon_form['riding']

    if 'stats' not in riding:
        riding['stats'] = {}

    stats = riding['stats']
    if 'seats' not in riding:
        riding['seats'] = []

    has_land = riding_row['Land'] != 'N/A'
    has_water = riding_row['Water'] != 'N/A'
    has_flying = riding_row['Flying'] != 'N/A'

    if riding_row['Land'] != 'N/A':
        set_flavour_ranges(stats, riding_row, 0, has_water or has_flying)
    if riding_row['Water'] != 'N/A':
        set_flavour_ranges(stats, riding_row, 1, has_flying or has_land)
    if riding_row['Flying'] != 'N/A':
        set_flavour_ranges(stats, riding_row, 2, has_land or has_water)

    riding['controller'] = setup_controller(riding_row, riding)

def merge_controller(riding_row, previous_controller, new_controller):
    if previous_controller['key'] == new_controller['key']:
        if 'composite' in previous_controller['key']:
            if 'landController' in previous_controller:
                previous_controller['landController'] = merge_controller(riding_row, previous_controller['landController'], new_controller['landController'])
                if 'landController' not in previous_controller:
                    return None
            if 'flightController' in previous_controller:
                previous_controller['flightController'] = merge_controller(riding_row, previous_controller['flightController'], new_controller['flightController'])
                if 'flightController' not in previous_controller:
                    return None
            if 'swimController' in previous_controller:
                previous_controller['swimController'] = merge_controller(riding_row, previous_controller['swimController'], new_controller['swimController'])
                if 'swimController' not in previous_controller:
                    return None
        return previous_controller
    else:
        return new_controller

def setup_controller(riding_row, riding):
    new_controller = None
    if riding_row['Composite'] != 'N/A':
        # Setup a composite controller
        new_controller = build_controller(riding_row, riding_row['Composite'])
    elif riding_row['Land'] != 'N/A':
        # Setup a specific controller
        new_controller = build_controller(riding_row, riding_row['Land'])
    elif riding_row['Water'] != 'N/A':
        # Setup a specific controller
        new_controller = build_controller(riding_row, riding_row['Water'])
    elif riding_row['Flying'] != 'N/A':
        # Setup a specific controller
        new_controller = build_controller(riding_row, riding_row['Flying'])

    if new_controller != None and 'controller' in riding:
        return merge_controller(riding_row, riding['controller'], new_controller)
    else:
        return new_controller

def build_controller(riding_row, type):
    controller = {}
    controller_key = controller_keys.get(type, None)
    if controller_key == None:
        return None
    controller['key'] = controller_key
    if type == 'Fall To Flight':
        controller['flightController'] = build_controller(riding_row, riding_row['Flying'])
        controller['landController'] = build_controller(riding_row, riding_row['Land'])
    elif type == 'Run Up To Flight':
        controller['flightController'] = build_controller(riding_row, riding_row['Flying'])
        controller['landController'] = build_controller(riding_row, riding_row['Land'])

    return controller

def set_flavour_ranges(stats, riding_row, category_index, has_others):
    for i in range(5):
        flavour_name = flavours[i]
        flavour_range = get_flavour_range(riding_row, category_index, i)
        if flavour_range[0] == '' or flavour_range[1] == '':
            continue
        flavour_object = stats.get(flavour_name, {})
        if 'ranges' not in flavour_object:
            flavour_object['ranges'] = {}
        else:
            ranges_object = flavour_object['ranges']
            if 'FLYING' in ranges_object:
                ranges_object.pop('FLYING', None)
            if 'WATER' in ranges_object:
                ranges_object.pop('WATER', None)
        if has_others == False:
            flavour_object['ranges'] = str(flavour_range[0]) + "-" + str(flavour_range[1])
        else:
            category_label = 'LAND'
            flavour_object.pop('range', None)
            if category_index == 1:
                category_label = 'LIQUID'
            elif category_index == 2:
                category_label = 'AIR'

            ranges = flavour_object['ranges']
            ranges[category_label] = str(flavour_range[0]) + "-" + str(flavour_range[1])
        stats[flavours[i]] = flavour_object

# Category index: 0 for Land, 1 for Water, 2 for Flying, 3 for Special
# Flavour index: 0 for Sweet/Speed, 1 for Spicy/Accel, 2 for Dry/Skill, 3 for Bitter/Jump, 4 for Sour/Stamina
def get_flavour_range(riding_row, category_index, flavour_index):
    index = category_index * 5 + flavour_index
    suffix = '.' + str(index)
    if index == 0:
        suffix = ''
    return [riding_row['Min' + suffix], riding_row['Max' + suffix]]

def has_flavour_ranges(riding_row, category_index):
    for i in range(5):
        if get_flavour_range(riding_row, category_index, i)[0] != '' and get_flavour_range(riding_row, category_index, i)[1] != '':
            return True
    return False

def download_spreadsheet_data(url, max_retries=10):
    delay = 1
    for attempt in range(max_retries):
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            return response.content.decode('utf-8')
        except requests.RequestException as e:
            if attempt < max_retries - 1:
                time.sleep(delay)
                delay *= 2
            else:
                raise e


def load_data_from_csv(csv_data):
    return pd.read_csv(StringIO(csv_data), encoding='utf8', engine='python', skiprows=2, dtype={'Species': str, 'Composite': str, 'Water': str, 'Flying': str, 'Special': str }, na_filter=False)
def has_riding(riding_row):
    return riding_row['Composite'] != 'N/A' or (riding_row['Land'] != 'N/A' and has_flavour_ranges(riding_row, 0)) or (riding_row['Water'] != 'N/A' and has_flavour_ranges(riding_row, 1)) or (riding_row['Flying'] != 'N/A' and has_flavour_ranges(riding_row, 2))

def filter_filenames_by_pokemon_names(directory, pokemon_names):
    # Apply the sanitize_pokemon function to the pokemon_names
    pokemon_names = pokemon_names.apply(sanitize_pokemon)

    # Get list of subdirectories in the provided directory
    subdirectories = [d for d in os.listdir(directory) if os.path.isdir(os.path.join(directory, d))]

    all_files = []
    for subdir in subdirectories:
        # List all files from the subdirectory
        files_in_subdir = os.listdir(os.path.join(directory, subdir))
        # Extend the all_files list with these files
        # While adding, prepend the subdirectory name
        all_files.extend([f"{subdir}/{file}" for file in files_in_subdir])

    filtered_files = [file for file in all_files if
                      file.split('/')[-1][:-5].lower() in pokemon_names.str.lower().tolist()]

    # Get the files that did not pass the filter
    not_filtered_files = [file for file in all_files if file not in filtered_files]

    if not_filtered_files:
        print("\nSpecies file found, but ignored:  [[located at resources/data/cobblemon/species/]]")
        print_list_filtered(not_filtered_files)
    return filtered_files


if __name__ == "__main__":
    dex_range = range(0, 1111)
    main(dex_range)
