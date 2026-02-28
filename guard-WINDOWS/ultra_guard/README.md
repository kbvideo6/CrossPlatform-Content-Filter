# UltraGuard: Lightweight Extreme Architecture

UltraGuard is a severe, OS-level self-control architecture, engineered for maximum performance without dependencies like browsers, AI models, or massive background processes.

**Its goal:** To be completely lightweight, consuming near zero PC resources while being extremely difficult to bypass.

## ⚠️ Design Philosophy

Most blockers fail, yet standard parental-control systems bloat up computers. This build is completely zero-cost against system tasks:
- **No AI Processing:** AI loops require RAM & GPU. We use strict string iteration logic.
- **No Browser Extensions:** Does not matter if the user accesses Google Chrome, Firefox, Tor, or even renames their app executable. `Win32GUI` captures the active window data directly from the system graphics rendering layer.

## How It Works

```text
Host DNS Blocking (Localhost traps)
        ↓
Windows Integrity Service (Startup + Privileges)
        ↓
Win32GUI OS Title Interceptor (Low impact polling)
        ↓
Crash-Guard Watchdog
        ↓
Session Lock / Forced Shutdown
```
                     
## EXTREME FEATURES SUMMARY

| Protection | Stops | Technology Used |
| :--- | :--- | :--- |
| **OS Window Title Polling** | renamed browser/tab bypass | `win32gui` |
| **Fail-Safe Watchdog** | task manager kill (x3) | `psutil` + `subprocess` |
| **DNS Poisoning** | direct port routing | Write to `etc\hosts` |
| **Windows Service** | process termination | PyWin32 `service_installer.py` |
| **Self-Integrity** | file tampering | SHA-256 baselines |
| **Session Lock** | ignoring shutdown warnings | `ctypes.windll.user32.LockWorkStation()` |

## Setup Instructions

1.  **Install Base Requirements:** Open an administrator prompt and run `pip install -r requirements.txt`.
2.  **Enable System Service:** Run `python service_installer.py install` and then `python service_installer.py start`. This ensures it cascades your watchdog and service securely.
3.  **Boot Persistent Registration:** Run `installer/install_startup.py` to write the watchdog to standard CurrentUser Windows Boot.