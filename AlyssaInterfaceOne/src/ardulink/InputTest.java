/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package ardulink;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;
import org.zu.ardulink.protocol.IProtocol;

public class InputTest {

	public static void main(String[] args) {
		Link link = Link.getDefaultInstance();
		
		link.addConnectionListener(new ConnectionListener() {
			
			@Override
			public void disconnected(DisconnectionEvent e) {
				System.out.println("Board disconnected");
			}
			
			@Override
			public void connected(ConnectionEvent e) {
				System.out.println("Board connected");
			}
		});
		
		link.connect("COM5");
		try {
			System.out.println("wait for a while");
			Thread.sleep(2000);
			System.out.println("proceed");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("start Listening");
		link.addDigitalReadChangeListener(new DigitalReadChangeListener() {
			
			@Override
			public void stateChanged(DigitalReadChangeEvent e) {
				System.out.println("PIN: " + e.getPin() + " STATE: " + e.getValue());
				System.out.println(e.getIncomingMessage());
			}
			
			@Override
			public int getPinListening() {
				return 3;
			}
		});

		for(int i = 0; i < 5; i++) {
			try {
				Thread.sleep(1000);
				System.out.println("sendPowerON");
				link.sendPowerPinSwitch(5, IProtocol.HIGH);
				Thread.sleep(1000);
				System.out.println("sendPowerOFF");
				link.sendPowerPinSwitch(5, IProtocol.LOW);
				Thread.sleep(1000);
				link.sendCustomMessage("dfasdf");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			System.out.println("wait for a while");
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		link.disconnect();
	}
}
