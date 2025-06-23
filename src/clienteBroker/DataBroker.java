package clienteBroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBroker {

	private final static Map<String, String> parsedDataStore = new ConcurrentHashMap<>();
	private final static Map<String, String> rawDataStore = new ConcurrentHashMap<>();
	
	public static void addDatos(String id, String datos) {
        parsedDataStore.put(id, datos);
    }

    public static Map<String, String> obtenerTodos() {
        return parsedDataStore;
    }

    public static String obtener(String id) {
        return parsedDataStore.get(id);
    }
    
	public static void addRawData(String id, String datos) {
		rawDataStore.put(id, datos);
	}
	
	public static Map<String, String> obtenerTodosRaw() {
		return rawDataStore;
	}
	
	public static String obtenerRaw(String id) {
		return rawDataStore.get(id);
	}
}
