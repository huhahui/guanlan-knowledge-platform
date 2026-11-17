#!/usr/bin/env sh
set -eu
: "${DEPLOY_TARGET:?Set DEPLOY_TARGET to user@host:/path}"
npm run build
scp -r ./dist "$DEPLOY_TARGET"
