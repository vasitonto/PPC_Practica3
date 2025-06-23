package HTTPBroker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale; 

public class ServerHeaderFactory {
	private final static String VER = "HTTP/1.1";
	private final static String OKHEADER = "200 OK";
	private final static String SALTO = "\r\n";
	private DateTimeFormatter formato; 
	private LocalDateTime fecha;
	
	public ServerHeaderFactory() {
		this.formato = formato.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	};
	
	public String generarCabeceras(String tipoContenido, int lenContenido, String recurso) {
		String cabeceras = VER + " " + OKHEADER + SALTO;
		this.fecha = LocalDateTime.now();
		String fechaHTTP = fecha.format(this.formato);
		cabeceras += "Connection: keep-alive" + SALTO;
		cabeceras += "Date: " + fechaHTTP + SALTO;
		cabeceras += "P1Server: P1Server Java" + SALTO;
		cabeceras += "Content-type: " + tipoContenido + SALTO;
		cabeceras += "Content-length: " + lenContenido + SALTO;
		return cabeceras;
	};
}
