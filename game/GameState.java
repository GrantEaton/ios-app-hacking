/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;
import java.io.File;
import java.util.*;

public abstract class GameState implements Cloneable {
	public static final char SEP = File.separatorChar;
	public static final String CONFIG_DIR = "src" + SEP + "config" + SEP;
	public enum Status { GAME_ON, HOME_WIN, AWAY_WIN, DRAW };
	public enum Who { HOME, AWAY };
	public Status status;			// status of current game
	public Who who;					// side that has next move
	public int numMoves;

	/**
	 * Constructs a generic game state.
	 */
	public GameState()
	{ clear(); }
	/**
	 * Converts a string into its Status type
	 * @param str String to be parsed
	 * @return Corresponding status, or null
	 */
	private static Status parseStatus(String str)
	{
		Status [] vals = Status.values();
		for (int i=0; i<vals.length; i++) {
			if (str.equals(vals[i].toString()))
				return vals[i];
		}
		return null;
	}
	private String who2string()
	{ return who == Who.HOME ? "HOME" : "AWAY"; }
	/**
	 * convert string to side
	 * @param s "HOME" or "AWAY"
	 * @return HOME or AWAY
	 */
	public static Who str2who(String s)
	{ return s.equals("HOME") ? Who.HOME : Who.AWAY; }
	/**
	 * Get number of moves played. 
	 * @return number of moves played
	 */
	public int getNumMoves()
	{ return numMoves; }
	/**
	 * Whose turn is next
	 * @return side that gets to move next
	 */
	public Who getWho()
	{ return who; }
	/**
	 * Retrieve status of game
	 * @return game status
	 */
	public Status getStatus()
	{ return status; }
	public void togglePlayer()
	{
		who = (who == Who.HOME ? Who.AWAY : Who.HOME);
	}
	protected void newMove()
	{
		numMoves++;
		togglePlayer();
	}
	protected void clear()
	{
		who = Who.HOME;
		numMoves = 0;
		status = Status.GAME_ON;
	}
	protected void copyInfo(GameState src)
	{
		who = src.who;
		numMoves = src.numMoves;
		status = src.status;
	}
	/**
	 * Create a deep copy of the State
	 */
	public abstract Object clone();
	/**
	 * Sets the game back to the beginning of the game.
	 */
	public abstract void reset();
	/**
	 * Updates the game based on a particular move
	 * @param mv Move to be made
	 * @return true if move was successfully performed
	 */
	public abstract boolean makeMove(GameMove mv);
	/**
	 * Recreates the board game's state from a string representation.
	 * In particular, the string is a string generated as a "message"
	 * string (i.e., one without newlines). This method must be able
	 * to fill in the "board", who's next, number of moves performed,
	 * and whether or not the game ends with this move.
	 * @param s Message string representation of the state.
	 */
	public abstract void parseMsgString(String s);
	/**
	 * Convert to a string 
	 * @return String representation of the State, suitable for display
	 */
	public abstract String toString();
	/**
	 * Convert to a string suitable for tournament (i.e., no newlines) 
	 * @return String representation of the State, suitable for tournament
	 * communication.
	 */
	public abstract String msgString();
	/**
	 * Determines if a particular move is legal, given the current
	 * board configuration.
	 * @param mv Move to be made
	 * @return true if the move is valid
	 */
	public abstract boolean moveOK(GameMove mv);
	/**
	 * Takes a message suffix and parses out the information common to
	 * all GameStates.
	 * @param suffix String suffix. Lookes like: "[HOME 12 GAME_ON]"
	 */
	protected void parseMsgSuffix(String suffix)
	{
		int len = suffix.length();
		suffix = suffix.substring(1, len-1);
		StringTokenizer toks = new StringTokenizer(suffix);
		who = str2who(toks.nextToken());
		numMoves = Integer.parseInt(toks.nextToken());
		status = parseStatus(toks.nextToken());
	}
	/**
	 * Creates a message suffix for this particular game state.
	 * @return String suffix for communication to/from server.
	 */
	protected String msgSuffix()
	{
		return "[" + who2string() + ' ' +
					 numMoves + ' ' +
					 status.toString() + "]";
	}
}
