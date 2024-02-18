curl -X DELETE \
  -H "Content-Type:application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrZWtAbG9sLnJ1Iiwicm9sZXMiOlsiVVNFUiJdLCJpYXQiOjE3MDc5OTAxNTAsImV4cCI6MTcwODU5NDk1MH0.ldmxLPi6pWOEkW-flp5Ln8bRlSZfzwrwI5as_yKnRFbAeko7qhn2Ga1I8NlWWaNHMV6h6MYJRLSXaSfJLPxGKA" \
  -d '{
        "messageIds": [
          6
        ],
        "userId": 1,
        "text_message": "test",
        "name": "test"
      }' \
  http://localhost:8080/message/delete > out.txt