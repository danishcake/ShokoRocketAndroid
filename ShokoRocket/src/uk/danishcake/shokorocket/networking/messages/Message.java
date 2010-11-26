package uk.danishcake.shokorocket.networking.messages;

public class Message {
	public static int MESSAGE_FRAME_END = 0;
	public static int MESSAGE_CURSOR_POSITION = 1;
	public static int MESSAGE_ARROW_PLACEMENT = 2;
	/* Message has hidden header items of no interest as follows */
	/*
	public int message_id;
	public int checksum;
	*/
	public byte message_type;
	public byte user_id;
}
