# LGPD

EcoGuard is aimed at people in São Paulo. Lei 13.709/2018 applies if we process personal data. The foundation is designed so that the product data is not personal data.

## What we store

| Data | Personal? | Notes |
| --- | --- | --- |
| InfoDengue municipal counts | No | Aggregated statistics. No patient row |
| Open-Meteo forecast | No | A grid cell |
| Future SIH extracts | Careful | Public AIH microdata has no patient id. Still do not join it to a person. District grain is not in the file |
| Census aggregates | No | Published statistics |
| Analyst questions, later | Can be | A question can name a person or an address. Do not write the text to analytics. Retention of the chat, if any, needs a purpose, a short lifetime, and a notice on screen 00 or the analyst screen |
| Product analytics | Must not be | Taxonomy in `docs/analytics.md`. No cookie before consent. Self-hosted PostHog |

There is no account, no mailing list, and no precise location from the browser. The map is a district the user chooses, stored as a district code in an event, not as a GPS fix.

## Duties when a feature starts to qualify

If a feature stores questions or any identifier, name the controller, the purpose, the legal basis (likely legitimate interest or consent, and that choice is Marco's to confirm with counsel), the retention, and who the operator is (the host of the database and of PostHog). This document is not legal advice.

Do not add a third-party analytics host without updating this note and the CSP `connect-src`.
