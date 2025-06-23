package HTTPBroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Resources.ControlCodes;
import clienteBroker.BrokerActionListener;
import clienteBroker.DataBroker;

public class GestorPeticiones extends Thread {

	private Socket s;
	private BrokerActionListener brokerListener;
	private String tipoContenido = "text/html";
	
	public GestorPeticiones(Socket s, BrokerActionListener brokerListener) {
		super("GestorPeticiones");
		this.s = s;
		this.brokerListener = brokerListener;
	}
	
	@Override
	public void run() {
			
		BufferedReader sIn;
		PrintWriter sOut;
		String texto;
		String respuesta;
		String cuerpoRespuesta;
		String cookies = "";
		String recurso = "";
		ServerHeaderFactory creadorCabeceras = new ServerHeaderFactory();
		SetCookieHeaderGenerator creadorCookies = new SetCookieHeaderGenerator();
		try {
			sIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
			sOut = new PrintWriter(s.getOutputStream());
			
			while(!s.isClosed()) {
				
				recurso = "";
		        cookies = "";
		        cuerpoRespuesta = "";
		        respuesta="";
		        
		        while ((texto = sIn.readLine()) != null && !texto.isEmpty()) {	
					//aqui voy a procesar la primera l�nea de la peticion
		        	if(texto.contains("exit")) break;
					if(texto.contains("GET") || texto.contains("POST") || texto.contains("HEAD")) {
						recurso = texto.split(" ")[1];
					}
					if(recurso.isBlank() || recurso.isEmpty()) break;
					
					if(texto.contains("Cookie:")) {
						cookies += creadorCookies.generarCookies(recurso, texto.substring(8));
					}
					respuesta += texto + "\r\n";
				}
		        if(recurso.isBlank() || recurso.isEmpty()) continue;
		        System.out.print("Se ha recibido:\r\n" + respuesta);
				
				//compruebo si el string de cookies est� vac�o. Si lo est� es que es la primera conexi�n.
				if(cookies.isEmpty()) cookies = creadorCookies.generarCookiesNuevas(recurso);
				
				//preparo la respuesta con cabeceras y un HTML que devuelve el recurso pedido
				if(recurso.equals("/index.html") || recurso.equals("/")) {
					cuerpoRespuesta = HTMLResourceCreator.creaIndex(recurso);
				}
				else if (recurso.equals("/meteorologia.html") || recurso.equals("meteorologia")) {
					cuerpoRespuesta = HTMLResourceCreator.creaMeteorologia();
				} else if(recurso.startsWith("/apirest")){
					cuerpoRespuesta = procesaSolicitud(recurso);
				}
				else {
					// si no es ninguno de los anteriores, se devuelve un error 404
					cuerpoRespuesta = HTMLResourceCreator.crea404();
				}
				
				//TODO leer la peticion y ver si incluye parámetros
				
				respuesta = creadorCabeceras.generarCabeceras(tipoContenido, cuerpoRespuesta.length(), recurso) + cookies + "\r\n\r\n" + cuerpoRespuesta + "\r\n\r\n";
				tipoContenido = "text/html";
				System.out.print("\nSe enviar�: \n"+ respuesta);
				sOut.print(respuesta);
				sOut.flush();
			}
			sIn.close();
			sOut.close();
			s.close();
		} catch (java.net.SocketException sockex) {
			System.out.println("Conexión terminada con el usuario anterior. Esperando nueva conexión...");
		} catch (IOException e) { e.printStackTrace (); }
	}
	
	private String procesaSolicitud(String recurso) {
		String[] datosPeticion = recurso.split("/");
		// la posicion 3 (2) contiene el num. del servidor
		int numServidor = 0;
		try {
			numServidor = Integer.parseInt(datosPeticion[2]);			
		} catch(NumberFormatException num) {
			 return HTMLResourceCreator.crea404Servidores();
		}
		if(numServidor < 1 || numServidor > 3) return HTMLResourceCreator.crea404Servidores();
		
		// la posicion 4 contiene el tipo de comando, es decir, "control" o "valores"
		String comando = datosPeticion[3];
		if(comando.equals("valores")) {
			tipoContenido="text/plain";
			return DataBroker.obtenerRaw(numServidor);
		}
		if(comando.equals("control")) {
			// la posicion 5 contiene el comando a enviar
			String tipo = datosPeticion[4];
			switch(tipo) {
				case "stop":
					brokerListener.notifyControlHTTP(ControlCodes.STOP, numServidor, 0);
					break;
				case "continue": 
					brokerListener.notifyControlHTTP(ControlCodes.CONTINUE, numServidor, 0);
					break;
				case "send_xml":
					brokerListener.notifyControlHTTP(ControlCodes.SEND_XML, numServidor, 0);
					break;
				case "send_json":
					brokerListener.notifyControlHTTP(ControlCodes.SEND_JSON, numServidor, 0);
					break;
				case "mod_freq":
					int dato = 0;
					try {
						dato = Integer.parseInt(datosPeticion[4].substring(
								datosPeticion[4].indexOf("?")+6, datosPeticion[4].length()));			
					} catch(NumberFormatException num) {
						 return HTMLResourceCreator.crea400();
					}
					brokerListener.notifyControlHTTP(ControlCodes.MOD_FREQ, numServidor, dato);
					break;
				default:
					return HTMLResourceCreator.crea400();
			}
		}
		else {
			return HTMLResourceCreator.crea400();
		}
		return HTMLResourceCreator.creaIndex(recurso);
	}
}
