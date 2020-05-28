--
-- Introduced as part of this ticket https://tools.hmcts.net/jira/browse/RIA-1359
-- to be able to update the appealReferenceNumber as part of the functional test
--
INSERT INTO ia_case_api.appeal_reference_numbers (
  case_id,
  type,
  year,
  sequence
) VALUES (
  1234,
  'PA',
  2019,
  50019
);
--