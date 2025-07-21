#!/bin/bash
# Cloud Run deployment script for auth-service
# Target project: lemicare-prod

# Define variables
PROJECT_ID="lemicare-prod"
REGION="asia-south1"
SERVICE_NAME="auth-service"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"

echo "=== Building ${SERVICE_NAME} JAR file ===" 
# Build the Java application with Maven
mvn clean package

# Check if the build was successful
if [ $? -ne 0 ] || [ ! -f "target/cosmicdoc-auth-service-0.0.1-SNAPSHOT.jar" ]; then
  echo "Maven build failed or JAR file not found. Aborting deployment."
  exit 1
fi

echo "=== Building Docker image using Cloud Build ===" 
# Use Cloud Build to build the Docker image from the existing Dockerfile
gcloud builds submit --tag="${IMAGE}" --project="${PROJECT_ID}" .

if [ $? -eq 0 ]; then
  echo "=== Deploying to Cloud Run ===" 
  gcloud run deploy "${SERVICE_NAME}" \
    --image="${IMAGE}" \
    --platform=managed \
    --region="${REGION}" \
    --allow-unauthenticated \
    --port=8081 \
    --memory=512Mi \
    --project="${PROJECT_ID}"
  
  if [ $? -eq 0 ]; then
    echo "=== Deployment completed successfully! ===" 
    echo "Your ${SERVICE_NAME} is now available. Check the URL in the output above."
  else
    echo "=== Deployment to Cloud Run failed ===" 
    exit 1
  fi
else
  echo "=== Docker image build failed ===" 
  exit 1
fi
