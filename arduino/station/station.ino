char inChar=-1;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.write("Station Initializing \n");

}

void loop() 
{
  if(Serial.available() > 0)
  {
    inChar = Serial.read();
    Serial.print(inChar);
    Serial.flush();
  }
}
