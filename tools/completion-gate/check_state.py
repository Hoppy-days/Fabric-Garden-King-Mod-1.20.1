#!/usr/bin/env python3
"""Validate a bounded project checkpoint using only Python's standard library.

This checks structure and Git/file consistency, never the truth of reported prose,
test execution, external evidence, or human acceptance. See docs/completion-gate.md.
"""

import argparse
from datetime import datetime, timezone
import hashlib
import ipaddress
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys
from urllib.parse import urlparse


STATE = "PROJECT_STATE.md"
STATUSES = {"in_progress", "blocked", "failed", "ready_for_review", "complete"}
KINDS = {"behavior_change", "documentation_only", "no_behavior_change"}


class InvalidState(ValueError):
    pass


def require(condition, message):
    if not condition:
        raise InvalidState(message)


def git(root, *args):
    result = subprocess.run(
        ["git", "-C", str(root), *args], capture_output=True, check=False
    )
    require(result.returncode == 0, f"Git command failed ({' '.join(args)}): " + result.stderr.decode(errors="replace").strip())
    return result.stdout


def text(value, label):
    require(isinstance(value, str) and bool(value.strip()), f"{label}: nonempty text required")
    require(value.strip().lower() not in {"todo", "tbd", "unknown", "replace me"},
            f"{label}: replace placeholder with an explicit observation or limitation")
    return value


def fields(value, names, label):
    require(isinstance(value, dict), f"{label}: object required")
    require(set(names) <= value.keys(), f"{label}: missing fields {sorted(set(names) - value.keys())}")


def timestamp(value, label):
    text(value, label)
    try:
        stamp = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as exc:
        raise InvalidState(f"{label}: ISO-8601 timestamp required") from exc
    require(stamp.tzinfo is not None, f"{label}: timezone required")
    require(stamp <= datetime.now(timezone.utc), f"{label}: future timestamp")
    return stamp


def local_path(root, value):
    text(value, "path")
    path = PurePosixPath(value)
    require(not path.is_absolute() and ".." not in path.parts and "\\" not in value
            and str(path) == value and value != ".", f"unsafe or noncanonical path: {value}")
    target = root / value
    require(target.resolve().is_relative_to(root.resolve()), f"path escapes repository: {value}")
    return target


def links(root, values, label, nonempty=True):
    require(isinstance(values, list) and (bool(values) or not nonempty), f"{label}: evidence/link list required")
    for value in values:
        text(value, label)
        if value.startswith("https://"):
            parsed = urlparse(value)
            require(not any(ord(char) <= 32 or ord(char) == 127 for char in value)
                    and bool(parsed.hostname) and parsed.username is None and parsed.password is None,
                    f"{label}: invalid HTTPS link")
            # Accessing .port rejects nonnumeric/out-of-range ports. No DNS/network lookup.
            require(not parsed.netloc.endswith(":") and (parsed.port is None or parsed.port > 0),
                    f"{label}: invalid HTTPS port")
            host = parsed.hostname
            if ":" in host:
                ipaddress.IPv6Address(host)
            else:
                labels = host.rstrip(".").encode("idna").decode("ascii").split(".")
                require(all(re.fullmatch(r"[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?", part)
                            for part in labels), f"{label}: invalid HTTPS hostname")
        else:
            durable_file(root, value.split("#", 1)[0], label)


def durable_file(root, value, label):
    path = local_path(root, value)
    require(path.is_file(), f"{label}: missing local detail: {value}")
    require(path == path.resolve(), f"{label}: symlinked state/evidence is unsupported: {value}")
    eligible = git(root, "ls-files", "--cached", "--others", "--exclude-standard", "-z",
                   "--", ":(literal)" + value).split(b"\0")
    require(value.encode("utf-8") in eligible,
            f"{label}: state/evidence outside Git's trackable inventory is not durable: {value}")
    return path


def check_index(root):
    """A commit uses the index, so never validate hidden staged bytes as worktree bytes."""
    require(git(root, "rev-parse", "--is-shallow-repository").strip() == b"false",
            "shallow history is unsupported; obtain authorized full history before validation")
    require(git(root, "config", "--bool", "--default", "true", "--get", "core.filemode").strip() == b"true",
            "core.filemode=false hides executable-bit changes; use a mode-aware checkout before validation")
    for entry in git(root, "ls-files", "-v", "-z").split(b"\0"):
        require(not entry or (entry[:1] != b"S" and not entry[:1].islower()),
                "hidden worktree flags (assume-unchanged/skip-worktree) are unsupported; review and clear them explicitly")
    require(not git(root, "ls-files", "--unmerged", "-z"), "resolve unmerged index entries before validation")
    # --cached without a revision also works on an unborn branch (all staged files).
    staged = git(root, "diff", "--cached", "--name-only", "--no-renames", "-z").split(b"\0")
    for raw in set(staged) - {b""}:
        name = raw.decode("utf-8")
        path = local_path(root, name)
        entries = git(root, "ls-files", "--stage", "-z", "--", ":(literal)" + name).split(b"\0")
        entries = [entry for entry in entries if entry]
        if not entries:  # A staged deletion must not hide a restored/untracked file.
            require(not path.exists() and not path.is_symlink(), f"staged/worktree mismatch: {name}")
            continue
        metadata, _ = entries[0].split(b"\t", 1)
        mode, oid, _ = metadata.split()
        require(mode in {b"100644", b"100755"} and not path.is_symlink() and path.is_file(),
                f"unsupported or mismatched staged path: {name}")
        working_mode = b"100755" if path.stat().st_mode & 0o111 else b"100644"
        require(working_mode == mode and path.read_bytes() == git(root, "cat-file", "blob", oid.decode()),
                f"staged/worktree mismatch: {name}; align the index and working file before validation")


def is_document(path):
    # Conservative: schemas, workflows, fixtures, dependency lists and assets count
    # as substantive. Markdown/reStructuredText can still change behavior; review it.
    return PurePosixPath(path).suffix.lower() in {".md", ".rst"}


def resolve_base(root, base):
    if base == "initial":
        # Bootstrap covers the entire tree even when a repository already has commits.
        return base
    resolved = git(root, "rev-parse", "--verify", "--end-of-options", base + "^{commit}").decode().strip()
    git(root, "merge-base", "--is-ancestor", resolved, "HEAD")
    return resolved


def snapshot(root, base, state_path=STATE):
    check_index(root)
    base = resolve_base(root, base)
    local_path(root, state_path)
    if base == "initial":
        paths = git(root, "ls-files", "-z").split(b"\0")
    else:
        paths = git(root, "diff", "--name-only", "-z", "--no-renames", base, "--").split(b"\0")
    paths += git(root, "ls-files", "--others", "--exclude-standard", "-z").split(b"\0")
    changes = []
    for raw in sorted(set(paths) - {b"", state_path.encode()}):
        name = raw.decode("utf-8")
        path = local_path(root, name)
        require(not path.is_symlink(), f"symlink changes require manual handling: {name}")
        require(not path.exists() or path.is_file(), f"unsupported changed path (e.g. submodule): {name}")
        digest = hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else "deleted"
        mode = ("100755" if path.stat().st_mode & 0o111 else "100644") if path.is_file() else "deleted"
        changes.append({"path": name, "sha256": digest, "mode": mode})
    substantive = [entry for entry in changes if not is_document(entry["path"])]
    digest = hashlib.sha256(json.dumps(substantive, sort_keys=True).encode()).hexdigest()
    return {"base": base, "changes": changes, "scope_digest": digest}


def no_duplicate_keys(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, f"duplicate JSON key: {key}")
        result[key] = value
    return result


def markdown_state(body):
    require(not re.match(r"\A\ufeff?\s*---[ \t]*\r?\n", body),
            "YAML/frontmatter is unsupported in the canonical Markdown checkpoint")
    # Ignore comments only outside fences: JSON strings must retain their exact bytes.
    fence = None
    in_comment = False
    blocks = []
    lines = []
    for raw_line in body.splitlines():
        line = raw_line
        if fence is None:
            line = ""
            remaining = raw_line
            while remaining:
                marker = "-->" if in_comment else "<!--"
                offset = remaining.find(marker)
                if offset < 0:
                    if not in_comment:
                        line += remaining
                    break
                if not in_comment:
                    line += remaining[:offset]
                remaining = remaining[offset + len(marker):]
                in_comment = not in_comment
        match = re.fullmatch(r" {0,3}(`{3,}|~{3,})(.*)", line)
        if fence is None:
            if match:
                marker, info = match.groups()
                fence = (marker[0], len(marker), info.strip() == "completion-state")
                lines = []
        elif (match and match[1][0] == fence[0] and len(match[1]) >= fence[1]
              and not match[2].strip()):
            if fence[2]:
                blocks.append("\n".join(lines))
            fence = None
        elif fence[2]:
            lines.append(line)
    require(fence is None, "unterminated Markdown fence in canonical checkpoint")
    require(not in_comment, "unterminated HTML comment in canonical checkpoint")
    require(len(blocks) == 1, "exactly one top-level completion-state JSON block required")
    return blocks[0]


def read_state(root, state_path=STATE):
    require(PurePosixPath(state_path).suffix.lower() in {".md", ".rst", ".json"},
            "canonical state must be Markdown/rST or JSON; YAML/frontmatter is not a state format")
    body = durable_file(root, state_path, "canonical checkpoint").read_text(encoding="utf-8")
    if PurePosixPath(state_path).suffix.lower() == ".json":
        document = json.loads(body, object_pairs_hook=no_duplicate_keys)
        require(isinstance(document, dict), "JSON checkpoint must be an object")
        return document.get("completion_state", document)
    return json.loads(markdown_state(body), object_pairs_hook=no_duplicate_keys)


def validate(root, state, current, require_complete=False, branch=None, state_path=STATE, target=None):
    fields(state, ["schema_version", "task_id", "kind", "status", "reviewed_at", "base",
                   "project_state", "changes", "documentation", "validation", "acceptance",
                   "blockers", "next_step", "details", "exception", "implementation",
                   "integration", "branch_evidence"], "state")
    require(type(state["schema_version"]) is int and state["schema_version"] == 1, "unsupported schema_version")
    for key in ["task_id", "project_state", "next_step"]:
        text(state[key], key)
    require(state["kind"] in KINDS, "invalid kind")
    require(state["status"] in STATUSES, "invalid status")
    require(state["implementation"] in {"not_started", "in_progress", "implemented"}, "invalid implementation status")
    integration = state["integration"]
    fields(integration, ["branch", "target", "status", "evidence"], "integration")
    text(integration["branch"], "integration.branch")
    text(integration["target"], "integration.target")
    require(integration["status"] in {"unmerged", "merged"}, "invalid integration status")
    if branch:
        require(integration["branch"] == branch, "checkpoint branch differs from inspected task branch")
    if target:
        require(integration["target"] == target, "checkpoint target differs from trusted integration target")
    links(root, integration["evidence"], "integration.evidence", nonempty=integration["status"] == "merged")
    require(isinstance(state["branch_evidence"], list), "branch_evidence: list required")
    for entry in state["branch_evidence"]:
        fields(entry, ["branch", "baseline", "summary", "links"], "branch_evidence entry")
        for key in ["branch", "baseline", "summary"]:
            text(entry[key], "branch_evidence." + key)
        links(root, entry["links"], "branch_evidence.links")
    reviewed = timestamp(state["reviewed_at"], "reviewed_at")
    require(state["base"] == current["base"], "base differs from comparison base; reconcile the full change range")
    require(isinstance(state["changes"], list), "changes: list required")
    recorded = {}
    for entry in state["changes"]:
        fields(entry, ["path", "sha256", "mode", "summary"], "change")
        local_path(root, entry["path"])
        text(entry["summary"], "change.summary")
        require(entry["path"] != state_path, "checkpoint must not fingerprint itself")
        require(entry["path"] not in recorded, "duplicate change path: " + entry["path"])
        recorded[entry["path"]] = (entry["sha256"], entry["mode"])
    actual = {entry["path"]: (entry["sha256"], entry["mode"]) for entry in current["changes"]}
    missing = sorted(actual.keys() - recorded.keys())
    extra = sorted(recorded.keys() - actual.keys())
    stale = sorted(path for path in actual.keys() & recorded.keys() if actual[path] != recorded[path])
    require(not (missing or extra or stale), f"change record mismatch: missing={missing}, extra={extra}, stale={stale}")
    if current["base"] != "initial" and (root / ".git").exists():
        # A matching old checkpoint cannot be silently reused for a new diff.
        previous = subprocess.run(["git", "-C", str(root), "show", f"{current['base']}:{state_path}"],
                                  capture_output=True, check=False)
        require(previous.returncode != 0 or previous.stdout != (root / state_path).read_bytes()
                or not actual, "changed files require an updated canonical checkpoint")
    substantive = any(not is_document(path) for path in actual)
    require(state["kind"] != "documentation_only" or not substantive,
            "documentation_only includes substantive files; use behavior_change or a justified no_behavior_change exception")
    if state["kind"] == "no_behavior_change":
        text(state["exception"], "no_behavior_change exception justification")
    else:
        require(state["exception"] is None, "exception applies only to no_behavior_change")

    docs = state["documentation"]
    fields(docs, ["affected", "assessment"], "documentation")
    text(docs["assessment"], "documentation.assessment")
    require(isinstance(docs["affected"], list) and state_path in docs["affected"], "documentation.affected must include canonical checkpoint")
    for path in docs["affected"]:
        require((path == state_path or is_document(path)) and local_path(root, path).is_file(), f"affected doc missing or not Markdown/rST/canonical state: {path}")

    require(isinstance(state["validation"], list) and state["validation"], "validation: at least one explicit result or not_run/not_applicable record required")
    outcomes = []
    for check in state["validation"]:
        fields(check, ["command", "outcome", "detail", "observed_at", "scope_digest", "evidence"], "validation")
        for key in ["command", "detail"]:
            text(check[key], "validation." + key)
        outcome = check["outcome"]
        require(outcome in {"passed", "failed", "not_run", "not_applicable"}, "invalid validation outcome")
        outcomes.append(outcome)
        if outcome in {"passed", "failed"}:
            require(timestamp(check["observed_at"], "validation.observed_at") <= reviewed,
                    "validation postdates checkpoint review")
            require(check["scope_digest"] == current["scope_digest"], "stale validation scope_digest; rerun affected checks and record outcomes")
            links(root, check["evidence"], "validation.evidence")
        else:
            require(check["observed_at"] is None and check["scope_digest"] is None,
                    "unrun checks must not claim an execution timestamp or digest")
            links(root, check["evidence"], "validation.evidence", nonempty=False)
            require(outcome != "not_applicable" or state["kind"] != "behavior_change",
                    "behavior changes require validation, not not_applicable")

    require(isinstance(state["acceptance"], list) and state["acceptance"], "acceptance: explicit pending/accepted/not_required record required")
    pending = False
    for gate in state["acceptance"]:
        fields(gate, ["scope", "status", "detail", "evidence"], "acceptance")
        text(gate["scope"], "acceptance.scope")
        text(gate["detail"], "acceptance.detail")
        require(gate["status"] in {"pending", "accepted", "not_required"}, "invalid acceptance status")
        pending |= gate["status"] == "pending"
        links(root, gate["evidence"], "acceptance.evidence", nonempty=gate["status"] == "accepted")
        if gate["status"] == "accepted":
            text(gate.get("reviewer"), "acceptance.reviewer")
            require(timestamp(gate.get("observed_at"), "acceptance.observed_at") <= reviewed,
                    "acceptance postdates checkpoint review")
    require(isinstance(state["blockers"], list), "blockers: list required (empty means none)")
    for blocker in state["blockers"]:
        text(blocker, "blocker")
    links(root, state["details"], "details")
    status = state["status"]
    require(status != "blocked" or state["blockers"], "blocked status requires a blocker and exact next_step")
    require(status != "failed" or "failed" in outcomes or state["blockers"], "failed status requires failed validation or a blocker")
    if status in {"complete", "ready_for_review"}:
        require(state["implementation"] == "implemented", f"{status} contradicts unfinished implementation")
        require(not state["blockers"] and "failed" not in outcomes, f"{status} contradicts blockers or failed validation")
        require(state["kind"] != "behavior_change" or "passed" in outcomes, f"{status} behavior change requires a passed check")
    if status == "complete":
        require(not pending and "not_run" not in outcomes, "complete contradicts pending acceptance or unrun checks")
    require(not require_complete or status == "complete", f"completion refused: status is {status}")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd(), help="repository root (default: current directory)")
    parser.add_argument("--base", help="trusted comparison commit/ref; use 'initial' to inventory the entire repository")
    parser.add_argument("--state", default=STATE, help="canonical Markdown or JSON checkpoint path relative to repository root")
    parser.add_argument("--snapshot", action="store_true", help="print observed file fingerprints; does not record verification")
    parser.add_argument("--require-complete", action="store_true", help="also reject honest but unfinished checkpoints")
    parser.add_argument("--branch", help="expected task branch (CI should supply its trusted PR head branch)")
    parser.add_argument("--target", help="expected integration target (CI should supply its trusted PR base branch)")
    args = parser.parse_args(argv)
    try:
        root = args.repo.resolve()
        require(git(root, "rev-parse", "--show-toplevel").decode().strip() == str(root), "--repo must name a repository root")
        if args.snapshot:
            require(args.base is not None, "--snapshot requires an explicit --base")
            print(json.dumps(snapshot(root, args.base, args.state), indent=2))
        else:
            state = read_state(root, args.state)
            fields(state, ["base"], "state")
            text(state["base"], "base")
            require(state["base"] == "initial" or re.fullmatch(r"[0-9a-f]{40}|[0-9a-f]{64}", state["base"]),
                    "state.base must be a full immutable commit ID or 'initial'")
            current = snapshot(root, args.base or state["base"], args.state)
            validate(root, state, current, args.require_complete, args.branch, args.state, args.target)
            print(f"Checkpoint structure and change coverage passed ({state['status']}). "
                  "Reported evidence and human acceptance are not independently verified.")
        return 0
    except (InvalidState, OSError, ValueError, TypeError) as exc:
        print(f"Completion gate failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
