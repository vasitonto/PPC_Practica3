package clienteBroker;

import java.nio.charset.StandardCharsets;

public class MensajeControl {
	//estp dice chatgpt
	public class ControlMessage {
	    private final String direccion;
	    private final int codigo;
	    private final Integer intervalo; // null si no aplica

	    public ControlMessage(String direccion, int codigo) {
	        this(direccion, codigo, null);
	    }

	    public ControlMessage(String direccion, int codigo, Integer intervalo) {
	        this.direccion = direccion;
	        this.codigo = codigo;
	        this.intervalo = intervalo;
	    }

	    public String toJson() {
	        StringBuilder json = new StringBuilder();
	        json.append("{");
	        json.append("\"direccion\": \"").append(direccion).append("\", ");
	        json.append("\"codigo\": ").append(codigo);
	        if (intervalo != null) {
	            json.append(", \"intervalo\": ").append(intervalo);
	        }
	        json.append("}");
	        return json.toString();
	    }

	    public byte[] toBytes() {
	        return toJson().getBytes(StandardCharsets.UTF_8);
	    }
	}
}
