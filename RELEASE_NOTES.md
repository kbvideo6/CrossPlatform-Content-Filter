## What's New in v2.0.0

### Dark Theme Dashboard
- 6-card dark UI with midnight blue/charcoal theme
- Emerald green (active), amber (warning), soft red (critical) accents
- Shield glow animation for protection status
- Network traffic summary and full system info

### Multi-DNS Failover
- 4 family-friendly DNS servers: Cloudflare Family, CleanBrowsing, AdGuard Family, Quad9
- Automatic health-check with port 853 connectivity test
- Instant fallback when primary DNS goes down

### Instant DNS Tamper Detection
- ContentObserver-based foreground service detects DNS changes in real-time
- No more 15-minute polling delay — changes are caught and reversed instantly

### Developer Options Auto-Disable
- Automatically turns off developer options after ADB provisioning
- Checked on every watchdog cycle and boot

### Custom Domain Blocking
- Add custom domains to block via the dashboard
- 21-day mandatory lock period before a domain can be unblocked
- Subdomain matching included

### Push Notifications
- Blocked access notifications (stacking, with domain info)
- Escalation warning notifications
- Critical event notifications (device lock)

### Other
- Network traffic dashboard (TrafficStats API)
- Full block history activity with timestamps
- Adaptive launcher icon from brand SVG
- Version bumped to 2.0.0-omega (versionCode 2)

## Download

Download **OmegaLite-v2.0.0-debug.apk** below and sideload via ADB:

```
adb install -r OmegaLite-v2.0.0-debug.apk
```
