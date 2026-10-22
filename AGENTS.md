# Agent working rules

Read this file, PROJECT_STATE.md, README.md and affected task-specific docs. Inspect
the actual branch, baseline and Git status before editing; preserve unrelated work.

Preserve existing Fabric/Gradle application code, assets and topic documentation. This maintenance repository has no newly verified Minecraft runtime in this task. Do not regenerate content, launch gameplay, change settings, or expand feature scope without its own task.

## Documentation completion rule

Every engineering task must update the canonical [PROJECT_STATE.md](PROJECT_STATE.md)
and affected docs before completion, following [docs/COMPLETION_GATE.md](docs/COMPLETION_GATE.md).
Record actual changes, tests actually run/outcomes, pending local/human acceptance,
blockers, exact next step and durable evidence links. Keep implementation, automated
checks, human acceptance and integration separate; never imply task-branch work is
merged. Historical reports are not fresh verification. Run the configured gate;
use `--require-complete` before claiming completion, otherwise report the honest
unfinished state. CI installation does not configure required branch checks.
