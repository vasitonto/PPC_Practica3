package HTTPBroker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SetCookieHeaderGenerator {
	
	static DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	
	
	protected String generarCookiesNuevas(String recurso) {
		String cookie = "Set-Cookie: Recurso=" + recurso +";";
		cookie += "Expires=" + (LocalDateTime.now().plusDays(3).format(formato)) + ";\n";
		System.out.println("no se recibieron cookies, enviando " + cookie);
		return cookie;
	}
	
	protected String generarCookies(String recurso, String cookiesLeidas) {
		String cookie = "Set-Cookie: Recurso=" + recurso +";";
		cookie += "Expires=" + (LocalDateTime.now().plusDays(3).format(formato)) + ";\n";
		cookie += cookiesLeidas;
		System.out.println("se recibieron cookies\nenviando: " + cookie);
		return cookie;
	}
}
