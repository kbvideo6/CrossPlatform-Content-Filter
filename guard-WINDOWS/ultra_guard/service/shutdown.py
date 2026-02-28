import os
import time
import ctypes

def lock_pc():
    print("Locking user session...")
    ctypes.windll.user32.LockWorkStation()

def trigger_violation():
    print("HARDCORE VIOLATION! Commencing escalation ladder...")
    
    for i in range(5, 0, -1):
        print(f"Warning: Disallowed content! Session locking in {i}...")
        time.sleep(1)

    # 1. Lock Workstation (Strongest immediate control)
    try:
        lock_pc()
    except Exception as e:
        print(f"Failed to lock session: {e}")

    # 2. Hardcore approach: Kill browser instances directly
    try:
        os.system("taskkill /IM chrome.exe /F")
        os.system("taskkill /IM firefox.exe /F")
        os.system("taskkill /IM msedge.exe /F")
        print("Browsers terminated.")
        
        # 3. Followed by a system shutdown
        os.system("shutdown /s /t 1")
    except Exception as e:
        print(f"Failed to execute shutdown protocol: {e}")