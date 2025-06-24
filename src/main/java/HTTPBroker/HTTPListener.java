package HTTPBroker;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import clienteBroker.BrokerActionListener;

class HTTPListener extends Thread {
	
	public static final int PORT = 8000;
	BrokerActionListener brokerListener;
	
	public HTTPListener(BrokerActionListener brokerListener) {
		super("HTTPListener");
		this.brokerListener = brokerListener;
	}
	
	public void run(){
		
		ServerSocket s = null;
		Socket cliente = null;
		
		try {
			s = new ServerSocket (PORT);
		} catch (IOException e) { e.printStackTrace (); }
		
		while (true)
		{
			try
			{
				cliente = s.accept();
				new GestorPeticiones(cliente, brokerListener, PORT).start();
			} catch (IOException e) { e.printStackTrace (); }
		}
	}
}

