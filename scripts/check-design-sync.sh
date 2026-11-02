#!/usr/bin/env bash
# design/ is the source of truth for tokens and the vector logo.
# frontend/ keeps a wired copy. The copies must match.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
diff -q "$root/design/tokens.css" "$root/frontend/shared/styles/tokens.css"
diff -q "$root/design/logo/logo-icone.svg" "$root/frontend/public/logo.svg"
diff -q "$root/design/logo/logo-icone-claro.svg" "$root/frontend/public/logo-light.svg"
echo "Design tokens and logos match frontend/."
