#!/usr/bin/env python3
"""Offline content smoke-check for the Minecraft 26.3 mod and generated assets."""
from __future__ import annotations

import json
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
json_count = 0
png_count = 0

for path in sorted(ROOT.rglob("*.json")):
    if ".git" in path.parts:
        continue
    try:
        json.loads(path.read_text(encoding="utf-8"))
        json_count += 1
    except Exception as exc:
        errors.append(f"Invalid JSON: {path.relative_to(ROOT)}: {exc}")

png_sizes: dict[str, tuple[int, int]] = {}
for path in sorted(ROOT.rglob("*.png")):
    if ".git" in path.parts:
        continue
    try:
        data = path.read_bytes()
        if data[:8] != b"\x89PNG\r\n\x1a\n" or len(data) < 24:
            raise ValueError("missing PNG signature or IHDR")
        width, height = struct.unpack(">II", data[16:24])
        if not width or not height:
            raise ValueError("empty image")
        png_sizes[path.relative_to(ROOT).as_posix()] = (width, height)
        png_count += 1
    except Exception as exc:
        errors.append(f"Invalid PNG: {path.relative_to(ROOT)}: {exc}")

pack_meta = ROOT / "src/main/resources/resourcepacks/nightfall/pack.mcmeta"
try:
    pack_info = json.loads(pack_meta.read_text(encoding="utf-8"))["pack"]
    if pack_info.get("min_format") != [97, 1] or pack_info.get("max_format") != [97, 1]:
        errors.append("Nightfall must declare Minecraft 26.3 resource-pack format [97, 1]")
    if "pack_format" in pack_info:
        errors.append("26.3 pack metadata must use min_format/max_format, not pack_format")
except Exception as exc:
    errors.append(f"Could not read pack metadata: {exc}")

required_files = [
    "build.gradle",
    "gradle.properties",
    "src/main/resources/fabric.mod.json",
    "src/main/resources/resourcepacks/nightfall/assets/redry/sounds.json",
    "src/main/resources/resourcepacks/nightfall/assets/redry/lang/cs_cz.json",
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/herobrine.png",
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/still_one.png",
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/chat_echo.png",
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/item/field_tape.png",
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/item/signal_receiver.png",
    "src/main/resources/data/redry/recipe/field_tape.json",
    "src/main/resources/data/redry/recipe/signal_receiver.json",
    "src/main/java/cz/redry/nightfall/command/RedryCommands.java",
    "src/main/java/cz/redry/nightfall/entity/SignalEchoEntity.java",
    "src/client/java/cz/redry/nightfall/client/render/RedryHudOverlay.java",
]
for relative in required_files:
    if not (ROOT / relative).is_file():
        errors.append(f"Missing required file: {relative}")

try:
    properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    if not re.search(r"(?m)^minecraft_version=26\.3$", properties):
        errors.append("Gradle target must be Minecraft 26.3")
    if not re.search(r"(?m)^loader_version=0\.19\.5$", properties):
        errors.append("Gradle target must use Fabric Loader 0.19.5")
    if not re.search(r"(?m)^fabric_api_version=0\.161\.0\+26\.3$", properties):
        errors.append("Gradle target must use Fabric API 0.161.0+26.3")
    if not re.search(r"(?m)^loom_version=1\.18\.2$", properties):
        errors.append("Gradle target must use Fabric Loom 1.18.2")
except Exception as exc:
    errors.append(f"Could not read Gradle properties: {exc}")

try:
    mod = json.loads((ROOT / "src/main/resources/fabric.mod.json").read_text(encoding="utf-8"))
    depends = mod["depends"]
    if depends.get("minecraft") != "~26.3" or depends.get("java") != ">=25":
        errors.append("fabric.mod.json must target Minecraft 26.3 and Java 25+")
except Exception as exc:
    errors.append(f"Could not check mod metadata: {exc}")

expected_sizes = {
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/herobrine.png": (64, 64),
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/still_one.png": (64, 64),
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/entity/chat_echo.png": (64, 64),
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/item/field_tape.png": (16, 16),
    "src/main/resources/resourcepacks/nightfall/assets/redry/textures/item/signal_receiver.png": (16, 16),
}
for path, expected in expected_sizes.items():
    actual = png_sizes.get(path)
    if actual != expected:
        errors.append(f"Unexpected dimensions for {path}: {actual!r}, expected {expected!r}")

try:
    sounds = json.loads((ROOT / "src/main/resources/resourcepacks/nightfall/assets/redry/sounds.json").read_text(encoding="utf-8"))
    sound_source = (ROOT / "src/main/java/cz/redry/nightfall/audio/RedrySounds.java").read_text(encoding="utf-8")
    for event in sounds:
        if f'register("{event}")' not in sound_source:
            errors.append(f"Resource-pack sound event is not registered in Java: redry:{event}")
except Exception as exc:
    errors.append(f"Could not cross-check registered sound events: {exc}")

if errors:
    print("REDRY validation failed:")
    for error in errors:
        print(f" - {error}")
    raise SystemExit(1)

print(f"REDRY 26.3 offline validation passed: {json_count} JSON files, {png_count} PNG files.")
