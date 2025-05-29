# URL-Based Authentication

This document explains how to use the URL-based authentication feature with encrypted passwords as an alternative to the traditional login page.

## Overview

The URL-based authentication allows users to log in by providing their encrypted password hash directly in the URL parameters, bypassing the traditional login form. This is useful for:

- Quick access via bookmarks
- External system integrations
- Mobile applications
- Direct linking scenarios

## Usage

### Encrypted Password Authentication

#### Online Learning System

```
https://yourdomain.com/online/urlLoginEncrypted?id=YOUR_STUDENT_ID&encPassword=ENCRYPTED_PASSWORD_HASH
```

**Example:**
```
https://yourdomain.com/online/urlLoginEncrypted?id=12345678&encPassword=$2a$10$4Bw7VtbBjzGIarBTZ13uYpTFUl7ay8...
```

#### Connected Class System

```
https://yourdomain.com/connected/urlLoginEncrypted?id=YOUR_STUDENT_ID&encPassword=ENCRYPTED_PASSWORD_HASH
```

**Example:**
```
https://yourdomain.com/connected/urlLoginEncrypted?id=12345678&encPassword=$2a$10$4Bw7VtbBjzGIarBTZ13uYpTFUl7ay8...
```

## Parameters

- `id`: Student ID (8-digit number)
- `encPassword`: BCrypt encrypted password hash from database (starts with `$2a$10$...`)

## Behavior

### Successful Authentication
- User is authenticated and logged into the system
- Login activity is recorded
- User is redirected to the appropriate lesson page:
  - Online system: `/online/lesson`
  - Connected system: `/connected/lesson`

### Failed Authentication
- User is redirected to the login page with an error message:
  - Online system: `/online/login?error=true`
  - Connected system: `/connected/login?error=true`

## Security Features

- Uses direct hash comparison for authentication
- Session management works identically to traditional login
- All existing security rules apply (enrollment validation, etc.)
- More secure than plain text passwords as it uses database hashes directly

## Error Handling

The system handles the following error scenarios:

1. **Invalid Credentials**: Redirects to login page with error
2. **Invalid ID Format**: Redirects to login page with error
3. **Authentication Exceptions**: Redirects to login page with error
4. **Missing Parameters**: Returns 400 Bad Request
5. **Hash Mismatch**: Redirects to login page with error

## Getting Encrypted Password from Database

To get the encrypted password hash for use with the encrypted endpoints:

```sql
SELECT id, password FROM Student WHERE id = 12345678;
```

The password field will contain a BCrypt hash like:
```
$2a$10$4Bw7VtbBjzGIarBTZ13uYpTFUl7ay8/ZQJ123456789abcdef...
```

## Security Considerations

⚠️ **Important Security Notes:**

1. **HTTPS Required**: Always use HTTPS in production to protect credentials in transit
2. **URL Logging**: Be aware that URLs may be logged by web servers, proxies, and browsers
3. **Browser History**: Encrypted passwords will appear in browser history
4. **Referrer Headers**: Encrypted passwords may be exposed in referrer headers
5. **Hash Sensitivity**: While more secure than plain text, the hash itself is still sensitive data

## Recommendations

- Use this feature primarily for:
  - Internal systems
  - Trusted networks
  - Development/testing environments
- Consider implementing additional security measures for production use
- Monitor authentication logs for unusual activity
- Ensure proper access controls to the database containing password hashes

## Traditional Login

The traditional login pages remain available and unchanged:

- Online system: `/online/login`
- Connected system: `/connected/login`

Both authentication methods can be used simultaneously without conflicts. 