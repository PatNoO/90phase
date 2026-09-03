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

## Step 1b — Check if the work already exists

Before creating anything, verify the implementation does not already exist in the codebase or in a closed ticket.

### 1. Extract symbols from the description

Read `$ARGUMENTS` and identify the key identifiers — class names, use case names, file names, method names. Look for:
- CamelCase nouns (e.g. `StartDiscoveryPhaseUseCase`, `SettingsViewModel`, `SleepLogDao`)
- Screen or feature names that map to files (e.g. "calculator screen" → `CalculatorScreen`)
- Repository or entity names

### 2. Grep the codebase for each symbol

For each identified symbol run:

```bash
grep -rn "<Symbol>" app/ domain/ data/ presentation/ --include="*.kt" | head -10
```

If **any symbol is found**:
- Show the file and line number where it exists
- Run `git log --oneline --all -- <file>` to find which commit/PR introduced it
- Print a clear warning:

```
⚠️  DUPLICATE RISK
"<Symbol>" already exists in <file:line>
Introduced in: <commit hash> <commit message>

Options:
  A) Stop — this is already implemented. Close or don't create the ticket.
  B) Proceed — this ticket is a refinement or extension of existing code (state what's new).
  C) Link — the new ticket should reference the existing file as a starting point.
```

Stop and ask the developer which option to take. Do not continue to Step 2 until they confirm.

If **no symbols are found** — continue to Step 2 silently.

### 3. Check closed tickets for overlap

```bash
gh issue list --repo PatNoO/90phase --state closed --json number,title --limit 200 | \
  python3 -c "
import sys, json
issues = json.load(sys.stdin)
query = '$ARGUMENTS'.lower()
for i in issues:
    if any(word in i['title'].lower() for word in query.split() if len(word) > 4):
        print(f'#{i[\"number\"]}: {i[\"title\"]}')
"
```

If matching closed tickets are found, show them and ask the developer to confirm there is no overlap before continuing.

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
| module, gradle, hilt, setup, config, project structure | EPIC-0: Project Setup | `p0,epic-0:setup,feature` |
| entity, usecase, domain, repository interface, result type | EPIC-1: Domain Layer | `p0,epic-1:domain,feature` |
| screen, compose, UI, design, component, theme, color, typography | EPIC-2: UI Design | `p0,epic-2:ui,feature` |
| room, dao, datastore, database, mapper, migration | EPIC-3: Data Layer | `p0,epic-3:data,feature` |
| notification, alarm, broadcast, receiver, boot | EPIC-4: Notifications | `p0,epic-4:notifications,feature` |
| firebase, firestore, auth, sync, worker | EPIC-5: Firebase Sync | `p1,epic-5:firebase,feature` |
| discovery, phase, analysis, adaptive, weekly shift | EPIC-6: Discovery Phase | `p2,epic-6:discovery,feature` |
| viewmodel, stateflow, integration, navigation | EPIC-7: ViewModels | `p0,epic-7:viewmodels,feature` |
| test, coverage, unit test, integration test, e2e | EPIC-8: Testing | `p0,epic-8:testing,feature` |
| polish, bug fix, performance, animation, icon, readme | EPIC-9: Polish & MVP | `p0,epic-9:polish,enhancement` |

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
- Night Sky design system only — `SleepColors.*`, `SleepTypography.*`, `SleepShapes.*`, `Spacing.*`
  (see CLAUDE.md § Design System). No raw hex, no raw `.sp`, no arbitrary `.dp`
- All user-facing strings via `stringResource` — added to both `values/strings.xml` (en) and `values-sv/strings.xml`
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

Save the returned issue number as `NEW_ISSUE_NUMBER` and the URL as `NEW_ISSUE_URL`.

---

## Step 5b — Add the ticket to the epic parent issue

Every epic has a parent issue (e.g. "[EPIC-3] Data Layer") that contains a checklist of all its child tickets. The new ticket must be appended to that checklist as an unchecked item.

**Find the epic parent issue for this milestone:**

```bash
gh issue list --repo PatNoO/90phase --state all --json number,title,labels \
  --limit 200 | jq -r '.[] | select(.labels[]?.name == "epic") | "\(.number) \(.title)"'
```

Match the result to the milestone (e.g. milestone "EPIC-3: Data Layer" → look for "[EPIC-3]" in the title). Call the matching issue number `EPIC_ISSUE_NUMBER`.

**Fetch the current epic body:**

```bash
gh issue view <EPIC_ISSUE_NUMBER> --repo PatNoO/90phase --json body | jq -r '.body'
```

**Append the new ticket as an unchecked checklist item** at the end of the `## Issues` list in the epic body:

```
- [ ] #<NEW_ISSUE_NUMBER> [PH-<NEXT>] <drafted title>
```

**Update the epic issue body:**

```bash
gh issue edit <EPIC_ISSUE_NUMBER> --repo PatNoO/90phase --body "<updated body with new line appended>"
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
