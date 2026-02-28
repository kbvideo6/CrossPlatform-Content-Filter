# Blocker

## What This Version Detects

✅ Google searches
✅ Browser tab titles
✅ Porn site titles
✅ Incognito windows (still visible title)

Because browsers expose page titles to OS window manager.

## Network-Level Blocking

Modify hosts file:

```
127.0.0.1 pornhub.com
127.0.0.1 xvideos.com
```

Python can auto-update the hosts file.

## DNS Monitoring

Use:
- Pi-hole
- AdGuard DNS
- Local proxy filter

Python watches DNS queries.

## Hardcore Mode (Admin Required)

Python:
- detects browser process
- kills browser instead of shutdown

```python
import os
os.system("taskkill /IM chrome.exe /F")
```

## Improvements You Can Add
- GUI warning popup (Tkinter)
- Sound alarm
- Grace period button
- Daily report
- Password override
- Startup auto-launch

## ⚠️ Important Notes
- This is self-control software, not surveillance.
- Admin rights needed for shutdown.
- Users can bypass unless hardened (hosts/DNS level).
