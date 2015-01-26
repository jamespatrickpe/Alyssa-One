#include <Servo.h>
#include <Wire.h>           // I2C library, gyroscope
//--------------Gyroscope ITG3200--------------------------------------------- 
#define GYRO 0x68           //  when AD0 is connected to GND ,gyro address is 0x68.
//#define GYRO 0x69   when AD0 is connected to VCC ,gyro address is 0x69  
#define G_SMPLRT_DIV 0x15
#define G_DLPF_FS 0x16
#define G_INT_CFG 0x17
#define G_PWR_MGM 0x3E
#define G_TO_READ 8         // 2 bytes for each axis x, y, z
// offsets are chip specific. 
int g_offx = 120;
int g_offy = 20;
int g_offz = 93;
int hx, hy, hz, turetemp;
//--------------ADXL345 accelorometer---------------------------------------
// Pin usage, change assignment if you want to
const byte spiclk=A0;   // connect to ADXL CLK
const byte spimiso=A1;  // connect to ADXL DO
const byte spimosi=A2;  // connect to ADXL DI
const byte spics=A3;    // connect to ADXL CS
// Don't forget, connect ADXL VDD-GND to gizDuino/Arduino +3.3V-GND
byte  xyz[8];           // raw data storage
int x,y,z;              // x, y, z accelerometer data
byte spiread;
//--------------------------------------------------------------------------
String strRx0 = "";                // Serial0 Rx buffer
boolean bRxEnd0 = false;                  // End of events
String strRx1 = "";               // Serial1 Rx buffer
boolean bRxEnd1 = false;                  // End of events
String strRx2 = "";               // Serial2 Rx buffer
boolean bRxEnd2 = false;                  // End of events
String strRx3 = "";                // Serial3 Rx buffer
boolean bRxEnd3 = false;                  // End of events

boolean bGPSdat = false;                  // Enabled Read GPS
char CompassVal[6]={'\0'};
char strLat[15]={'\0'};
char strLong[15]={'\0'};

boolean GPSorCompass = true;              // true GPS / false DIgital Compass
boolean bSTX = false;                     // End Read Compass Rx
int r = 0;
int g = 0;
int bDC_GPS = 0;
int decval = 0;

unsigned long previousMillis = 0;         // will store last time LED was updated
const long interval = 700;               // interval at which to blink (milliseconds)

Servo myservo1, myservo2, myservo3;

int pos = 0;

int GSMpwrPin = 2;
int Servo1Pin = 4;
int Servo2Pin = 5;
int Servo3Pin = 6;
int EnbDCpPin = 8;
int EnbGPSPin = 9;

//initializes the gyroscope
void initGyro()
{
    writeTo(GYRO, G_PWR_MGM, 0x00);
    writeTo(GYRO, G_SMPLRT_DIV, 0x07);    // EB, 50, 80, 7F, DE, 23, 20, FF
    writeTo(GYRO, G_DLPF_FS, 0x1E);       // +/- 2000 dgrs/sec, 1KHz, 1E, 19
    writeTo(GYRO, G_INT_CFG, 0x00);
}
void getGyroscopeData(int * result)
{
    int regAddress = 0x1B;
    int temp, x, y, z;
    byte buff[G_TO_READ];
    readFrom(GYRO, regAddress, G_TO_READ, buff);         //read the gyro data from the ITG3200
    result[0] = ((buff[2] << 8) | buff[3]) + g_offx;
    result[1] = ((buff[4] << 8) | buff[5]) + g_offy;
    result[2] = ((buff[6] << 8) | buff[7]) + g_offz;
    result[3] = (buff[0] << 8) | buff[1];               // temperature
}
void setup()
{
    strRx0.reserve(60);
    strRx1.reserve(100);
    strRx2.reserve(80);
    strRx3.reserve(60);
    pinMode(GSMpwrPin, OUTPUT);
    pinMode(Servo1Pin, OUTPUT);
    pinMode(EnbDCpPin, OUTPUT);
    pinMode(EnbGPSPin, OUTPUT);
    Serial.begin(9600);
    Serial1.begin(9600);          // GSM Shield   
    Serial2.begin(9600);          // GPS Shield   
    Serial3.begin(9600);          // RF module 433
    Wire.begin();
    
    myservo1.attach(Servo1Pin); 
    myservo2.attach(Servo2Pin);
    myservo3.attach(Servo3Pin);
    digitalWrite(EnbDCpPin, HIGH);
    digitalWrite(EnbGPSPin, HIGH);
    digitalWrite(GSMpwrPin, HIGH);
    delay(500);
    digitalWrite(GSMpwrPin, LOW);
    
    //initGSM();
    init_adxl();            // initialize ADXL345 accelorometer
    initGyro();             // initialize Gyroscope ITG3200
    for(int i=0; i<60; i++) { strRx2[i]=0; }
    delay(1000);
}
void loop()
{  
    sensorStatus();
    SerialStatus();
}
void sensorStatus()
{    
    unsigned long currentMillis = millis();
    if(currentMillis - previousMillis >= interval) {
      previousMillis = currentMillis; 
            
      bDC_GPS++;
      if(bDC_GPS == 1) GPSorCompass = false;       // enabled Compass Rx Reading
      else if(bDC_GPS == 2) GPSorCompass = true;   // enabled GPS Rx Reading
      else if(bDC_GPS == 3) alternateReading();    // send data packet to RF
      else if(bDC_GPS == 4) {
        for (int i=0; i<6; i++) { CompassVal[i] = 0; }
        bDC_GPS = 0;
      }
    }
}
void alternateReading()
{
    String strDC="";
    strDC.reserve(15);

    accgyroSensor(); 
    strDC = ";dc="; strDC += CompassVal; strDC += ";GPS=";
    Serial3.print(strDC);  
    Serial3.print(strLat); 
    Serial3.print("-");
    Serial3.println(strLong);   
}
void accgyroSensor()
{
    byte addr;
    int gyro[4];
    String DOFdata="";
    DOFdata.reserve(60);
    getGyroscopeData(gyro);
    hx = gyro[0] / 14.375;
    hy = gyro[1] / 14.375;
    hz = gyro[2] / 14.375;
    turetemp = 35+ ((double) (gyro[3] + 13200)) / 280; // temperature

    DOFdata = "gX=";   DOFdata += hx;
    DOFdata += ";gY="; DOFdata += hy;
    DOFdata += ";gZ="; DOFdata += hz;
    DOFdata += ";gF="; DOFdata += turetemp;
   
    read_xyz();                          // read ADXL345 accelerometer
    DOFdata += ";Ax="; DOFdata += x;
    DOFdata += ";Ay="; DOFdata += y;
    DOFdata += ";Az="; DOFdata += z;
    Serial3.print(DOFdata);
}
//Writes val to address register on ACC
void writeTo(int DEVICE, byte address, byte val) 
{
    Wire.beginTransmission(DEVICE);     //start transmission to ACC 
    Wire.write(address);                // send register address
    Wire.write(val);                    // send value to write
    Wire.endTransmission();             //end transmission
}
//reads num bytes starting from address register on ACC in to buff array
void readFrom(int DEVICE, byte address, int num, byte buff[]) {
    Wire.beginTransmission(DEVICE);     //start transmission to ACC 
    Wire.write(address);                //sends address to read from
    Wire.endTransmission();             //end transmission
    Wire.beginTransmission(DEVICE);     //start transmission to ACC
    Wire.requestFrom(DEVICE, num);      // request 6 bytes from ACC
    int i = 0;
    while(Wire.available())             //ACC may send less than requested (abnormal)
    { 
      buff[i] = Wire.read();            // receive a byte
      i++;
    }
    Wire.endTransmission();             //end transmission
}
void spi_out(byte spidat){
  byte bitnum=8;
    spiread=0;
    // start spi bit bang
    while(bitnum>0){
      pinMode(spiclk,OUTPUT);                          // SPI CLK =0
      if((spidat & 0x80)!=0) pinMode(spimosi,INPUT);   // MOSI = 1 if MSB =1
      else pinMode(spimosi,OUTPUT);                    // else MOSI = 0
      spidat=spidat<<1; 
      pinMode(spiclk,INPUT);                          // SPI CLK = 1
      // read spi data
      spiread=spiread<<1;
      if(digitalRead(spimiso)==HIGH) spiread |= 0x01; // shift in a 1 if MISO is 1
      pinMode(spimosi,INPUT);                         // reset MOSI to 1
      bitnum--; 
    } 
}
void  init_adxl(void)
{
    delay(250);
    pinMode(spics,OUTPUT);  // CS=0     
    spi_out(0x31);          //Write to register 0x31, DATA FORMAT
    // uncomment your desired range
    spi_out(0x0B);          //full resolution, +/- 16g range//spi_out(0x0A);//full resolution, +/- 8g range//spi_out(0x09);//full resolution, +/- 4g range//spi_out(0x08);//full resolution, +/- 2g range  
    pinMode(spics,INPUT);   //CS HIGH
    delay(1);
    pinMode(spics,OUTPUT);  // CS=0   
    spi_out(0x2d);          // Write to register 0x2d, POWER_CTL
    spi_out(0x08);          //set to measure mode
    pinMode(spics,INPUT);   //CS HIGH
    delay(1);
}
void read_xyz(void)         //Read all 3 axis x,y,z
{
    int i;
    pinMode(spics,OUTPUT);  // CS=0   
    spi_out(0xF2);          // dump xyz content to array
    for(i=0;i<6;i++){
      spi_out(0x00);
      xyz[i]=spiread;
    }
    // merge to convert to 16 bits
    x=((int)xyz[1]<<8) + xyz[0];
    y=((int)xyz[3]<<8) + xyz[2];
    z=((int)xyz[5]<<8) + xyz[4];  
    pinMode(spics,INPUT);  //CS HIGH
} 
void SerialStatus()
{    
    if (bRxEnd0 == true) {                // Serial Rx0 Recieve events 60
      char cbuff[10]={'\0'};
      //Serial.println(strRx0);
      strRx0.toCharArray(cbuff, 10);
      if (strstr(cbuff, "") != 0) {  }
      strRx0 = "";
      bRxEnd0 = false;
    }
    if (bRxEnd1 == true) {                // Serial Rx1 Recieve events GSM Shield 100
      char cbuff[20]={'\0'};
      strRx1.toCharArray(cbuff, 10);
      
      if (strstr(cbuff, "+CMT:") != 0) { }
      else if (strstr(cbuff, "GPSLOC") != 0) {
        //bGPSdat = true;
        // shutdown
      }  
      r=0;
      clrdat();
      bRxEnd1 = false;
    }
    if (bRxEnd2 == true) {              // Serial Rx2 Recieve events GPS Shield 80
      char cbuff[80]={'\0'};
      strRx2.toCharArray(cbuff, 80);
      
      if (strstr(cbuff, "$GPRMC") != 0) getLatLong(cbuff);
      g = 0;
      strRx2 = "";
      bRxEnd2 = false;
    } 
    if (bRxEnd3 == true) {              // Serial Rx3 Recieve events RF Module 60
      char cbuff[50]={'\0'};
      strRx3.toCharArray(cbuff, 50);
      decval = 0;  
      // [s1180][s2090][s3045][m1100][m2030][m3120] 0-41     
      if (cbuff[1]=='s' && cbuff[2]=='1') {
        decodedVal(cbuff, 3, 5); 
        myservo1.write(decval);
      }
      if (cbuff[8]=='s' && cbuff[9]=='2') {
        decodedVal(cbuff, 10, 12); 
        myservo2.write(decval);
      }
      if (cbuff[15]=='s' && cbuff[16]=='3') {
        decodedVal(cbuff, 17, 19); 
        myservo3.write(decval);
      }  
      
      if (cbuff[22]=='m' && cbuff[23]=='1') {
        decodedVal(cbuff, 24, 26);
        analogWrite(A8, decval);
      }
      if (cbuff[28]=='m' && cbuff[29]=='2') {
        decodedVal(cbuff, 31, 33);
        analogWrite(A9, decval);
      }
      if (cbuff[35]=='m' && cbuff[36]=='3') {
        decodedVal(cbuff, 37, 39);
        analogWrite(A10, decval);
      }  

      strRx3 = "";
      bRxEnd3 = false;      
    } 
}
void decodedVal(char *buff, char una, char huli)
{
    char decoded[6]={'\0'};
    int t=0;
    for (int i=una; i<=huli; i++) {
      decoded[t]=buff[i];
      t++;
    }  
    decval = atoi(decoded);
}
void serialEvent()
{  
    while (Serial.available()){        // Main
      char cRx = Serial.read();
      if (cRx == 0x03) bRxEnd0 = true;
      else strRx0 += cRx;
    }
}
void serialEvent1()                     // GSM Shield
{
    while (Serial1.available()){
      char cRx = Serial1.read();
      strRx1 += cRx;
      if (cRx == '\n') bRxEnd1 = true;
      r++;
    }
}
void serialEvent2()                     // GPS Shield
{
    while (Serial2.available()){
      char cRx = Serial2.read();
      
      strRx2 += cRx;
      if(GPSorCompass == true) {        // true 
        digitalWrite(EnbDCpPin, HIGH);
        digitalWrite(EnbGPSPin, LOW);
        
        if (cRx == '\n') bRxEnd2 = true;
        bSTX = false;
      }
      else {                            // false COMPASS 
        digitalWrite(EnbDCpPin, LOW);
        digitalWrite(EnbGPSPin, HIGH);

        if (cRx == 0x03) bSTX = true;
        bRxEnd2 = false;
           
        if (bSTX == true) {
          char cbuff[10]={'\0'};
          strRx2.toCharArray(cbuff, 10);
         
          CompassVal[0]=cbuff[1];
          CompassVal[1]=cbuff[2];
          CompassVal[2]=cbuff[3];
          CompassVal[3]=0; 
          strRx2 = "";
          g = 0;
          bSTX = false; 
        }
      }
      g++;
    }
}
void serialEvent3()                     // RF 433 Module
{
    while (Serial3.available()){
      char cRx = Serial3.read();
      if (cRx == '\n') bRxEnd3 = true;
      else strRx3 += cRx;
    }
}
void getLatLong(char *LatLong)
{
    char *p = LatLong;
    char *str;    //char strLat[15]={'\0'};    //char strLong[15]={'\0'};
    int t=0;
    boolean bValid = false;
    strLat[0]='0'; strLong[0]='0';
  
    while ((str = strtok_r(p, ",", &p)) != NULL)
    {
      if(t == 2) { if (strcmp(str, "A") == 0) bValid = true; }
      if(bValid == true) {
        if(t == 3) strcpy(strLat,str);
        else if(t == 5) strcpy(strLong,str);
      }
      t++;
    }   
    if(bGPSdat == true) {
      //send_msg("09499342258", GPSLocation);  // change Mobile #
      bGPSdat = false;
    }
}
void send_msg(char *number, char *msg) {
    char at_cmgs_cmd[30] = {'\0'};
    char msg1[160] = {'\0'};
    char ctl_z = 0x1A;
    
    sprintf(at_cmgs_cmd, "AT+CMGS=\"%s\"\r\n",number);
    sendGSM(at_cmgs_cmd);
    delay(500);
    sprintf(msg1, "%s%c", msg, ctl_z);
    sendGSM(msg1);
    delay(100);
}
void sendGSM(char *string)
{
    Serial1.write(string);
    delay(90);
}
void initGSM() {
    Serial1.print("AT\r\n");
    delay(500);						
    Serial1.print("ATE0\r\n");              // Turn off automatic echo of the GSM Module	
    delay(500);
    Serial1.print("AT+CMGF=1\r\n");	    // Set message format to text mode	
    delay(500);
}
void clrdat() {
    for (int i = 0; i < 100; i++)
        strRx1[i] = 0;
}
