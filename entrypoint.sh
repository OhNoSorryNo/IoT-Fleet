#!/bin/sh
set -e

# For debugging, you can uncomment the next line:
# echo "Using SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}, SECRET_TOKEN=${SECRET_TOKEN}"

java \
  -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE:-default}" \
  -DSECRET_TOKEN="${SECRET_TOKEN}" \
  -jar /app/app.jar