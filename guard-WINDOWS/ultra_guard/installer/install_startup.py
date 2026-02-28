import winreg
import os
import sys

APP_NAME = "UltraGuard"
# We now point to the battery-efficient watchdog rather than main, ensuring it spins up securely.
FILE_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "service", "watchdog.py"))

def install_startup():
    try:
        key = winreg.OpenKey(
            winreg.HKEY_CURRENT_USER,
            r"Software\Microsoft\Windows\CurrentVersion\Run",
            0,
            winreg.KEY_SET_VALUE
        )

        winreg.SetValueEx(key, APP_NAME, 0, winreg.REG_SZ, f'pythonw "{FILE_PATH}"')
        winreg.CloseKey(key)

        print("Startup persistence enabled. Watchdog will auto-launch at boot.")
    except Exception as e:
        print(f"Failed to set startup persistence: {e}")

if __name__ == "__main__":
    install_startup()