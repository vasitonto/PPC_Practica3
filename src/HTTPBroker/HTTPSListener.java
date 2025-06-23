package HTTPBroker;

import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import clienteBroker.BrokerActionListener;

public class HTTPSListener extends Thread {
	
	public static final int PORT = 9000;
	BrokerActionListener brokerListener;
	
	static public final String			SV_STORE	= "certs/server12-1.p12";
	static public final String			SV_PWD		= "practicas";
	static public final String			SV_CERT_PWD	= "practicas";
	static public final String			CA_STORE	= "certs/ca12.p12";
	static public final String			CA_PWD		= "practicas";
	
	public HTTPSListener(BrokerActionListener brokerListener) {
		super("HTTPSListener");
		this.brokerListener = brokerListener;
	}
	
	public void run(){
		
		SSLContext				ctx;
		SSLServerSocketFactory	fac; 
		SSLServerSocket			s; 
		Socket					s1;
		KeyStore 				ksm, kst;
		KeyManagerFactory 		kmf;
		TrustManagerFactory 	tmf;		
		
		try {
						
			ksm = KeyStore.getInstance("PKCS12");
			ksm.load(new FileInputStream(SV_STORE), SV_PWD.toCharArray());
			kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ksm, SV_CERT_PWD.toCharArray());
			
			kst = KeyStore.getInstance("PKCS12");
			kst.load(new FileInputStream(CA_STORE), CA_PWD.toCharArray());
			tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(kst);

			ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			fac = ctx.getServerSocketFactory();
			s =  (SSLServerSocket) fac.createServerSocket(PORT);
			// cambiar a true para autenticaci�n mutua con el cliente
			s.setNeedClientAuth(false);
			
			while (s != null)
			{
				s1 = s.accept();
				// lanzo un nuevo GestorPeticiones que se encargar� de la comunicaci�n
				new GestorPeticiones(s1, brokerListener).start();
			}
		
		} catch (Exception e) { e.printStackTrace(); }		
	}
}
