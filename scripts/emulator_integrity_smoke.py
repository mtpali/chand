#!/usr/bin/env python3
"""Exercise this project's own APK on an emulator, including controlled re-signing.

The disposable test key is not a release key. Never runs against a physical device.
"""
import argparse
import hashlib
import os
from pathlib import Path
import re
import subprocess
import time
import zipfile
import zlib

PACKAGE = "com.chand.mobiletina"


def run(args, check=True):
    result = subprocess.run(list(map(str, args)), check=check, text=True,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return result.stdout.strip()


def adb(*args, check=True):
    return run(["adb", "-e", *args], check=check)


def certificate(apksigner, apk):
    text = run([apksigner, "verify", "--print-certs", apk])
    match = re.search(r"certificate SHA-256 digest: ([0-9a-fA-F]{64})", text)
    if not match:
        raise RuntimeError("APK signer fingerprint was not found")
    return match.group(1).upper()


def launch():
    component = adb("shell", "cmd", "package", "resolve-activity", "--brief", PACKAGE).splitlines()[-1]
    if not component.startswith(PACKAGE + "/"):
        raise RuntimeError("Launcher activity was not resolved: " + component)
    adb("logcat", "-c")
    adb("shell", "am", "start", "-W", "-n", component, check=False)
    # Require a stable process, rather than a momentary PID during Application.onCreate.
    time.sleep(5)
    return bool(adb("shell", "pidof", PACKAGE, check=False))


def require_rejected(apk, name, output):
    adb("uninstall", PACKAGE, check=False)
    adb("install", "--no-streaming", apk)
    alive = launch()
    logs = adb("logcat", "-d", "-v", "brief")
    (output / (name + ".log")).write_text(logs)
    if alive or "Application integrity verification failed" not in logs:
        raise AssertionError(name + " did not fail at the intended integrity gate")
    print(name + ": startup rejected by integrity guard", flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    if adb("shell", "getprop", "ro.kernel.qemu") != "1":
        raise RuntimeError("This smoke test is restricted to an Android emulator")
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    tools = Path(os.environ.get("ANDROID_HOME", os.environ.get("ANDROID_SDK_ROOT", ""))) / "build-tools/36.0.0"
    apksigner, zipalign = tools / "apksigner", tools / "zipalign"
    apk = args.apk.resolve()
    original_pin = certificate(apksigner, apk)
    adb("uninstall", PACKAGE, check=False)
    adb("install", "--no-streaming", apk)
    if not launch():
        (output / "original.log").write_text(adb("logcat", "-d", "-v", "brief"))
        raise AssertionError("The original hardened APK failed to start")
    print("original: stable startup", flush=True)
    (output / "original.log").write_text(adb("logcat", "-d", "-v", "brief"))

    store = output / "disposable-test.p12"
    password = "ci-tamper-test-only"
    run(["keytool", "-genkeypair", "-noprompt", "-alias", "tamper", "-keyalg", "RSA",
         "-keysize", "2048", "-validity", "2", "-dname", "CN=Disposable CI Test",
         "-storetype", "PKCS12", "-keystore", store, "-storepass", password])
    cert_file = output / "disposable-cert.der"
    run(["keytool", "-exportcert", "-alias", "tamper", "-keystore", store,
         "-storepass", password, "-file", cert_file])
    new_pin = hashlib.sha256(cert_file.read_bytes()).hexdigest().upper()

    def variant(name, patch_dex_pin):
        unsigned, aligned, signed = [output / (name + suffix) for suffix in ("-unsigned.apk", "-aligned.apk", ".apk")]
        replacements = 0
        with zipfile.ZipFile(apk) as src, zipfile.ZipFile(unsigned, "w") as dst:
            for info in src.infolist():
                if re.match(r"META-INF/[^/]+\.(RSA|DSA|EC|SF|MF)$", info.filename, re.I):
                    continue
                data = src.read(info)
                if patch_dex_pin and info.filename.endswith(".dex"):
                    count = data.count(original_pin.encode("ascii"))
                    if count:
                        replacements += count
                        data = bytearray(data.replace(original_pin.encode("ascii"), new_pin.encode("ascii")))
                        data[12:32] = hashlib.sha1(data[32:]).digest()
                        data[8:12] = zlib.adler32(data[12:]).to_bytes(4, "little")
                dst.writestr(info, data)
            dst.writestr("assets/controlled-integrity-test.txt", b"Controlled CI tamper probe")
        if patch_dex_pin and replacements < 1:
            raise AssertionError("The Kotlin certificate pin was not found for the native-layer test")
        run([zipalign, "-P", "16", "-f", "4", unsigned, aligned])
        run([apksigner, "sign", "--ks", store, "--ks-key-alias", "tamper",
             "--ks-pass", "pass:" + password, "--out", signed, aligned])
        if certificate(apksigner, signed) != new_pin:
            raise AssertionError("Controlled APK was not signed by the disposable key")
        require_rejected(signed, name, output)

    variant("resigned", False)
    # Even after changing the DEX pin to trust the new signer, the independent native pin rejects it.
    variant("dex-pin-patched", True)
    # The upload artifact contains logs only, never the disposable private key or tampered APKs.
    print("original and both tamper cases passed", flush=True)


if __name__ == "__main__":
    main()
