#include <Arduino.h>
#include <WiFi.h>
#include <esp_wifi.h>

const char* ssid = "H369A33B136";
const char* password = "22A5E7EA2E2D";

void setup(){
    Serial.begin(115200);
    delay(1000);

    WiFi.mode(WIFI_STA); //Optional
    esp_wifi_set_max_tx_power(50); // 50 = 12.5dBm, default is 78 (19.5dBm)
    WiFi.begin(ssid, password);
    WiFi.begin(ssid, password);
    Serial.println("\nConnecting");

    while(WiFi.status() != WL_CONNECTED){
        Serial.print(".");
        delay(100);
    }

    Serial.println("\nConnected to the WiFi network");
    Serial.print("Local ESP32 IP: ");
    Serial.println(WiFi.localIP());
}

void loop(){}