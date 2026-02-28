import pygetwindow as gw
from keywords import BLOCKED_KEYWORDS

def get_active_window_title():
    try:
        window = gw.getActiveWindow()
        if window:
            return window.title.lower()
    except Exception:
        pass
    return ""

def detect_blocked_content():
    title = get_active_window_title()

    for word in BLOCKED_KEYWORDS:
        if word in title:
            return True

    return False
