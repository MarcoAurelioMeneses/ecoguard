import { dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { FlatCompat } from "@eslint/eslintrc";
import prettier from "eslint-config-prettier";

const compat = new FlatCompat({
  baseDirectory: dirname(fileURLToPath(import.meta.url)),
});

const fetchBan = {
  selector: "CallExpression[callee.name='fetch']",
  message: "Call the typed client in src/lib/api. Feature api modules map the result.",
};

const eslintConfig = [
  {
    ignores: ["next-env.d.ts", ".next/**", "src/lib/api/generated.ts", "public/maplibre-gl-worker.mjs"],
  },
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  {
    files: ["src/components/ui/**/*.{ts,tsx}"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/features/**", "@/lib/api/**", "@/app/**"],
              message: "UI primitives stay independent of features and the HTTP client.",
            },
          ],
        },
      ],
    },
  },
  {
    files: ["src/features/**/components/**/*.{ts,tsx}", "src/app/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: [
                "@/lib/api",
                "@/lib/api/**",
                "@/features/*/api",
                "@/features/*/api/**",
                "**/api",
                "**/api/**",
              ],
              message: "Compose hooks and components. Do not import a feature API or the HTTP client.",
            },
          ],
        },
      ],
      "no-restricted-syntax": ["error", fetchBan],
    },
  },
  {
    files: ["src/features/**/hooks/**/*.{ts,tsx}", "src/features/**/model/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/lib/api", "@/lib/api/**"],
              message: "Hooks and models call the feature api module, not the HTTP client.",
            },
          ],
        },
      ],
      "no-restricted-syntax": ["error", fetchBan],
    },
  },
  {
    files: ["src/**/*.tsx"],
    ignores: ["**/*.test.tsx"],
    rules: {
      "max-lines": ["error", { max: 200, skipBlankLines: true, skipComments: true }],
    },
  },
  prettier,
];

export default eslintConfig;
