package uk.danishcake.shokorocket.networking.messages;

public class CursorPositioningMessage extends Message {
	public int x;
	public int y;
	
	public CursorPositioningMessage(int x, int y) {
		this.x = x;
		this.y = y;
		this.message_type = MESSAGE_CURSOR_POSITION;
	}
}
