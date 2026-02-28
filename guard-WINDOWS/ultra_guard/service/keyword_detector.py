import os
import sys
import urllib.request

# Core explicit targets
BLOCKED_KEYWORDS = {
    "porn", "xvideos", "pornhub", "xnxx", "xhamster", "onlyfans", 
    "chaturbate", "spankbang", "rule34", "camgirls", "nsfw", "nude",
    "redtube", "youporn", "tube8", "eporner", "bitchute", "stripchat"
}

# Calculate persistent installation path vs standard python path
BASE_DIR = os.path.dirname(sys.executable) if getattr(sys, 'frozen', False) else os.path.dirname(__file__)

# Cache file for extensive 2000+ string database
DB_FILE = os.path.join(BASE_DIR, "keywords_db.txt")

# Using a robust open-source GitHub repository containing over 2,500 explicit/banned words
BLOCKLIST_URL = "https://raw.githubusercontent.com/LDNOOBW/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/master/en"

def init_database():
    global BLOCKED_KEYWORDS
    
    # On first startup, download the massive list of keywords to a local txt file
    if not os.path.exists(DB_FILE):
        print("Downloading extreme keyword database (2000+ words)...")
        try:
            urllib.request.urlretrieve(BLOCKLIST_URL, DB_FILE)
        except Exception as e:
            print(f"Failed to fetch remote DB, falling back to core list: {e}")
            return
            
    # Parse the downloaded file into our quick-lookup Set
    try:
        with open(DB_FILE, 'r', encoding='utf-8') as f:
            extra = f.read().splitlines()
            for word in extra:
                clean_word = word.strip().lower()
                if len(clean_word) > 3: # Ignore 1-3 char combinations to prevent false-positives
                    BLOCKED_KEYWORDS.add(clean_word)
        print(f"Active Guardian Protocol: Monitoring against {len(BLOCKED_KEYWORDS)} explicit keywords.")
    except Exception as e:
        print(f"Failed to read keyword database: {e}")

# Build DB automatically when service starts
init_database()

def is_explicit(text):
    if not text:
        return False
        
    text_lower = text.lower()
    
    # We use purely direct substring matching for zero-cost performance
    for word in BLOCKED_KEYWORDS:
        if word in text_lower:
            return True
            
    return False
