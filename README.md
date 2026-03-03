# CrossPlatform Content Filter

> **Cross-platform content filtering and digital wellbeing system for Android and Windows — DNS filtering, always-on VPN enforcement, and device policy management in a single open-source project.**

CrossPlatform Content Filter is a privacy-focused, open-source content filter that blocks adult content at the OS and DNS level — no cloud services, no screen reading, no data collection. Built for **digital wellbeing**, **parental control**, and **self-control**, it uses DNS filtering, device policy management, and system-level enforcement to create a safe, distraction-free browsing environment on both **Android** and **Windows**.

Unlike browser extensions or simple DNS apps, CrossPlatform Content Filter enforces content filtering at the operating system level using **Device Owner policies** (Android) and **Windows Service architecture** (Windows) — making it extremely difficult to bypass, disable, or uninstall.

[![GitHub release](https://img.shields.io/github/v/tag/kbvideo6/CrossPlatform-Content-Filter?label=version&style=flat-square)](https://github.com/kbvideo6/CrossPlatform-Content-Filter/releases)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Windows-blue?style=flat-square)](#supported-platforms)
[![License](https://img.shields.io/badge/license-personal--use-green?style=flat-square)](#license)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](https://github.com/kbvideo6/CrossPlatform-Content-Filter/pulls)
[![Topics](https://img.shields.io/badge/topics-dns--filtering%20%7C%20parental--control%20%7C%20digital--wellbeing-informational?style=flat-square)](#)

---

## Why CrossPlatform Content Filter?

Most content blockers fail because they're **too easy to bypass** — a VPN toggle, a DNS change, a quick uninstall, or switching to incognito mode defeats them. CrossPlatform Content Filter is engineered differently:

| Problem | How We Solve It |
|---------|-----------------|
| User changes DNS | DNS locked at OS level (Device Owner / hosts file) + instant re-enforcement |
| User uninstalls blocker | Device Owner prevents uninstall; Windows Service respawns |
| User kills process | Watchdog restarts within 2 seconds |
| User uses incognito | Window title monitoring catches it (Windows) |
| User installs bypass app | Known bypass apps auto-hidden (Android) |
| User factory resets | Factory reset disabled via Device Owner policy |
| User enables dev options | Auto-disabled on detection (Android) |

**Zero cloud. Zero telemetry. Zero screen reading. 100% local.**

---

## Overview

- **Android:** Uses the Device Owner API to lock Private DNS to Cloudflare Family DNS. Cannot be uninstalled, disabled, or bypassed through Settings. Compatible with third-party VPNs.
- **Windows:** Runs as a persistent Windows Service with a crash-guard watchdog, hosts-file DNS poisoning, SHA-256 integrity verification, and real-time window title monitoring with keyword detection.

No kiosk mode. No surveillance. No internet required. All data stays on-device.

---

## Features

### Content Filtering & DNS Security
- **DNS-level content filtering** — blocks 100+ adult domains at the network layer using family-friendly DNS
- **Multi-DNS failover** — 4 family-friendly servers (Cloudflare, CleanBrowsing, AdGuard, Quad9) with health-check
- **Cross-platform content filter** — single project covers Android (Kotlin) and Windows (Python)
- **Instant DNS tamper detection** — ContentObserver-based service catches DNS changes in real-time
- **Custom domain block lists** — user-configurable with 21-day mandatory lock period

### Android Device Owner Protection
- **Device Owner enforcement** — locked Private DNS, anti-uninstall, anti-factory-reset
- **VPN-compatible** — no local VPN used, works alongside NordVPN, Mullvad, Proton VPN, etc.
- **Developer Options auto-disable** — automatically turns off dev options after ADB provisioning
- **App visibility control** — automatically hides known bypass apps (Tor, Opera VPN)
- **Violation escalation** — warn → hide offending app → lock device + push notification
- **Push notifications** — alerts for blocked access, escalation events, and critical actions
- **Dark theme UI** — 6-card dashboard with network traffic, analytics, and custom domain management

### Windows System-Level Protection
- **Windows Service architecture** — survives task manager kills, reboots, and safe mode
- **Real-time window title monitoring** — catches renamed browsers, incognito tabs, and private browsing
- **SHA-256 integrity verification** — detects file tampering of critical components
- **Hosts file DNS poisoning** — redirects blocked domains to 127.0.0.1

### Privacy & Security
- **Privacy-first** — zero telemetry, zero cloud sync, zero screen reading
- **Auto-heal watchdog** — re-applies policies every 15 minutes on both platforms
- **Local analytics dashboard** — streak counter, daily blocks, event history
- **Open source** — fully auditable, no hidden behavior

---

## Screenshots

| Android Dashboard (v2.0) | Windows Service | Architecture |
|:-:|:-:|:-:|
| ![Android Dashboard](assets/android-dashboard-v2.png) | ![Windows Service](assets/windows-service.png) | ![Architecture](assets/architecture.png) |

---

## Supported Platforms

| Platform | Directory | Technology | Blocking Method |
|----------|-----------|------------|-----------------|
| **Android 10+** | [`guard-ANDROID/`](guard-ANDROID/) | Kotlin, Gradle | Device Owner + Multi-DNS Lock + Failover |
| **Windows 10/11** | [`guard-WINDOWS/`](guard-WINDOWS/) | Python 3.10+ | Hosts File + Win32GUI + Windows Service |

---

## Architecture

### Android Content Filter

The Android content filter uses the **Device Owner** API (provisioned via ADB) to enforce system-wide Private DNS filtering through family-friendly DNS servers with automatic failover. No local VPN is required — the DNS lock works at the OS level, even when a third-party VPN is active.

```
Device Owner (core authority)
        ↓
Multi-DNS Lock (4-server failover)
        ↓
DNS Monitor Service (instant ContentObserver)
        ↓
Developer Options Auto-Disable
        ↓
User Restrictions (anti-tamper)
        ↓
App Visibility Control
        ↓
Violation Enforcement (warn → hide → lock → notify)
        ↓
Custom Domain Blocking (21-day lock)
        ↓
Auto-Heal Watchdog (15 min cycle)
        ↓
Local Analytics + Network Traffic Dashboard
```

### Windows Content Filter

The Windows content filter operates at two tiers — a lightweight Python blocker and a hardened system service with integrity verification.

```
Hosts File DNS Blocking (127.0.0.1 redirect)
        ↓
Win32GUI Window Title Interceptor
        ↓
Keyword Detection Engine
        ↓
Windows Service + Crash-Guard Watchdog
        ↓
SHA-256 File Integrity Checks
        ↓
Session Lock / Forced Shutdown
```

---

## Installation

### Android Setup (Pixel / Android 10+)

**Prerequisites:** Android Studio, USB debugging enabled, ADB installed.

```bash
# 1. Clone the repo
git clone https://github.com/kbvideo6/CrossPlatform-Content-Filter.git
cd CrossPlatform-Content-Filter/guard-ANDROID

# 2. Open in Android Studio → Build → Run on device

# 3. Remove all Google accounts from Settings → Accounts

# 4. Provision as Device Owner
adb shell dpm set-device-owner com.ultraguard/.admin.AdminReceiver
```

After provisioning, the dashboard should show:
- **Protection Active** (green shield glow)
- **DNS Lock:** family.cloudflare-dns.com ✓
- **Bypass Prevention:** Active ✓
- **Watchdog:** Active (15 min)

### Windows Setup

**Prerequisites:** Python 3.10+, Administrator privileges.

```powershell
# 1. Clone the repo
git clone https://github.com/kbvideo6/CrossPlatform-Content-Filter.git
cd CrossPlatform-Content-Filter/guard-WINDOWS/ultra_guard

# 2. Install dependencies
pip install -r requirements.txt

# 3. Install and start the Windows Service
python service_installer.py install
python service_installer.py start
```

See each platform's README for detailed configuration:
- [Android README](guard-ANDROID/README.md)
- [Windows README (UltraGuard)](guard-WINDOWS/ultra_guard/README.md)
- [Windows README (Guard Lite)](guard-WINDOWS/guard_lite/README.md)

---

## Security Model

### Bypass Difficulty

| Attack Vector | Windows | Android |
|---------------|---------|---------|
| Uninstall app | ❌ Service respawns | ❌ Device Owner prevents |
| Change DNS settings | ❌ Hosts file + service | ❌ DPM global setting locked |
| Kill process | ❌ Watchdog restarts <2s | ❌ WorkManager re-applies |
| Factory reset | — | ❌ `DISALLOW_FACTORY_RESET` |
| Safe mode boot | ❌ Service persists | ❌ `DISALLOW_SAFE_BOOT` |
| USB debugging | — | ❌ `DISALLOW_DEBUGGING_FEATURES` |
| **Full bypass** | Reformat OS | Reflash firmware via bootloader |

### Android Protection Layers

| # | Layer | Purpose |
|---|-------|---------|
| 1 | Device Owner | Full DevicePolicyManager authority |
| 2 | Private DNS Lock | Forces `family.cloudflare-dns.com` system-wide |
| 3 | User Restrictions | Blocks factory reset, safe boot, debugging, add user |
| 4 | App Visibility | Hides known bypass apps (Tor, Opera VPN) |
| 5 | Violation Escalation | warn → hide app → lock device + notification |
| 6 | DNS Monitor Service | ContentObserver-based instant tamper detection |
| 7 | Watchdog | Re-verifies DNS + restrictions every 15 min |
| 8 | Custom Domain Blocks | User-added domains with 21-day lock |
| 9 | Event Logger | Local-only analytics (streak, daily count, history) |

### Windows Protection Layers

| # | Layer | Purpose |
|---|-------|---------|
| 1 | DNS Poisoning | Redirects blocked domains to 127.0.0.1 via hosts file |
| 2 | Window Title Monitor | Detects adult keywords in any active window title |
| 3 | Keyword Engine | Pattern matching against curated keyword database |
| 4 | Windows Service | Persistent background service with auto-restart |
| 5 | Watchdog | Restarts killed processes within 2 seconds |
| 6 | Integrity Check | SHA-256 verification of critical files |

---

## Privacy

This content filter does **NOT**:

- 🚫 Read screen content or take screenshots
- 🚫 Send any data to external servers
- 🚫 Require internet to function
- 🚫 Lock users in kiosk mode
- 🚫 Monitor app usage beyond DNS metadata
- 🚫 Collect personal information

All analytics are stored **locally only** on the device. Zero telemetry. Fully auditable open-source code.

---

## Project Structure

```
CrossPlatform-Content-Filter/
│
├── README.md
│
├── guard-ANDROID/                    # Android content filter
│   ├── app/src/main/java/com/ultraguard/
│   │   ├── MainActivity.kt          # Dashboard UI (6-card dark theme)
│   │   ├── BlockHistoryActivity.kt  # Full block history view
│   │   ├── OmegaApp.kt              # Application entry point
│   │   ├── admin/
│   │   │   ├── AdminReceiver.kt     # Device Owner receiver
│   │   │   └── PolicyManager.kt     # Central policy orchestrator
│   │   ├── dns/
│   │   │   ├── DnsEnforcer.kt       # Multi-DNS lock with failover
│   │   │   ├── DnsMonitorService.kt # Instant DNS tamper detection
│   │   │   ├── DomainBlockList.kt   # 100+ blocked domains
│   │   │   └── DomainBlockManager.kt # Custom 21-day domain blocking
│   │   ├── appcontrol/
│   │   │   └── AppVisibilityManager.kt
│   │   ├── violation/
│   │   │   ├── ViolationEnforcer.kt # Escalation ladder + notifications
│   │   │   └── NotificationHelper.kt # Push notification system
│   │   ├── watchdog/
│   │   │   ├── BootReceiver.kt      # Re-apply on reboot
│   │   │   ├── WatchdogScheduler.kt
│   │   │   └── WatchdogWorker.kt    # 15-min health check + DNS fallback
│   │   └── analytics/
│   │       ├── EventLogger.kt       # Local event logging
│   │       └── NetworkTrafficSummary.kt # Network traffic stats
│   ├── build.gradle.kts
│   └── README.md
│
└── guard-WINDOWS/                    # Windows content filter
    ├── guard_lite/                   # Lightweight Python blocker
    │   ├── main.py
    │   ├── monitor.py
    │   ├── network_blocker.py
    │   └── keywords.py
    └── ultra_guard/                  # Hardened Windows Service
        ├── service/
        │   ├── main_service.py
        │   ├── dns_blocker.py
        │   ├── keyword_detector.py
        │   ├── watchdog.py
        │   └── integrity.py
        ├── service_installer.py
        └── README.md
```

---

## Tech Stack

| | Windows | Android |
|---|---------|---------|
| **Language** | Python 3.10+ | Kotlin |
| **DNS Blocking** | Hosts file (127.0.0.1) | Cloudflare Family DNS |
| **Persistence** | Windows Service, Registry | Device Owner API, WorkManager |
| **Detection** | Win32GUI, keyword matching | DNS resolver (Cloudflare) |
| **Build** | PyInstaller | Gradle / Android Studio |
| **Min Version** | Windows 10 | Android 10 (API 29) |

---

## Roadmap

- [x] Custom domain block lists (user-configurable, 21-day lock)
- [x] Multi-DNS failover (4-server health-check)
- [x] Instant DNS tamper detection (ContentObserver)
- [x] Push notifications for blocked access
- [x] Dark theme UI redesign
- [x] Network traffic dashboard
- [ ] iOS support (Supervised MDM profile)
- [ ] Web dashboard for multi-device analytics
- [ ] Scheduled blocking (study hours / bedtime)
- [ ] Browser extension companion
- [ ] macOS support (Network Extension framework)

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## License

This project is for **personal self-control and digital wellbeing use only**. Not intended for surveillance or monitoring others without consent.

---

## Frequently Asked Questions

<details>
<summary><strong>Is this a parental control app?</strong></summary>

CrossPlatform Content Filter can be used for parental control, but it's primarily designed as a **self-control and digital wellbeing tool**. It enforces DNS-level content filtering to help users maintain focus and avoid distracting or harmful content.
</details>

<details>
<summary><strong>Does it work with VPNs?</strong></summary>

**Android:** Yes. Because the content filter uses Device Owner Private DNS (not a local VPN), it works alongside any third-party VPN — NordVPN, Mullvad, ProtonVPN, Surfshark, etc.

**Windows:** The hosts file blocking works independently of VPN connections. Window title monitoring also functions regardless of network configuration.
</details>

<details>
<summary><strong>Does it collect any data?</strong></summary>

No. Zero telemetry, zero cloud sync, zero network requests. All analytics (streak counter, block counts) are stored locally on-device only. The source code is fully auditable.
</details>

<details>
<summary><strong>How is this different from browser extensions?</strong></summary>

Browser extensions only work in one browser and are trivially disabled. CrossPlatform Content Filter operates at the **operating system level** — it blocks content across all apps, all browsers, incognito mode, and even renamed executables. On Android, it locks DNS system-wide via Device Owner. On Windows, it uses hosts file poisoning and Win32GUI monitoring.
</details>

<details>
<summary><strong>Can I customize the blocked domains?</strong></summary>

Yes. The domain block list is fully configurable:
- **Android:** Edit `DomainBlockList.kt`
- **Windows:** Edit `keywords_db.txt` and `dns_blocker.py`
</details>

<details>
<summary><strong>What's the performance impact?</strong></summary>

Minimal. The Android content filter has no background processes beyond a 15-minute WorkManager check. The Windows service uses lightweight Win32GUI polling and hosts file modification — no CPU-intensive AI or ML processing.
</details>

---

## Comparison with Other Content Filters

| Feature | CrossPlatform Content Filter | Browser Extensions | DNS Apps | Parental Control Suites |
|---------|------------------------------|-------------------|----------|------------------------|
| Cross-platform (Android + Windows) | ✅ | ❌ | ⚠️ Some | ⚠️ Paid |
| Bypass-resistant | ✅ Device Owner + Service | ❌ Easy disable | ❌ DNS changeable | ⚠️ Moderate |
| Privacy-first (no cloud) | ✅ | ⚠️ Varies | ❌ Cloud DNS | ❌ Cloud required |
| VPN compatible | ✅ | ✅ | ❌ Conflicts | ⚠️ Varies |
| Open source | ✅ | ⚠️ Some | ❌ Mostly closed | ❌ |
| Free | ✅ | ✅ | ⚠️ Freemium | ❌ Subscription |
| No screen reading | ✅ | ⚠️ | ✅ | ❌ |

---

## Star History

If this project helps you, consider giving it a ⭐ — it helps others discover this content filtering tool.

---

## Related Keywords

`content filter` · `dns filtering` · `parental control` · `digital wellbeing` · `android device owner` · `windows service blocker` · `cross-platform content blocker` · `privacy focused` · `open source content filter` · `cloudflare family dns` · `self-control app` · `focus tool` · `network security` · `cybersecurity` · `productivity tool`

---

<p align="center">
  <b>Built for focus. Built for freedom.</b><br>
  <sub>CrossPlatform Content Filter — open-source cross-platform DNS filtering and digital wellbeing for Android and Windows</sub>
</p>
