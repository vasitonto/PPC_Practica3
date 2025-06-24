package clienteBroker;

import utils.ControlCodes;

public interface BrokerActionListener {
	void notifyControlHTTP(ControlCodes codigo, int serverSelection, int dato);
}
