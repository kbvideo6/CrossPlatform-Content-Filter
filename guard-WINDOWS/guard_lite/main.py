import time
from monitor import detect_blocked_content
from countdown import start_countdown
from config import CHECK_INTERVAL, COUNTDOWN_SECONDS

def main():
    print("Porn blocker running...")

    while True:
        if detect_blocked_content():
            print("Blocked content detected!")
            start_countdown(COUNTDOWN_SECONDS)

        time.sleep(CHECK_INTERVAL)

if __name__ == "__main__":
    main()
