Deployment notes

Profiles
- Use SPRING_PROFILES_ACTIVE=azure when deploying to Azure
- Use SPRING_PROFILES_ACTIVE=pcf when deploying to PCF

Topics
- Azure upstream topic: configured as app.kafka.topics.upstream in application-azure.yml
- PCF upstream topic: configured as app.kafka.topics.upstream in application-pcf.yml

Header-based routing
- Upstream producers should set Kafka header "env" with value "azure" or "pcf".
- Beatroot and Carrot listeners filter messages by checking the env header against the active profile; if header doesn't match, message is ignored.

PCF manifest
- manifest-pcf.yml contains example applications entries for Cloud Foundry.

Azure
- application-azure.yml contains placeholders for Azure Event Hubs / Kafka bootstrap servers and Azure SQL connection string. Use Azure Key Vault or managed identity to provide secrets in production.

Next steps
- Add secrets management (Key Vault / CredHub)
- Add health checks and readiness/liveness probes
- Configure consumer group IDs, partitioning strategy, and dead-letter topic
- Add metrics/tracing (Micrometer + Azure Monitor / PCF metrics)
