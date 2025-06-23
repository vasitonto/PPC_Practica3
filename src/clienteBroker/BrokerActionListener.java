package clienteBroker;

import Resources.ControlCodes;

public interface BrokerActionListener {
	void notifyControlHTTP(ControlCodes codigo, int serverSelection, int dato);
}
