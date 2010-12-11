package uk.danishcake.shokorocket.networking.messages;

import uk.danishcake.shokorocket.simulation.Direction;

public class ArrowClearMessage extends Message {
	public ArrowClearMessage() {
		this.message_type = Message.MESSAGE_ARROW_CLEAR;
	}
	
}
