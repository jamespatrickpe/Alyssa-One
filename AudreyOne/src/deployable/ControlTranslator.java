package deployable;

public class ControlTranslator 
{	
	//Sender
	AudreyHandler audreySender = new AudreyHandler();
	
	//current values
	static float currentX = 0;
	static float currentY = 0;
	static float currentZ = 0;
	static String currentCommandString = null;
	
	private float map(float x, float in_min, float in_max, float out_min, float out_max)
	{
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	public void translate(float x, float y, float z)
	{
		currentX = x; currentY = y; currentZ = z;
		
		int servoOne = 0;
		int servoTwo = 0;
		int servoThree = 0;
		int motorOne = Math.round(map(currentZ,-1,1,400,450));
		int motorTwo = Math.round(map(currentZ,-1,1,400,450));
		int motorThree = Math.round(map(currentX,-1,1,400,450)); // work on this when you get the new ESC
		// [s1180][s2090][s3045][m1100][m2030][m3120] 0-41
		currentCommandString = "[s1"+servoOne+"][s2"+servoTwo+"][s3"+servoThree+"][m1"+motorOne+"][m2"+motorTwo+"][m3"+motorThree+"]";
		audreySender.sendCommand(currentCommandString);
		//System.out.println("Command String: "+currentCommandString);
	}
	
	public void currentControlParameters()
	{
		
	}
}
