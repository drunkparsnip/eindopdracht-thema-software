#include <Arduino.h>

#include <WiFi.h>
#include <HTTPClient.h>

#include <time.h>

#include <stdint.h>

#include <SPI.h>

// SPI GPIO pins
#define SPI_MISO 19
#define SPI_MOSI 23
#define SPI_SCK  18
#define SPI_CS   5

// other GPIO pins
#define potentiometer 4

// WIFI credentials
const char* ssid     = "H369A33B136";
const char* password = "22A5E7EA2E2D";

// Server adres
const char* serverUrl = "http://192.168.2.16:8000/metingen";

// time variables
unsigned long last_read  = 0;
unsigned long current_time;

// potentiometer variables
int pot_value;

// WiFi

void wifi_connect() {
    Serial.print("Verbinden met WiFi");
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println();
    Serial.print("Verbonden! IP-adres: ");
    Serial.println(WiFi.localIP());
}

// HTTP POST

void sendData(float sensorWaarde, float meterWaarde) {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi niet verbonden, data niet verstuurd.");
        return;
    }

    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");


    String body = "{\"sensor_waarde\": " + String(sensorWaarde, 4) +
                  ", \"meter_waarde\": "  + String(meterWaarde,  4) + "}";

    Serial.print("Versturen: ");
    Serial.println(body);

    int httpCode = http.POST(body);

    if (httpCode > 0) {
        Serial.print("HTTP response: ");
        Serial.println(httpCode);           
        Serial.println(http.getString());   
    } else {
        Serial.print("HTTP fout: ");
        Serial.println(http.errorToString(httpCode));
    }

    http.end();
}

// spi functions

void spi_master_init() {
    pinMode(SPI_MISO, INPUT);
    pinMode(SPI_MOSI, OUTPUT);
    pinMode(SPI_SCK,  OUTPUT);
    pinMode(SPI_CS,   OUTPUT);

    digitalWrite(SPI_SCK,  LOW);
    digitalWrite(SPI_CS,   HIGH);
    digitalWrite(SPI_MOSI, LOW);
}

uint8_t spi_transfer_byte(uint8_t data) {
    uint8_t received = 0;

    for (int i = 7; i >= 0; i--) {
        digitalWrite(SPI_MOSI, (data & (1 << i)) ? HIGH : LOW);
        delayMicroseconds(5);

        digitalWrite(SPI_SCK, HIGH);
        delayMicroseconds(5);

        if (digitalRead(SPI_MISO)) {
            received |= (1 << i);
        }

        delayMicroseconds(5);
        digitalWrite(SPI_SCK, LOW);
        delayMicroseconds(5);
    }

    return received;
}

float spi_receive_float() {
    uint8_t bytes[4];
    union {
        float   f;
        uint8_t b[4];
    } float_converter;

    digitalWrite(SPI_CS, LOW);
    delayMicroseconds(100);

    for (int i = 0; i < 4; i++) {
        bytes[i] = spi_transfer_byte(0x00);
        delayMicroseconds(500);
    }

    delayMicroseconds(20);
    digitalWrite(SPI_CS, HIGH);

    for (int i = 0; i < 4; i++) {
        float_converter.b[i] = bytes[i];
    }

    return float_converter.f;
}


void getData() {
    if (current_time - last_read >= 500) {

        float sensorWaarde = spi_receive_float();
        float meterWaarde  = analogRead(potentiometer) / 4095.0f * 3.3f;

        Serial.print("Sensor waarde: ");
        Serial.println(sensorWaarde, 6);
        Serial.print("Meter waarde: ");
        Serial.println(meterWaarde, 4);

    
        sendData(sensorWaarde, meterWaarde);

        last_read = current_time;
    }
}

// main code

void setup() {
    Serial.begin(115200);
    delay(1000);

    spi_master_init();
    pinMode(potentiometer, INPUT);

    wifi_connect();
}

void loop() {
    current_time = millis();
    getData();
}