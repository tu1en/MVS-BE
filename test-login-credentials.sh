#!/bin/bash

echo "=== TESTING LOGIN CREDENTIALS ==="
echo

# Define the base URL
BASE_URL="http://localhost:8088"

# Test credentials from DataLoader
declare -a credentials=(
    "admin:admin123"
    "manager:manager123" 
    "teacher:teacher123"
    "student:student123"
)

echo "Testing backend connectivity..."
curl -s -I "$BASE_URL/api/auth/login" | head -n 1

echo
echo "Testing login credentials..."

for cred in "${credentials[@]}"; do
    IFS=':' read -r username password <<< "$cred"
    
    echo "Testing: $username / $password"
    
    response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}" \
        -w "%{http_code}")
    
    # Extract HTTP status code (last 3 characters)
    http_code="${response: -3}"
    response_body="${response%???}"
    
    echo "HTTP Status: $http_code"
    if [ "$http_code" = "200" ]; then
        echo "✅ SUCCESS: Login successful for $username"
        echo "Response: $response_body"
    else
        echo "❌ FAILED: Login failed for $username"
        echo "Response: $response_body"
    fi
    echo "---"
done

echo
echo "=== CHECKING H2 CONSOLE ==="
echo "Try accessing: $BASE_URL/h2-console"
echo "JDBC URL: jdbc:h2:mem:testdb"
echo "Username: sa"
echo "Password: (empty)"
