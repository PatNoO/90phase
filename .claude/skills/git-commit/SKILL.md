---
name: git-commit
description: 'Commit message formatting for 90phase. Use when: committing changes, writing a commit message, staging and committing files, first commit on a branch, subsequent commits, git commit -m.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf) — omit for first commit on branch'
---

# Git Commit Format — 90phase

Commit format for all Claude-assisted commits in 90phase.

## All Commits

```
(MODEL_NAME) <type> [PH-<N>] <imperative description>
```

⚠️ Replace `MODEL_NAME` with the **actual Claude model running this session** — never hardcode a model name.
Common values: `claude-sonnet-4-6`, `claude-opus-4-7`, `claude-haiku-4-5`

Examples (running claude-sonnet-4-6):
```
(claude-sonnet-4-6) chore [PH-02] Configure Gradle build system
(claude-sonnet-4-6) feat [PH-05] Add sleep calculator screen
(claude-sonnet-4-6) fix [PH-12] Fix notification not firing at 18:00
(claude-sonnet-4-6) refactor [PH-07] Extract domain entities to separate files
```

## Commit Types

| Type | When |
|------|------|
| `feat` | New feature or component |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Compose/UI only, no logic change |
| `refactor` | Restructure, no behaviour change |
| `test` | Tests only |
| `chore` | Config, deps, non-src changes |
| `perf` | Performance improvement |

## Multi-Commit Strategy

When changes span multiple concerns, split into focused commits:

1. Run `git status` and `git diff --stat` to survey all changed files
2. Group by layer cohesion — domain, data, presentation each get their own commit when changed independently
3. Order: domain first, then data, then presentation, then DI/Hilt, then tests
4. Stage each group explicitly with `git add <files>` — never `git add .` when splitting

## Rules

- Format is always `(MODEL_NAME) <type> [PH-<N>] description` — no `[claude]` prefix
- Use the actual active model name, never hardcode a specific model
- Never include `Co-Authored-By` footer
- Imperative mood: "Add feature" not "Added feature"
- Subject line under 72 characters — no body, no bullet points
- Never skip hooks (`--no-verify`) unless explicitly instructed by the developer
- Stage specific files by name — avoid `git add -A` or `git add .`
