import time
import win32gui
from keyword_detector import is_explicit
from shutdown import trigger_violation
from dns_blocker import enforce_hosts
from logger import log_event
import threading

def get_active_window_title():
    try:
        hwnd = win32gui.GetForegroundWindow()
        title = win32gui.GetWindowText(hwnd)
        return title
    except Exception:
        return ""

def window_monitor_loop():
    print("Zero-cost window title monitoring active...")
    while True:
        title = get_active_window_title()
        
        if title and is_explicit(title):
            print(f"Violation detected in window title: {title}")
            
            # Log the incident for accountability
            log_event(title)

            # Trigger violation in separate thread to prevent loop blocking
            threading.Thread(target=trigger_violation).start()
            
            # Sleep a bit longer after detection to allow shutdown script time to run
            time.sleep(5)
            
        time.sleep(1) # Check exactly once a second, extremely battery efficient

if __name__ == "__main__":
    print("Enforcing system protections...")
    
    # 1. First defense layer: DNS-Level Blocking (Updates Host File)
    enforce_hosts()
    
    # Run the window listener loop locally
    window_monitor_loop()