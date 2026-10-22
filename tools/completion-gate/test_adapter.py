"""Tests of the small per-repository configuration adapter."""
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
import run_gate


class AdapterTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        subprocess.run(["git", "init", "-b", "main", str(self.root)], check=True, capture_output=True)
        self.config = {"schema_version": 1, "state_path": "PROJECT_STATE.md", "target_branch": "main", "toolkit_revision": "d" * 40}

    def save(self):
        (self.root / ".completion-gate.json").write_text(json.dumps(self.config))

    def test_valid_nested_json(self):
        self.config["state_path"] = "docs/state.json"
        self.save()
        self.assertEqual(run_gate.read_config(self.root, "main"), self.config)

    def test_trusted_target_mismatch(self):
        self.save()
        with self.assertRaisesRegex(ValueError, "target differs"):
            run_gate.read_config(self.root, "release")

    def test_missing_and_duplicate_fields(self):
        del self.config["state_path"]
        self.save()
        with self.assertRaisesRegex(ValueError, "missing fields"):
            run_gate.read_config(self.root)
        (self.root / ".completion-gate.json").write_text('{"schema_version":1,"schema_version":2}')
        with self.assertRaisesRegex(ValueError, "duplicate"):
            run_gate.read_config(self.root)

    def test_escaping_or_unsupported_state_path(self):
        for name in ["../state.md", "/state.md", "state.yaml"]:
            self.config["state_path"] = name
            self.save()
            with self.assertRaises(ValueError):
                run_gate.read_config(self.root)

    def test_invalid_config_schema_and_provenance(self):
        for key, value in [("schema_version", True), ("schema_version", 2), ("toolkit_revision", "HEAD")]:
            old = self.config[key]
            self.config[key] = value
            self.save()
            with self.assertRaises(ValueError):
                run_gate.read_config(self.root)
            self.config[key] = old

    def test_invalid_json_or_missing_config(self):
        with self.assertRaises(ValueError):
            run_gate.read_config(self.root)
        (self.root / ".completion-gate.json").write_text('{broken')
        with self.assertRaises(ValueError):
            run_gate.read_config(self.root)

    def test_ignored_config_rejected(self):
        self.save()
        (self.root / ".gitignore").write_text(".completion-gate.json\n")
        with self.assertRaises(ValueError):
            run_gate.read_config(self.root)

    def test_symlink_config_rejected(self):
        self.save()
        (self.root / ".completion-gate.json").rename(self.root / "actual.json")
        (self.root / ".completion-gate.json").symlink_to("actual.json")
        with self.assertRaises(ValueError):
            run_gate.read_config(self.root)
