# Completion-gate installation evidence

Repository: Hoppy-days/Fabric-Garden-King-Mod-1.20.1. Inspected default: `master` at `a89e33aeecc0c8182e3f13e775670fe57e3d0875`.
Scope: authorized cloud checkout, branch `codex/documentation-completion-gate`; no workstation access.
Portable validator copied byte-for-byte from reviewed Agent Toolkit `8ff44a8d02eec5411a43128adc4e8fe213c65886`.
Core, adapter, workflow and all 66 tests are byte-identical to this canonical source. Eight adapter tests include config durability regressions.

An initial local fixture run failed because its simulated CI omitted the job's bytecode-suppression environment; the fixture was corrected, including its adapter-level target diagnostic expectation. Final results follow.
Historical product claims remain historical; no application runtime, geometry/print, gameplay or data regeneration was performed.
No required-check/security settings were changed. Publication, if authorized, is a draft PR only; merge/deployment are not acceptance.

## python3 -B -m unittest discover -s tools/completion-gate -p 'test_*.py' -v

Observed: 2026-10-22T05:51:31.147754+00:00; exit 0. 66 tests passed: 58 reviewed portable regressions (including deliberate negative cases) and 8 configuration-adapter tests.

```text
test_escaping_or_unsupported_state_path (test_adapter.AdapterTests.test_escaping_or_unsupported_state_path) ... ok
test_ignored_config_rejected (test_adapter.AdapterTests.test_ignored_config_rejected) ... ok
test_invalid_config_schema_and_provenance (test_adapter.AdapterTests.test_invalid_config_schema_and_provenance) ... ok
test_invalid_json_or_missing_config (test_adapter.AdapterTests.test_invalid_json_or_missing_config) ... ok
test_missing_and_duplicate_fields (test_adapter.AdapterTests.test_missing_and_duplicate_fields) ... ok
test_symlink_config_rejected (test_adapter.AdapterTests.test_symlink_config_rejected) ... ok
test_trusted_target_mismatch (test_adapter.AdapterTests.test_trusted_target_mismatch) ... ok
test_valid_nested_json (test_adapter.AdapterTests.test_valid_nested_json) ... ok
test_accepted_human_gate_requires_attribution_and_evidence (test_check_state.CompletionGateTests.test_accepted_human_gate_requires_attribution_and_evidence) ... ok
test_all_required_fields_are_required (test_check_state.CompletionGateTests.test_all_required_fields_are_required) ... ok
test_assume_unchanged_and_skip_worktree_cannot_hide_source_edits (test_check_state.CompletionGateTests.test_assume_unchanged_and_skip_worktree_cannot_hide_source_edits) ... ok
test_blocked_and_failed_are_honest_but_not_complete (test_check_state.CompletionGateTests.test_blocked_and_failed_are_honest_but_not_complete) ... ok
test_branch_evidence_index_requires_durable_links (test_check_state.CompletionGateTests.test_branch_evidence_index_requires_durable_links) ... ok
test_branch_scope_and_implementation_are_separate_from_integration (test_check_state.CompletionGateTests.test_branch_scope_and_implementation_are_separate_from_integration) ... ok
test_checkpoint_only_task_needs_no_self_record (test_check_state.CompletionGateTests.test_checkpoint_only_task_needs_no_self_record) ... ok
test_ci_script_checks_target_and_treats_shell_metacharacters_as_data (test_check_state.CompletionGateTests.test_ci_script_checks_target_and_treats_shell_metacharacters_as_data) ... ok
test_cli_reports_errors_and_exit_codes (test_check_state.CompletionGateTests.test_cli_reports_errors_and_exit_codes) ... ok
test_commented_nested_and_unterminated_state_blocks_are_rejected (test_check_state.CompletionGateTests.test_commented_nested_and_unterminated_state_blocks_are_rejected) ... ok
test_complete_cannot_have_blockers_or_unrun_documentation_review (test_check_state.CompletionGateTests.test_complete_cannot_have_blockers_or_unrun_documentation_review) ... ok
test_complete_cannot_hide_pending_acceptance (test_check_state.CompletionGateTests.test_complete_cannot_hide_pending_acceptance) ... ok
test_cosmetic_checkpoint_edit_does_not_cover_changed_code (test_check_state.CompletionGateTests.test_cosmetic_checkpoint_edit_does_not_cover_changed_code) ... ok
test_deleted_and_renamed_files_are_covered (test_check_state.CompletionGateTests.test_deleted_and_renamed_files_are_covered) ... ok
test_deleted_canonical_fails_and_explicit_rename_migration_passes (test_check_state.CompletionGateTests.test_deleted_canonical_fails_and_explicit_rename_migration_passes) ... ok
test_disabled_filemode_tracking_cannot_hide_executable_change (test_check_state.CompletionGateTests.test_disabled_filemode_tracking_cannot_hide_executable_change) ... ok
test_documentation_edit_requires_record_but_not_code_retest (test_check_state.CompletionGateTests.test_documentation_edit_requires_record_but_not_code_retest) ... ok
test_documentation_only_cannot_hide_workflow_or_schema_changes (test_check_state.CompletionGateTests.test_documentation_only_cannot_hide_workflow_or_schema_changes) ... ok
test_documentation_only_task_can_explicitly_skip_inapplicable_tests (test_check_state.CompletionGateTests.test_documentation_only_task_can_explicitly_skip_inapplicable_tests) ... ok
test_duplicate_json_keys_and_multiple_blocks_fail (test_check_state.CompletionGateTests.test_duplicate_json_keys_and_multiple_blocks_fail) ... ok
test_empty_required_evidence_arrays_fail (test_check_state.CompletionGateTests.test_empty_required_evidence_arrays_fail) ... ok
test_exception_cannot_exempt_missing_changes (test_check_state.CompletionGateTests.test_exception_cannot_exempt_missing_changes) ... ok
test_explicit_ci_base_cannot_be_overridden_by_checkpoint (test_check_state.CompletionGateTests.test_explicit_ci_base_cannot_be_overridden_by_checkpoint) ... ok
test_external_links_are_structural_not_network_checks (test_check_state.CompletionGateTests.test_external_links_are_structural_not_network_checks) ... ok
test_extra_and_duplicate_records_fail (test_check_state.CompletionGateTests.test_extra_and_duplicate_records_fail) ... ok
test_failed_validation_blocks_ready_and_complete (test_check_state.CompletionGateTests.test_failed_validation_blocks_ready_and_complete) ... ok
test_future_and_inconsistent_times_fail (test_check_state.CompletionGateTests.test_future_and_inconsistent_times_fail) ... ok
test_git_internal_and_nested_repository_evidence_are_not_durable (test_check_state.CompletionGateTests.test_git_internal_and_nested_repository_evidence_are_not_durable) ... ok
test_ignored_checkpoint_and_evidence_are_not_durable (test_check_state.CompletionGateTests.test_ignored_checkpoint_and_evidence_are_not_durable) ... ok
test_initial_checkpoint_in_existing_repository (test_check_state.CompletionGateTests.test_initial_checkpoint_in_existing_repository) ... ok
test_initial_repository_without_commit (test_check_state.CompletionGateTests.test_initial_repository_without_commit) ... ok
test_internal_symlink_cannot_alias_canonical_state (test_check_state.CompletionGateTests.test_internal_symlink_cannot_alias_canonical_state) ... ok
test_invalid_and_unrelated_baselines_fail (test_check_state.CompletionGateTests.test_invalid_and_unrelated_baselines_fail) ... ok
test_invalid_status_types_fail_without_traceback (test_check_state.CompletionGateTests.test_invalid_status_types_fail_without_traceback) ... ok
test_malformed_https_links_and_credentials_are_rejected (test_check_state.CompletionGateTests.test_malformed_https_links_and_credentials_are_rejected) ... ok
test_malformed_yaml_frontmatter_and_yaml_canonical_are_rejected (test_check_state.CompletionGateTests.test_malformed_yaml_frontmatter_and_yaml_canonical_are_rejected) ... ok
test_markdown_parser_preserves_literal_json_comment_text (test_check_state.CompletionGateTests.test_markdown_parser_preserves_literal_json_comment_text) ... ok
test_missing_local_links_fail (test_check_state.CompletionGateTests.test_missing_local_links_fail) ... ok
test_mode_only_change_invalidates_record_and_test_scope (test_check_state.CompletionGateTests.test_mode_only_change_invalidates_record_and_test_scope) ... ok
test_nested_evidence_fields_are_required (test_check_state.CompletionGateTests.test_nested_evidence_fields_are_required) ... ok
test_nested_markdown_and_json_canonical_state_cli (test_check_state.CompletionGateTests.test_nested_markdown_and_json_canonical_state_cli) ... ok
test_no_behavior_change_exception_can_explain_inapplicable_tests (test_check_state.CompletionGateTests.test_no_behavior_change_exception_can_explain_inapplicable_tests) ... ok
test_not_applicable_is_not_available_for_behavior_changes (test_check_state.CompletionGateTests.test_not_applicable_is_not_available_for_behavior_changes) ... ok
test_passing_checkpoint_and_complete_gate (test_check_state.CompletionGateTests.test_passing_checkpoint_and_complete_gate) ... ok
test_refreshed_file_record_does_not_refresh_test_claim (test_check_state.CompletionGateTests.test_refreshed_file_record_does_not_refresh_test_claim) ... ok
test_same_snapshot_before_and_after_commit (test_check_state.CompletionGateTests.test_same_snapshot_before_and_after_commit) ... ok
test_self_fingerprint_is_rejected (test_check_state.CompletionGateTests.test_self_fingerprint_is_rejected) ... ok
test_shallow_and_missing_base_fail_without_fetching (test_check_state.CompletionGateTests.test_shallow_and_missing_base_fail_without_fetching) ... ok
test_shell_payloads_in_refs_paths_and_commands_are_data (test_check_state.CompletionGateTests.test_shell_payloads_in_refs_paths_and_commands_are_data) ... ok
test_staged_and_unstaged_edits_invalidate_fingerprints (test_check_state.CompletionGateTests.test_staged_and_unstaged_edits_invalidate_fingerprints) ... ok
test_staged_checkpoint_cannot_differ_from_reviewed_checkpoint (test_check_state.CompletionGateTests.test_staged_checkpoint_cannot_differ_from_reviewed_checkpoint) ... ok
test_staged_code_cannot_hide_behind_restored_worktree (test_check_state.CompletionGateTests.test_staged_code_cannot_hide_behind_restored_worktree) ... ok
test_staged_deletion_cannot_hide_restored_file (test_check_state.CompletionGateTests.test_staged_deletion_cannot_hide_restored_file) ... ok
test_symlinks_fail_without_reading_target (test_check_state.CompletionGateTests.test_symlinks_fail_without_reading_target) ... ok
test_unmerged_index_fails_closed (test_check_state.CompletionGateTests.test_unmerged_index_fails_closed) ... ok
test_unrun_is_not_a_pass (test_check_state.CompletionGateTests.test_unrun_is_not_a_pass) ... ok
test_unsafe_paths_fail (test_check_state.CompletionGateTests.test_unsafe_paths_fail) ... ok
test_untracked_code_requires_record (test_check_state.CompletionGateTests.test_untracked_code_requires_record) ... ok

----------------------------------------------------------------------
Ran 66 tests in 4.248s

OK
```

## git diff --exit-code a89e33aeecc0c8182e3f13e775670fe57e3d0875 -- src config gradle build.gradle settings.gradle gradle.properties gradlew gradlew.bat croptopia_block_ids_all.json

Observed: 2026-10-22T05:51:31.150917+00:00; exit 0. Protected application/source/data paths are unchanged from the inspected default-branch baseline.

```text
(no differences)
```

## PyYAML BaseLoader parse; bash -n on workflow run blocks

Observed: 2026-10-22T05:51:31.155026+00:00; exit 0. Workflow YAML and shell syntax passed locally; no hosted GitHub execution is claimed.

```text
YAML parsed; read-only permissions/retarget trigger checked; shell blocks parsed.
```


## Independent review and configured-check fixtures

Independent reviewer found and confirmed fixes for ignored-config durability (now regular, non-symlink, tracked/trackable) and Art minimum-read routing. Core and protected paths remain unchanged. The adapter adds ignored/symlink regressions.

Observed 2026-10-22T05:51:36.376716+00:00. Negative cases ran in disposable cloud Git copies, not against product files.

- Configured gate on actual checkout: exit 0 (expected).

```text
Checkpoint structure and change coverage passed (ready_for_review). Reported evidence and human acceptance are not independently verified.
```

- Unrecorded substantive change: exit 1 (expected).

```text
Completion gate failed: change record mismatch: missing=['rogue_change.py'], extra=[], stale=[]
```

- Trusted target mismatch: exit 1 (expected).

```text
Completion gate config failed: config target differs from trusted target
```

- Premature complete claim: exit 1 (expected).

```text
Completion gate failed: completion refused: status is ready_for_review
```

## Publication observation

Observed 2026-10-22T05:54:27.618316+00:00. Non-force push verified remote SHA `0467ee9b7d47c22e7dd636dde877cfc1bf939738`. [Draft PR 504](https://github.com/Hoppy-days/Fabric-Garden-King-Mod-1.20.1/pull/504) targets the recorded default branch. [Hosted completion-gate run 39730582637](https://github.com/Hoppy-days/Fabric-Garden-King-Mod-1.20.1/actions/runs/39730582637) completed successfully at that SHA. This documentation-only follow-up records the observed result without asserting its own future run. No merge, product/human acceptance or required-check settings change occurred.
