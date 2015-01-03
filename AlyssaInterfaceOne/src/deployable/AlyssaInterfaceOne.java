package deployable;

import java.math.BigDecimal;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Component.*;
import jssc.SerialPort;
import jssc.SerialPortException;

//Initializes System
public class AlyssaInterfaceOne
{
	// Serial Port Initialization
	static SerialPort serialPort = new SerialPort("COM4");
	
	//Controllers
	static Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
	static Controller firstJoystick = null;
	
	//Current Raw Values
	static float x = 0;
	static float y = 0;
	static float z = 0;
	static float button[] = new float[10];
	
	//Current Processed Values
	static float motor[] = new float[10];
	static float servo[];
	static boolean motorChange[];
	static boolean servoChange[];
	
	//Initialization
	public void initializeSystems()
	{
		// Initialize Comport
		try
		{
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0); //Set params.
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		// Joystick
        for(int i =0;i<controllers.length;i++)
        {
            /* Get the name of the controller */
            System.out.println(controllers[i].getName());
            System.out.println("Type: "+controllers[i].getType().toString());

            /* Get this controllers components (buttons and axis) */
            Component[] components = controllers[i].getComponents();
            System.out.println("Component Count: "+components.length);
            for(int j=0;j<components.length;j++)
            {
                /* Get the components name */
            	System.out.println("Component "+j+": "+components[j].getName());
            	System.out.println("    Identifier: "+ components[j].getIdentifier().getName());
            	System.out.print("    ComponentType: ");
	            if (components[j].isRelative()) 
	            {System.out.print("Relative");} 
	            else 
	            {System.out.print("Absolute");}
	            if (components[j].isAnalog()) 
	            {System.out.print(" Analog");} 
	            else 
	            {System.out.print(" Digital");}
            }
        }
        
        //initialize first joystick found
        for(int i=0;i<controllers.length && firstJoystick==null;i++) 
        {
        	if(controllers[i].getType()==Controller.Type.STICK) 
        	{
                // Found a mouse
        		firstJoystick = controllers[i];
        		System.out.println("\n\n FOUND JOYSTICK: " + firstJoystick.getName() + "@" + firstJoystick.getPortNumber());
        	}
        	else
        	{
        		System.out.println("NO JOYSTICK FOUND! Please Plug In Attack 3 Joystick for Manual Control");
        	}
        }
      
    }
	
	private static void delay(int seconds)
	{
        try {
            Thread.sleep(seconds);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
	}
	
	//Round
	public static float round(float d, int decimalPlace) 
	{
	        BigDecimal bd = new BigDecimal(Float.toString(d));
	        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	        return bd.floatValue();
	}
	
	//pid
	private void pidMod(float oldValue)
	{
		
	}
	
	
	//translates manual controls into action
	private static void translateManualControls(String name, float signal)
	{
		//store last known values
		switch (name) 
		{
        	case "X Axis":  x = signal; 
							sendCommand("s0",x);
							sendCommand("s1",x);
        					break;
        	case "Y Axis":  y = signal;
							sendCommand("m2",y);
							sendCommand("s2",y);
        					break;
        	case "Z Axis":  z = signal;
        					sendCommand("m0",z);
        					sendCommand("m1",z);
        					break;
        	case "Button 0":  button[0] = signal; break;
        	case "Button 1":  button[1] = signal; break;
        	case "Button 2":  button[2] = signal; break;
        	case "Button 3":  button[3] = signal; break;
        	case "Button 4":  button[4] = signal; break;
        	case "Button 5":  button[5] = signal; break;
        	case "Button 6":  button[6] = signal; break;
        	case "Button 7":  button[7] = signal; break;
        	case "Button 8":  button[8] = signal; break;
        	case "Button 9":  button[9] = signal; break;
        	case "Button 10":  button[9] = signal; break;
        	default: System.out.println("Unidentified Joystick Input"); 
		}
	}
	
	//receive data from comport
	private static void readComportData()
	{
		try
		{
			String incomingComPortData;
			incomingComPortData = serialPort.readString();
			if(incomingComPortData != null)
			{
				System.out.print(incomingComPortData);
			}	
		}
		catch(Exception e)
		{
			System.out.println("COMPORT ERROR: " + e);
		}
	}
	
	private static void sendCommand(String command, float val)
	{
		val = round(val,2);
		String commandString = "["+command+":"+val+"]";
		System.out.println("Sending Command: "+commandString);
		try
		{
			serialPort.writeString( commandString );
		}
		catch(Exception e)
		{
			System.out.println("Error Writing to Serial Port");
		}
		
	}
	
	//-------------------------------------------
	public static void main(String[] args) throws Exception
	{
		AlyssaInterfaceOne main = new AlyssaInterfaceOne();
		main.initializeSystems();
		
		System.out.println("Alyssa Interface One Started");
		
		try
		{
			while(true)
			{
				readComportData();
				
				//extract joystick data
				firstJoystick.poll();
				Component comp;
				EventQueue queue = firstJoystick.getEventQueue();
				Event event = new Event();
				float componentValue = 0;
				while(queue.getNextEvent(event))
				{
					comp = event.getComponent();
					componentValue = event.getValue(); //remember to put back round function
					if(comp.isAnalog())
					{
						translateManualControls( comp.getName(), componentValue);
					}
					else
					{
						if(componentValue == 1.0)
						{componentValue = (float) 0.00;}
						else
						{componentValue = (float) 1.00;}
						translateManualControls( comp.getName(), componentValue);
					}
				}
			}
		}
		catch (Exception ex) 
		{System.out.println(ex);}
		}
		
	}