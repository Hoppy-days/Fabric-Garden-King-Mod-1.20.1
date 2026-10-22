#!/usr/bin/env python3
"""Repository configuration adapter for the vendored Agent Toolkit gate."""
import argparse
import json
from pathlib import Path
import re
import sys
import check_state as gate


def read_config(root, target=None):
    path = gate.durable_file(root, ".completion-gate.json", "config")
    config = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=gate.no_duplicate_keys)
    gate.fields(config, ["schema_version", "state_path", "target_branch", "toolkit_revision"], "config")
    gate.require(type(config["schema_version"]) is int and config["schema_version"] == 1, "unsupported config schema")
    for key in ["state_path", "target_branch", "toolkit_revision"]:
        gate.text(config[key], "config." + key)
    gate.local_path(root, config["state_path"])
    gate.require(Path(config["state_path"]).suffix.lower() in {".md", ".rst", ".json"}, "unsupported canonical state format")
    gate.require(re.fullmatch(r"[0-9a-f]{40}", config["toolkit_revision"]), "invalid toolkit provenance revision")
    gate.require(target is None or target == config["target_branch"], "config target differs from trusted target")
    return config


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", required=True)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--target")
    parser.add_argument("--snapshot", action="store_true")
    parser.add_argument("--require-complete", action="store_true")
    args = parser.parse_args(argv)
    root = Path(__file__).resolve().parents[2]
    try:
        config = read_config(root, args.target)
    except (ValueError, TypeError, OSError) as exc:
        print(f"Completion gate config failed: {exc}", file=sys.stderr)
        return 1
    options = ["--repo", str(root), "--state", config["state_path"], "--base", args.base,
               "--branch", args.branch, "--target", args.target or config["target_branch"]]
    if args.snapshot:
        options.append("--snapshot")
    if args.require_complete:
        options.append("--require-complete")
    return gate.main(options)


if __name__ == "__main__":
    sys.exit(main())
