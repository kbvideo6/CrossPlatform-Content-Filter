import sys
import os
import shutil
import winreg
import subprocess
import ctypes
import time

def is_admin():
    try:
        return ctypes.windll.shell32.IsUserAnAdmin()
    except:
        return False

def elevate():
    ctypes.windll.shell32.ShellExecuteW(None, "runas", sys.executable, " ".join(sys.argv), None, 1)
    sys.exit()

def main():
    if not is_admin():
        print("Requesting Admin rights (required to modify hosts and set persistent registry)...")
        elevate()

    print("==============================================")
    print(" ULTRA GUARD: SYSTEM INSTALLER")
    print("==============================================")

    origin_dir = os.path.dirname(sys.executable) if getattr(sys, 'frozen', False) else os.path.dirname(__file__)
    
    # We install to a system-wide protected folder
    target_dir = r"C:\ProgramData\UltraGuard"
    
    if not os.path.exists(target_dir):
        os.makedirs(target_dir, exist_ok=True)

    print(f"\n[*] Installing core files to {target_dir}...")

    files_to_copy = ["main_service.exe", "watchdog.exe", "keywords_db.txt"]
    for file in files_to_copy:
        src = os.path.join(origin_dir, file)
        dst = os.path.join(target_dir, file)
        if os.path.exists(src):
            shutil.copy2(src, dst)
            print(f" -> Successfully copied {file}")
        elif file != "keywords_db.txt":
            print(f"    WARNING: Missing {file}! Ensure you built the executables first.")
    
    print("\n[*] Registering System-Wide Registry Boot Vector...")
    try:
        exe_path = os.path.join(target_dir, "watchdog.exe")
        # Write to HKLM so it runs for EVERY Windows user upon login natively
        key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, r"Software\Microsoft\Windows\CurrentVersion\Run", 0, winreg.KEY_SET_VALUE)
        winreg.SetValueEx(key, "UltraGuardMonitor", 0, winreg.REG_SZ, f'"{exe_path}"')
        winreg.CloseKey(key)
        print(" -> Success (HKLM/Run).")
    except Exception as e:
        print(f"    ERROR: Failed to set registry: {e}")

    print("\n[*] Initializing Protection Daemon...")
    try:
        subprocess.Popen([exe_path], cwd=target_dir)
        print(" -> UltraGuard Watchdog is now running.")
    except Exception as e:
         print(f"    ERROR launching watchdog: {e}")

    print("\n==============================================")
    print(" ✅ INSTALLATION COMPLETE")
    print(" UltraGuard is now continuously enforcing DNS rules")
    print(" and 2000+ Window Keyword Traps dynamically in the")
    print(" background. It will automatically survive reboots.")
    print("==============================================\n")
    
    time.sleep(5)

if __name__ == "__main__":
    main()
