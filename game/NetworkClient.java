/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkClient extends Client {
	private Socket sock;
	private PrintWriter output;
	private BufferedReader input;
	public NetworkClient(ServerSocket mainSocket, int initTimeLimit,
					int pLimit, int fLimit,
					int gLimit, int nWarnings) throws Exception
	{
		sock = mainSocket.accept();
		output = new PrintWriter(sock.getOutputStream(), true);
		input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		name = hardLimitResponse(initTimeLimit);
		if (name == null) {
			DQd = true;
			name = "DQd on initialization";
			return;
		}
		maxWarnings = nWarnings;
		moveLimit = pLimit;
		finalPlayLimit = fLimit;
		gameTimeLimit = gLimit;
		busy = false;
	}
    private static void flushSocket(BufferedReader in)
    {
    	try {
	    	String cmd = in.readLine();
	    	while (!cmd.equals("OVER")) {
	    		cmd = in.readLine();
	    	}
    	}
    	catch (Exception e) {
    	}
    }
	public void setTimeout(int d) throws Exception
	{ sock.setSoTimeout(d); }
	public void flush()
	{ flushSocket(input); }
	public void simpleMsg(String s)
	{
		if (DUMP) System.err.println("SDUMP1/1: " + s);
		output.println(s);
		if (DUMP) System.err.println("EDUMP");
	}
	public void simpleMsg(String s1, String s2)
	{
		if (DUMP) {
			System.err.println("DUMP1/2: " + s1);
			System.err.println("DUMP2/2: " + s2);
		}
		output.println(s1);
		output.println(s2);
		if (DUMP) System.err.println("EDUMP");
	}
	public void simpleMsg(String s1, String s2, String s3)
	{
		if (DUMP) {
			System.err.println("DUMP1/3: " + s1);
			System.err.println("DUMP2/3: " + s2);
			System.err.println("DUMP3/3: " + s3);
		}
		output.println(s1);
		output.println(s2);
		output.println(s3);
		if (DUMP) System.err.println("EDUMP");
	}
	public String hardLimitResponse(int seconds)
	{
		String name = null;
		try {
			sock.setSoTimeout(seconds * 1000);
			name = input.readLine();
		}
		catch (Exception e) {
			System.err.printf("init timeout %s %d%n", e.toString(), seconds);
			System.err.flush();
		}
		return name;
	}
	public double timedResponse(double seconds, GameMove move, String lastMove, GameState brd)
	{
		long start = System.currentTimeMillis();
		try {
			sock.setSoTimeout((int)(seconds * 1000));
			if (DUMP) {
				System.err.println("Timed response: " + seconds);
			}
			String mvStr = input.readLine();
			if (DUMP) {
				System.err.println("RESPONSE: " + mvStr);
			}
			long diff = System.currentTimeMillis() - start;
			
			output.println("TIME");
			output.println(diff/1000.0);
			move.parseMove(mvStr);

			double elapsedTime = diff / 1000.0;
			return elapsedTime;
		}
		catch (Exception e) {
			System.err.printf("timeout %s%n", name);
			output.println("TIME");
			output.println(seconds + 10.0);
			return -1; 
		}
	}
}
