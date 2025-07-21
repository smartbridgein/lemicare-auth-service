#!/bin/bash
set -e

echo "Building minimal auth-service for Cloud Run..."
./mvnw clean package -DskipTests

echo "Building minimal Docker image..."
# Create a simple cloud build config file
cat << EOF > cloudbuild-minimal.yaml
steps:
- name: 'gcr.io/cloud-builders/docker'
  args: ['build', '-t', 'gcr.io/pivotal-store-459018-n4/auth-service-minimal:latest', '-f', 'Dockerfile.minimal', '.']
images:
- 'gcr.io/pivotal-store-459018-n4/auth-service-minimal:latest'
EOF

# Submit the build
gcloud builds submit --config=cloudbuild-minimal.yaml

echo "Deploying minimal app to Cloud Run..."
gcloud run deploy auth-service-minimal \
  --image gcr.io/pivotal-store-459018-n4/auth-service-minimal:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port=8080 \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=1 \
  --set-env-vars=SPRING_PROFILES_ACTIVE=minimal
