-- 40. Validation data (#176)

ALTER TABLE VALIDATION_STAMPS
  ADD DATA_TYPE_ID VARCHAR(120) NULL;

ALTER TABLE VALIDATION_STAMPS
  ADD DATA_TYPE_CONFIG VARCHAR(10000) NULL;
