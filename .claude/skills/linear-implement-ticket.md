---
name: linear-implement-ticket
description: "Implement a Linear ticket end-to-end: look up the ticket, implement the feature following 90phase standards, then post a structured comment. Use when asked to implement a ticket, work on a Linear issue, or start a ticket (e.g. 'Implement ticket SP-07')."
argument-hint: "Linear ticket ID, e.g. SP-07"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash(git checkout main), Bash(git pull origin main), Bash(git checkout -b), Bash(git branch --show-current), Bash(./gradlew detekt), Bash(./gradlew ktlintCheck), Bash(./gradlew ktlintFormat), Bash(./gradlew test), Bash(./gradlew build), mcp__linear__get_issue, mcp__linear__save_comment, mcp__linear__save_issue
disable-model-invocation: false
---

# Linear Ticket Implementation — 90phase

Use this skill to implement a Linear ticket end-to-end, following all 90phase coding standards and architecture rules.

The ticket to implement is: `$ARGUMENTS`

---

## Step 1 — Look up the ticket

1. Use `mcp__linear__get_issue` to fetch the full ticket: title, description, acceptance criteria, labels
2. Identify ticket type: **feature / bug / chore / spike**
3. Note the **module** label: calculator / history / notifications / settings / onboarding / discovery / shared
4. Note the **layer(s)**: Presentation only / Presentation + Domain / Multi-layer / Data only
5. Note whether ticket is **Presentation only** — if so, all values must use typed fake/preview data and every ViewModel integration point must have a `// TODO: wire to ViewModel` comment
6. If the ticket touches a **Room schema change**, stop and confirm the migration strategy with the developer before writing code
7. If the ticket touches **Firebase schema or auth**, stop and require explicit developer confirmation
8. If the ticket requires a **new Gradle dependency**, stop, explain why, and get approval before adding it
9. Note any specific starting instruction from the user

---

## Step 2 — Create the feature branch

```bash
git checkout main
git pull origin main
git checkout -b 90p/SP-<N>-short-description
git branch --show-current
```

Branch naming: `90p/SP-<N>-short-kebab-description`

---

## Step 3 — Plan the implementation

Read existing code in the relevant area before making changes. Identify which files need to change:

| Layer | Location |
|-------|----------|
| Compose screen | `presentation/screens/<module>/<Name>Screen.kt` |
| ViewModel | `presentation/viewmodels/<Name>ViewModel.kt` |
| Navigation | `presentation/navigation/AppNavGraph.kt` |
| Theme / design tokens | `presentation/theme/` |
| Use case | `domain/usecases/<Name>UseCase.kt` |
| Entity | `domain/entities/<Name>.kt` |
| Repository interface | `domain/repositories/<Name>Repository.kt` |
| Result type | `domain/common/Result.kt` |
| Room DAO | `data/local/room/<Name>Dao.kt` |
| Room entity | `data/local/room/<Name>Entity.kt` |
| DataStore | `data/local/datastore/<Name>DataStore.kt` |
| Firebase remote | `data/remote/firebase/<Name>RemoteDataSource.kt` |
| Repository impl | `data/repositories/<Name>RepositoryImpl.kt` |
| Hilt modules | `app/di/<Name>Module.kt` |

Check for existing classes before creating new ones. Only touch the layers the ticket actually requires.

---

## Step 4 — Implement the feature

Follow all architecture and coding standards from `CLAUDE.md`.

### Architecture rules (STRICT)

- **Domain purity** — `:domain` MUST NOT import Android framework, Firebase, Room, or any external library except Kotlin stdlib and coroutines
- **Room is source of truth** — never read directly from Firebase inside a use case or ViewModel
- **Offline-first** — every feature must work without network; Firebase is background sync only
- **Unidirectional data flow** — Presentation → Domain → Data; Data never updates UI state directly
- **Result<T>** — all repository methods and use cases return `Result<T>`; never throw across layer boundaries
- **StateFlow** — ViewModels expose `StateFlow`, never `MutableStateFlow`
- **No background work outside WorkManager** — no raw threads, no `GlobalScope`, no `CoroutineScope` in Application class
- **Notification channels** — registered in `Application` class only, never in Activity or Fragment

### Material3 / Compose rules

- `MaterialTheme.colorScheme.*` — never hardcoded hex
- `MaterialTheme.typography.*` — never hardcoded sp values
- `MaterialTheme.shapes.*` — cards 16.dp, buttons 12.dp
- Spacing scale: 4, 8, 12, 16, 24, 32 dp — no arbitrary values

### State coverage

Every interactive Compose screen must handle:
- [ ] Loading state (CircularProgressIndicator or skeleton composable)
- [ ] Error state (fetch failed, Room failure, Firebase sync error)
- [ ] Empty state (no sleep logs, no results)

### Presentation-only tickets

- Use typed fake data matching the real domain entities (`UserProfile`, `SleepLog`, `SleepCalculation`)
- Add `// TODO: wire to ViewModel` at every ViewModel integration point
- Add `@Preview` composables with the fake data
- Never use `Any` or unchecked casts even for preview data

### Decision points

| Condition | Action |
|-----------|--------|
| New Room table or column | Stop — confirm migration strategy with developer first |
| New Firebase schema change | Stop — confirm with developer first |
| New Gradle dependency | Stop — explain why, get approval before adding |
| Touches notification channels | Register in `Application` class only |
| Touches WorkManager | Never trigger side effects in unit tests |
| Domain layer touched | Verify zero Android/Firebase imports in `:domain` |
| Presentation-only ticket | Typed fake data + `// TODO: wire to ViewModel` markers |
| New shared composable | Place in `presentation/theme/` or a shared composables package |
| New domain type | Add to `domain/entities/` |

---

## Step 5 — Run validation

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
```
Fix all errors. Then:

```bash
./gradlew detekt
```
Fix all violations.

```bash
./gradlew test
```
All unit tests must pass.

```bash
./gradlew build
```
Must succeed with no Kotlin compilation errors.

**All four must pass before committing.**

---

## Step 6 — Commit

Stage specific files — never `git add .` or `git add -A`.

First commit on the branch:
```
[claude] (claude-sonnet-4-6) SP-<N> Title Case Description
```

Subsequent commits:
```
[claude] (claude-sonnet-4-6) [<type>] imperative description
```

Types: `feat` `fix` `style` `refactor` `chore` `docs` `perf` `test`

Rules:
- Imperative tense — under 72 characters
- Never include `Co-Authored-By` footer
- Never `--no-verify`

---

## Step 7 — Post implementation comment and update ticket status

Use `mcp__linear__save_comment` to post:

```markdown
## SP-<N> Implementation Complete

Task: <Linear ticket URL>

### Summary
<One or two sentences describing what was implemented and why.>

### What Was Done
1. **Domain layer** — <item or "Not touched">
2. **Data layer** — <item or "Not touched">
3. **Presentation layer** — <item or "Not touched">
4. **DI / Hilt modules** — <item or "Not touched">
5. **Tests** — <item or "Not touched">

### Acceptance Criteria Coverage
- [ ] AC1: <description>
- [ ] AC2: <description>

### Manual Test Steps
1. <Step 1>
2. <Step 2>

### Notes / Tradeoffs
- <Presentation-only placeholders? TODO markers? Room migration needed? New Gradle deps? Firebase schema changes? Architecture decisions?>
```

Omit layers not touched. If Presentation-only, note which values are fake/preview data.

Then use `mcp__linear__save_issue` to move the ticket to **In Progress** (or **Done** if the PR is ready to merge — ask the developer which is appropriate).

---

## Completion checks

- [ ] Ticket fetched and all acceptance criteria addressed
- [ ] Feature branch created from `main` following `90p/SP-<N>-...` convention
- [ ] Domain purity verified — zero Android/Firebase imports in `:domain`
- [ ] Room is source of truth — no direct Firebase reads in use cases or ViewModels
- [ ] All repository methods and use cases return `Result<T>`
- [ ] ViewModel exposes `StateFlow`, not `MutableStateFlow`
- [ ] Loading, error, and empty states handled in every Compose screen
- [ ] No hardcoded colors, spacing, or typography — Material3 tokens only
- [ ] No `Any` types or unchecked casts in domain/data layers
- [ ] Presentation-only tickets use typed fake data with `// TODO: wire to ViewModel`
- [ ] Room schema changes confirmed with developer before implementation
- [ ] Firebase schema/auth changes confirmed with developer before implementation
- [ ] New Gradle dependencies approved before adding
- [ ] Notification channels registered in `Application` class only
- [ ] No background work outside WorkManager
- [ ] `./gradlew ktlintCheck` passes
- [ ] `./gradlew detekt` passes
- [ ] `./gradlew test` passes
- [ ] `./gradlew build` succeeds
- [ ] Commits follow `[claude] (MODEL_NAME)` format, staged per-file
- [ ] Implementation comment posted to Linear ticket
- [ ] Ticket status updated in Linear
