#!/bin/bash

# Exit on error
set -e

echo "Building auth-service with Java 17 compatibility..."
mvn clean package -DskipTests -Djava.version=17 -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Dmaven.compiler.release=17 

echo "Using existing Dockerfile for deployment..."

echo "Submitting to Google Cloud Build..."
# First create a cloud build config file
cat > cloudbuild.yaml << EOF
steps:
- name: 'gcr.io/cloud-builders/docker'
  args: ['build', '-t', 'gcr.io/pivotal-store-459018-n4/auth-service:latest', '.']
images:
- 'gcr.io/pivotal-store-459018-n4/auth-service:latest'
EOF

# Then use the config file
gcloud builds submit --config=cloudbuild.yaml

echo "Deploying to Cloud Run with basic config and extended timeout..."
gcloud run deploy auth-service \
  --image gcr.io/pivotal-store-459018-n4/auth-service:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port=8080 \
  --timeout=10m
