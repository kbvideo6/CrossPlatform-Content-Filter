import win32serviceutil
import win32service
import win32event
import servicemanager
import subprocess
import os

class UltraGuardService(win32serviceutil.ServiceFramework):
    _svc_name_ = "UltraGuardService"
    _svc_display_name_ = "Ultra Guard Protection"

    def __init__(self, args):
        win32serviceutil.ServiceFramework.__init__(self, args)
        self.stop_event = win32event.CreateEvent(None, 0, 0, None)

    def SvcStop(self):
        self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
        win32event.SetEvent(self.stop_event)

    def SvcDoRun(self):
        servicemanager.LogInfoMsg("UltraGuard started")
        
        # Path to watchdog.py relative to this script
        service_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'service')
        watchdog_path = os.path.join(service_dir, 'watchdog.py')
        
        # Start the Watchdog (which cascades to main_service)
        process = subprocess.Popen(["python", watchdog_path], cwd=service_dir)
        
        # Wait for service stop signal
        win32event.WaitForSingleObject(self.stop_event, win32event.INFINITE)
        
        # Cleanup
        process.terminate()

if __name__ == '__main__':
    win32serviceutil.HandleCommandLine(UltraGuardService)
