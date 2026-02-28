/*
  LEVEL-OMEGA HARDWARE WATCHDOG
  
  This Arduino sits plugged into a USB port acting as an emulated Hardware Keyboard interface.
  It expects a 'Ping' character ('P') via Serial from the Python `main_service.py` every few seconds.
  
  If the Python program is forcibly terminated by a user via Task Manager, the pinging stops.
  Upon reaching the timeout latency, the Arduino will physically emulate keyboard strokes to
  force a Session Lock on the computer (Win + L) and optionally issue shutdown commands, serving
  as an unstoppable detached guardian.
*/

#include <Keyboard.h>

unsigned long lastPing = 0;
const int WATCHDOG_TIMEOUT = 15000; // 15 seconds threshold

void setup() {
  Serial.begin(9600);
  Keyboard.begin();
  
  // Give the OS 10 seconds to boot and the Python service to start
  delay(10000); 
  lastPing = millis();
}

void loop() {
  // 1. Listen for the Python Service lifeline
  if (Serial.available() > 0) {
    char incoming = Serial.read();
    if (incoming == 'P') { 
      // Alive! Update our heartbeat timestamp
      lastPing = millis();
    }
  }

  // 2. Evaluate Hardware Guardian condition
  if (millis() - lastPing > WATCHDOG_TIMEOUT && lastPing != 0) {
    // WATCHDOG TRIGGERED
    // The Python service has died or was killed manually. Ensure machine is secured.
    
    // Command Windows to Session Lock immediately: Win + L
    Keyboard.press(KEY_LEFT_GUI);
    Keyboard.press('l');
    delay(100);
    Keyboard.releaseAll();
    
    // Halt further commands unless a serial reset happens to avoid spamming the keyboard
    lastPing = millis(); 
  }
}