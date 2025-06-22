package servidor;

import com.google.gson.JsonElement;

public class Solicitud {
	private String solicitud;
	private Integer intervalo;
	private String formato;
	
	public Solicitud(){};
	
	public String getTipo() {
		return solicitud;
	}
	
	public int getIntervalo() {
		return intervalo;
	}
	
	public String getFormato() {
		return formato;
	}
}
