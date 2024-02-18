curl -X POST \
  -H "Content-Type:application/json" \
  -d '{"messageIds": [1]}' \
  http://localhost:8080/message/filtered > out.txt