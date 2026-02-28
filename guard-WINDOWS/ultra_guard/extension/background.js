chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    // Send telemetry to the local python background daemon if URL exists
    if (tab.url) {
        fetch("http://127.0.0.1:5000/check", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            // Transmit both URL and Title to AI intent detector
            body: JSON.stringify({
                url: tab.url, 
                title: tab.title || ""
            })
        }).catch(err => console.log("UltraGuard service unreachable", err));
    }
});