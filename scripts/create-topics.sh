#!/usr/bin/env bash
# Creates local topics used for testing
set -e

KAFKA_CONTAINER=$(docker-compose ps -q kafka)
if [ -z "$KAFKA_CONTAINER" ]; then
  echo "Please start docker-compose first: docker-compose up -d"
  exit 1
fi

# Use kafka container's kafka-topics.sh
docker-compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic local-upstream-topic --partitions 1 --replication-factor 1 || true
docker-compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic local-beatroot-topic --partitions 1 --replication-factor 1 || true
docker-compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic local-carrot-topic --partitions 1 --replication-factor 1 || true

echo "Topics created (or already exist)."
