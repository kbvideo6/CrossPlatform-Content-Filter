import os
import subprocess

# Domains to block OUTBOUND via Firewall
BLOCK_LIST = [
    "pornhub.com",
    "xvideos.com",
    "xnxx.com",
    "xhamster.com"
]

def check_rule_exists(domain):
    rule_name = f"block_{domain}"
    result = subprocess.run(
        f'netsh advfirewall firewall show rule name="{rule_name}"', 
        capture_output=True, text=True, shell=True
    )
    return "No rules match the specified criteria" not in result.stdout

def apply_firewall_rules():
    print("Enforcing firewall rules...")
    for domain in BLOCK_LIST:
        try:
            if not check_rule_exists(domain):
                print(f"Adding firewall rule for: {domain}")
                os.system(
                    f'netsh advfirewall firewall add rule '
                    f'name="block_{domain}" dir=out action=block '
                    f'remoteip={domain}'
                )
            else:
                 print(f"Firewall rule already active for: {domain}")
        except Exception as e:
            print(f"Failed to enforce firewall rule for {domain}: {e}")

if __name__ == "__main__":
    apply_firewall_rules()