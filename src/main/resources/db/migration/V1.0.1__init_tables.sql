CREATE SCHEMA IF NOT EXISTS ia_case_api;

CREATE TABLE IF NOT EXISTS ia_case_api.appeal_reference_numbers (
  case_id INT NOT NULL,
  type VARCHAR(30),
  "year" INT NOT NULL,
  sequence INT,
  CONSTRAINT unique_case_id UNIQUE (case_id),
  CONSTRAINT unique_appeal_reference_number UNIQUE (type, "year", sequence)
);