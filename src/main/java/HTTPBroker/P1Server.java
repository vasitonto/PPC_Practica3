package HTTPBroker;

import clienteBroker.BrokerActionListener;

public class P1Server implements Runnable{
	
	private final BrokerActionListener listener;

    public P1Server( BrokerActionListener listener) {
        this.listener = listener;
    }
	
	public void run() {
		// This method will be called when the thread starts
		HTTPListener servidorHttp = new HTTPListener(listener);
		HTTPSListener servidorHttps = new HTTPSListener(listener);
		
		servidorHttp.start();
		servidorHttps.start();
	}	
}

