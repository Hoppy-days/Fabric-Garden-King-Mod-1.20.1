"""Contract and real-Git regression tests; no network or third-party packages."""

import copy
from datetime import datetime, timezone
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SPEC = importlib.util.spec_from_file_location("check_state", Path(__file__).with_name("check_state.py"))
gate = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(gate)


class CompletionGateTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name).resolve()
        self.git("init", "-b", "main")
        self.write("README.md", "Existing project\n")
        self.commit("baseline")
        self.base = self.git("rev-parse", "HEAD").strip()
        self.write("src/app.py", "print('changed')\n")
        self.write("docs/results.md", "Reported test run: example command passed.\n")
        self.state = self.make_state()
        self.save()

    def git(self, *args):
        result = subprocess.run(["git", "-C", str(self.root), *args], capture_output=True, text=True, check=True)
        return result.stdout

    def commit(self, message):
        self.git("add", ".")
        self.git("-c", "user.name=Gate Tests", "-c", "user.email=gate-tests@example.invalid", "commit", "-m", message)

    def write(self, name, body):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(body, encoding="utf-8")

    def make_state(self, base=None, kind="behavior_change"):
        current = gate.snapshot(self.root, base or self.base)
        now = datetime.now(timezone.utc).isoformat()
        return {
            "schema_version": 1, "task_id": "test-task", "kind": kind, "status": "complete",
            "reviewed_at": now, "base": current["base"], "project_state": "Example change implemented.",
            "changes": [dict(entry, summary="Describe the actual change.") for entry in current["changes"]],
            "documentation": {"affected": [gate.STATE], "assessment": "Only the checkpoint needs updating."},
            "validation": [{"command": "example-test-command", "outcome": "passed", "detail": "One test passed.",
                            "observed_at": now, "scope_digest": current["scope_digest"], "evidence": ["docs/results.md"]}],
            "acceptance": [{"scope": "Example change", "status": "not_required", "detail": "No human-only gate applies.", "evidence": []}],
            "blockers": [], "next_step": "Review the change for integration.", "details": ["docs/results.md"], "exception": None,
            "implementation": "implemented", "integration": {"branch": "main", "target": "release", "status": "unmerged", "evidence": []},
            "branch_evidence": [],
        }

    def save(self):
        self.write(gate.STATE, "# Project state\n\n```completion-state\n" + json.dumps(self.state, indent=2) + "\n```\n")

    def validate(self, complete=False):
        gate.validate(self.root, self.state, gate.snapshot(self.root, self.state.get("base", self.base)), complete)

    def assertInvalid(self, fragment, complete=False):
        with self.assertRaisesRegex(gate.InvalidState, fragment):
            self.validate(complete)

    def test_passing_checkpoint_and_complete_gate(self):
        self.validate(complete=True)

    def test_all_required_fields_are_required(self):
        original = copy.deepcopy(self.state)
        for key in original:
            with self.subTest(field=key):
                self.state = copy.deepcopy(original)
                del self.state[key]
                self.assertInvalid("missing fields")

    def test_nested_evidence_fields_are_required(self):
        for section in ["changes", "validation", "acceptance"]:
            for key in list(self.state[section][0]):
                with self.subTest(section=section, field=key):
                    value = self.state[section][0].pop(key)
                    self.assertInvalid("missing fields")
                    self.state[section][0][key] = value

    def test_untracked_code_requires_record(self):
        self.write("new.py", "print('unrecorded')\n")
        self.assertInvalid("missing=.*new.py")

    def test_staged_and_unstaged_edits_invalidate_fingerprints(self):
        self.commit("implementation")
        for stage in [False, True]:
            with self.subTest(staged=stage):
                self.write("src/app.py", f"print('new {stage}')\n")
                if stage:
                    self.git("add", "src/app.py")
                self.assertInvalid("stale=.*src/app.py")

    def test_refreshed_file_record_does_not_refresh_test_claim(self):
        self.write("src/app.py", "print('new behavior after testing')\n")
        self.state["changes"] = self.make_state()["changes"]
        self.assertInvalid("stale validation scope_digest")

    def test_mode_only_change_invalidates_record_and_test_scope(self):
        before = gate.snapshot(self.root, self.base)["scope_digest"]
        (self.root / "src/app.py").chmod(0o755)
        self.assertInvalid("stale=.*src/app.py")
        self.assertNotEqual(before, gate.snapshot(self.root, self.base)["scope_digest"])

    def test_documentation_edit_requires_record_but_not_code_retest(self):
        before = self.state["validation"][0]["scope_digest"]
        self.write("docs/results.md", "More accurate evidence detail.\n")
        self.assertInvalid("stale=.*docs/results.md")
        self.state["changes"] = self.make_state()["changes"]
        self.validate()
        self.assertEqual(before, gate.snapshot(self.root, self.base)["scope_digest"])

    def test_deleted_and_renamed_files_are_covered(self):
        self.commit("implementation")
        old_base = self.git("rev-parse", "HEAD").strip()
        (self.root / "src/app.py").rename(self.root / "src/new.py")
        self.state = self.make_state(base=old_base)
        entries = {item["path"]: item for item in self.state["changes"]}
        self.assertEqual(entries["src/app.py"]["sha256"], "deleted")
        self.assertIn("src/new.py", entries)
        self.save()
        self.validate()

    def test_extra_and_duplicate_records_fail(self):
        self.state["changes"].append(dict(self.state["changes"][0]))
        self.assertInvalid("duplicate change")
        self.state["changes"][-1]["path"] = "gone.md"
        self.assertInvalid("extra=.*gone.md")

    def test_self_fingerprint_is_rejected(self):
        self.state["changes"].append({"path": gate.STATE, "sha256": "irrelevant", "mode": "100644", "summary": "State"})
        self.assertInvalid("must not fingerprint itself")

    def test_complete_cannot_hide_pending_acceptance(self):
        self.state["acceptance"][0]["status"] = "pending"
        self.assertInvalid("complete contradicts pending")
        self.state["status"] = "ready_for_review"
        self.validate()
        self.assertInvalid("completion refused", complete=True)

    def test_accepted_human_gate_requires_attribution_and_evidence(self):
        human = self.state["acceptance"][0]
        human["status"] = "accepted"
        self.assertInvalid("evidence/link list")
        human["evidence"] = ["docs/results.md"]
        self.assertInvalid("acceptance.reviewer")
        human["reviewer"] = "Example reviewer (fixture only)"
        human["observed_at"] = self.state["reviewed_at"]
        self.validate()

    def test_blocked_and_failed_are_honest_but_not_complete(self):
        for status in ["blocked", "failed"]:
            with self.subTest(status=status):
                self.state["status"] = status
                self.state["blockers"] = []
                self.assertInvalid("requires")
                self.state["blockers"] = ["Test runner unavailable; install in the authorized environment."]
                self.validate()
                self.assertInvalid("completion refused", complete=True)

    def test_failed_validation_blocks_ready_and_complete(self):
        self.state["validation"][0]["outcome"] = "failed"
        for status in ["ready_for_review", "complete"]:
            self.state["status"] = status
            self.assertInvalid("contradicts blockers or failed")
        self.state["status"] = "failed"
        self.validate()

    def test_unrun_is_not_a_pass(self):
        check = self.state["validation"][0]
        check.update(outcome="not_run", observed_at=None, scope_digest=None, evidence=[])
        self.assertInvalid("requires a passed check")
        self.state["status"] = "in_progress"
        self.validate()
        check["observed_at"] = self.state["reviewed_at"]
        self.assertInvalid("unrun checks must not claim")

    def test_documentation_only_task_can_explicitly_skip_inapplicable_tests(self):
        (self.root / "src/app.py").unlink()
        self.state = self.make_state(kind="documentation_only")
        self.state["validation"][0].update(outcome="not_applicable", observed_at=None, scope_digest=None,
                                           detail="Prose-only correction; no executable behavior changed.", evidence=[])
        self.save()
        self.validate(complete=True)

    def test_documentation_only_cannot_hide_workflow_or_schema_changes(self):
        self.state["kind"] = "documentation_only"
        self.assertInvalid("includes substantive files")
        self.state["kind"] = "no_behavior_change"
        self.assertInvalid("exception justification")
        self.state["exception"] = "Move unchanged implementation; behavior is unchanged, scope reviewed."
        self.validate()

    def test_not_applicable_is_not_available_for_behavior_changes(self):
        self.state["validation"][0].update(outcome="not_applicable", observed_at=None, scope_digest=None, evidence=[])
        self.assertInvalid("behavior changes require validation")

    def test_missing_local_links_fail(self):
        self.state["details"] = ["does-not-exist.md"]
        self.assertInvalid("missing local detail")

    def test_external_links_are_structural_not_network_checks(self):
        self.state["details"] = ["https://example.invalid/review/123"]
        self.validate()

    def test_unsafe_paths_fail(self):
        for path in ["../outside.md", "/outside.md", "docs/../README.md", "docs\\results.md"]:
            with self.subTest(path=path):
                self.state["details"] = [path]
                self.assertInvalid("unsafe")

    def test_symlinks_fail_without_reading_target(self):
        (self.root / "escape.py").symlink_to(self.root.parent / "outside.py")
        with self.assertRaisesRegex(gate.InvalidState, "escapes repository"):
            gate.snapshot(self.root, self.base)

    def test_future_and_inconsistent_times_fail(self):
        self.state["reviewed_at"] = "2999-01-01T00:00:00Z"
        self.assertInvalid("future timestamp")
        self.state["reviewed_at"] = "2000-01-01T00:00:00Z"
        self.assertInvalid("validation postdates")
        self.state["reviewed_at"] = "2020-01-01"
        self.assertInvalid("timezone required")

    def test_initial_repository_without_commit(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder).resolve()
            subprocess.run(["git", "init", "-b", "main", str(root)], capture_output=True, check=True)
            (root / "README.md").write_text("Initial repository\n")
            snap = gate.snapshot(root, "initial")
            self.assertEqual([entry["path"] for entry in snap["changes"]], ["README.md"])
            self.assertEqual(snap["base"], "initial")
            state = copy.deepcopy(self.state)
            state.update(base="initial", kind="documentation_only",
                         changes=[dict(entry, summary="Initial documentation") for entry in snap["changes"]],
                         details=["README.md"])
            state["validation"][0].update(outcome="not_applicable", observed_at=None, scope_digest=None,
                                          detail="First documentation checkpoint; no executable behavior.", evidence=[])
            (root / gate.STATE).write_text("```completion-state\n" + json.dumps(state) + "\n```\n")
            result = subprocess.run(["python3", str(Path(gate.__file__).resolve()), "--repo", str(root),
                                     "--base", "initial", "--require-complete"], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stderr)

    def test_initial_checkpoint_in_existing_repository(self):
        self.state = self.make_state(base="initial")
        self.save()
        self.validate()
        self.assertIn("README.md", [entry["path"] for entry in self.state["changes"]])

    def test_explicit_ci_base_cannot_be_overridden_by_checkpoint(self):
        current = gate.snapshot(self.root, self.base)
        self.state["base"] = "initial"
        with self.assertRaisesRegex(gate.InvalidState, "base differs"):
            gate.validate(self.root, self.state, current)

    def test_branch_scope_and_implementation_are_separate_from_integration(self):
        current = gate.snapshot(self.root, self.base)
        # A completed task may still be unmerged; completion does not assert shipping.
        gate.validate(self.root, self.state, current, True, "main")
        with self.assertRaisesRegex(gate.InvalidState, "checkpoint branch differs"):
            gate.validate(self.root, self.state, current, branch="another-task")
        self.state["implementation"] = "in_progress"
        self.assertInvalid("unfinished implementation")
        self.state["implementation"] = "implemented"
        self.state["integration"]["status"] = "merged"
        self.assertInvalid("integration.evidence")

    def test_branch_evidence_index_requires_durable_links(self):
        self.state["branch_evidence"] = [{"branch": "older-task", "baseline": self.base,
                                          "summary": "Separate work remains unmerged.", "links": []}]
        self.assertInvalid("branch_evidence.links")
        self.state["branch_evidence"][0]["links"] = ["docs/results.md"]
        self.validate()

    def test_invalid_and_unrelated_baselines_fail(self):
        with self.assertRaises(gate.InvalidState):
            gate.snapshot(self.root, "not-a-revision")
        self.git("checkout", "--orphan", "unrelated")
        self.commit("unrelated root")
        with self.assertRaises(gate.InvalidState):
            gate.snapshot(self.root, self.base)

    def test_same_snapshot_before_and_after_commit(self):
        before = gate.snapshot(self.root, self.base)
        self.commit("implementation and checkpoint")
        self.assertEqual(before, gate.snapshot(self.root, self.base))
        self.validate()

    def test_nested_markdown_and_json_canonical_state_cli(self):
        (self.root / gate.STATE).unlink()
        for name, wrapped in [("docs/PROJECT_STATE.md", False), ("state/PROJECT_STATE.json", False),
                              ("state/PROJECT_STATE.json", True)]:
            with self.subTest(path=name, wrapped=wrapped):
                current = gate.snapshot(self.root, self.base, name)
                self.state["changes"] = [dict(entry, summary="Actual change") for entry in current["changes"]]
                self.state["validation"][0]["scope_digest"] = current["scope_digest"]
                self.state["documentation"]["affected"] = [name]
                document = {"existing_project_field": "preserved", "completion_state": self.state} if wrapped else self.state
                body = json.dumps(document)
                if name.endswith(".md"):
                    body = "```completion-state\n" + body + "\n```\n"
                self.write(name, body)
                self.assertEqual(gate.read_state(self.root, name), self.state)
                result = subprocess.run(["python3", str(Path(gate.__file__).resolve()), "--repo", str(self.root),
                                         "--base", self.base, "--state", name, "--require-complete"], capture_output=True, text=True)
                self.assertEqual(result.returncode, 0, result.stderr)
                (self.root / name).unlink()

    def test_duplicate_json_keys_and_multiple_blocks_fail(self):
        self.write(gate.STATE, '```completion-state\n{"base":"initial","base":"initial"}\n```\n')
        with self.assertRaisesRegex(gate.InvalidState, "duplicate JSON key"):
            gate.read_state(self.root)
        self.save()
        self.write(gate.STATE, (self.root / gate.STATE).read_text() * 2)
        with self.assertRaisesRegex(gate.InvalidState, "exactly one"):
            gate.read_state(self.root)

    def test_cli_reports_errors_and_exit_codes(self):
        script = str(Path(gate.__file__).resolve())
        def cli(*args):
            return subprocess.run(["python3", script, "--repo", str(self.root), *args], capture_output=True, text=True)
        result = cli("--base", self.base, "--require-complete")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("not independently verified", result.stdout)
        self.write("src/app.py", "changed again\n")
        result = cli("--base", self.base)
        self.assertEqual(result.returncode, 1)
        self.assertIn("stale=", result.stderr)
        self.write(gate.STATE, "not a checkpoint")
        self.assertEqual(cli().returncode, 1)

    def test_checkpoint_only_task_needs_no_self_record(self):
        self.commit("previous task")
        self.base = self.git("rev-parse", "HEAD").strip()
        self.state = self.make_state(kind="documentation_only")
        self.assertEqual(self.state["changes"], [])
        self.save()
        self.validate(complete=True)

    def test_no_behavior_change_exception_can_explain_inapplicable_tests(self):
        self.state["kind"] = "no_behavior_change"
        self.state["exception"] = "Comment-only source clarification; executable statements unchanged after diff review."
        self.state["validation"][0].update(outcome="not_applicable", observed_at=None, scope_digest=None,
                                          detail="Comment-only diff reviewed; no runtime check applies.", evidence=[])
        self.validate(complete=True)

    def test_complete_cannot_have_blockers_or_unrun_documentation_review(self):
        self.state["blockers"] = ["Awaiting required access"]
        self.assertInvalid("contradicts blockers")
        (self.root / "src/app.py").unlink()
        self.state = self.make_state(kind="documentation_only")
        self.state["validation"][0].update(outcome="not_run", observed_at=None, scope_digest=None, evidence=[])
        self.assertInvalid("complete contradicts pending acceptance or unrun checks")

    def test_empty_required_evidence_arrays_fail(self):
        for key in ["validation", "acceptance", "details"]:
            with self.subTest(field=key):
                saved = self.state[key]
                self.state[key] = []
                self.assertInvalid(key)
                self.state[key] = saved

    def cli(self, *args):
        return subprocess.run(["python3", str(Path(gate.__file__).resolve()), "--repo", str(self.root), *args],
                              capture_output=True, text=True)

    def test_staged_code_cannot_hide_behind_restored_worktree(self):
        self.commit("documented implementation")
        documented = (self.root / "src/app.py").read_text()
        self.write("src/app.py", "print('undocumented staged behavior')\n")
        self.git("add", "src/app.py")
        self.write("src/app.py", documented)
        self.assertInvalid("staged/worktree mismatch")

    def test_staged_checkpoint_cannot_differ_from_reviewed_checkpoint(self):
        self.commit("documented implementation")
        self.write(gate.STATE, "staged checkpoint is invalid\n")
        self.git("add", gate.STATE)
        self.save()
        self.assertInvalid("staged/worktree mismatch")

    def test_staged_deletion_cannot_hide_restored_file(self):
        self.commit("documented implementation")
        self.git("rm", "src/app.py")
        self.write("src/app.py", "print('changed')\n")
        self.assertInvalid("staged/worktree mismatch")

    def test_assume_unchanged_and_skip_worktree_cannot_hide_source_edits(self):
        self.commit("previous task")
        self.base = self.git("rev-parse", "HEAD").strip()
        self.write("new.py", "print('declared task change')\n")
        self.state = self.make_state()
        self.save()
        for flag in ["--assume-unchanged", "--skip-worktree"]:
            with self.subTest(flag=flag):
                self.git("update-index", flag, "src/app.py")
                self.write("src/app.py", "print('hidden unrelated behavior')\n")
                self.assertInvalid("hidden worktree flags")
                self.git("update-index", "--no-assume-unchanged", "--no-skip-worktree", "src/app.py")
                self.write("src/app.py", "print('changed')\n")

    def test_unmerged_index_fails_closed(self):
        oid = self.git("rev-parse", "HEAD:README.md").strip()
        data = "0 " + "0" * 40 + "\tREADME.md\n"
        data += f"100644 {oid} 1\tREADME.md\n100644 {oid} 2\tREADME.md\n"
        subprocess.run(["git", "-C", str(self.root), "update-index", "--index-info"],
                       input=data, text=True, capture_output=True, check=True)
        self.assertInvalid("unmerged index")

    def test_disabled_filemode_tracking_cannot_hide_executable_change(self):
        self.commit("previous task")
        self.base = self.git("rev-parse", "HEAD").strip()
        self.write("new.py", "print('declared change')\n")
        self.state = self.make_state()
        self.git("config", "core.filemode", "false")
        (self.root / "src/app.py").chmod(0o755)
        self.assertInvalid("core.filemode=false")

    def test_cosmetic_checkpoint_edit_does_not_cover_changed_code(self):
        self.commit("documented implementation")
        self.write("src/app.py", "print('unrecorded code')\n")
        self.write(gate.STATE, (self.root / gate.STATE).read_text() + "\n")
        result = self.cli("--base", self.base, "--require-complete")
        self.assertEqual(result.returncode, 1)
        self.assertIn("stale=", result.stderr)

    def test_exception_cannot_exempt_missing_changes(self):
        self.state.update(kind="no_behavior_change", exception="Claimed comment-only update; requires review.")
        self.write("new.py", "print('not recorded')\n")
        self.assertInvalid("missing=.*new.py")

    def test_ignored_checkpoint_and_evidence_are_not_durable(self):
        self.write(".gitignore", "PROJECT_STATE.md\ndocs/private.md\n")
        self.write("docs/private.md", "private result\n")
        with self.assertRaisesRegex(gate.InvalidState, "not durable"):
            gate.read_state(self.root)
        with self.assertRaisesRegex(gate.InvalidState, "not durable"):
            gate.links(self.root, ["docs/private.md"], "evidence")

    def test_internal_symlink_cannot_alias_canonical_state(self):
        (self.root / gate.STATE).rename(self.root / "actual.md")
        (self.root / gate.STATE).symlink_to("actual.md")
        result = self.cli("--base", self.base)
        self.assertEqual(result.returncode, 1)
        self.assertIn("symlinked", result.stderr)

    def test_git_internal_and_nested_repository_evidence_are_not_durable(self):
        for name in [".git/config", ".git/HEAD"]:
            with self.assertRaisesRegex(gate.InvalidState, "not durable"):
                gate.links(self.root, [name], "evidence")
        nested = self.root / "nested"
        nested.mkdir()
        subprocess.run(["git", "init", str(nested)], capture_output=True, check=True)
        (nested / "report.md").write_text("Another repository's evidence")
        with self.assertRaisesRegex(gate.InvalidState, "not durable"):
            gate.links(self.root, ["nested/report.md"], "evidence")

    def test_malformed_https_links_and_credentials_are_rejected(self):
        for link in ["https://bad host/path", "https://example.com:garbage/path", "https://\x00/path",
                     "https://:password@example.com/path", "https://example.com:/path", "https://-bad.example/path"]:
            with self.subTest(link=link):
                with self.assertRaises((gate.InvalidState, ValueError)):
                    gate.links(self.root, [link], "evidence")
        gate.links(self.root, ["https://example.invalid:443/path#proof", "https://[::1]/proof"], "evidence")

    def test_commented_nested_and_unterminated_state_blocks_are_rejected(self):
        original = (self.root / gate.STATE).read_text()
        for body in ["<!--\n" + original + "\n-->", "````markdown\n" + original + "\n````\n",
                     original.rsplit("```", 1)[0], original + "\n```example\nunterminated"]:
            with self.subTest(body=body[:20]):
                self.write(gate.STATE, body)
                self.assertEqual(self.cli().returncode, 1)

    def test_malformed_yaml_frontmatter_and_yaml_canonical_are_rejected(self):
        original = (self.root / gate.STATE).read_text()
        for header in ["---\nstatus: [broken\n---\n", "---\nstatus: complete\n---\n"]:
            self.write(gate.STATE, header + original)
            result = self.cli()
            self.assertEqual(result.returncode, 1)
            self.assertIn("frontmatter", result.stderr)
        self.write("state.yaml", original)
        self.assertEqual(self.cli("--state", "state.yaml").returncode, 1)

    def test_markdown_parser_preserves_literal_json_comment_text(self):
        self.state["project_state"] = "Literal <!--comment--> text is part of this JSON string."
        self.save()
        self.assertEqual(gate.read_state(self.root), self.state)
        self.state["status"] = "com<!--comment-->plete"
        self.save()
        result = self.cli()
        self.assertEqual(result.returncode, 1)
        self.assertIn("invalid status", result.stderr)

    def test_deleted_canonical_fails_and_explicit_rename_migration_passes(self):
        self.commit("original checkpoint")
        (self.root / gate.STATE).unlink()
        self.assertEqual(self.cli().returncode, 1)
        name = "docs/PROJECT_STATE.json"
        current = gate.snapshot(self.root, self.base, name)
        self.state["changes"] = [dict(entry, summary="Canonical path migration") for entry in current["changes"]]
        self.state["documentation"]["affected"] = [name]
        self.state["validation"][0]["scope_digest"] = current["scope_digest"]
        self.write(name, json.dumps(self.state))
        result = self.cli("--base", self.base, "--state", name, "--require-complete")
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_shallow_and_missing_base_fail_without_fetching(self):
        self.commit("implementation")
        with tempfile.TemporaryDirectory() as folder:
            shallow = Path(folder) / "shallow"
            subprocess.run(["git", "clone", "--depth=1", self.root.as_uri(), str(shallow)],
                           capture_output=True, check=True)
            with self.assertRaisesRegex(gate.InvalidState, "shallow history"):
                gate.snapshot(shallow, "HEAD")
        with self.assertRaisesRegex(gate.InvalidState, "Git command failed"):
            gate.snapshot(self.root, "0" * 40)

    def test_invalid_status_types_fail_without_traceback(self):
        for value in [None, [], {}, "done", 1]:
            self.state["status"] = value
            self.save()
            result = self.cli("--base", self.base)
            self.assertEqual(result.returncode, 1)
            self.assertNotIn("Traceback", result.stderr)

    def test_shell_payloads_in_refs_paths_and_commands_are_data(self):
        marker = self.root / "injected"
        payload = "$(touch injected)"
        self.state["validation"][0]["command"] = payload
        self.save()
        result = self.cli("--base", self.base, "--require-complete")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.cli("--base", payload).returncode, 1)
        self.write("src/" + payload + ".py", "literal path\n")
        self.state = self.make_state()
        self.save()
        self.assertEqual(self.cli("--base", self.base).returncode, 0)
        self.assertFalse(marker.exists())

    def test_ci_script_checks_target_and_treats_shell_metacharacters_as_data(self):
        workflow = Path(gate.__file__).resolve().parents[2] / ".github/workflows/completion-gate.yml"
        body = workflow.read_text()
        self.assertIn("edited", body.split("permissions:")[0])
        self.assertIn("fetch-depth: 0", body)
        self.assertIn("persist-credentials: false", body)
        script = body.split("run: |\n", 1)[1]
        script = "\n".join(line[10:] for line in script.splitlines())
        self.write("tools/completion-gate/check_state.py", Path(gate.__file__).read_text())
        self.write("tools/completion-gate/run_gate.py", Path(gate.__file__).with_name("run_gate.py").read_text())
        self.write(".completion-gate.json", json.dumps({"schema_version": 1, "state_path": "PROJECT_STATE.md",
                   "target_branch": "release", "toolkit_revision": "d" * 40}))
        self.state = self.make_state()
        payload = "task/$(touch${IFS}injected)"
        self.state["integration"]["branch"] = payload
        self.save()
        env = dict(os.environ, PYTHONDONTWRITEBYTECODE="1", BASE_SHA=self.base, HEAD_BRANCH=payload, TARGET_BRANCH="release")
        args = ["bash", "--noprofile", "--norc", "-e", "-o", "pipefail", "-c", script]
        result = subprocess.run(args, cwd=self.root, env=env, text=True, capture_output=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertFalse((self.root / "injected").exists())
        env["TARGET_BRANCH"] = "retargeted-branch"
        result = subprocess.run(args, cwd=self.root, env=env, text=True, capture_output=True)
        self.assertEqual(result.returncode, 1)
        self.assertIn("target differs", result.stderr)


if __name__ == "__main__":
    unittest.main()
