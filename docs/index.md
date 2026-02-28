---
layout: default
title: CrossPlatform Content Filter
---

# CrossPlatform Content Filter

**Cross-platform content filtering and digital wellbeing system for Android and Windows.**

CrossPlatform Content Filter is a privacy-focused, open-source content filter that blocks adult content at the OS and DNS level. It uses DNS filtering, Device Owner policies, and system-level enforcement to create a safe, distraction-free browsing environment.

## Key Features

- **DNS-level content filtering** using Cloudflare Family DNS
- **Cross-platform** — Android (Kotlin) and Windows (Python)
- **Device Owner enforcement** — locked DNS, anti-uninstall, anti-factory-reset
- **Windows Service architecture** — survives task manager kills and reboots
- **Privacy-first** — zero telemetry, zero cloud sync, fully open source

## Get Started

- [View on GitHub](https://github.com/kbvideo6/CrossPlatform-Content-Filter)
- [Android Setup Guide](https://github.com/kbvideo6/CrossPlatform-Content-Filter/tree/main/guard-ANDROID)
- [Windows Setup Guide](https://github.com/kbvideo6/CrossPlatform-Content-Filter/tree/main/guard-WINDOWS/ultra_guard)

## How It Works

### Android
The Device Owner API locks system-wide Private DNS to Cloudflare Family (`family.cloudflare-dns.com`). All DNS queries across all apps are filtered. The user cannot change DNS settings, uninstall the app, or factory reset without reflashing firmware.

### Windows
A persistent Windows Service monitors the hosts file and active window titles. DNS poisoning redirects blocked domains to 127.0.0.1. A crash-guard watchdog restarts killed processes within 2 seconds. SHA-256 integrity checks detect file tampering.

## Privacy

- No data collection
- No cloud services
- No screen reading
- No telemetry
- Fully auditable open-source code

---

*Built for focus. Built for freedom.*
