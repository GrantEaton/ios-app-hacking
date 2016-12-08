/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;

import java.net.*;
import java.io.*;
import java.lang.Exception;
import java.util.*;

public class Tournament {
	public static Params tournamentParams;
	public static Params gameParams;
	public static String gameName;
	public static Client[] clients;
	public static ArrayList<GameThread> threads;
	public static final char SEP = GameState.SEP;

	private static class Game {
		public int h, a;
		public int gameNum;

		public Game(int h, int a, int n) {
			this.h = h;
			this.a = a;
			this.gameNum = n;
		}
	}

	private static String printableName(String name) {
		int maxLen = gameParams.bool("TIES") ? 13 : 9;
		if (name.length() <= maxLen)
			return name;
		else
			return name.substring(0, maxLen);
	}

	private static int randInt(int lo, int hi) {
		int delta = hi - lo + 1;
		return (int) (Math.random() * delta) + lo;
	}

	private static void displaySummary(int wins, int losses, int ties) {
		if (gameParams.bool("TIES")) {
			System.out.printf("(%3d %3d %3d) ", wins, losses, ties);
		} else {
			System.out.printf("(%3d %3d) ", wins, losses);
		}
	}

	/*
	 * private static String format(int max, int a, int b, int c) { int digs =
	 * (int)Math.ceil(Math.log10(max)); if (digs <= 0) { digs = 1; } String sfmt
	 * = String.format("%c%dd", '%', digs); String fmt = sfmt + " " + sfmt + " "
	 * + sfmt; String s = String.format(fmt, a, b, c); return s; }
	 */
	private static void shuffle(ArrayList<Game> list) {
		int sz = list.size();
		for (int i = 0; i < sz; i++) {
			int newSpot = randInt(i, sz - 1);

			Game old = list.get(i);
			Game neww = list.get(newSpot);
			list.set(i, neww);
			list.set(newSpot, old);
		}
	}

	public static void schedule(ArrayList<Game> sched, int NUM_GAMES, int p1, int p2) {
		int numToPlay;
		numToPlay = NUM_GAMES;
		for (int i = 0; i < numToPlay; i++) {
			sched.add(new Game(p1, p2, i));
		}
	}

	public static char lastChar(String s) {
		int len = s.length();
		return s.charAt(len - 1);
	}

	public static boolean xor(boolean a, boolean b) {
		return (a && !b) || (!a && b);
	}

	public static void displaySummary(String suffixes, String format,
			int NUM_CLIENTS, int[][][] headToHeadSummary, int[][] homeSummary) {
		boolean doAll = suffixes.length() == 0;
		System.out.printf(format, "");
		for (int p1 = 0; p1 < NUM_CLIENTS; p1++) {
			String name = clients[p1].name;
			if (doAll || suffixes.indexOf(lastChar(name)) != -1) {
				System.out.printf(format, printableName(name));
			}
		}
		System.out.println();

		for (int p1 = 0; p1 < NUM_CLIENTS; p1++) {
			String name = clients[p1].name;
			if (doAll || suffixes.indexOf(lastChar(name)) != -1) {
				System.out.printf(format, printableName(clients[p1].name));
				for (int p2 = 0; p2 < NUM_CLIENTS; p2++) {
					String name2 = clients[p2].name;
					if (doAll || suffixes.indexOf(lastChar(name2)) != -1) {
						if (p1 == p2) {
							System.out.printf(format, "");
						} else {
							displaySummary(headToHeadSummary[p1][p2][0],
									headToHeadSummary[p1][p2][1],
									headToHeadSummary[p1][p2][2]);
						}
					}
				}
				displaySummary(homeSummary[p1][0], homeSummary[p1][1],
						homeSummary[p1][2]);
				System.out.printf("%n");
			}
		}

	}
	public static void launchTournament(String gameName, boolean regularTournament)
	{
		gameParams = new Params(GameState.CONFIG_DIR + gameName.toLowerCase() + ".txt");
		
		GameMove move = null;
		GameState state = null;
		try {
			Class<?> stateClass = Class.forName(gameName.toLowerCase() + "." + gameName + "State");
			Class<?> mvClass = Class.forName(gameName.toLowerCase() + "." + gameName + "Move");
			state = (GameState)stateClass.newInstance();
			move = (GameMove)mvClass.newInstance();
		} catch (Exception e) {
			System.err.println("Problem launching");
			System.exit(-1);
		}
		
		if (regularTournament)
			Tournament.launchTournament(state, move);
		else
			Tournament.launchSystematicTournament(state, move);
	}
	public static void launchTournament(GameState st, GameMove move) {
		File dumpsDir = new File("Dumps");
		dumpsDir.mkdir();
		Date date = new Date();
		String dumpDir = "Dumps" + SEP + date.toString() + SEP;
		dumpDir = dumpDir.replace(":", ";");
		File file = new File(dumpDir);
		file.mkdir();
		GameThread.dir = dumpDir;
		int NUM_CLIENTS = tournamentParams.integer("NUMCLIENTS");
		int PORT = tournamentParams.integer("PORT");
		int NUM_GAMES = tournamentParams.integer("NUMGAMES");

		int INIT_LIMIT = tournamentParams.integer("INITTIME");
		int MOVE_LIMIT = tournamentParams.integer("MOVETIME");
		int GAME_LIMIT = tournamentParams.integer("GAMETIME");
		int FINAL_PLAY_LIMIT = tournamentParams.integer("MAXMOVETIME");
		int MAX_WARNINGS = tournamentParams.integer("NUMWARNINGS");
		int HUMAN_TIME = tournamentParams.integer("HUMANTIME");

		int p1, p2;
		int i;
		int headToHeadSummary[][][] = new int[NUM_CLIENTS][NUM_CLIENTS][3];
		int homeSummary[][] = new int[NUM_CLIENTS][3];
		int awaySummary[][] = new int[NUM_CLIENTS][3];
		int totalSummary[][] = new int[NUM_CLIENTS][3];
		ArrayList<Game> schedule = new ArrayList<Game>();
		threads = new ArrayList<GameThread>();
		String format = gameParams.bool("TIES") ? "%13s " : "%9s ";

		try {
			ServerSocket socket = new ServerSocket(PORT);
			clients = new NetworkClient[NUM_CLIENTS];
			for (i = 0; i < NUM_CLIENTS; i++) {
				clients[i] = new NetworkClient(socket, INIT_LIMIT, MOVE_LIMIT,
						FINAL_PLAY_LIMIT, GAME_LIMIT, MAX_WARNINGS);
				if (clients[i].name.toUpperCase().contains("HUMAN")) {
					clients[i].gameTimeLimit = clients[i].moveLimit = clients[i].finalPlayLimit = HUMAN_TIME;
				}
				File subdir = new File(dumpDir + SEP + clients[i].name);
				subdir.mkdir();
				System.out.printf("%s has joined%n", clients[i].name);
			}

			for (p1 = 0; p1 < NUM_CLIENTS; p1++) {
				for (p2 = 0; p2 < NUM_CLIENTS; p2++) {
					if (p1 == p2)
						continue;
					schedule(schedule, NUM_GAMES, p1, p2);
				}
			}
			shuffle(schedule);

			int remain = schedule.size();

			while (remain > 0) {
				Thread.sleep(500);
				boolean cont = true;
				while (cont) {
					cont = false;
					for (i = 0; i < schedule.size(); i++) {
						Game g = schedule.get(i);
						p1 = g.h;
						p2 = g.a;
						if (!clients[g.h].busy && !clients[g.a].busy) {
							GameThread game = new GameThread(clients[g.h], g.h,
									clients[g.a], g.a, g.gameNum, move.clone(),
									st.clone());
							game.start();
							clients[g.h].busy = clients[g.a].busy = true;
							threads.add(game);
							schedule.remove(i);
							System.out.printf("%s-%s (%d)%n",
									clients[g.h].name, clients[g.a].name,
									remain);
							cont = true;
						}
					}
				}
				i = 0;
				while (i < threads.size()) {
					GameThread t = threads.get(i);
					if (t.isAlive()) {
						i++;
					} else {
						p1 = t.homeID;
						p2 = t.awayID;
						clients[p1].busy = false;
						clients[p2].busy = false;
						GameState.Status outcome = t.result;
						threads.remove(i);
						remain--;

						if (outcome == GameState.Status.HOME_WIN) {
							System.out.printf("home (%s) won%n", clients[p1].name);
							headToHeadSummary[p1][p2][0]++;
							awaySummary[p2][1]++;
							homeSummary[p1][0]++;
							totalSummary[p1][0]++;
							totalSummary[p2][1]++;
						} else if (outcome == GameState.Status.AWAY_WIN) {
							System.out.printf("away (%s) won%n", clients[p2].name);
							headToHeadSummary[p1][p2][1]++;
							awaySummary[p2][0]++;
							homeSummary[p1][1]++;
							totalSummary[p1][1]++;
							totalSummary[p2][0]++;
						} else if (outcome == GameState.Status.DRAW) {
							System.out.println(" draw\n");
							headToHeadSummary[p1][p2][2]++;
							awaySummary[p2][2]++;
							homeSummary[p1][2]++;
							totalSummary[p1][2]++;
							totalSummary[p2][2]++;
						} else {
							System.err.println("Error with game outcome");
						}
					}
				}
			}

			for (int j = 0; j < NUM_CLIENTS; j++) {
				clients[j].simpleMsg("DONE");
			}

			displaySummary("", format, NUM_CLIENTS, headToHeadSummary,
					homeSummary);

			System.out.printf("%n%n");
			for (p1 = 0; p1 < NUM_CLIENTS; p1++) {
				System.out.printf(format + "%3s ",
						printableName(clients[p1].name),
						clients[p1].DQd ? "DQd" : "  ");
				displaySummary(totalSummary[p1][0], totalSummary[p1][1],
						totalSummary[p1][2]);
				System.out.println();
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println("Server problem" + e);
		}
	}
	public static void launchSystematicTournament(GameState st, GameMove move) {
		File dumpsDir = new File("Dumps");
		dumpsDir.mkdir();
		Date date = new Date();
		String dumpDir = "Dumps" + SEP + date.toString() + SEP;
		dumpDir = dumpDir.replace(":", ";");
		File file = new File(dumpDir);
		file.mkdir();
		GameThread.dir = dumpDir;
//		String [] systematicPlayers = { "Systematic2"};
		String [] systematicPlayers = { "Systematic", "Systematic2","Random", "Greedy"};
		int NUM_SYSTEMATIC_CLIENTS = systematicPlayers.length;
		int NUM_ACTUAL_CLIENTS = tournamentParams.integer("NUMCLIENTS");
		int NUM_CLIENTS = NUM_ACTUAL_CLIENTS + NUM_SYSTEMATIC_CLIENTS;
		int PORT = tournamentParams.integer("PORT");
		int NUM_GAMES = tournamentParams.integer("NUMGAMES");

		int INIT_LIMIT = tournamentParams.integer("INITTIME");
		int MOVE_LIMIT = tournamentParams.integer("MOVETIME");
		int GAME_LIMIT = tournamentParams.integer("GAMETIME");
		int FINAL_PLAY_LIMIT = tournamentParams.integer("MAXMOVETIME");
		int MAX_WARNINGS = tournamentParams.integer("NUMWARNINGS");
		int HUMAN_TIME = tournamentParams.integer("HUMANTIME");

		int p1, p2;
		int i;
		int headToHeadSummary[][][] = new int[NUM_CLIENTS][NUM_CLIENTS][3];
		int homeSummary[][] = new int[NUM_CLIENTS][3];
		int awaySummary[][] = new int[NUM_CLIENTS][3];
		int totalSummary[][] = new int[NUM_CLIENTS][3];
		ArrayList<Game> schedule = new ArrayList<Game>();
		threads = new ArrayList<GameThread>();
		String format = gameParams.bool("TIES") ? "%13s " : "%9s ";

		try {
			ServerSocket socket = new ServerSocket(PORT);
			clients = new Client[NUM_CLIENTS];
			for (int j=0; j<NUM_SYSTEMATIC_CLIENTS; j++) {
				clients[NUM_ACTUAL_CLIENTS+j] = new PlayerClient(gameName, systematicPlayers[j]);
				File subdir = new File(dumpDir + SEP + clients[NUM_ACTUAL_CLIENTS+j].name);
				subdir.mkdir();
			}
			for (i = 0; i < NUM_ACTUAL_CLIENTS; i++) {
				clients[i] = new NetworkClient(socket, INIT_LIMIT, MOVE_LIMIT,
						FINAL_PLAY_LIMIT, GAME_LIMIT, MAX_WARNINGS);
				if (clients[i].name.toUpperCase().contains("HUMAN")) {
					clients[i].gameTimeLimit = clients[i].moveLimit = clients[i].finalPlayLimit = HUMAN_TIME;
				}
				File subdir = new File(dumpDir + SEP + clients[i].name);
				subdir.mkdir();
				System.out.printf("%s has joined%n", clients[i].name);
			}
			for (p1 = 0; p1 < NUM_ACTUAL_CLIENTS; p1++) {
				for (p2 = 0; p2 < NUM_SYSTEMATIC_CLIENTS; p2++) {
					schedule(schedule, NUM_GAMES, p1, NUM_ACTUAL_CLIENTS+p2);
					schedule(schedule, NUM_GAMES, NUM_ACTUAL_CLIENTS+p2, p1);
				}
			}
			shuffle(schedule);

			int remain = schedule.size();

			while (remain > 0) {
				Thread.sleep(500);
				boolean cont = true;
				while (cont) {
					cont = false;
					for (i = 0; i < schedule.size(); i++) {
						Game g = schedule.get(i);
						p1 = g.h;
						p2 = g.a;
						boolean playerAisBusy = clients[g.a] instanceof NetworkClient && clients[g.a].busy;
						boolean playerHisBusy = clients[g.h] instanceof NetworkClient && clients[g.h].busy;
						if (!playerAisBusy && !playerHisBusy) {
							GameThread game = new GameThread(clients[g.h], g.h,
									clients[g.a], g.a, g.gameNum, move.clone(),
									st.clone());
							game.start();
							clients[g.h].busy = clients[g.a].busy = true;
							threads.add(game);
							schedule.remove(i);
							System.out.printf("%s-%s (%d)%n",
									clients[g.h].name, clients[g.a].name,
									remain);
							cont = true;
						}
					}
				}
				i = 0;
				while (i < threads.size()) {
					GameThread t = threads.get(i);
					if (t.isAlive()) {
						i++;
					} else {
						p1 = t.homeID;
						p2 = t.awayID;
						clients[p1].busy = false;
						clients[p2].busy = false;
						GameState.Status outcome = t.result;
						threads.remove(i);
						remain--;

						if (outcome == GameState.Status.HOME_WIN) {
							System.out.printf("home (%s) won%n", clients[p1].name);
							headToHeadSummary[p1][p2][0]++;
							awaySummary[p2][1]++;
							homeSummary[p1][0]++;
							totalSummary[p1][0]++;
							totalSummary[p2][1]++;
						} else if (outcome == GameState.Status.AWAY_WIN) {
							System.out.printf("away (%s) won%n", clients[p2].name);
							headToHeadSummary[p1][p2][1]++;
							awaySummary[p2][0]++;
							homeSummary[p1][1]++;
							totalSummary[p1][1]++;
							totalSummary[p2][0]++;
						} else if (outcome == GameState.Status.DRAW) {
							System.out.println(" draw\n");
							headToHeadSummary[p1][p2][2]++;
							awaySummary[p2][2]++;
							homeSummary[p1][2]++;
							totalSummary[p1][2]++;
							totalSummary[p2][2]++;
						} else {
							System.err.println("Error with game outcome");
						}
					}
				}
			}

			for (int j = 0; j < NUM_CLIENTS; j++) {
				clients[j].simpleMsg("DONE");
			}

			displaySummary("", format, NUM_CLIENTS, headToHeadSummary,
					homeSummary);

			System.out.printf("%n%n");
			for (p1 = 0; p1 < NUM_CLIENTS; p1++) {
				System.out.printf(format + "%3s ",
						printableName(clients[p1].name),
						clients[p1].DQd ? "DQd" : "  ");
				displaySummary(totalSummary[p1][0], totalSummary[p1][1],
						totalSummary[p1][2]);
				System.out.println();
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println("Server problem" + e);
		}
	}
	public static void main(String[] args) {
		tournamentParams = new Params(GameState.CONFIG_DIR + "tournament.txt");
		gameName = tournamentParams.string("GAME");
		System.out.printf("Starting %s tournament%n", gameName.toString());
		System.out.printf("%d clients%n", tournamentParams.integer("NUMCLIENTS"));
		System.out.printf("%d clients%n", tournamentParams.integer("NUMCLIENTS2"));
		Tournament.launchTournament(gameName, true);
		System.out.println("Tournament is over");
	}
}
