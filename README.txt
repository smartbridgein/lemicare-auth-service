# CosmiDoc Auth Service API Documentation

This file contains sample curl commands for testing the authentication endpoints.

## Base URL
For local testing: http://localhost:8080

## Registration/Signup

### Admin Signup
```bash
curl -X POST http://localhost:8080/api/public/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "mobileNumber": "9876543210",
    "organizationName": "Sample Hospital",
    "initialBranchName": "Main Branch",
    "hasMultipleBranches": true,
    "address": {
      "street": "123 Main St",
      "city": "Bangalore",
      "state": "Karnataka",
      "zipCode": "560001",
      "country": "India"
    }
  }'
```

## Login

### Mobile OTP Login Request
```bash
curl -X POST http://localhost:8080/api/public/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "purpose": "login"
  }'
```

### Login with OTP
```bash
curl -X POST http://localhost:8080/api/public/auth/login-with-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otp": "123456"
  }'
```

### Email Login
```bash
curl -X POST http://localhost:8080/api/public/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "YourSecurePassword123"
  }'
```

### Logout
```bash
curl -X POST http://localhost:8080/api/public/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Forgot Password Flow

### Step 1: Request OTP for Password Reset
```bash
curl -X POST http://localhost:8080/api/public/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "purpose": "reset"
  }'
```

### Step 2: Reset Password with OTP
```bash
curl -X POST http://localhost:8080/api/public/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "token": "123456",
    "password": "NewSecurePassword123"
  }'
```

### Alternative: Email-based Forgot Password
```bash
curl -X POST http://localhost:8080/api/public/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

## Notes
- OTPs are 6-digit numbers and are valid for 5 minutes
- For development, OTPs are logged to the console instead of being sent via SMS
- Mobile numbers should be 10 digits without country code
- Replace placeholders with actual values (like YOUR_JWT_TOKEN)
- Passwords must be at least 6 characters long

## Error Handling
All endpoints return appropriate HTTP status codes:
- 200: Success
- 400: Bad request (validation errors)
- 401: Unauthorized
- 404: Resource not found
- 500: Server error

## Additional Info
For security reasons, forgot password and OTP request endpoints always return success responses
regardless of whether the email/mobile exists in the database. This prevents user enumeration attacks.
