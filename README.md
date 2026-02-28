# CrossPlatform Content Filter

Cross-platform, privacy-respecting content blocker for **Windows** and **Android**.

Built for self-control — blocks adult content at the OS / DNS level with no cloud dependencies, no screen reading, and no data collection.

---

## Platforms

| Platform | Directory | Mechanism | Status |
|----------|-----------|-----------|--------|
| **Windows** | [`guard-WINDOWS/`](guard-WINDOWS/) | Hosts file + Window title detection + Windows Service | ✅ Production |
| **Android** | [`guard-ANDROID/`](guard-ANDROID/) | Device Owner + Private DNS lock (Cloudflare Family) | ✅ Production |

---

## How It Works

### Windows (`guard-WINDOWS/`)

Two tiers of protection:

- **guard_lite/** — Lightweight Python blocker: hosts-file DNS poisoning, browser window title monitoring via `win32gui`, keyword detection, and forced shutdown on violation.
- **ultra_guard/** — Hardened service architecture: runs as a Windows Service with a crash-guard watchdog, SHA-256 file integrity checks, firewall rules, and boot persistence. Designed to survive `taskkill`, safe mode, and file tampering.

```
Hosts File DNS Blocking
        ↓
Win32GUI Window Title Interceptor
        ↓
Keyword Detection Engine
        ↓
Windows Service + Watchdog
        ↓
Session Lock / Forced Shutdown
```

### Android (`guard-ANDROID/`)

Uses Android's **Device Owner** API (provisioned via ADB) to lock the system-wide Private DNS to Cloudflare Family (`family.cloudflare-dns.com`). No local VPN required — works alongside any real VPN.

```
Device Owner (core authority)
        ↓
System Private DNS Lock (Cloudflare Family)
        ↓
App Visibility Control
        ↓
Violation Enforcement (warn → hide → lock)
        ↓
Auto-Heal Watchdog (every 15 min)
        ↓
Local Analytics
```

---

## Quick Start

### Windows

```powershell
cd guard-WINDOWS/ultra_guard
pip install -r requirements.txt
python service_installer.py install
python service_installer.py start
```

### Android (Pixel 6 / Android 10+)

```bash
# Build in Android Studio, install APK, then:
adb shell dpm set-device-owner com.ultraguard/.admin.AdminReceiver
```

See each platform's README for detailed setup instructions.

---

## Privacy

This software does **NOT**:

- Read screen content (Android)
- Send data to any external server
- Require an internet connection to function
- Lock users in kiosk mode
- Monitor app usage beyond DNS metadata

All data stays **local** on the device.

---

## Bypass Difficulty

| Vector | Windows | Android |
|--------|---------|---------|
| Uninstall app | Blocked (service respawns) | Blocked (Device Owner) |
| Change DNS | Blocked (hosts + service) | Blocked (DPM global setting) |
| Kill process | Watchdog restarts in <2s | WorkManager re-applies |
| Factory reset | N/A | Blocked (`DISALLOW_FACTORY_RESET`) |
| Safe mode | Service persists | Blocked (`DISALLOW_SAFE_BOOT`) |
| **Full bypass** | Reformat OS | Reflash firmware via bootloader |

---

## Project Structure

```
CrossPlatform-Content-Filter/
├── README.md                  ← you are here
├── guard-WINDOWS/
│   ├── guard_lite/            # Lightweight Python blocker
│   │   ├── main.py
│   │   ├── monitor.py
│   │   ├── network_blocker.py
│   │   ├── keywords.py
│   │   └── ...
│   └── ultra_guard/           # Hardened Windows Service
│       ├── service/
│       │   ├── main_service.py
│       │   ├── dns_blocker.py
│       │   ├── keyword_detector.py
│       │   ├── watchdog.py
│       │   └── ...
│       ├── service_installer.py
│       ├── build_exe.py
│       └── ...
└── guard-ANDROID/
    └── app/src/main/java/com/ultraguard/
        ├── MainActivity.kt
        ├── admin/PolicyManager.kt
        ├── dns/DnsEnforcer.kt
        ├── watchdog/WatchdogWorker.kt
        └── ...
```

---

## Tech Stack

| | Windows | Android |
|---|---------|---------|
| Language | Python 3.10+ | Kotlin |
| Blocking | Hosts file, `win32gui` | Cloudflare Family DNS |
| Persistence | Windows Service, Registry | Device Owner API, WorkManager |
| Build | PyInstaller | Gradle / Android Studio |

---

## License

This project is for **personal self-control use only**. Not intended for surveillance or monitoring others without consent.
