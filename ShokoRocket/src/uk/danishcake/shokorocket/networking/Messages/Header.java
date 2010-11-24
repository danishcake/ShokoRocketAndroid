package uk.danishcake.shokorocket.networking.messages;

public class Header {
	private int message_id;
	private int checksum;
	private byte message_type;
	private byte user_id;

	public void getMessage(byte[] message)
	{
		message[0] = message_type;
		message[1] = user_id;
		
	}
}
