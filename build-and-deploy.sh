#!/bin/bash
# Complete build and deploy script for auth-service

PROJECT_ID="pivotal-store-459018-n4"
REGION="us-central1"
SERVICE_NAME="auth-service"
IMAGE="gcr.io/$PROJECT_ID/$SERVICE_NAME:latest"

echo "=== Building $SERVICE_NAME JAR file ==="
# Build the Java application with Maven
mvn clean package

# Check if the build was successful
if [ $? -ne 0 ] || [ ! -f "target/cosmicdoc-auth-service-0.0.1-SNAPSHOT.jar" ]; then
  echo "Maven build failed or JAR file not found. Aborting deployment."
  exit 1
fi

echo "=== Building $SERVICE_NAME container ==="
# Submit the build to Cloud Build
gcloud builds submit --tag=$IMAGE \
  --project=$PROJECT_ID \
  --timeout=30m .

# Check if the build was successful
if [ $? -ne 0 ]; then
  echo "Container build failed. Aborting deployment."
  exit 1
fi

echo "=== Deploying $SERVICE_NAME to Cloud Run ==="
# Deploy the service to Cloud Run
gcloud run deploy $SERVICE_NAME \
  --image=$IMAGE \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=cloud" \
  --set-env-vars="ALLOWED_ORIGINS=https://healthcare-app-1078740886343.us-central1.run.app" \
  --port=8081 \
  --project=$PROJECT_ID
