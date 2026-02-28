import os
import subprocess
import sys
import shutil

def run_command(cmd, cwd=None):
    print(f"Running: {' '.join(cmd)}")
    subprocess.check_call(cmd, cwd=cwd)

def build_all():
    print("====================================")
    print(" ULTRA GUARD COMPILE SCRIPT")
    print("====================================\n")

    base_dir = os.path.dirname(os.path.abspath(__file__))
    service_dir = os.path.join(base_dir, "service")
    dist_dir = os.path.join(base_dir, "dist")

    print("[*] Ensuring PyInstaller is available...")
    run_command([sys.executable, "-m", "pip", "install", "pyinstaller"])

    print("\n[*] Compiling main_service.py -> main_service.exe")
    run_command([
        sys.executable, "-m", "PyInstaller",
        "--noconfirm", "--onefile", "--windowed",
        os.path.join(service_dir, "main_service.py")
    ], cwd=base_dir)

    print("\n[*] Compiling watchdog.py -> watchdog.exe")
    run_command([
        sys.executable, "-m", "PyInstaller",
        "--noconfirm", "--onefile", "--windowed",
        os.path.join(service_dir, "watchdog.py")
    ], cwd=base_dir)

    print("\n[*] Compiling install_setup.py -> Install_UltraGuard.exe")
    run_command([
        sys.executable, "-m", "PyInstaller",
        "--noconfirm", "--onefile", 
        os.path.join(base_dir, "install_setup.py")
    ], cwd=base_dir)

    print("\n[*] Copying generated executables into deployment folder...")
    deploy_dir = os.path.join(base_dir, "UltraGuard_Deployment")
    os.makedirs(deploy_dir, exist_ok=True)
    
    shutil.copy2(os.path.join(dist_dir, "main_service.exe"), deploy_dir)
    shutil.copy2(os.path.join(dist_dir, "watchdog.exe"), deploy_dir)
    
    # Rename installer to look professional
    shutil.copy2(os.path.join(dist_dir, "install_setup.exe"), os.path.join(deploy_dir, "Install_UltraGuard.exe"))

    print("\n====================================")
    print(" ✅ COMPILE COMPLETE!")
    print(f" Your final files are ready in: {deploy_dir}")
    print(" You can now upload or share this folder.")
    print(" Users simply need to run 'Install_UltraGuard.exe'.")
    print("====================================\n")

if __name__ == "__main__":
    build_all()
