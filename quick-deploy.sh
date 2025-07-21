#!/bin/bash
# Quick deployment script for auth-service

PROJECT_ID="pivotal-store-459018-n4"
REGION="us-central1"
SERVICE_NAME="auth-service"
IMAGE="gcr.io/$PROJECT_ID/$SERVICE_NAME:latest"

echo "=== Building $SERVICE_NAME container ==="
# Submit the build to Cloud Build
gcloud builds submit --tag=$IMAGE \
  --project=$PROJECT_ID \
  --timeout=30m .

echo "=== Deploying $SERVICE_NAME to Cloud Run ==="
# Deploy the service to Cloud Run with environment variables
gcloud run deploy $SERVICE_NAME \
  --image=$IMAGE \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=cloud" \
  --set-env-vars="ALLOWED_ORIGINS=https://healthcare-app-1078740886343.us-central1.run.app" \
  --project=$PROJECT_ID
