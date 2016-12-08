/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;

public abstract class GameMove implements Cloneable {
	/**
	 * Converts a move to a string presentable to the console
	 * @return String for human viewing or network communication.
	 * The string can't contain newline characters.
	 */
	public abstract String toString();
	/**
	 * Converts a String (created by toString) into a GameMove.
	 * @param s String to be parsed.
	 */
	public abstract void parseMove(String s);
	/**
	 * Clones a GameMove
	 * @return Returns a clone of the GameMove.
	 */
	public abstract Object clone();
}
