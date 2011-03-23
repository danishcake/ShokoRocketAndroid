package uk.danishcake.shokorocket.simulation;

/**
 * Extends the special square concept to multiplayer 
 * by associating a player with arrows/rockets. Also stores 
 * the order of placement in case of arrows
 * @author Edward
 *
 */
public class MPSquareType {
	public SquareType square_type;
	public int player_id;
	public int order;
}
