-- Confirmed default weights. They stay in this table so a later change is data, not a deploy.
-- These are formula weights, not observations. Mockup district scores are not loaded.
INSERT INTO marts.risk_score_config (
  config_key,
  weight_environmental,
  weight_health,
  weight_social,
  status,
  notes
)
VALUES (
  'default',
  0.400,
  0.350,
  0.250,
  'confirmed',
  'Confirmed by Marco: overall = 0.40*environmental + 0.35*health + 0.25*social. '
  'Configurable in this table. Not fitted to outcomes. '
  'The product UI must read this row rather than hard-code the weights.'
)
ON CONFLICT (config_key) DO NOTHING;
