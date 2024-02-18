curl -X PUT \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrZWtAbG9sLnJ1Iiwicm9sZXMiOlsiVVNFUiJdLCJpYXQiOjE3MDc5OTAxNTAsImV4cCI6MTcwODU5NDk1MH0.ldmxLPi6pWOEkW-flp5Ln8bRlSZfzwrwI5as_yKnRFbAeko7qhn2Ga1I8NlWWaNHMV6h6MYJRLSXaSfJLPxGKA" \
  -d '{
        "messageIds": [
          1
        ],
        "userId": 1,
        "text_message": "ps3",
        "name": "string"
      }' \
  http://localhost:8080/message/add > out.txt