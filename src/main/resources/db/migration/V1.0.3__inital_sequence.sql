--
-- these rows provide an offset for the first numbers to use after deployment
--
INSERT INTO ia_case_api.appeal_reference_numbers (
  case_id,
  type,
  "year",
  sequence
) VALUES (
  -1,
  'PA',
  2019,
  50019
);
--
INSERT INTO ia_case_api.appeal_reference_numbers (
  case_id,
  type,
  "year",
  sequence
) VALUES (
  -2,
  'RP',
  2019,
  50019
);
--