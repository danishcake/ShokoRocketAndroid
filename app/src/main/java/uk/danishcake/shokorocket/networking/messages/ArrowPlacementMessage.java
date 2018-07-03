package uk.danishcake.shokorocket.networking.messages;

import uk.danishcake.shokorocket.simulation.Direction;

public class ArrowPlacementMessage extends Message {
	public Direction direction;
	public int x;
	public int y;
	public ArrowPlacementMessage(int x, int y, Direction direction) {
		this.direction = direction;
		this.x = x;
		this.y = y;
		this.message_type = Message.MESSAGE_ARROW_PLACEMENT;
	}
	
}
