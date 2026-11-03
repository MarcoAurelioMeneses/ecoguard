#!/usr/bin/env bash
# design/ is the source of truth for tokens and the vector logo.
# frontend/ keeps a wired copy. The copies must match.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
diff -q "$root/design/tokens.css" "$root/frontend/src/styles/tokens.css"
diff -rq "$root/design/logo/v2" "$root/frontend/public/logo/v2"
diff -q "$root/design/logo/v2/favicon.ico" "$root/frontend/public/favicon.ico"
diff -q "$root/design/logo/v2/favicon.svg" "$root/frontend/public/favicon.svg"
echo "Design tokens and logos match frontend/."
