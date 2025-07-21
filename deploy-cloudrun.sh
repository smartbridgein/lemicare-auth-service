#!/bin/bash
set -e

echo "Building ultra-minimal auth-service for Cloud Run..."
./mvnw clean package -DskipTests

echo "Building ultra-minimal Docker image..."
# Create a simple cloud build config file
cat << EOF > cloudbuild-ultra.yaml
steps:
- name: 'gcr.io/cloud-builders/docker'
  args: ['build', '-t', 'gcr.io/pivotal-store-459018-n4/auth-service-cloudrun:latest', '-f', 'Dockerfile.cloudrun', '.']
images:
- 'gcr.io/pivotal-store-459018-n4/auth-service-cloudrun:latest'
EOF

# Submit the build
gcloud builds submit --config=cloudbuild-ultra.yaml

echo "Deploying ultra-minimal app to Cloud Run..."
gcloud run deploy auth-service-cloudrun \
  --image gcr.io/pivotal-store-459018-n4/auth-service-cloudrun:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port=8080
