/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;
import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

public class GameThread extends Thread {
	public static char SEP = File.separatorChar;
	public static String dir;
	public GameState.Status result;
	public int homeID, awayID;
	private int gameNum;
	private Client home, away;
	private GameMove mv;
	private GameState board;
	
	public GameThread(Client h, int hID, Client a, int aID, int num, Object move, Object brd)
	{
		homeID = hID;
		awayID = aID;
		home = h;
		away = a;
		mv = (GameMove)move;
		board = (GameState)brd;
		gameNum = num;
		result = GameState.Status.GAME_ON;
	}
	private static String getFname(String n1, char c1, String n2, char c2, int n, String result)
	{
		return n1 + SEP + n1 + "(" + c1 + ") vs " + n2 + "(" + c2 + ") #" + n + " " +  result + ".txt"; 
	}

    private static void copy(File src, String dir, String dest)
    {
    	if (dest.charAt(0) == '-')
    		return;
		Scanner input = null;
    	PrintStream output = null;
		try {
			input = new Scanner(src);
			output = new PrintStream(new File(dir + dest));
		}
		catch (Exception e) {
			System.out.println(e.toString());
			output.printf("error");
			output.printf(e.toString());
			e.printStackTrace();
		}

		while (input.hasNext()) {
			String line = input.nextLine();
			output.println(line);
		}
    }
	private static void copyDumpFile(File src, String [] names, String dir, int gameNum, String homeResult)
	{
		String awayResult;
		if (homeResult.equals("WIN")) {
			awayResult = "LOSS";
		} else if (homeResult.equals("LOSS")) {
			awayResult = "WIN";
		} else {
			awayResult = "DRAW";
		}
		copy(src, dir, getFname(names[0], Tournament.gameParams.character("HOMESYM"), 
								names[1], Tournament.gameParams.character("AWAYSYM"), gameNum, homeResult)); 
		copy(src, dir, getFname(names[1], Tournament.gameParams.character("AWAYSYM"),
								names[0], Tournament.gameParams.character("HOMESYM"), gameNum, awayResult)); 
	}
	public void run()
	{
		Client [] players = { home, away };
		String [] names = { players[0].name, players[1].name };

		int [] warnings = {0, 0};
		File tmpFile = null;
		PrintStream m = null;
		char homeSym = Tournament.gameParams.character("HOMESYM");
		char awaySym = Tournament.gameParams.character("AWAYSYM");
		
		try {
			tmpFile = File.createTempFile(names[0] + gameNum, names[1]);
			m = new PrintStream(tmpFile);
			m.flush();
		}
		catch (Exception e) {
			m.printf("Game #%d between %s (%c) and %s (%c)%n", gameNum, 
					names[0], homeSym, names[1], awaySym);
		}
	
		board.reset();
		if (players[0].DQd && players[1].DQd) {
			m.printf("Draw. Both DQ'd%n");
			copyDumpFile(tmpFile, names, dir, gameNum, "DRAW");
			result = GameState.Status.DRAW;
			return;
		} else if (players[0].DQd) {
			m.printf("%s won. %s DQ'd", names[1], names[0]);
			copyDumpFile(tmpFile, names, dir, gameNum, "LOSS");
			result = GameState.Status.AWAY_WIN;
			return;
		} else if (players[1].DQd) {
			m.printf("%s won. %s DQ'd", names[0], names[1]);
			copyDumpFile(tmpFile, names, dir, gameNum, "WIN");
			result = GameState.Status.HOME_WIN;
			return;
		}
		
		try {
			players[0].setTimeout(0);
			players[1].setTimeout(0);
			//players[0].sock.setSoTimeout(0);
			//players[1].sock.setSoTimeout(0);
		}
		catch (Exception e) {
			System.err.println("Error setting infinte timeout:" + e);
			System.err.flush();
		}
	
		String m0 = "", m1 = "";
		players[0].simpleMsg("START", "HOME", names[1]);
		players[1].simpleMsg("START", "AWAY", names[0]);
		m0 = players[0].hardLimitResponse(10);
		m1 = players[1].hardLimitResponse(10);
		players[0].simpleMsg(m1);
		players[1].simpleMsg(m0);
		players[0].gameTimeRemaining = players[0].gameTimeLimit;
		players[1].gameTimeRemaining = players[1].gameTimeLimit;

		int who = 0;
		GameState.Status status = board.getStatus();
		String lastMove = "--";
		
		while (status == GameState.Status.GAME_ON) {
			//System.out.println(board.toString());
			players[who].simpleMsg("MOVE", lastMove, board.msgString());
			double moveLimit = Math.min(players[who].gameTimeRemaining, 
										players[who].finalPlayLimit);
			
			double actualTime = players[who].timedResponse(moveLimit, mv, lastMove, board);
			players[who].gameTimeRemaining -= actualTime;
			//System.out.println(players[who].name + " " + players[who].gameTimeRemaining);
			if (actualTime < 0) {
				players[who].DQd = true;
				System.err.printf("%s DQd%n", players[who].name);
				System.err.flush();
				players[who].simpleMsg("DONE");
				m.printf("%s won. %s DQd%n", names[(who+1)%2], names[who]);
				break;
			} else if (actualTime > players[who].moveLimit) {
				warnings[who]++;
				System.err.printf("warning %s %f%n", players[who].name, actualTime);
				System.err.flush();
				if (warnings[who] > players[who].maxWarnings) {
					System.err.printf("Too many timeouts: %s%n", players[who].name);
					System.err.flush();
					m.printf("%s won. %s had too many timeouts%n", names[(who+1)%2], names[who]);
					break;
				}
			}
			if (!board.makeMove(mv)) {
				System.err.printf("%s won. %s made bad move %s%n", names[(who+1)%2], names[who], mv.toString());
				System.err.flush();
				m.printf("%s won. %s made bad move %s%n", names[(who+1)%2], names[who], mv.toString());
				break;
			}
			lastMove = mv.toString();
			m.printf("%s (%c) played %s%n", names[who], (who == 0 ? homeSym : awaySym), lastMove);
			m.printf("%s%n", board.toString());
			status = board.getStatus();
			who = (who + 1) % 2;
		}
		
		m.flush();
		String winner;
		if (status == GameState.Status.GAME_ON) {
			if (who == 0) {
				status = GameState.Status.AWAY_WIN;
				winner = "AWAY";
			} else {
				status = GameState.Status.HOME_WIN;
				winner = "HOME";
			}
		} else if (status == GameState.Status.HOME_WIN) {
			m.printf("%s (%c) (%f seconds remaining) won.%n", names[0], homeSym, players[0].gameTimeRemaining);
			m.printf("%s had %f seconds remaining.%n", names[1], players[1].gameTimeRemaining);
			winner = "HOME";
		} else if (status == GameState.Status.AWAY_WIN) {
			m.printf("%s (%c) (%f seconds remaining) won.%n", names[1], awaySym, players[1].gameTimeRemaining);
			m.printf("%s had %f seconds remaining.%n", names[0], players[0].gameTimeRemaining);
			winner = "AWAY";
		} else {
			m.printf("draw.%n");
			winner = "DRAW";
		}
		
		for (int i=0; i<2; i++) {
			players[i].simpleMsg("OVER", winner);
			if (!players[i].DQd)
				players[i].flush();
				//flushSocket(players[i].input);
		}
		m.close();
		if (winner.equals("HOME")) {
			copyDumpFile(tmpFile, names, dir, gameNum, "WIN");
		} else if (winner.equals("AWAY")) {
			copyDumpFile(tmpFile, names, dir, gameNum, "LOSS");
		} else {
			copyDumpFile(tmpFile, names, dir, gameNum, "DRAW");
		}
		result = status;
		return;
	}
}
