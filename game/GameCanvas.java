/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

public abstract class GameCanvas extends Canvas implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected GameState state;
	public GameMove move;
	protected Object waiting;
	protected boolean gettingMove = false;
	public Semaphore ready = new Semaphore(0, true);
	
	public void setBoard(GameState st)
	{ state = st; }
	public abstract void paint(Graphics g);
	public abstract int getW();
	public abstract int getH();
	public abstract void getMove(GameMove move, GameState state, Object waiting);
	public static Color [] colors = { Color.RED, Color.GREEN, Color.BLACK, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE };
	public static String colorNames = "RGBYCMW";
	
	public static Color getColor(char sym)
	{
		int which = colorNames.indexOf(sym);
		Color result = Color.LIGHT_GRAY;
		if (which != -1) {
			result = colors[which];
		}
		return result; 
	}
    public void mouseClicked(MouseEvent mouseEvent) {
    }
    public void mouseExited(MouseEvent mouseEvent) {
    }
    public void mouseReleased(MouseEvent mouseEvent) {
    }
    public void mouseEntered(MouseEvent mouseEvent) {
    }
}
