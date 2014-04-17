
import java.util.Random;

interface Opponent
{
  public void pushObstaclePuyos (int obstacles);
  public void youWin ();
}

class PuyoGame implements Runnable, Opponent
{
  Thread thread;
  BoardTable table;
  Drop drop;
  BasicDrop nextDrop;
  boolean running, pausing;
  Random rand;

  Board board;
  NextDropArea nextArea;
  ObstacleArea obstacleArea;
  Opponent opponent;
  int modulo;
  int score;
  
  public final int width = 6;
  public final int height = 12;

  public PuyoGame ()
    {
      table = new BoardTable (width, height);
      drop = new Drop (table);
      nextDrop = new BasicDrop ();
      running = true;
      pausing = true;
      modulo = 0;
      score = 0;
      thread = new Thread (this);
      thread.start ();
    }

  public void setOpponent (Opponent opponent)
    { this.opponent = opponent; }

  public BoardTable getBoardTable ()
    { return table; }
  public Drop getDrop ()
    { return drop; }
  public BasicDrop getNextDrop ()
    { return nextDrop; }
  public void setBoard (Board board)
    { this.board = board; }
  public void setNextArea (NextDropArea nextArea)
    { this.nextArea = nextArea; }
  public void setObstacleArea (ObstacleArea obstacleArea)
    { this.obstacleArea = obstacleArea; }

  public boolean running ()
    { return running; }
    
  public synchronized void newGame (int seed)
    {  
      rand = new Random (seed);
      nextDrop.recreate (rand);
      nextArea.repaint ();
      drop.reinitialize ();
      obstacleArea.makeEmpty ();
      board.flushup ();
      running = true;
	  pausing = true;
    }
  public synchronized void startGame ()
    {
      if (running && pausing) {
		pausing = false;
		notify ();
	  }
    }
  public void pauseGame ()
    {
      if (running && !pausing) {
      	pausing = true;
      }
    }

  public void pushObstaclePuyos (int obstacles)
    {
      obstacleArea.addObstacles (obstacles);
    }

  public void youWin ()
    {
      running = false;
    }

  	public synchronized void run () {
 		if (Thread.currentThread () != thread)
 			return;
  		try {
	    	while (true) {
		  		if (!running || pausing)
		  			wait ();
		  		else {
					if (!obstacleArea.empty ())
					  table.obstacleFalling (board, 
								 obstacleArea.getObstacles (30), rand);

					if (!table.filledup ()) {
						drop.reinitialize ();
						drop.copyFrom (nextDrop);
						nextDrop.recreate (rand);
						nextArea.repaint ();

						drop.dropping (board);
						int shotScore = table.relaxing (board);
						if (shotScore > 0 && opponent != null)
					  		opponent.pushObstaclePuyos (computeObstacles (shotScore));					
					} else {
			    		if (opponent != null)
			      			opponent.youWin ();
			    		table.allPieceFalling (board);
					}
			    }
			}
		} catch (InterruptedException e) {
		 	e.printStackTrace ();
		}
    }

  private int computeObstacles (int shotScore)
    {
      shotScore += modulo;
      modulo = shotScore % 70;
      return shotScore / 70;
    }

  public void moveLeft ()
    { drop.moveLeft (); }
  public void moveRight ()
    { drop.moveRight (); }
  public void rotateLeft ()
    { drop.rotateLeft (); }
  public void rotateRight ()
    { drop.rotateRight (); }
  public void accelerate ()
    { drop.accelerate (); }
  public void deaccelerate ()
    { drop.deaccelerate (); }
}
