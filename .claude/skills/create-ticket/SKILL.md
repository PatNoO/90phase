---
name: create-ticket
description: "Create a GitHub issue end-to-end for the 90phase project: classify type, infer labels/milestone, draft title and description from the canonical template, then post the issue via gh CLI. Use when asked to create, open, or add a ticket/issue (e.g. 'Create a ticket for …')."
argument-hint: "Short description of what the ticket is for"
---

# Create GitHub Issue — 90phase

Use this skill to create a well-structured GitHub issue following 90phase conventions.

The request is: `$ARGUMENTS`

---

## Step 1 — Classify the ticket

| Type | When | Example title |
|------|------|---------------|
| `feature` | New user-facing screen, composable, or use case | `Implement sleep calculator screen` |
| `bug` | Something broken or incorrect | `Fix notification not firing at 18:00` |
| `chore` | Technical work, no user-visible change | `Set up Hilt DI modules for data layer` |
| `spike` | Time-boxed investigation | `Spike: Evaluate WorkManager vs AlarmManager for exact triggers` |

---

## Step 2 — Determine the next PH number

Every issue title is prefixed with `PH-<N>`.

```bash
gh issue list --repo PatNoO/90phase --state all --json title --limit 200 | \
  python3 -c "
import sys, json, re
issues = json.load(sys.stdin)
nums = [int(m.group(1)) for i in issues for m in [re.search(r'PH-(\d+)', i['title'])] if m]
print(max(nums) + 1 if nums else 1)
"
```

Call the result `NEXT`. Use it as `PH-<NEXT>` in the title.

---

## Step 3 — Infer labels and milestone

**Infer the epic/module from the description:**

| Clue in description | Epic milestone | Labels |
|---|---|---|
| module, gradle, hilt, setup, config, project structure | EPIC-0: Project Setup | `p0,epic:setup,setup` |
| entity, usecase, domain, repository interface, result type | EPIC-1: Domain Layer | `p0,epic:domain,feature` |
| screen, compose, UI, design, component, theme, color, typography | EPIC-2: UI Design | `p0,epic:ui,feature` |
| room, dao, datastore, database, mapper, migration | EPIC-3: Data Layer | `p0,epic:data,feature` |
| notification, alarm, broadcast, receiver, boot | EPIC-4: Notifications | `p0,epic:notifications,feature` |
| firebase, firestore, auth, sync, worker | EPIC-5: Firebase Sync | `p1,epic:firebase,feature` |
| discovery, phase, analysis, adaptive, weekly shift | EPIC-6: Discovery Phase | `p2,epic:discovery,feature` |
| viewmodel, stateflow, integration, navigation | EPIC-7: ViewModels | `p0,epic:viewmodels,feature` |
| test, coverage, unit test, integration test, e2e | EPIC-8: Testing | `p0,epic:testing,feature` |
| polish, bug fix, performance, animation, icon, readme | EPIC-9: Polish & MVP | `p0,epic:polish,enhancement` |

**Add type label** from Step 1: `feature` / `bug` / `chore` / `spike`

**Add size label** based on scope:
- Single file or small change → `size/small`
- 2–4 files or moderate logic → `size/medium`
- Multi-layer or complex → `size/large`

**Priority from user language:**

| User says | Priority label |
|-----------|---------------|
| critical / urgent / blocking | `p0` |
| high / important | `p0` |
| normal / medium (default) | match epic default |
| low / nice-to-have | `p2` |

---

## Step 4 — Draft the issue body

Use the template matching the ticket type from Step 1.

### Feature template

```markdown
## Context
<Why does this feature exist? What user problem does it solve?>

## Acceptance Criteria
- [ ] AC1: <Specific, testable outcome>
- [ ] AC2: <Specific, testable outcome>
- [ ] AC3: <Specific, testable outcome>

## Scope Boundary
**In scope:**
- <What is included>

**Out of scope:**
- <What is explicitly not included>

## Design Reference
- Follows Material3 theming — `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes` only
- Relevant screen: <Calculator / History / Settings / Onboarding / Discovery>
- Dark mode must be verified

## Technical Notes
- Module: <calculator / history / notifications / settings / onboarding / discovery / shared>
- Layer(s) touched: <Presentation only / Presentation + Domain / Multi-layer / Data only>
- ViewModel state: StateFlow exposed, MutableStateFlow kept private
- New Room schema change: <Yes — describe migration / No>
- New Firebase schema change: <Yes — describe / No>
- New Gradle dependency: <Yes — name and reason / No>
- Domain purity check: <No Android/Firebase imports in :domain / N/A>
- Dependencies: <Other SP ticket numbers or "none">

## Edge Cases to Handle
- [ ] Loading state (skeleton or progress indicator)
- [ ] Empty state (no sleep logs yet)
- [ ] Error state (Room failure, Firebase sync failure)
- [ ] Dark mode
- [ ] Back navigation (predictive back gesture on Android 14+)

## Definition of Done
- [ ] `./gradlew build` passes
- [ ] `./gradlew detekt` passes
- [ ] `./gradlew ktlintCheck` passes
- [ ] `./gradlew test` passes
- [ ] Domain purity verified (no Android/Firebase in :domain)
```

### Bug template

```markdown
## What Happens
<Actual behaviour>

## What Should Happen
<Expected behaviour>

## Steps to Reproduce
1. <Step 1>
2. <Step 2>
3. <Step 3>

## Environment
- Device: <e.g. Pixel 7 / emulator API 34>
- Android version: <e.g. Android 14 / API 34>
- Build: <debug / release, commit SHA if known>
- Screen: <e.g. Calculator, History>

## Logs / Stack Trace
<Paste Logcat output or "Not yet captured">

## Definition of Done
- [ ] Bug reproduced and root cause identified
- [ ] Fix implemented and verified on same environment
- [ ] Regression test added
- [ ] `./gradlew build` and `./gradlew test` pass
```

### Chore template

```markdown
## Context
<Why is this technical work needed? What does it enable?>

## Done When
- [ ] <Concrete, verifiable outcome>
- [ ] <Concrete, verifiable outcome>

## Technical Notes
- <Relevant details, constraints>
- Touches Room schema: <Yes — migration required / No>
- Touches Firebase schema: <Yes — describe / No>
- New Gradle dependencies: <Yes — name and reason / No>
- Domain purity maintained: <Yes / N/A>

## Definition of Done
- [ ] `./gradlew build` passes
- [ ] `./gradlew detekt` passes
- [ ] `./gradlew test` passes
```

### Spike template

```markdown
## Question to Answer
<Specific question this spike must resolve>

## Time Box
<Maximum time, e.g. 2 hours>

## Output
- [ ] Written findings posted as comment on this issue
- [ ] Recommendation with pros/cons
- [ ] Follow-up tickets created if needed
```

---

## Step 5 — Create the issue

```bash
gh issue create \
  --repo PatNoO/90phase \
  --title "[PH-<NEXT>] <drafted title>" \
  --body "<drafted body from Step 4>" \
  --label "<labels from Step 3>" \
  --milestone "<epic milestone from Step 3>"
```

---

## Step 6 — Report back

```
✅ Issue created: PH-<NEXT> — <Title>
🔗 <GitHub issue URL>

Type:      <feature / bug / chore / spike>
Epic:      <EPIC-N: Name>
Labels:    <labels>
Milestone: <milestone>

Suggested branch: 90p/PH-<NEXT>-short-description
```

---

## Rules

- Never push code or create branches — issues only
- Ticket titles follow format: `[PH-N] Short imperative description`
- Always use `|| true` on label/milestone ops so re-runs are safe
- Room schema changes must be flagged in Technical Notes
- Firebase schema changes must be flagged in Technical Notes
- New Gradle dependencies must be flagged and require developer approval before adding
- Domain tickets must note: no Android/Firebase imports allowed in `:domain` module
- Reference the relevant spec doc from `docs/` when applicable
