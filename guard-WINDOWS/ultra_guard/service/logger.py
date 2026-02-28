import json
import os
import requests
import sys
from datetime import datetime

# Calculate persistent installation path vs standard python path
BASE_DIR = os.path.dirname(sys.executable) if getattr(sys, 'frozen', False) else os.path.dirname(__file__)

# Place log inside the directory securely
FILE = os.path.join(BASE_DIR, "log.json")

# Set to your remote analytics server endpoint.
REMOTE_DASHBOARD_WEBHOOK = "https://yourserver.com/log" 

def log_event(url):
    data = []

    try:
        if os.path.exists(FILE):
            with open(FILE, "r") as f:
                data = json.load(f)
    except Exception as e:
        print(f"Warning: Could not read existing log file: {e}")
        pass

    event_payload = {
        "date": str(datetime.now().date()),
        "time": str(datetime.now().time()),
        "trigger": url,
        "attempts": len(data) + 1
    }

    data.append(event_payload)

    # 1. Local Logging
    try:
        with open(FILE, "w") as f:
            json.dump(data, f, indent=2)
    except Exception as e:
         print(f"Warning: Could not write to log file: {e}")
         
    # 2. Remote Accountability Logging
    try:
        requests.post(
           REMOTE_DASHBOARD_WEBHOOK,
           json={"event": "violation", "details": event_payload},
           timeout=3
        )
    except requests.exceptions.RequestException as e:
        print(f"Failed to transmit to remote dashboard: {e}")
