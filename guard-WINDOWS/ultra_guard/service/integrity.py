import hashlib
import os

# Files inside the service folder
BASE_DIR = os.path.dirname(__file__)
FILES = [
    "main_service.py", 
    "ai_detector.py", 
    "dns_blocker.py", 
    "shutdown.py"
]

def file_hash(path):
    try:
        with open(path, 'rb') as f:
            return hashlib.sha256(f.read()).hexdigest()
    except Exception:
        return None

# Generate initial baseline hashes at startup
BASELINE = {f: file_hash(os.path.join(BASE_DIR, f)) for f in FILES}

def verify_integrity():
    """Returns True if files match baseline, False if tampered."""
    for f in FILES:
        current_hash = file_hash(os.path.join(BASE_DIR, f))
        if current_hash != BASELINE[f]:
            print(f"INTEGRITY VIOLATION DETECTED in {f}")
            return False
    return True

if __name__ == "__main__":
    if verify_integrity():
        print("System integrity verified.")
    else:
        print("Violation found.")
