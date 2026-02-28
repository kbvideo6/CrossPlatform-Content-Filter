import time
from shutdown import shutdown_pc
from monitor import detect_blocked_content

def start_countdown(seconds):
    for i in range(seconds, 0, -1):
        if not detect_blocked_content():
            print("\nSafe content detected. Aborting shutdown.")
            return

        print(f"Quit the site! Shutdown in {i} seconds...")
        time.sleep(1)

    # Final check just in case
    if detect_blocked_content():
        shutdown_pc()
