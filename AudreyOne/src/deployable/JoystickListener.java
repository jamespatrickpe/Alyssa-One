package deployable;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class JoystickListener implements Runnable
{
	//Controllers
	static Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
	static Controller firstJoystick = null;
	
	//Control Translator
	ControlTranslator controlTranslator = new ControlTranslator();
	
	//variables
	static float currentX = 0;
	static float currentY = 0;
	static float currentZ = 0;
	static boolean[] currentButton = new boolean[12];
	
	public JoystickListener()
	{
		initializeJoystick();
	}
	
	public void initializeJoystick()
	{
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
	
	public void latestJoystickCommand()
	{
		Component comp = null; 
		float componentValue = 0;
		try
		{
			//extract joystick data
			firstJoystick.poll();
			EventQueue queue = firstJoystick.getEventQueue();
			Event event = new Event(); 
			while(queue.getNextEvent(event))
			{
				comp = event.getComponent();
				componentValue = event.getValue(); //remember to put back round function
				if(comp.isAnalog())
				{
					if(comp.getName().equals("X Axis"))
					{ currentX = componentValue; }
					else if(comp.getName().equals("Y Axis"))
					{ currentY = componentValue; }
					if(comp.getName().equals("Z Axis"))
					{ currentZ = componentValue; }
					
					controlTranslator.translate(currentX, currentY, currentZ);
				}
				else
				{
					if(componentValue == 1.0)
					{componentValue = (float) 0.00;}
					else
					{componentValue = (float) 1.00;}
					//INSERT TO VALUE
					
				}
			}
			
		}
		catch (Exception ex) 
		{System.out.println(ex);}
		//return comp.getName() + componentValue;
	}
	
	public static void main(String args[])
	{
		JoystickListener joystickListener = new JoystickListener();
		
		while(true)
		{
			joystickListener.latestJoystickCommand();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		JoystickListener joystickListener = new JoystickListener();
		while(true)
		{
			joystickListener.latestJoystickCommand();
		}
		
	}
}
