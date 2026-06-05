#!/usr/bin/env python3
"""Manual AutoJs6 ADB project bytes_command replay.

Prerequisites:
  - adb is available and sees the target device.
  - AutoJs6 Server mode is enabled on the device.
  - The selected device runs AutoJs6 >= 6.7.0 / 3591.
"""

import argparse
import hashlib
import json
import socket
import struct
import subprocess
import tempfile
import time
import zipfile
from pathlib import Path

TYPE_JSON = 1
TYPE_BYTES = 2
SERVER_PORT = 7347


def free_port():
    s = socket.socket()
    s.bind(("127.0.0.1", 0))
    port = s.getsockname()[1]
    s.close()
    return port


def frame(t, payload):
    return struct.pack(">II", len(payload), t) + payload


def read_frame(sock, timeout=8):
    sock.settimeout(timeout)
    header = sock.recv(8)
    if len(header) != 8:
        raise RuntimeError(f"incomplete header: {len(header)}")
    length, typ = struct.unpack(">II", header)
    payload = b""
    while len(payload) < length:
        payload += sock.recv(length - len(payload))
    return typ, payload


def send_json(sock, obj):
    sock.sendall(frame(TYPE_JSON, json.dumps(obj, ensure_ascii=False, separators=(",", ":")).encode("utf-8")))


def make_zip(root: Path):
    out = tempfile.NamedTemporaryFile(delete=False, suffix=".zip")
    out.close()
    zip_path = Path(out.name)
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as z:
        for p in sorted(root.rglob("*")):
            if p.is_file():
                z.write(p, p.relative_to(root).as_posix())
    data = zip_path.read_bytes()
    zip_path.unlink(missing_ok=True)
    return data, hashlib.md5(data).hexdigest()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--serial", required=True)
    ap.add_argument("--project", required=True, type=Path)
    ap.add_argument("--adb", default="adb")
    args = ap.parse_args()

    project = args.project.resolve()
    if not (project / "project.json").exists():
        raise SystemExit(f"missing project.json: {project}")
    payload, md5 = make_zip(project)
    local = free_port()
    subprocess.run([args.adb, "-s", args.serial, "forward", f"tcp:{local}", f"tcp:{SERVER_PORT}"], check=True)
    try:
        with socket.create_connection(("127.0.0.1", local), timeout=8) as sock:
            typ, hello_payload = read_frame(sock)
            print("DEVICE_HELLO", typ, hello_payload.decode("utf-8", "replace"))
            send_json(sock, {"id": 1, "type": "hello", "data": {"extensionVersion": "0.1.0"}})
            for idx, command in enumerate(["save_project", "run_project"], start=1):
                sock.sendall(frame(TYPE_BYTES, payload))
                send_json(sock, {
                    "type": "bytes_command",
                    "md5": md5,
                    "data": {
                        "id": str(project),
                        "name": str(project),
                        "deletedFiles": [],
                        "override": idx > 1,
                        "command": command,
                    },
                })
                print("SENT", command, "bytes", len(payload), "md5", md5)
                time.sleep(1)
            try:
                typ, extra = read_frame(sock, 1)
                print("EXTRA_FRAME", typ, extra[:300].decode("utf-8", "replace"))
            except Exception as exc:
                print("NO_EXTRA_FRAME_WITHIN_1S", type(exc).__name__, exc)
    finally:
        subprocess.run([args.adb, "-s", args.serial, "forward", "--remove", f"tcp:{local}"], check=False)


if __name__ == "__main__":
    main()
