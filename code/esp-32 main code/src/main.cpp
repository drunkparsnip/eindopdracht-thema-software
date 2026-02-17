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
const char* ssid = "H369A33B136";
const char* password = "22A5E7EA2E2D";

// time variables
unsigned long last_read = 0;
unsigned long current_time;

// potentiometer variables
int pot_value;

// WIFI functions

// SPI functions
void spi_master_init() {
    // Configure GPIO pins
    pinMode(SPI_MISO, INPUT);
    pinMode(SPI_MOSI, OUTPUT);
    pinMode(SPI_SCK, OUTPUT);
    pinMode(SPI_CS, OUTPUT);
    
    // Set initial states
    digitalWrite(SPI_SCK, LOW);    // Clock idle low for mode 0
    digitalWrite(SPI_CS, HIGH);    // CS high (inactive)
    digitalWrite(SPI_MOSI, LOW);
}

uint8_t spi_transfer_byte(uint8_t data) {
    uint8_t received = 0;
    
    for (int i = 7; i >= 0; i--) {
        // Set MOSI bit (MSB first)
        digitalWrite(SPI_MOSI, (data & (1 << i)) ? HIGH : LOW);
        
        // Delay for ATtiny to process
        delayMicroseconds(5);
        
        // Clock rising edge - ATtiny samples MOSI here
        digitalWrite(SPI_SCK, HIGH);
        delayMicroseconds(5);
        
        // Read MISO bit - sample in middle of high period
        if (digitalRead(SPI_MISO)) {
            received |= (1 << i);
        }
        
        delayMicroseconds(5);
        
        // Clock falling edge - ATtiny shifts next bit
        digitalWrite(SPI_SCK, LOW);
        delayMicroseconds(5);
    }
    
    return received;
}

float spi_receive_float() {
    uint8_t bytes[4];
    union {
        float f;
        uint8_t b[4];
    } float_converter;
    
    // Pull CS low to start transmission
    digitalWrite(SPI_CS, LOW);
    delayMicroseconds(100); // Give ATtiny time to detect SS low and prepare data
    
    // Receive 4 bytes and print them as they arrive
    // Receive 4 bytes
    for (int i = 0; i < 4; i++) {
        bytes[i] = spi_transfer_byte(0x00);  // Send dummy byte
        delayMicroseconds(500); // Delay for ATtiny to prepare next byte
    }
    
    // Pull CS high to end transmission
    delayMicroseconds(20);
    digitalWrite(SPI_CS, HIGH);
    
    // Convert bytes to float
    for (int i = 0; i < 4; i++) {
        float_converter.b[i] = bytes[i];
    }
    
    return float_converter.f;
}

void print_float_bytes(float value) {
    uint8_t* bytes = (uint8_t*)&value;
    Serial.print("Float bytes in memory: ");
    for(int i = 0; i < 4; i++) {
        Serial.print("0x");
        if(bytes[i] < 16) Serial.print("0");
        Serial.print(bytes[i], HEX);
        Serial.print(" ");
    }
    Serial.println();
}

void getData() {
       if (current_time - last_read >= 500) {
        //Serial.print("Time: ");
        //Serial.print(current_time / 1000);
        //Serial.println("s");
        
        float received_value = spi_receive_float();
        
        //Serial.println("=================================");
        Serial.print("Received float: ");
        Serial.println(received_value, 6);
        pot_value = analogRead(potentiometer);
        Serial.print("Potentiometer value: ");
        Serial.println(pot_value / 4095.0 * 3.3); 
        
        // Print the bytes as they were received
        //print_float_bytes(received_value);
        
        //Serial.println("=================================");
        //Serial.println();
        
        last_read = current_time;
    }
}
// main code

void setup() {
  Serial.begin(115200);
  delay(1000); // Wait for Serial to initialize
  spi_master_init();
  pinMode(potentiometer, INPUT);
}

void loop() {
    current_time = millis();
    getData();
}