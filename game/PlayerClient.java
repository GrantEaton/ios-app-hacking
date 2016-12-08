package game;
import java.lang.reflect.*;

public class PlayerClient extends Client {
	private GamePlayer player;
	public PlayerClient(String gameName, String systematicName)
	{
		try {
			name = systematicName;
			String className = gameName.toLowerCase() + "." + systematicName + gameName + "Player";
			Class<?> playerClass = Class.forName(className);
			Constructor<?> constructor =
					playerClass.getConstructor(new Class[]{String.class});
			player = (GamePlayer)constructor.newInstance(systematicName);
		} catch (Exception e) {
			System.err.println("Problem launching");
			System.exit(-1);
		}
	}
	public double timedResponse(double seconds, GameMove move, String lastMove, GameState brd)
	{
		GameMove mv = player.getMove(brd, lastMove);
		move.parseMove(mv.toString());
		return 0;
	}
}
