package deployable;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.math.BigDecimal;

public class AudreyHandler implements Runnable
{
	// Serial Port Initialization
	static int comportNumber = 0;
	static SerialPort serialPort;
	
	//Initialize Listener
	private boolean initializeListener(String comport)
	{
		boolean found = false;
		try
		{
			serialPort = new SerialPort(comport);
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0); //Set params.
			found = true;
		}
		catch(SerialPortException exception)
		{
			System.out.println(exception);
			found = false;
		}
		return found;
	}
	
	public void findValidComport()
	{
		String beginningString = "COM" + comportNumber;
		while(initializeListener(beginningString) == false)
		{
			System.out.println("Finding Comport at: " + comportNumber);
	        try {
	            Thread.sleep(100);
	         } catch (InterruptedException e) {
	            e.printStackTrace();
	         }
			comportNumber++;
			beginningString = "COM" + comportNumber;
		}
		System.out.println("Connected to COM"+comportNumber);
	}
	
	//receive data from comport
	public void readComportData()
	{
        try 
        {
            String buffer = serialPort.readString();
            if(buffer != null)
            {
                System.out.print( buffer );
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
	}
	
	//Round
	public float round(float d, int decimalPlace) 
	{
	        BigDecimal bd = new BigDecimal(Float.toString(d));
	        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	        return bd.floatValue();
	}
	
	//Send Command
	public void sendCommand(String commandString)
	{
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
	
	//MAIN
	public static void main(String args[])
	{
		System.out.println("Audrey Handler Test");
		AudreyHandler audreyListener = new AudreyHandler();
		audreyListener.findValidComport();
		while(true)
		{
			audreyListener.readComportData();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Audrey Handler Test");
		AudreyHandler audreyListener = new AudreyHandler();
		audreyListener.findValidComport();
		while(true)
		{
			audreyListener.readComportData();
		}
	}

}
