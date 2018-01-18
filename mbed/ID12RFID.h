#ifndef MBED_ID12RFID_H
#define MBED_ID12RFID_H
 
#include "mbed.h"

class ID12RFID {
 
public:
    /** Create an ID12 RFID interface, connected to the specified Serial rx port
     *
     * @param rx Recieve pin 
     */
    ID12RFID(PinName rx);
 
    /** Non blocking function to determine if an ID has been received
     *
     * @return Non zero value when the device is readable
     */
    int readable();    
    
    /** A blocking function that will return a tag ID when available
     *
     * @return The ID tag value
     */
    char* read();
    
    short int read_checksum();
    
    char data[10];
    char checksum[2];
 
private:
    Serial _rfid; 
};

#endif