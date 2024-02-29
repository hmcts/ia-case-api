import unittest
import json
import os
import shutil

from convert_json_to_txt import convert_json_to_txt


class TestConvertJsonToTxt(unittest.TestCase):

    def setUp(self):
        # Create a temporary directory for testing
        self.test_dir = "test_temp_dir"
        os.makedirs(self.test_dir, exist_ok=True)

    def tearDown(self):
        # Remove the temporary directory and its contents
        shutil.rmtree(self.test_dir)

    def test_convert_json_to_txt(self):
        # Prepare a sample JSON data
        json_data = {'name': 'John', 'age': 30, 'city': 'New York'}

        # Write the sample JSON data to a file
        input_file_path = os.path.join(self.test_dir, "input.json")
        with open(input_file_path, "w") as json_file:
            json.dump(json_data, json_file)

        # Call the function to convert JSON to TXT
        convert_json_to_txt(input_file_path)

        # Check if the output file exists
        output_file_path = os.path.join(self.test_dir, "input.txt")
        self.assertTrue(os.path.exists(output_file_path))

        # Read the content of the output file
        with open(output_file_path, "r") as text_file:
            text_data = text_file.read()

        # Check if the content matches the expected compact text representation
        expected_text_data = '{"name":"John","age":30,"city":"New York"}'
        self.assertEqual(text_data, expected_text_data)


if __name__ == '__main__':
    unittest.main()
