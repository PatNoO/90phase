---
name: setup-github-issues
description: "Bootstrap a new GitHub project with MoSCoW labels, epic labels, milestones, EPIC parent issues (with child checklists), and all child issues. Run once at project start from a config file the developer creates."
argument-hint: "Path to the config file (default: docs/GITHUB_SETUP.md)"
---

# Setup GitHub Issues — New Project Bootstrap

Use this skill to create the complete GitHub issue structure for a new project from scratch.

The config file is: `$ARGUMENTS` (default: `docs/GITHUB_SETUP.md`)

---

## Config File Format

Before running this skill the developer must create the config file.
The file must follow this exact format:

```markdown
# GitHub Issue Setup

## Config
repo: PatNoO/my-app
prefix: APP

## EPIC-0: Project Setup
Short description of what this epic covers.

### Issues
- [must-have] [size/small] Create project scaffold
- [must-have] [size/medium] Configure build system
- [should-have] [size/small] Setup code quality tools

## EPIC-1: Domain Layer
Short description.

### Issues
- [must-have] [size/medium] Implement core entities
- [must-have] [size/large] Implement use cases
- [could-have] [size/small] Add domain layer docs
```

**MoSCoW values:** `must-have` / `should-have` / `could-have` / `wont-have`
**Size values:** `size/small` / `size/medium` / `size/large`

---

## Step 1 — Read and validate the config file

Read the config file at the path given in `$ARGUMENTS` (default: `docs/GITHUB_SETUP.md`).

Extract:
- `REPO` — from `repo:` line (e.g. `PatNoO/my-app`)
- `PREFIX` — from `prefix:` line (e.g. `APP`, `PH`, `DD`)
- All epics: each `## EPIC-N: Name` block → description + list of issues
- Per issue: MoSCoW priority, size, and title

Verify the repo exists:
```bash
gh repo view <REPO> --json name,url
```

If the repo doesn't exist or is inaccessible, **stop and report to the developer.**

---

## Step 2 — Create standard labels

Create the MoSCoW priority labels and size labels. Use `|| true` so re-runs are safe.

### MoSCoW labels
```bash
gh label create "must-have"    --repo <REPO> --color "D73A4A" --description "Required — must ship in this version" || true
gh label create "should-have"  --repo <REPO> --color "FBCA04" --description "Important — ship soon after MVP" || true
gh label create "could-have"   --repo <REPO> --color "0E8A16" --description "Nice to have — defer if needed" || true
gh label create "wont-have"    --repo <REPO> --color "CCCCCC" --description "Explicitly out of scope for now" || true
```

### Size labels
```bash
gh label create "size/small"  --repo <REPO> --color "C2E0C6" --description "1-2 hours" || true
gh label create "size/medium" --repo <REPO> --color "FEF2C0" --description "2-4 hours" || true
gh label create "size/large"  --repo <REPO> --color "F9D0C4" --description "4-8 hours" || true
```

### Type labels
```bash
gh label create "feature" --repo <REPO> --color "A2EEEF" --description "New functionality" || true
gh label create "bug"     --repo <REPO> --color "D73A4A" --description "Something isn't working" || true
gh label create "chore"   --repo <REPO> --color "D4C5F9" --description "Configuration or scaffolding" || true
gh label create "spike"   --repo <REPO> --color "FEF2C0" --description "Time-boxed investigation" || true
```

---

## Step 3 — Create epic labels and milestones

For each `EPIC-N: Name` from the config:

### Epic label
Pick a color from this palette by epic number (cycle if more than 10):
```
0 → B60205   1 → 0052CC   2 → 7057FF   3 → FF6F00   4 → FBCA04
5 → FFA500   6 → 5319E7   7 → 1D76DB   8 → E99695   9 → C5DEF5
```

Derive the short slug from the epic name (lowercase, spaces → hyphens, e.g. "Project Setup" → `setup`):

```bash
gh label create "epic-<N>:<slug>" \
  --repo <REPO> \
  --color "<color>" \
  --description "EPIC-<N> <Name>" || true
```

Also create a label to mark the EPIC parent issues themselves:
```bash
gh label create "epic" --repo <REPO> --color "5319E7" --description "Epic parent issue" || true
```

### Milestone
```bash
gh api repos/<REPO>/milestones \
  --method POST \
  --field title="EPIC-<N>: <Name>" \
  --field description="<Epic description from config>" || true
```

---

## Step 4 — Create EPIC parent issues

For each epic, create the parent issue **first** so you have its number for the child checklist later.

At this point create the issue with a placeholder body — you will update it with the full checklist in Step 6 once all child issues exist.

```bash
gh issue create \
  --repo <REPO> \
  --title "[EPIC-<N>] <Name>" \
  --body "## Epic
<Epic description from config>

## Issues
*(will be populated after child issues are created)*" \
  --label "epic,epic-<N>:<slug>" \
  --milestone "EPIC-<N>: <Name>"
```

Record the returned issue number as `EPIC_<N>_NUMBER`.

---

## Step 5 — Create child issues

Process epics in order (EPIC-0 first). Within each epic, create issues in the order they appear in the config.

For each issue entry `- [<moscow>] [<size>] <Title>`:

### Determine next PREFIX number

```bash
gh issue list --repo <REPO> --state all --json title --limit 500 | \
  python3 -c "
import sys, json, re
issues = json.load(sys.stdin)
nums = [int(m.group(1)) for i in issues for m in [re.search(r'<PREFIX>-(\d+)', i['title'])] if m]
print(max(nums) + 1 if nums else 1)
"
```

Call the result `NEXT`.

### Create the issue

```bash
gh issue create \
  --repo <REPO> \
  --title "[<PREFIX>-<NEXT>] <Title>" \
  --body "## Context
<Brief description of what this issue covers, inferred from the title and epic context.>

## Acceptance Criteria
- [ ] <AC1 — infer from title and epic>
- [ ] <AC2>

## Definition of Done
- [ ] Implementation complete
- [ ] Tests written if applicable
- [ ] Build passes" \
  --label "<moscow>,epic-<N>:<slug>,<size>,feature" \
  --milestone "EPIC-<N>: <Name>"
```

Record: `epic_N_issues` list of `#<number> [<PREFIX>-<NEXT>] <Title>`

---

## Step 6 — Update each EPIC body with child checklist

For each EPIC-N, edit the parent issue body to replace the placeholder with the full checklist:

```bash
gh issue edit <EPIC_N_NUMBER> --repo <REPO> --body "$(cat <<'EOF'
## Epic
<Epic description>

## Issues
- [ ] #<child1_number> [<PREFIX>-<NN>] <Title>
- [ ] #<child2_number> [<PREFIX>-<NN>] <Title>
...
EOF
)"
```

---

## Step 7 — Report summary

Print a summary table:

```
✅ GitHub issue structure created for <REPO>

Labels created:
  MoSCoW:  must-have, should-have, could-have, wont-have
  Size:    size/small, size/medium, size/large
  Type:    feature, bug, chore, spike
  Epics:   epic-0:<slug> … epic-N:<slug>

Epics and issues:
  EPIC-0: <Name>  (#<epic_issue>)  →  <N> issues  [<PREFIX>-01 … <PREFIX>-NN]
  EPIC-1: <Name>  (#<epic_issue>)  →  <N> issues
  …

Total issues created: <total>
```

---

## Rules

- Always use `|| true` on label/milestone creation — re-runs must be safe
- Never delete existing labels or issues — only create/update
- EPIC parent issues are created before child issues so their numbers are lower
- Child issues are numbered sequentially using `PREFIX` — the number comes from the actual highest existing issue, not a guess
- Ticket IDs in titles always use the format `[PREFIX-NN]` with zero-padded two digits minimum (e.g. `[APP-01]`, `[APP-12]`)
- EPIC bodies always use `## Issues` as the section header
- Child issue checklist items always use `- [ ] #<number> [PREFIX-NN] Title` format so GitHub renders them as task progress on the EPIC issue
- MoSCoW replaces numeric priority (p0/p1/p2/p3) — never create p-labels in new projects
