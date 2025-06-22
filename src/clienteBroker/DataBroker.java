package clienteBroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBroker {

	private final static Map<String, String> dataStore = new ConcurrentHashMap<>();
	
	public static void addDatos(String id, String datos) {
        dataStore.put(id, datos);
    }

    public static Map<String, String> obtenerTodos() {
        return dataStore;
    }

    public static String obtener(String id) {
        return dataStore.get(id);
    }
}
