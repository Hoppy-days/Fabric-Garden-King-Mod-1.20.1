# Engineering completion gate

Every engineering task must keep `PROJECT_STATE.md` and affected documentation
accurate before being called complete. Keep one canonical checkpoint; link durable
detail instead of repeating status in README, chats, or a parallel tracker. Prior
checkpoints remain in Git. Existing project scope and human acceptance rules apply.

## Commands and configuration

Python 3.10+ and Git are sufficient; no package installation or paid API is needed.
`.completion-gate.json` selects the existing canonical path, intended target and
reviewed toolkit provenance. The adapter checks that a trusted CI target matches
configuration; the portable validator remains unchanged from the reviewed toolkit.
The validator, configured adapter, workflow and all 66 tests match the pinned
canonical source. Eight adapter tests include ignored/symlink configuration failures.

```sh
python3 -B -m unittest discover -s tools/completion-gate -p 'test_*.py' -v
python3 -B tools/completion-gate/run_gate.py --snapshot --base <merge-base-sha> --branch <task-branch> --target <target>
python3 -B tools/completion-gate/run_gate.py --base <merge-base-sha> --branch <task-branch> --target <target>
python3 -B tools/completion-gate/run_gate.py --base <merge-base-sha> --branch <task-branch> --target <target> --require-complete
```

Use an immutable task/PR comparison base, not the checkpoint's own commit SHA.
Snapshots include tracked/staged/unstaged/nonignored new files, deletions and modes.
Copy observed hashes/modes and write actual change summaries; snapshot generation
never runs tests or proves verification. Initial repositories can use `initial`,
but PR CI requires its independently computed merge base. New tasks on a continuing
PR retain a compact cumulative record for the complete comparison range.

## Canonical state and evidence

The canonical Markdown contains one outermost `completion-state` JSON block;
JSON canonical files also work via config, directly or under `completion_state`.
Required fields are demonstrated in this repository's checkpoint: task/kind/status,
implementation state, review time, baseline, project summary, exact changes,
affected docs/assessment, test results and evidence, human acceptance, integration
branch/target/status, branch evidence links, blockers, exact next step and detail
links. No self-fingerprint is required. Keep pending human/runtime work explicit.

`passed`/`failed` validation needs the actual command, result/detail, observation
time, substantive-file scope digest and durable evidence. `not_run` has a reason
and null observation/digest. `documentation_only` cannot include substantive code;
`no_behavior_change` needs a specific exception justification and still covers all
changed paths. Those kinds may explain `not_applicable` checks. An explanation
is a reported assertion, not automatically verified truth.

`blocked` requires blockers; `failed` requires failed evidence or a blocker.
`ready_for_review` needs implemented work, no failed checks/blockers, and a passed
check for behavior changes. `complete` additionally prohibits pending human gates
and unrun checks. `--require-complete` refuses unfinished handoffs; ordinary CI may
pass an honest unfinished state. Keep integration distinct: unmerged work has not
shipped on the default branch. Other relevant authorized branches belong in a
compact `branch_evidence` link index, not flattened status prose.

Human `accepted` entries require reviewer, time and durable evidence from actual
acceptance; agents must not accept for the owner. Historical repository reports
must be labeled historical, not treated as newly executed tests. Local evidence
must be a regular file in Git's trackable inventory; URLs are syntax checked only.

## CI and limits

The additive read-only PR workflow checks source/target/base metadata, includes
retarget events, runs gate/adapter tests and checks the complete PR diff. It does
not publish or use custom secrets. Required branch checks are **not configured**;
CI installation alone does not enforce merging policy. Human review is still
needed, including review of changes to the gate/workflow themselves.

Structural checks cannot prove prose freshness, adequate test coverage, actual
execution of reported commands, external evidence, runtime behavior, or human
acceptance. Markdown extensions are a heuristic, not a guarantee of no behavior.
YAML/frontmatter state, shallow history, unresolved indexes, hidden index flags,
disabled filemode tracking, changed symlinks/submodules and staged/working byte
mismatches fail closed. The bounded parser is not a full Markdown/HTML renderer.
Filters/CRLF, LFS and other checkout conventions need explicit adaptation; do not
weaken checks or alter Git/security settings silently. No application runtime is
installed or exercised by this workflow. See the [installation evidence](evidence/completion-gate-installation.md).
