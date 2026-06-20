Prompt: Implement Azure/PCF environment-aware Kafka routing and delivery

Context
You have a multi-module Spring Boot 4 project (modules: apple, beatroot, carrot). Apple consumes upstream events, processes them, and forwards to beatroot or carrot based on an event flag. The application must be deployable unchanged to both Azure and PCF. Upstream publishes to two separate topics: one for Azure and one for PCF. All processing for events from the Azure topic must remain inside Azure; likewise PCF events must remain inside PCF.

Goal
Implement a production-ready, environment-aware Kafka configuration and routing layer across the existing multi-module codebase that:
- Preserves all existing business logic (no functional changes to domain logic)
- Uses profile-driven configuration (spring.profiles.active) and platform-supplied environment variables to determine per-deployment topics and Kafka bootstrap servers
- Ensures each deployment instance consumes only the correct upstream topic for its environment (Azure vs PCF)
- Propagates the environment origin via a Kafka header (env=azure or env=pcf) when Apple forwards messages to downstream topics
- Ensures downstream modules (beatroot, carrot) process only messages intended for their running environment (check header), and in Azure profile persist events to DB; in PCF profile do not persist (or use PCF-specific persistence if required)
- Adds production-grade features: RecordFilterStrategy (optional), DefaultErrorHandler and DLQ, idempotency guidance, transactional publishing where appropriate, metrics and health checks, secrets externalization (Key Vault / CredHub)

Task for implementer (scan & implement)
1. Scan the repository root and module structure to find where Kafka consumers and producers are implemented.
2. Replace hard-coded topic names with property placeholders (app.kafka.topics.*) and wire these properties in application-*.yml per-profile (application-azure.yml, application-pcf.yml).
3. Ensure Apple consumer uses the profile-specific upstream topic: @KafkaListener(topics = "${app.kafka.topics.upstream}").
4. When Apple forwards to downstream topics, attach a header "env" set to the instance's active profile (spring.profiles.active) using ProducerRecord and RecordHeader.
   Example: ProducerRecord<String,String> rec = new ProducerRecord<>(topic, key, json); rec.headers().add(new RecordHeader("env", profile.getBytes(StandardCharsets.UTF_8)));
5. Update Beatroot and Carrot listeners to subscribe to downstream topics via property placeholders (@KafkaListener(topics = "${app.downstream.beatroot-topic}")). In the listener method accept @Header(name="env", required=false) String envHeader and ignore messages that have non-matching envHeader (defense-in-depth).
6. Configure per-profile application-azure.yml and application-pcf.yml with:
   - spring.kafka.bootstrap-servers (platform-provided broker endpoints or service bindings)
   - app.kafka.topics.upstream mapping (azure-upstream-topic, pcf-upstream-topic)
   - app.downstream.* names if needed
   - DB connection properties for Azure profile (use placeholders to be supplied by Key Vault)
7. Add container-level filtering (RecordFilterStrategy) to drop messages whose env header doesn't match activeProfile before listener invocation (optional but recommended).
8. Add DefaultErrorHandler with backoff and dead-letter-topic configuration for each listener factory.
9. Make producers and consumers optionally transactional (use KafkaTransactionManager / enable.idempotence and transactions where partitions and ordering are important).
10. Externalize secrets: use application properties placeholders; document platform bindings (Azure Key Vault + Managed Identity; PCF CredHub or user-provided service).
11. Add sample manifest-pcf.yml with SPRING_PROFILES_ACTIVE=pcf and sample Azure deployment guidance (ARM/Bicep or CLI) showing SPRING_PROFILES_ACTIVE=azure and how to inject Key Vault secrets.
12. Add integration tests (embedded Kafka) to validate routing and header behavior and a small Docker Compose for local testing (optional but recommended).

Acceptance criteria
- No changes to domain/business logic in existing services; only infrastructure/config and non-functional plumbing is added.
- When deployed with SPRING_PROFILES_ACTIVE=azure and app.kafka.topics.upstream=azure-upstream-topic, only events published to azure-upstream-topic are consumed and processed end-to-end within Azure instances.
- When deployed with SPRING_PROFILES_ACTIVE=pcf and app.kafka.topics.upstream=pcf-upstream-topic, only pcf-upstream-topic events are consumed and processed end-to-end within PCF instances.
- Apple forwards messages with env header equal to the producer instance profile. Downstream services ignore messages with mismatched env header.
- Dead-letter topic exists for failed messages. Error handler logs failures and retries per backoff.
- Secrets are not stored in Git; placeholders or references to Key Vault/CredHub are used in yml.

Deliverables
- Modifications annotated via PR: list of changed files (Apple listeners, KafkaConfig container factory, Beatroot/Carrot listeners, application-*.yml files, manifest-pcf.yml)
- Unit and integration tests demonstrating behavior
- README (deployment guidance) describing how platforms must bind secrets and topics
- Optional: Docker Compose for local testing and shell scripts to create topics

Security and production notes
- Use SSL/SASL for Kafka connections (Event Hubs requires TLS+SASL). Configure spring.kafka.properties.* accordingly.
- Prefer managed identity / Key Vault for Azure secrets; for PCF use CredHub/service bindings.
- Ensure idempotency keys and DB constraints or deduplication are in place when replaying messages.

Instruction to the implementer/AI assistant
- Make only non-functional/infrastructure changes; do not alter business logic or method signatures that affect domain behavior.
- Run unit/integration tests and include test coverage for header routing and profile behavior.
- Add clear PR description listing what changed and why.

---

Please implement these changes across the repo. If anything in the codebase prevents a non-invasive integration (e.g., topics hardcoded in many places), document the minimal adapter layers required and provide a migration plan.
