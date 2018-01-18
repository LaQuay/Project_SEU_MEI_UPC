#include "ID12RFID.h"

ID12RFID::ID12RFID(PinName rx)
        : _rfid(NC, rx) {
}

int ID12RFID::readable() {
    return _rfid.readable();
}
 
char* ID12RFID::read() { 
    _rfid.getc(); // flush STX
    
    //data[10] = "";
 
    for (int i=9; i>=0; i--) {
        char c = _rfid.getc();
        data[i] = c;
    }
    
    //checksum[2] = "";
    
    checksum[1] = _rfid.getc();
    checksum[0] = _rfid.getc();
    
    _rfid.getc(); // flush CR
    _rfid.getc(); // flush LF
    _rfid.getc(); // flush ETX   
    
    return 0;
}

short int ID12RFID::read_checksum() {
    while(_rfid.getc() != 2);
    
    return 0;   
}