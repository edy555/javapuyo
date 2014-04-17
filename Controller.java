
import java.awt.Event;

abstract class Controller
{
  public abstract void start ();
  public abstract void pause ();
  public abstract void newGame (int seed);
  public abstract boolean handleEvent (Event e);
}

class SingleController extends Controller
{
  PuyoGame player;

  public SingleController (PuyoGame p)
    {
      player = p;
    }

  public void start ()
    {
      player.startGame ();
    }
  public void pause ()
    {
      player.pauseGame ();
    }
  public void newGame (int seed)
    {
      player.newGame (seed);
    }

  public boolean handleEvent (Event e)
    {
      switch (e.id)
	{
	case Event.KEY_ACTION:
	case Event.KEY_PRESS:
	  switch (e.key)
	    {
	    case 's':
	      start ();
	      return true;
	    case 'p':
	      pause ();
	      return true;

	    case 'h':
	      player.moveLeft ();
	      return true;
	    case 'j':
	      player.rotateLeft ();
	      return true;
	    case 'k':
	      player.rotateRight ();
	      return true;
	    case 'l':
	      player.moveRight ();
	      return true;
	    case ' ':
	      player.accelerate ();
	      return true;
	    }
	  break;
	case Event.KEY_RELEASE:
	  switch (e.key)
	    {
	    case ' ':
	      player.deaccelerate ();
	      return true;
	    }
	  break;
	}

      return false;
    }
}  

abstract class VSController extends Controller
{
  PuyoGame player1;
  PuyoGame player2;

  public VSController (PuyoGame p1, PuyoGame p2)
    {
      player1 = p1;
      player2 = p2;
    }

  public void start ()
    {
      player1.startGame ();
      player2.startGame ();
    }
  public void pause ()
    {
      player1.pauseGame ();
      player2.pauseGame ();
    }
  public void newGame (int seed)
    {
      player1.newGame (seed);
      player2.newGame (seed);
    }
}

class VSLocalController extends VSController
{
  public VSLocalController (PuyoGame p1, PuyoGame p2)
    {
      super (p1, p2);
    }

  public boolean handleEvent (Event e)
    {
      switch (e.id)
	{
	case Event.KEY_ACTION:
	case Event.KEY_PRESS:
	  switch (e.key)
	    {
	    case 's':
	      start ();
	      return true;
	    case 'p':
	      pause ();
	      return true;

	    case 'q':
	      player1.moveLeft ();
	      return true;
	    case 'w':
	      player1.rotateLeft ();
	      return true;
	    case 'e':
	      player1.rotateRight ();
	      return true;
	    case 'r':
	      player1.moveRight ();
	      return true;
	    case 'z':
	      player1.accelerate ();
	      return true;

	    case 'h':
	      player2.moveLeft ();
	      return true;
	    case 'j':
	      player2.rotateLeft ();
	      return true;
	    case 'k':
	      player2.rotateRight ();
	      return true;
	    case 'l':
	      player2.moveRight ();
	      return true;
	    case ' ':
	      player2.accelerate ();
	      return true;
	    }
	  break;
	case Event.KEY_RELEASE:
	  switch (e.key)
	    {
	    case 'z':
	      player1.deaccelerate ();
	      return true;
	    case ' ':
	      player2.deaccelerate ();
	      return true;
	    }
	  break;
	}

      return false;
    }
}






