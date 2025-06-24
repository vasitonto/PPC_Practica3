package HTTPBroker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SetCookieHeaderGenerator {
	
	static DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	
	
	protected String generarCookiesNuevas(String recurso) {
		String cookie = "Set-Cookie: Recurso=" + recurso +"; ";
		cookie += "\n";  
		return cookie;
	}
	
	protected String generarCookies(String recurso, String cookiesLeidas) {
		String cookie = "Set-Cookie: Recurso=" + recurso +"; ";
		cookie += "\r\n";
		cookie += "Set-Cookie: " + cookiesLeidas;
		return cookie;
	}
}
