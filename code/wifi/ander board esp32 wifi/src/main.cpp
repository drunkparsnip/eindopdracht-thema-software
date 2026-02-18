#include <Arduino.h>
#include <WiFi.h>
#include <esp_wifi.h>
#include <time.h>

const char* ssid = "H369A33B136";
const char* password = "22A5E7EA2E2D";

// NTP server and timezone settings
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 3600;        // Amsterdam is UTC+1 (3600 seconds)
const int daylightOffset_sec = 3600;    // DST adds another hour when active

void setup(){
    Serial.begin(115200);
    delay(1000);

    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    Serial.print("\nConnecting to: ");
    Serial.println(ssid);

    while(WiFi.status() != WL_CONNECTED){
        Serial.print(".");
        delay(500);
    }

    Serial.println("\nConnected to the WiFi network");
    Serial.print("Local ESP32 IP: ");
    Serial.println(WiFi.localIP());

    // Initialize and configure time
    configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
    Serial.println("Waiting for time synchronization...");
    
    // Wait for time to be set
    struct tm timeinfo;
    while(!getLocalTime(&timeinfo)){
        Serial.print(".");
        delay(500);
    }
    Serial.println("\nTime synchronized!");
}

void loop(){
    struct tm timeinfo;
    if(getLocalTime(&timeinfo)){
        // Print time in readable format
        Serial.print("Amsterdam Time: ");
        Serial.print(&timeinfo, "%A, %B %d %Y %H:%M:%S");
        Serial.println();
    } else {
        Serial.println("Failed to obtain time");
    }
    
    delay(1000); // Wait 1 second
}