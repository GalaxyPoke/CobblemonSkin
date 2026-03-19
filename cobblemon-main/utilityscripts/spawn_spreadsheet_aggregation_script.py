import pandas as pd
from io import StringIO
import cobblemon_spawn_csv_to_json as spawn

# This script is used to aggregate the data from the spreadsheet and write it to a file,
# to be copy and pasted back to the spreadsheet for entry unification
def main():
    df = spawn.download_excel_data(spawn.spawn_spreadsheet_excel_url)
    # Validate and filter the data
    df = spawn.validateAndFilterData(df)
    aggregated_df = aggregate_data(df)
    # write the output in a file to copy and paste in a excel spreadsheet
    aggregated_df.to_csv('copyAndPasteMeContents.csv', index=False, header=False, sep='\t')
    # write the output in the sqlite database
    # spawn.write_to_sqlite(aggregated_df, spawn.sqlite_db_name, "copying")


# Function to read and clean the data
def process_data(data):
    df = pd.read_csv(StringIO(data), sep='\t', header=None)
    df = df.dropna(axis=1, how='all')
    return df


# Function to apply the aggregation according to the specified rules
def aggregate_data(df):
    # Extract the first digit from the Entry column and create a new column
    df['EntryGroup'] = df['Entry'].astype(str).str[0]

    # Define the aggregation rules for non-unique columns
    special_aggregation_rules = {
        'Biome': lambda x: ', '.join(sorted(set(x.dropna().astype(str)))), # Join unique biomes with comma
        # do the same for Excluded
        'Excluded': lambda x: ', '.join(sorted(set(x.dropna().astype(str)))),
        # ...
    }
    # For columns that don't have special rules, just take the first non-NaN value, or NaN if all are NaN
    default_aggregation_rules = {col: lambda x: x.dropna().iloc[0] if x.dropna().size > 0 else pd.NA for col in
                                 df.columns if col not in special_aggregation_rules}

    # Combine the special rules with the default rules
    aggregation_rules = {**default_aggregation_rules, **special_aggregation_rules}

    # Define columns to check for consistency and raise an error if there are multiple different non-NaN values per group
    consistent_columns = ['Time', 'Weather', 'Multiplier', 'Context', 'Preset', 'Requirements', 'Prohibitions',
                          'Bucket', 'Weight', 'Lv. Min', 'Lv. Max', 'canSeeSky', 'Type', 'AI Type']
    for col in consistent_columns:
        # Group by 'Gen', 'No.', and 'EntryGroup', then check if there's more than one unique non-NaN value
        if df.groupby(['Gen', 'No.', 'EntryGroup'])[col].nunique(dropna=True).max() > 1:
            # Print any row where nonunique values > 1
            print(df[df.groupby(['Gen', 'No.', 'EntryGroup'])[col].transform('nunique') > 1])
            print("Printed")
            raise ValueError(f'Column {col} has inconsistent non-NaN values within the group.')

    print(df.head())
    # Perform the aggregation
    aggregated_df = df.groupby(['Gen', 'No.', 'EntryGroup'], as_index=False).agg(aggregation_rules)
    # Replace the Entry values with the EntryGroup values
    aggregated_df['Entry'] = aggregated_df['EntryGroup']

    # Retain the original order of columns
    aggregated_df = aggregated_df[df.columns]

    # drop the 'EntryGroup' column
    aggregated_df = aggregated_df.drop('EntryGroup', axis=1)

    return aggregated_df


if __name__ == "__main__":
    main()