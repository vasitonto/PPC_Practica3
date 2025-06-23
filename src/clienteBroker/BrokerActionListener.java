package clienteBroker;

import Resources.ControlCodes;

public interface BrokerActionListener {
	void enviaControl(ControlCodes codigo, int source);
}
