# GitHub Integration

This document explains how to set up and use the GitHub integration in the application.

## Prerequisites

1. A GitHub account
2. A GitHub OAuth App registered in your GitHub account

## Setup

### 1. Register a GitHub OAuth App

1. Go to GitHub > Settings > Developer settings > OAuth Apps
2. Click "New OAuth App"
3. Fill in the following details:
   - Application name: Your app name
   - Homepage URL: Your application URL (e.g., http://localhost:8080)
   - Authorization callback URL: `http://localhost:8080/api/github/auth/callback`
4. Click "Register application"
5. Note down the Client ID and generate a new Client Secret

### 2. Configure Environment Variables

Add the following environment variables to your `.env` file:

```
# GitHub OAuth2
GITHUB_CLIENT_ID=your_client_id
GITHUB_CLIENT_SECRET=your_client_secret
GITHUB_REDIRECT_URI=http://localhost:8080/api/github/auth/callback
```

## API Endpoints

### 1. Authenticate with GitHub

1. Redirect users to the GitHub authorization URL:
   ```
   GET https://github.com/login/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI&scope=repo,user&state=random_string
   ```

2. After user authorization, GitHub will redirect to your callback URL with a code parameter.

3. Exchange the code for an access token:
   ```
   POST /api/github/auth/callback?code={code}&state={state}
   ```

   Response:
   ```json
   {
     "accessToken": "gho_...",
     "tokenType": "bearer",
     "scope": "repo,user"
   }
   ```

### 2. Get User Repositories

```
GET /api/github/repositories
Headers: 
  Authorization: Bearer YOUR_ACCESS_TOKEN
```

### 3. Get Repository Branches

```
GET /api/github/repositories/{owner}/{repo}/branches
Headers: 
  Authorization: Bearer YOUR_ACCESS_TOKEN
```

## Security Notes

1. Always use HTTPS in production
2. Store client secrets securely (never commit them to version control)
3. Use the `state` parameter to prevent CSRF attacks
4. Request only the scopes your application needs
