
import java.awt.Graphics;
import java.lang.Thread;
import java.awt.image.ImageObserver;

class Drop extends BasicDrop
{
  BoardTable table;
  int x, y;
  double xoffset, yoffset;

  boolean changed;
  boolean rotating;
  boolean hidden;
  int count;

  Drop previous;

  private static final int ROTATING_LEFT = 0;
  private static final int ROTATING_RIGHT = 1;
  int status;

  public Drop (BoardTable table)
    {
      this.table = table;
      dir = new SubDirection (4);
      dir.setDirection (Direction.NORTH);
      center = new Piece (0);
      satellite = new Piece (0);
      rotating = false;
      hidden = true;
    }

  public void copyFrom (Drop drop)
    {
      super.copyFrom (drop);
      x = drop.x;
      y = drop.y;
      xoffset = drop.xoffset;
      yoffset = drop.yoffset;
    }

  private static final int INITIAL_X = 2;
  private static final int INITIAL_Y = -1;
  public void reinitialize ()
    {
      dir.setDirection (Direction.NORTH);
      x = INITIAL_X;
      y = INITIAL_Y;
      xoffset = 0.; yoffset = 0.;
      count = 0;

      accelerated = false;
      hidden = false;
      rotating = false;

      if (previous == null)
	previous = new Drop (null);
      previous.copyFrom (this);
      changed = true;
    }

  private int centerX (int w)
    { return x * w + (int)(xoffset*(double)w); }
  private int centerY (int h)
    { return y * h +(int)(yoffset*(double)h); }

  public void clear (Graphics g, ImageObserver observer)
    {
      if (changed && previous != null)
	{
	  int w = previous.center.getWidth (observer);
	  int h = previous.center.getHeight (observer);
	  previous.clear (g, previous.centerX (w), previous.centerY (h),
			  observer);
	  previous.copyFrom (this);
	}
    }

  public void forcePaint (Graphics g, ImageObserver observer)
    {
      if (!hidden)
	{
	  int w = center.getWidth (observer);
	  int h = center.getHeight (observer);
	  super.paint (g, centerX (w), centerY (h), observer);
	}
      changed = false;
    }

  public void paint (Graphics g, ImageObserver observer)
    {
      if (changed)
	forcePaint (g, observer);
    }

  private static final int FAST_INTERVAL = 20;
  private static final int SLOW_INTERVAL = 150;
  boolean accelerated = false;

  public void dropping (Board board) throws InterruptedException
    {
      Thread me = Thread.currentThread ();
      boolean conti = true;

      while (conti)
	{
	  me.sleep (FAST_INTERVAL);
	  for (int n = 0; !accelerated && n < 5; n++)
	    {
	      if (rotating)
		{
		  rotating = stepRotate ();
		  board.repaint ();
		}

	      me.sleep (FAST_INTERVAL);
	    }
	  conti = stepSlidedown ();
	  board.repaint ();
	}

      conti = true;
      while (conti)
	conti = waitBaking ();
      board.repaint ();
    }

  private boolean stepSlidedown ()
    {
      yoffset += 0.5;
      changed = true;
      if (yoffset >= 0)
	{
	  if (rotating)
	    {
	      yoffset -= 0.5;
	      return true;
	    }
	  else if (tryToMove (x, y+1))
	    {
	      yoffset -= 1.;
	      return true;
	    }
	  yoffset = 0.;
	  return false;
	}
      return true;
    }

  private boolean waitBaking ()
    {
      if (count < 5)
	{
	  count++;
	  return true;
	}
      else
	{
	  bakePieces ();
	  return false;
	}
    }

  private void bakePieces ()	
    {
      TablePiece c = table.tableEntry (x, y);
      TablePiece s = table.tableEntry (x + dir.xoffset(), y + dir.yoffset());
      changed = true;
      hidden = true;

      c.copyFrom (center);
      c.startFalling ();
      table.fallDitherPool.add (c);

      s.copyFrom (satellite);
      s.startFalling ();
      table.fallDitherPool.add (s);
    }

  public void accelerate ()
    { accelerated = true; }
  public void deaccelerate ()
    { accelerated = false; }


  private synchronized boolean stepRotate ()
    {
      boolean conti = false;
      changed = true;
      switch (status)
	{
	case ROTATING_LEFT:
	  conti = dir.stepRotateLeft ();
	  break;
	case ROTATING_RIGHT:
	  conti = dir.stepRotateRight ();
	  break;
	}

      return conti;
    }

  private void startRotate (int newStatus)
    {
      status = newStatus;
      rotating = true;

    }
	
  public synchronized void rotateLeft ()
    {
      if (!hidden && !rotating &&
	  ableToRotate (dir.leftDirection ()))
	startRotate (ROTATING_LEFT);
    }

  public synchronized void rotateRight ()
    {
      if (!hidden && !rotating && 
	  ableToRotate (dir.rightDirection ()))
	startRotate (ROTATING_RIGHT);
    }

  private boolean ableToRotate (int d)
    {
      if (table.isEmpty (x + Direction.xoffset (d), y + Direction.yoffset (d)))
	return true;
      else
	switch (d)
	  {
	  case Direction.NORTH:
	    return tryToMoveRotate (x, y+1, d);
	  case Direction.EAST:
	    return tryToMoveRotate (x-1, y, d);
	  case Direction.SOUTH:
	    return tryToMoveRotate (x, y-1, d);
	  case Direction.WEST:
	    return tryToMoveRotate (x+1, y, d);
	  }
      return false;
    }

  public synchronized void moveLeft ()
    {
      if (!hidden && !rotating)
	tryToMove (x - 1, y);
    }
  public synchronized void moveRight ()
    {
      if (!hidden && !rotating)
	tryToMove (x + 1, y);
    }

  private boolean tryToMove (int x, int y)
    {
      return tryToMoveRotate (x, y, dir.dir);
    }

  private boolean tryToMoveRotate (int x, int y, int d)
    {
      if (ableToMoveRotate (x, y, d))
	{
	  this.x = x; this.y = y;
	  changed = true;
	  return true;
	}
      return false;
    }

  private boolean ableToMoveRotate (int x, int y, int d)
    {
      return table.isEmpty (x, y)
	  && table.isEmpty (x + Direction.xoffset (d),
			    y + Direction.yoffset (d));
    }
}


class Direction
{
  static final int NORTH = 0;
  static final int EAST = 1;
  static final int SOUTH = 2;
  static final int WEST = 3;
  static final int NUMDIRS = 4;

  static final int xoffsettbl[] = { 0, 1, 0, -1};
  static final int yoffsettbl[] = { -1, 0, 1, 0};

  public int dir = NORTH;

  public void setDirection (int d)
    {
      if (d >= 0 && d < NUMDIRS)
	dir = d;
    }

  public void copyFrom (Direction from)
    {
      dir = from.dir;
    }

  public int xoffset ()
    {
      return xoffsettbl[dir];
    } 

  public int yoffset ()
    {
      return yoffsettbl[dir];
    } 

  static int xoffset (int d)
    { return xoffsettbl[d]; }
  static int yoffset (int d)
    { return yoffsettbl[d]; }
  public int rightDirection ()
    { int d = dir + 1;
      if (d >= NUMDIRS) d -= NUMDIRS;
      return d;
    }
  public int leftDirection ()
    { int d = dir - 1;
      if (d < 0) d += NUMDIRS;
      return d;
    }

  public void rotateLeft ()
    { dir = leftDirection (); }
  public void rotateRight ()
    { dir = rightDirection (); }
}

class SubDirection extends Direction
{
  double offsettbl[][];
  int division;
  int subdir;

  public SubDirection (int div) // of right angle(90 degrees).
    {
      division = div;
      offsettbl = new double[2][division * 4];

      for (int n = 0; n < division * 4; n++)
	{
	  offsettbl[0][n] =   Math.sin (Math.PI / 2. * n / (double)division);
	  offsettbl[1][n] = - Math.cos (Math.PI / 2. * n / (double)division);
	}
    }

  public void copyFrom (SubDirection from)
    {
      super.copyFrom (from);
      subdir = from.subdir;
    }

  public double xoffsetf ()
    {
      int d = dir * division + subdir;
      return offsettbl[0][d];
    }
  public double yoffsetf ()
    {
      int d = dir * division + subdir;
      return offsettbl[1][d];
    }

  public boolean stepRotateLeft ()
    {
      subdir--;
      if (subdir < 0)
	{
	  rotateLeft ();
	  subdir += division;
	}
      return subdir != 0;
    }
  public boolean stepRotateRight ()
    {
      subdir++;
      if (subdir >= division) 
	{
	  rotateRight ();
	  subdir -= division;
	}
      return subdir != 0;
    }
}
