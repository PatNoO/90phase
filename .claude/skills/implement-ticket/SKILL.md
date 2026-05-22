---
name: implement-ticket
description: "Implement a GitHub issue end-to-end: fetch the issue, implement the feature following 90phase standards, then post a structured comment and close the issue. Use when asked to implement a ticket, work on an issue, or start a ticket (e.g. 'Implement PH-07' or 'implement issue #12')."
argument-hint: "GitHub issue number or PH-N reference, e.g. PH-07 or #12"
---

# GitHub Issue Implementation — 90phase

Use this skill to implement a GitHub issue end-to-end, following all 90phase coding standards and architecture rules.

The ticket to implement is: `$ARGUMENTS`

---

## Step 1 — Fetch the issue

Resolve the issue number from `$ARGUMENTS` — accept either `PH-07` or `#7` format.

```bash
gh issue list --repo PatNoO/90phase --state all --search "PH-<N>" --json number,title,body,labels,milestone --limit 5
```

Or by number directly:
```bash
gh issue view <number> --repo PatNoO/90phase --json number,title,body,labels,milestone
```

From the issue, identify:
1. **Ticket type:** feature / bug / chore / spike (from labels)
2. **Module:** calculator / history / notifications / settings / onboarding / discovery / shared (from labels or body)
3. **Layer(s):** Presentation only / Presentation + Domain / Multi-layer / Data only (from Technical Notes in body)
4. **Acceptance Criteria:** extract all `- [ ]` items from the body
5. **Presentation-only?** — if so, all values must use typed fake/preview data and every ViewModel integration point gets a `// TODO: wire to ViewModel` comment
6. **Room schema change?** — if yes, stop and confirm migration strategy with developer before writing any code
7. **Firebase schema/auth change?** — if yes, stop and require explicit developer confirmation
8. **New Gradle dependency?** — if yes, stop, explain why, and get approval before adding

---

## Step 2 — Create the feature branch

```bash
git checkout main
git pull origin main
git checkout -b 90p/PH-<N>-short-description
git branch --show-current
```

Branch naming: `90p/PH-<N>-short-kebab-description`

---

## Step 3 — Plan the implementation

Read existing code in the relevant area before making any changes. Identify which files need to change:

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
- **Notification channels** — registered in `Application` class only, never in Activity

### Material3 / Compose rules

- `MaterialTheme.colorScheme.*` — never hardcoded hex
- `MaterialTheme.typography.*` — never hardcoded sp values
- `MaterialTheme.shapes.*` — cards 16.dp, buttons 12.dp
- Spacing scale: 4, 8, 12, 16, 24, 32 dp — no arbitrary values

### State coverage

Every interactive Compose screen must handle:
- [ ] Loading state (CircularProgressIndicator or skeleton composable)
- [ ] Error state (Room failure, Firebase sync error)
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
Fix all violations. Then:

```bash
./gradlew test
```
All unit tests must pass. Then:

```bash
./gradlew build
```
Must succeed with no Kotlin compilation errors.

**All four must pass before committing.**

---

## Step 6 — Commit

Stage specific files only — never `git add .` or `git add -A`.

All commits:
```
(MODEL_NAME) <type> [PH-<N>] imperative description
```

Example:
```
(claude-sonnet-4-6) feat [PH-05] Add sleep calculator screen
```

Types: `feat` `fix` `style` `refactor` `chore` `docs` `perf` `test`

Rules:
- No `[claude]` prefix — format starts with `(MODEL_NAME)`
- Imperative tense — under 72 characters
- Never include `Co-Authored-By` footer
- Never `--no-verify`

---

## Step 7 — Post implementation comment and close issue

Post a structured comment on the GitHub issue:

```bash
gh issue comment <number> --repo PatNoO/90phase --body "$(cat <<'EOF'
## PH-<N> Implementation Complete

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
<Presentation-only placeholders? TODO markers? Room migration needed? New Gradle deps? Firebase schema changes? Architecture decisions?>

### Validation
- `./gradlew ktlintCheck` ✅
- `./gradlew detekt` ✅
- `./gradlew test` ✅
- `./gradlew build` ✅
EOF
)"
```

Then check off all completed Acceptance Criteria and Definition of Done checkboxes directly on the issue body:

```bash
gh issue edit <number> --repo PatNoO/90phase --body "$(cat <<'EOF'
<full issue body with - [ ] changed to - [x] for every completed item>
EOF
)"
```

Then ask the developer whether to close the issue now or leave it open until the PR is merged:

- Close now:
```bash
gh issue close <number> --repo PatNoO/90phase
```
- Or leave open — it will auto-close when the PR merges if the commit contains `closes #<number>`

---

## Completion checks

- [ ] Issue fetched and all acceptance criteria addressed
- [ ] Feature branch created from `main` following `90p/PH-<N>-...` convention
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
- [ ] `./gradlew ktlintFormat` run
- [ ] `./gradlew ktlintCheck` passes
- [ ] `./gradlew detekt` passes
- [ ] `./gradlew test` passes
- [ ] `./gradlew build` succeeds
- [ ] Commits follow `(MODEL_NAME) <type> [PH-<N>]` format, staged per-file
- [ ] Implementation comment posted to GitHub issue
- [ ] AC and DoD checkboxes checked off on the issue body via `gh issue edit`
- [ ] Issue closed or `closes #N` in commit for auto-close on merge
