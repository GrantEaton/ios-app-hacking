/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;

import java.util.*;

public class Util {
	public static class Pair {
		public int row, col;
		public Pair(int r, int c)
		{ row = r; col = c; }
	}
	
	public static void clear(char [][] ary, char empty)
	{
		for (int r=0; r<ary.length; r++) {
			Arrays.fill(ary[r], empty);
		}
	}
	public static boolean inrange(int v, int lo, int hi)
	{ return v >= lo && v <= hi; }
	public static boolean inrange(int v, int hi)
	{ return inrange(v, 0, hi); }
	public static void copy(char [][] dest, char [][] src)
	{
		int NUM_ROWS = src.length;
		int NUM_COLS = src[0].length;
		for (int r=0; r<NUM_ROWS; r++) {
			for (int c=0; c<NUM_COLS; c++) {
				dest[r][c] = src[r][c];
			}
		}
	}
	public static char [][] clone(char [][] ary)
	{
		int NUM_ROWS = ary.length;
		int NUM_COLS = ary[0].length;
		char [][] copyAry = new char [NUM_ROWS][NUM_COLS];
		copy(copyAry, ary);
		return copyAry;
	}
	public static void parseMsgString(String s, char [][] grid, char empty)
	{
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int cnt = 0;
		for (int r=NUM_ROWS-1; r>=0; r--) {
			for (int c=0; c<NUM_COLS; c++, cnt++) {
				grid[r][c] = s.charAt(cnt);
			}
		}
	}
	public static void parseMsgString(String s, StringBuffer [][] grid)
	{
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int capacity = grid[0][0].capacity();
		int cnt = 0;
		for (int r=NUM_ROWS-1; r>=0; r--) {
			for (int c=0; c<NUM_COLS; c++, cnt+=capacity) {
				grid[r][c].replace(0, capacity, s.substring(cnt, cnt+capacity));
			}
		}
	}
	public static String toString(StringBuffer [][] grid)
	{
		int capacity = grid[0][0].capacity();
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int NUM_SPOTS = NUM_ROWS * NUM_COLS * (capacity+1);
		StringBuffer buf = new StringBuffer(NUM_SPOTS + NUM_ROWS);
		for (int r=NUM_ROWS-1; r>=0; r--) {
			for (int c=0; c<NUM_COLS; c++) {
				buf.append(grid[r][c]);
				buf.append(' ');
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	public static String toString(char [][] grid)
	{
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int NUM_SPOTS = NUM_ROWS * NUM_COLS;
		StringBuffer buf = new StringBuffer(NUM_SPOTS + NUM_ROWS);
		for (int r=NUM_ROWS-1; r>=0; r--) {
			buf.append(grid[r]);
			buf.append('\n');
		}
		return buf.toString();
	}
	public static String msgString(char [][] grid)
	{
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int NUM_SPOTS = NUM_ROWS * NUM_COLS;
		StringBuffer buf = new StringBuffer(NUM_SPOTS);
		for (int r=NUM_ROWS-1; r>=0; r--) {
			buf.append(grid[r]);
		}
		return buf.toString();
	}
	public static String msgString(StringBuffer [][] grid)
	{
		int capacity = grid[0][0].capacity();
		int NUM_ROWS = grid.length;
		int NUM_COLS = grid[0].length;
		int NUM_SPOTS = NUM_ROWS * NUM_COLS * capacity;
		StringBuffer buf = new StringBuffer(NUM_SPOTS);
		for (int r=NUM_ROWS-1; r>=0; r--) {
			for (int c=0; c<NUM_COLS; c++) {
				buf.append(grid[r][c]);
			}
		}
		return buf.toString();
	}
	public static int randInt(int lo, int hi)
	{
		int delta = hi - lo + 1;
		return lo + (int)(Math.random() * delta);
	}
}
