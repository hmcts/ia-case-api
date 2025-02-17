import json
import re
import csv
from datetime import datetime

#####################################################################################################
# This script produces 2 output csv files
# ES_DLQ_all_records_<<timestamp>>.csv - contains all records from DLQ files ordered by timestamp.
# ES_DLQ_unique_case_records_<<timestamp>>.csv - contains records with unique case references
#                                           with reason from the latest record in the DLQ files.
#####################################################################################################

# List of input file paths
input_files = [
    "DlqBatch1.txt",
    "DlqBatch2.txt"
    ]

# Get the current timestamp
current_timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

# Output file path
output_file_path = f"ES_DLQ_all_records_{current_timestamp}.csv"
unique_records_file_path = f"ES_DLQ_unique_case_records_{current_timestamp}.csv"

# Initialize an empty list to store results and a set for unique references
results = []

# patterns to extract case reference, state, timestamp (when the case put in DLQ) and reason (error message)
reference_pattern = r'"ccdReferenceNumberForDisplay"\s*=>\s*"([\d\s]+)"'
state_pattern = r'"state"\s*=>\s*"([^"]+)"'
timestamp_pattern = r'"@timestamp"\s*=>\s*([\d\-:T.]+)'
reason_pattern = r'"reason"\s*=>\s*"([^"]+)"'

# Process each file
for file_path in input_files:
    try:
        with open(file_path, "r") as file:
            data = json.load(file)

        # Loop through hits.hits array in the current file
        for record in data.get("hits", {}).get("hits", []):
            reason = record.get("_source", {}).get("reason", "")

            # Extract all "reason" matches
            all_reasons = re.findall(reason_pattern, reason)
            last_reason = all_reasons[-1] if all_reasons else None

            # Extract values using corrected patterns
            reference_match = re.search(reference_pattern, reason)
            state_matches = re.findall(state_pattern, reason)
            timestamp_match = re.search(timestamp_pattern, reason)

            # Filter "PUBLIC" and get the other state
            state = next((match for match in state_matches if match != "PUBLIC"), None)

            # Get the matched values or default to None
            reference = reference_match.group(1).replace(" ", "") if reference_match else None
            timestamp = timestamp_match.group(1) if timestamp_match else None

            # Combine extracted values if all are present
            if reference and state and timestamp and last_reason:
                results.append({
                    "Reference": reference,
                    "State": state,
                    "Timestamp": timestamp,
                    "Reason": last_reason
                })

    except Exception as e:
        print(f"Error processing file {file_path}: {e}")

# Sort results by timestamp in descending order
def parse_timestamp(record):
    try:
        return datetime.fromisoformat(record["Timestamp"])
    except Exception as e:
        print(f"Error parsing timestamp for record '{record}': {e}")
        return datetime.min  # Use minimum datetime for safety in sorting

results.sort(key=parse_timestamp, reverse=True)

# Write the sorted results to a CSV file
with open(output_file_path, "w", newline="") as csvfile:
    fieldnames = ["Reference", "State", "Timestamp", "Reason"]
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()  # Write the header row
    writer.writerows(results)  # Write all the results

# Filter unique case references from the sorted results
unique_records = []
seen_references = set()

for result in results:
    if result["Reference"] not in seen_references:
        unique_records.append(result)
        seen_references.add(result["Reference"])

# Write unique records to the CSV file
with open(unique_records_file_path, "w", newline="") as csvfile:
    fieldnames = ["Reference", "State", "Timestamp", "Reason"]
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()  # Write the header row
    writer.writerows(unique_records)  # Write the unique records

print(f"Extraction and sorting by timestamp is completed. Results saved in '{output_file_path}'.")
print(f"Unique records saved in '{unique_records_file_path}'.")

