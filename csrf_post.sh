#!/bin/bash

# Step 1: Retrieve the CSRF token and set session cookies
curl -c cookies.txt -s http://localhost:8080/csrf-token > /dev/null

# Step 2: Extract the CSRF token from the cookie file
csrf_token=$(grep 'XSRF-TOKEN' cookies.txt | awk '{print $7}')

# Verify that the CSRF token was retrieved
if [ -z "$csrf_token" ]; then
  echo "Failed to retrieve CSRF token."
  exit 1
fi

echo "CSRF Token: $csrf_token"

# Step 3: Use the token and session cookies to make the POST request
response=$(curl -b cookies.txt -X POST -v \
  -H "X-CSRF-TOKEN: $csrf_token" \
  -d "username=darkness" -d "password=noway" \
  http://localhost:8080/auth/register)

echo "Response: $response"


