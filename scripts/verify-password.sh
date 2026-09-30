#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
read -r -s -p 'Hash guardado en accounts.password_hash: ' VERIFY_PASSWORD_HASH
printf '\n'
read -r -s -p 'Contraseña a verificar: ' VERIFY_PASSWORD
printf '\n'
export VERIFY_PASSWORD_HASH VERIFY_PASSWORD
trap 'unset VERIFY_PASSWORD_HASH VERIFY_PASSWORD' EXIT
./mvnw -q -Dtest=StoredPasswordVerificationTest test
