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
public class AudreyOne
{
	public static void main(String args)
	{
		//Thread audreyHandler = new Thread(new AudreyHandler());
		Thread joystickListener = new Thread(new JoystickListener());
		joystickListener.start();
		//audreyHandler.start();
	}
}