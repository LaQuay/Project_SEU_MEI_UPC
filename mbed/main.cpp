// Practica SEU

// Robert Carausu, Francesc de Puig, Marc Vila

//https://www.sparkfun.com/datasheets/Sensors/ID-12-Datasheet.pdf
//https://os.mbed.com/components/ID12-RFID-Reader/
//http://slab.concordia.ca/2008/arduino/rfid/

#include "mbed.h"
#include "ID12RFID.h"
#include "rtos.h"
Timer timer;
Serial hm10(p9,p10); //TX, RX
Serial pc(USBTX, USBRX);
DigitalOut led_L1(LED1);
DigitalOut led_L2(LED2);
DigitalOut led_L3(LED3);
DigitalOut led_L4(LED4);

PwmOut pwmout(p21);
PwmOut pwmout2(p22);  

void setUpBLE() {
    hm10.baud(9600); //make sure the baud rate is 9600
    while (!hm10.writeable()) { } //wait until the HM10 is ready
    
    // SET - Posem el nom a PEPITOBLE
    // MAC: 00:15:83:00:8B:4D
    hm10.printf("AT+NAME PEPITOBLE\r\n");
    printf("Name setup done: PEPITOBLE\n");
}


ID12RFID rfid(p14); // uart rx

unsigned int chartohex(char c) {
    if (c >= '0' && c <= '9') {
        return c - '0';
    } else {
        return c - '@' + 9;
    }
}

char hextochar(int i) {
    if (i > 9) {
        return i + '@' - 9;
    }
    
    return (char)i + '0';
}

void turnLed1OffIn2Sec(){//void const *args) {
    Thread::wait(2000);//free CPU
    led_L1 = false;
}

void controllOpen(){
    led_L2 = true;
    timer.reset();
    char read = '-';
    bool blink=true;
    while(read=='-')
    {
            if (hm10.readable())
            {
                printf("Read!\n");
                read=hm10.getc();
                if(read=='1')
                {
                    pwmout.write(0.99f);
                    led_L4=true;
                }
                else
                {
                    pwmout2.write(0.99f);
                    led_L3=true;
                }
            }
            if(timer.read()>10){
                read='0';
            }
            if((timer.read_ms()%500)>250){
                if(blink)
                {
                    led_L2 = !led_L2;
                }
                blink=false;
            }
            else{
                blink=true;
            }
    }
    led_L2 = false;
    printf("Exiting!\n");
    while(timer.read()<12 && (led_L4||led_L3));
    printf("Done!\n");
    pwmout.period(0.01f);
    pwmout2.period(0.01f);
    led_L4 = false;
    led_L3 = false;
}

int main() {
    printf("Lets use threads!!!\n");
    printf("Hello World\n");
    timer.start();
    setUpBLE();
    // ROLE0 slave
    // ROLE1 master
    hm10.printf("AT+ROLE0\r\n");
    hm10.printf("AT+INQ");
    while(1) {
        if(rfid.readable()) {
            rfid.read();  
            
            printf("data: ");
            for (int i = 9; i >= 0; --i) {
                printf("%c", rfid.data[i]);
            }
            printf("\n");          
            
            printf("checksum: ");
            for (int i = 1; i >= 0; --i) {
                printf("%c", rfid.checksum[i]);
            }
            printf("\n");
            
            int odd = chartohex(rfid.data[9]);
            int even = chartohex(rfid.data[8]);

            for (int i = 7; i >= 0; --i) {
                if (i % 2 == 0) {
                    even ^= chartohex(rfid.data[i]);
                } else {
                    odd ^= chartohex(rfid.data[i]);                    
                }
            }
            
            printf("checksum calculated: %c%c\n", hextochar(odd), hextochar(even));
            
            if(rfid.checksum[0] == hextochar(even) && rfid.checksum[1] == hextochar(odd)){
                led_L1 = true;
                Thread thread(turnLed1OffIn2Sec, osPriorityRealtime);
                printf("Lectura correcta! Los checksum son iguales.\n");
                for (int i = 9; i >= 0; --i) {
                    hm10.putc(rfid.data[i]);
                }
                Thread thread2(controllOpen, osPriorityRealtime);
            } else {
                printf("Error! Los checksum no son iguales\n");  
                 for (int i = 9; i >= 0; --i) {
                    hm10.putc('X');
                }
            }
        } 
         
        if (hm10.readable())
        {
            pc.putc(hm10.getc());
        }
        
        if (pc.readable())
        {
            hm10.putc(pc.getc());                    
        }
    }
}
