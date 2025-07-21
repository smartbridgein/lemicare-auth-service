#!/bin/bash

# Test authentication with curl
echo "Testing authentication API directly with curl..."

curl -X POST http://localhost:8081/api/public/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cosmicmed.com", "password":"Test123!"}' \
  -v
