import os
from config import HARDCORE_MODE

def shutdown_pc():
    if HARDCORE_MODE:
        print("Hardcore Mode: Killing browser...")
        os.system("taskkill /IM chrome.exe /F")
        # Add other browsers as needed:
        # os.system("taskkill /IM firefox.exe /F")
        # os.system("taskkill /IM msedge.exe /F")
    else:
        print("Shutting down system...")
        os.system("shutdown /s /t 1")
