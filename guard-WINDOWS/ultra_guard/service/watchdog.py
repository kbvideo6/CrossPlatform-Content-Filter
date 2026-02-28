import psutil
import subprocess
import time
import os
import sys

# Detect if we are running as a normal python script or a compiled `.exe`
IS_COMPILED = getattr(sys, 'frozen', False)
TARGET = "main_service.exe" if IS_COMPILED else "main_service.py"

if IS_COMPILED:
    # If compiled, the executables will be right next to each other
    TARGET_PATH = os.path.join(os.path.dirname(sys.executable), TARGET)
else:
    TARGET_PATH = os.path.join(os.path.dirname(__file__), TARGET)

MAX_RETRIES = 3
# If it fails 3 times within 30 seconds, consider it a deliberate bypass or critical fault
RETRY_WINDOW = 30  

def ensure_running():
    # Only print if we're not compiled (when compiled to windowed, prints fail)
    if not IS_COMPILED:
        print(f"Watchdog started. Monitoring for {TARGET}...")

    restart_times = []
    
    while True:
        running = False

        for p in psutil.process_iter(['name', 'cmdline']):
            try:
                name = p.info.get('name') or ""
                cmdline = p.info.get('cmdline') or []
                cmd_str = " ".join(cmdline)
                
                if TARGET in name or TARGET in cmd_str:
                    running = True
                    break
            except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
                pass

        if not running:
            current_time = time.time()
            
            # Prune restart points that are older than our retry window
            restart_times = [t for t in restart_times if (current_time - t) < RETRY_WINDOW]
            
            if len(restart_times) >= MAX_RETRIES:
                # We attempt to restart it. If it's not successful (fails rapidly 3x), shut down the computer.
                os.system("shutdown /s /t 1")
                break # Exit watchdog to trigger shutdown

            # Register this failure and restart
            restart_times.append(current_time)
            
            if IS_COMPILED:
                subprocess.Popen([TARGET_PATH], cwd=os.path.dirname(TARGET_PATH))
            else:
                subprocess.Popen(["python", TARGET_PATH], cwd=os.path.dirname(TARGET_PATH))

        # Sleep for 5 seconds to ensure perfect battery efficiency
        time.sleep(5)

if __name__ == "__main__":
    ensure_running()