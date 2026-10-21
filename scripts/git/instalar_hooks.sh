#!/bin/sh
# Activa los hooks versionados en .githooks/ (una vez por clon).
cd "$(dirname "$0")/../.." || exit 1
git config core.hooksPath .githooks
chmod +x .githooks/* 2>/dev/null
echo "Hooks activados: $(git config core.hooksPath) -> pre-commit, commit-msg"
