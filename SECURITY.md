# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.x.x   | ✅ Active support   |

## Reporting a Vulnerability

If you discover a security vulnerability in CrossPlatform Content Filter, please report it responsibly:

1. **Do NOT** open a public issue for security vulnerabilities
2. Email the maintainer directly or use GitHub's [private vulnerability reporting](https://github.com/kbvideo6/CrossPlatform-Content-Filter/security/advisories/new)
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Response Timeline

- **Acknowledgment:** Within 48 hours
- **Initial assessment:** Within 1 week
- **Fix release:** Within 2 weeks for critical issues

## Security Design

CrossPlatform Content Filter is designed with security at its core:

- **No network communication** — all processing is local
- **No data collection** — zero telemetry, zero cloud sync
- **SHA-256 integrity verification** — detects file tampering (Windows)
- **Device Owner policies** — OS-level enforcement (Android)
- **Open source** — fully auditable codebase

## Scope

Security issues in the following areas are in scope:

- Bypass methods that circumvent content filtering
- Privilege escalation vulnerabilities
- Integrity check bypass
- Watchdog evasion techniques

## Out of Scope

- Social engineering attacks requiring physical device access
- Bootloader/firmware-level attacks (documented as known limitation)
- Issues in third-party dependencies (report upstream)
