/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;
import java.util.*;
import java.io.*;
	
public class Params {
	public HashMap<String, String> map = new HashMap<String, String>();
	public Params(String fname)
	{
		try {
			Scanner input = new Scanner(new File(fname));
			String line;
			while (input.hasNext()) {
				line = input.nextLine().trim();
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				StringTokenizer toks = new StringTokenizer(line, "=");
				String key = toks.nextToken();
				String val = toks.nextToken();
				map.put(key, val);
			}
			input.close();
		}
		catch (Exception e) {
			System.err.printf("Problem reading %s%n", fname);
		}
	}
	public boolean defined(String key)
	{ return map.containsKey(key); }
	public boolean bool(String key)
	{ return defined(key) ? Boolean.parseBoolean(key) : false; }
	public String string(String key)
	{ return map.get(key); }
	public int integer(String key)
	{ return defined(key) ? Integer.parseInt(string(key)) : 0; }
	public char character(String key)
	{ return string(key).charAt(0); }
}
