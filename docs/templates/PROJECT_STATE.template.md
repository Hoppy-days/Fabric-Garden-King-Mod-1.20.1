# Project state

Copy into the repository's canonical checkpoint. Replace every placeholder using
the observed snapshot, actual results, and reviewed branch scope. This template is
intentionally not a passing completion claim. Keep older checkpoints in Git.

```completion-state
{
  "schema_version": 1,
  "task_id": "<task identifier>",
  "kind": "behavior_change",
  "status": "in_progress",
  "implementation": "in_progress",
  "reviewed_at": "<actual ISO-8601 time with timezone>",
  "base": "<immutable comparison SHA, or initial for a whole-tree bootstrap>",
  "project_state": "<concise branch-scoped project state>",
  "integration": {
    "branch": "<inspected task branch>",
    "target": "<intended target branch>",
    "status": "unmerged",
    "evidence": []
  },
  "branch_evidence": [],
  "changes": [
    {"path": "<changed path>", "sha256": "<snapshot hash or deleted>", "mode": "100644", "summary": "<actual change>"}
  ],
  "documentation": {
    "affected": ["PROJECT_STATE.md"],
    "assessment": "<which docs were reviewed/updated and why these suffice>"
  },
  "validation": [
    {"command": "<actual or planned command>", "outcome": "not_run", "detail": "<why not run and exact next action>",
     "observed_at": null, "scope_digest": null, "evidence": []}
  ],
  "acceptance": [
    {"scope": "<local/human gate, or why none applies>", "status": "pending", "detail": "<owner/action needed>", "evidence": []}
  ],
  "blockers": [],
  "next_step": "<specific next action and responsible actor>",
  "details": ["<existing durable local detail or HTTPS evidence link>"],
  "exception": null
}
```
