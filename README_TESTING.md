Local testing guide

Overview
This repository includes a local testing setup that uses Docker Compose (Kafka + Zookeeper + Kafka-UI) and a `local` Spring profile so you can run the three modules locally and simulate upstream events.

Quick steps
1. Start Kafka locally:
   docker-compose up -d
2. Create topics:
   ./scripts/create-topics.sh
3. Start applications (in separate terminals):
   ./gradlew :apple:bootRun -Dspring.profiles.active=local
   ./gradlew :beatroot:bootRun -Dspring.profiles.active=local
   ./gradlew :carrot:bootRun -Dspring.profiles.active=local

4. Publish an upstream event (example):
   curl -X POST http://localhost:8080/api/upstream/publish \
     -H "Content-Type: application/json" \
     -d '{"id":"1","flag":"beatroot","payload":"hello local"}'

   - If flag is "beatroot" Apple consumes the upstream topic and forwards to local-beatroot-topic. The Beatroot app (running on port 8081) consumes and logs the message.
   - If flag is "carrot" it will be forwarded to local-carrot-topic and consumed by Carrot app (running on port 8082).

What to observe
- Apple logs show it consumed the upstream event and forwarded it with env header.
- Beatroot or Carrot logs show which module consumed the message. Example log lines: "Beatroot consumed message: ..." or "Carrot consumed message: ..."

Notes
- Applications will run with the local profile which points to localhost:9092.
- This setup is intended for manual functional testing. For automated tests, consider adding embedded-kafka integration tests.

Cleanup
- Stop apps and docker compose:
  docker-compose down
