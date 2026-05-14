---
name: git-commit
description: 'Commit message formatting for 90phase. Use when: committing changes, writing a commit message, staging and committing files, first commit on a branch, subsequent commits, git commit -m.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf) — omit for first commit on branch'
---

# Git Commit Format — 90phase

All commits made with Claude Code assistance must start with the `[claude]` prefix. This is a strict project rule from CLAUDE.md.

## First Commit on a Branch

Mirror the branch ticket ID and use Title Case with model name:

```
[claude] (MODEL_NAME) PH-<N> <Title Case Description>
```

⚠️ Replace `MODEL_NAME` with the **actual Claude model running this session** — never hardcode a model name.
Check which model is active and use that exact name.
Common values: `claude-sonnet-4-6`, `claude-opus-4-7`, `claude-haiku-4-5`

Example (branch `90p/PH-05-sleep-calculator-screen`, running claude-sonnet-4-6):
```
[claude] (claude-sonnet-4-6) PH-05 Sleep Calculator Screen
```

## Subsequent Commits

```
[claude] (MODEL_NAME) [<type>] <imperative description>
```

Examples (using claude-sonnet-4-6):
```
[claude] (claude-sonnet-4-6) [feat] Add bedtime recommendation list to calculator screen
[claude] (claude-sonnet-4-6) [fix] Fix sleep latency offset in cycle calculation
[claude] (claude-sonnet-4-6) [chore] Add SleepLog entity to Room database
[claude] (claude-sonnet-4-6) [style] Adjust spacing on calculator screen cards
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

- Prefix is always `[claude] (MODEL_NAME)` — use the actual active model, never hardcode a specific model name
- Never include `Co-Authored-By` footer
- Imperative mood: "Add feature" not "Added feature"
- Subject line under 72 characters — no body, no bullet points
- First commit on branch = ticket mirror (no type prefix); all subsequent = type prefix required
- Never skip hooks (`--no-verify`) unless explicitly instructed by the developer
- Stage specific files by name — avoid `git add -A` or `git add .`
