package game;

public abstract class Client {
		public String name;
		public boolean DQd;
		public boolean busy;
		public int moveLimit;
		public double gameTimeRemaining, gameTimeLimit;
		public int finalPlayLimit;
		public int maxWarnings;
		public static final boolean DUMP = false;
		public void simpleMsg(String s)
		{ }
		public void simpleMsg(String s1, String s2)
		{ }
		public void simpleMsg(String s1, String s2, String s3)
		{ }
		public void flush()
		{ }
		public void setTimeout(int d) throws Exception
		{ }
		public String hardLimitResponse(int seconds)
		{ return ""; }
		public abstract double timedResponse(double seconds, GameMove move, String lastMove, GameState brd);
}
