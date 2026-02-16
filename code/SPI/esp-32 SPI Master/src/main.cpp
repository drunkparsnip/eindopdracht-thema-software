#include <stdint.h>
#include <Arduino.h>
#include <SPI.h>

// SPI GPIO pins
#define SPI_MISO 19
#define SPI_MOSI 23
#define SPI_SCK  18
#define SPI_CS   5

// Function declarations
void spi_master_init();
uint8_t spi_transfer_byte(uint8_t data);
float spi_receive_float();
void print_float_bytes(float value);

void setup() {
    Serial.begin(115200);
    delay(100); // Give serial time to initialize
    spi_master_init();
    Serial.println("SPI Master initialized");
    Serial.println("Waiting for data from ATtiny...");
    delay(1000); // Give ATtiny time to initialize
}

void loop() {
    static unsigned long last_read = 0;
    unsigned long current_time = millis();
    

    if (current_time - last_read >= 500) {
        Serial.print("Time: ");
        Serial.print(current_time / 1000);
        Serial.println("s");
        
        float received_value = spi_receive_float();
        
        Serial.println("=================================");
        Serial.print("Received float: ");
        Serial.println(received_value, 6);
        
        // Print the bytes as they were received
        print_float_bytes(received_value);
        
        Serial.println("=================================");
        Serial.println();
        
        last_read = current_time;
    }
}

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
    Serial.print("Raw bytes received: ");
    for (int i = 0; i < 4; i++) {
        bytes[i] = spi_transfer_byte(0x00);  // Send dummy byte
        Serial.print("0x");
        if(bytes[i] < 16) Serial.print("0");
        Serial.print(bytes[i], HEX);
        Serial.print(" ");
        delayMicroseconds(20); // Delay between bytes for ATtiny to prepare next byte
    }
    Serial.println();
    
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