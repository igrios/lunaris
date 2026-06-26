#!/bin/bash
FILE=$1
PROMPT=$2
# Leemos el archivo y lo pasamos por jq para que lo escape correctamente
CONTENT=$(cat "$FILE" | jq -Rs .)

curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=TU_CLAVE_REAL" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d "{ \"contents\": [{ \"parts\": [{ \"text\": \"$PROMPT: \" + $CONTENT }]}]}"#!/bin/bash
# Este script lee un archivo y se lo envía a Gemini para que lo analice
FILE=$1
PROMPT=$2
CONTENT=$(cat "$FILE")

curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=TU_API_KEY" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d "{ \"contents\": [{ \"parts\": [{ \"text\": \"$PROMPT: $CONTENT\" }]}]}"
