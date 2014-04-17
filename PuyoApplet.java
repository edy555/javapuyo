import java.applet.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;
import java.net.*;

public class PuyoApplet extends Applet
{
	MediaTracker tracker;
	Controller controller;
	boolean ready = false;
	Random rand;
	
	public void init () {
		String param;
		URL url = null;
		int size;
		
		param = getParameter ("puyotile");
		try {
			if (param != null)
				url = new URL (getDocumentBase (), param);
		} catch (MalformedURLException e) {
			e.printStackTrace ();
		}
		if (url == null)
			try {
				url = new URL (getDocumentBase (), "puyotile.gif");
			} catch (MalformedURLException e) {
				System.exit (-1);
			}
		
		param = getParameter ("piecesize");
		if (param != null)
			size = Integer.parseInt (param);
		else size = 24;

		PieceImage pi = new PieceImage (url, this, size, size);

		Piece.setPieceImage (pi);
		ObstacleBall.setPieceImage (pi);
		rand = new Random (1);

		setLayout (new BorderLayout ());
		
		String mode = getParameter ("mode");
		if (mode.equals ("versuslocal"))
		init_versuslocal ();
		else
		init_single ();
		
		Panel p = new Panel ();
		add ("South", p);
		p.add (new Button ("Start"));
		p.add (new Button ("Pause"));
		p.add (new Button ("New Game"));
		
		requestFocus ();
	}

  private void init_single ()
    {
      PuyoGame player = new PuyoGame ();
      player.setOpponent (player);

      controller = new SingleController (player);

      Panel p = new Panel ();
      Board board = new Board (player,
			       player.getBoardTable (), player.getDrop ());
      p.add (board);
      add ("Center", p);
      player.setBoard (board);

      p = new Panel ();
      NextDropArea next = new NextDropArea (player, player.getNextDrop ());
      add ("East", p);
      p.add (next);
      player.setNextArea (next);

      p = new Panel ();
      ObstacleArea oa = new ObstacleArea (player);
      add ("North", p);
      p.add (oa);
      player.setObstacleArea (oa);

      int seed = rand.nextInt ();
      player.newGame (seed);
    }

  private void init_versuslocal ()
    {
      Board board;
      NextDropArea next;
      ObstacleArea oa;
      PuyoGame player1 = new PuyoGame ();
      PuyoGame player2 = new PuyoGame ();
      player1.setOpponent (player2);
      player2.setOpponent (player1);
      
      controller = new VSLocalController (player1, player2);

      Panel pa = new Panel ();
      add ("Center", pa);

      Panel p1 = new Panel ();
      p1.setLayout (new BorderLayout ());
      pa.add (p1);

      Panel p = new Panel ();
      p1.add ("Center", p);
      board = new Board (player1, 
			 player1.getBoardTable (), player1.getDrop ());
      p.add (board);
      player1.setBoard (board);

      p = new Panel ();
      p1.add ("East", p);
      next = new NextDropArea (player1, player1.getNextDrop ());
      p.add (next);
      player1.setNextArea (next);

      p = new Panel ();
      oa = new ObstacleArea (player1);
      p.add (oa);
      p1.add ("North", p);
      player1.setObstacleArea (oa);

      Panel p2 = new Panel ();
      p2.setLayout (new BorderLayout ());
      pa.add (p2);

      p = new Panel ();
      p2.add ("Center", p);
      board = new Board (player2,
			 player2.getBoardTable (), player2.getDrop ());
      p.add (board);
      player2.setBoard (board);

      p = new Panel ();
      p2.add ("West", p);
      next = new NextDropArea (player2, player2.getNextDrop ());
      p.add (next);
      player2.setNextArea (next);

      p = new Panel ();
      oa = new ObstacleArea (player2);
      p.add (oa);
      p2.add ("North", p);
      player2.setObstacleArea (oa);

      int seed = rand.nextInt ();
      player1.newGame (seed);
      player2.newGame (seed);
    }

  public void start ()
    {
/*
      showStatus ("Loading images...");
      try {
	tracker.waitForID(0);
      } catch (InterruptedException e) {
	showStatus ("Loading images... Interrupted!");
	return;
      }
      showStatus ("Loading images... Done!");
*/
    }

  public void stop ()
    {
      controller.pause ();
    }

  public boolean action (Event e, Object arg)
    {
      if (arg.equals ("Start")) {
	controller.start ();
	return true;
      } else if (arg.equals ("Pause")) {
	controller.pause ();
	return true;
      } else if (arg.equals ("New Game")) {
	int seed = rand.nextInt ();
	controller.newGame (seed);
	return true;
      } else 
	return super.action (e, arg);
    }

  public synchronized boolean handleEvent (Event e)
    {
      switch (e.id)
	{
	case Event.KEY_ACTION:
	case Event.KEY_PRESS:
	case Event.KEY_RELEASE:
	  if (controller.handleEvent (e))
	    return true;
	}
      return super.handleEvent (e);
    }
}
