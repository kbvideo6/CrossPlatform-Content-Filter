import time
import win32gui
from keyword_detector import is_explicit

def get_active_window_title():
    try:
        hwnd = win32gui.GetForegroundWindow()
        title = win32gui.GetWindowText(hwnd)
        return title
    except Exception:
        return ""

print("---------------------------------------------------------")
print("⚠️ ULTRA GUARD: SAFE TESTING MODE ⚠️")
print("This script simulates the background daemon, but WILL NOT ")
print("lock your computer or format DNS. It only prints outputs.")
print("---------------------------------------------------------\n")

print("Initializing Keyword Database...")
# It will load or download the keywords text file upon import.

print("\nStarting Window Monitor (Press CTRL+C to stop)...")
last_title = ""

while True:
    try:
        current_title = get_active_window_title()
        
        # Only print when we click to a new window so the console isn't spammed
        if current_title != last_title and current_title.strip() != "":
            print(f"[OS Title] -> {current_title}")
            
            # Check the title text against our 2000+ string payload
            if is_explicit(current_title):
                print(f"   🚨 VIOLATION DETECTED! 🚨")
                print(f"   (Real script would now lock the session and terminate the browser)")
                print("   -------------------------------------------------")
                
            last_title = current_title

        time.sleep(1)
        
    except KeyboardInterrupt:
        print("\nTest stopped.")
        break
