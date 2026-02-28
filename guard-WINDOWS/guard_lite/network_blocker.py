import os

# Warning: Modifying the hosts file usually requires administrative/root privileges.

HOSTS_FILE_PATH = r"C:\Windows\System32\drivers\etc\hosts" # Windows path. For Linux/Mac use "/etc/hosts"
REDIRECT_IP = "127.0.0.1"

DOMAINS_TO_BLOCK = [
    "pornhub.com",
    "xvideos.com",
    "www.pornhub.com",
    "www.xvideos.com"
]

def block_sites():
    print("Blocking domains in hosts file...")
    try:
        with open(HOSTS_FILE_PATH, 'r+') as file:
            content = file.read()
            for domain in DOMAINS_TO_BLOCK:
                if domain not in content:
                    file.write(f"{REDIRECT_IP} {domain}\n")
        print("Domains blocked successfully.")
    except PermissionError:
        print("Permission Denied: Please run this script as an Administrator.")
    except Exception as e:
         print(f"An error occurred: {e}")

if __name__ == "__main__":
    block_sites()
