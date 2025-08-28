# Mailjet Email API Test Service

A Spring Boot application for testing Mailjet email API integration.

## Features

- Send emails via Mailjet API
- REST endpoint for email sending
- Input validation
- Comprehensive error handling
- Health check endpoint

## Setup Instructions

### 1. Configure Mailjet Credentials

Set the following environment variables or update `application.yml`:

```bash
# Environment Variables
MAILJET_API_KEY=your-mailjet-api-key
MAILJET_API_SECRET=your-mailjet-api-secret
MAILJET_FROM_EMAIL=your-verified-sender@example.com
MAILJET_FROM_NAME=Your Service Name
```

### 2. Get Mailjet Credentials

1. Sign up at [Mailjet](https://www.mailjet.com/)
2. Go to Account Settings > API Keys
3. Copy your API Key and Secret Key
4. Verify your sender email address in Mailjet dashboard

### 3. Run the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Send Email
- **URL**: `POST /api/email/send`
- **Content-Type**: `application/json`

**Request Body:**
```json
{
    "to": "recipient@example.com",
    "subject": "Test Email Subject",
    "body": "This is the email body content.\nIt can contain multiple lines."
}
```

**Success Response (200):**
```json
{
    "success": true,
    "message": "Email sent successfully",
    "messageId": "123456789",
    "timestamp": 1693123456789
}
```

**Error Response (400/500):**
```json
{
    "success": false,
    "message": "Error description",
    "messageId": null,
    "timestamp": 1693123456789
}
```

### Health Check
- **URL**: `GET /api/email/health`
- **Response**: Service configuration status

## Testing Examples

### Using cURL

```bash
# Send a test email
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email from API",
    "body": "Hello from Mailjet API!\n\nThis is a test email sent via the REST API."
  }'

# Check service health
curl http://localhost:8080/api/email/health
```

### Using Postman

1. Create a new POST request to `http://localhost:8080/api/email/send`
2. Set Content-Type header to `application/json`
3. Add the JSON request body as shown above
4. Send the request

## Validation Rules

- **to**: Must be a valid email address (required)
- **subject**: Cannot be blank (required)
- **body**: Cannot be blank (required)

## Error Handling

The service handles various error scenarios:
- Invalid email format
- Missing required fields
- Mailjet API errors
- Network connectivity issues
- Service configuration problems

## Logging

The application logs important events:
- Email send attempts
- Success/failure status
- Error details
- API responses

Check the console output for detailed logs when testing.

## Project Structure

```
src/main/java/com/apitest/
├── controller/
│   └── EmailController.java      # REST endpoints
├── dto/
│   ├── EmailRequestDTO.java      # Request data structure
│   └── EmailResponseDTO.java     # Response data structure
├── service/
│   └── MailjetService.java       # Mailjet integration logic
└── apitest/
    └── ApitestApplication.java   # Main application class
```

## Dependencies

- Spring Boot 3.5.5
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Mailjet Client 6.0.0
- Spring Boot Starter Actuator
- Spring Boot Starter Data MongoDB

## Next Steps

1. Set your Mailjet API credentials
2. Run the application
3. Test the `/api/email/send` endpoint
4. Check the health endpoint to verify configuration
5. Monitor logs for any issues

For production use, consider:
- Adding authentication/authorization
- Implementing rate limiting
- Adding email templates
- Setting up monitoring and alerting
- Using environment-specific configurations
