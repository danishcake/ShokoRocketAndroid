package uk.danishcake.shokorocket.networking.messages;

public class Message {
	public static final int MESSAGE_FRAME_END = 0;
	public static final int MESSAGE_CURSOR_POSITION = 1;
	public static final int MESSAGE_ARROW_PLACEMENT = 2;
	/* Message has hidden header items of no interest as follows */
	/*
	public int message_id;
	public int checksum;
	*/
	public int message_type;
	public int user_id;
	public int sub_frame_id;
	
	public void setCommon(int user_id, int sub_frame_id) {
		this.user_id = user_id;
		this.sub_frame_id = sub_frame_id;
	}
}
