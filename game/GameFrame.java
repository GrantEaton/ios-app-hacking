/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package game;
import java.awt.*;

public class GameFrame extends Frame {
	public GameCanvas canvas;
	public static final long serialVersionUID = 0;
    public GameFrame(String name, GameCanvas can)
    {
        super(name);
        canvas = can;
        setSize(canvas.getW(), canvas.getH());
        addWindowListener(new WindowCloser());
        add(canvas);
    }
}
