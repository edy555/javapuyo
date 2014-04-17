/*
 * puyopuyo game board
 *   written by tak (takahasi@huee.hokudai.ac.jp)
 */

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Event;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import java.lang.Thread;
import java.util.Enumeration;
import java.util.Random;

class Board extends Canvas
{
  PuyoGame game;
  BoardTable table;
  Drop drop;

  public final int width = 6;
  public final int height = 12;
  public final int pieceWidth = 24;
  public final int pieceHeight = 24;

  public Board (PuyoGame game, BoardTable table, Drop drop)
    {
      this.game = game;
      this.table = table;
      this.drop = drop;
      resize (width * pieceWidth, height * pieceHeight);
    }

/*
  public Dimension preferedSize ()
    { return new Dimension (24*6, 24*12); }
  public Dimension minimumSize ()
    { return preferedSize (); }
*/
	private Image image;
	private Graphics graph;
	private Dimension dim;

	private void prepareOffscreen () {
		Dimension d = size ();
		if (image == null || dim.width != d.width || dim.height != d.height) {
			image = createImage (d.width, d.height);
			graph = image.getGraphics ();
			dim = d;
		}
	}
	
	public void update (Graphics g) {
		prepareOffscreen ();
		table.clear (graph, this);
		drop.clear (graph, this);
		drop.paint (graph, this);
		table.paint (graph, this);
		g.drawImage (image, 0, 0, this);
	}

	public void paint (Graphics g) {
		Dimension d = size ();
		g.setColor (getBackground ());
		g.fillRect (0, 0, d.width, d.height);
		if (game.running ()) {
			prepareOffscreen ();
			drop.forcePaint (graph, this);
			table.forcePaint (graph, this);
			g.drawImage (image, 0, 0, this);
		}
	}

  public void flushup ()
    {
      table.flushup ();
      repaint ();
    }
}


class BoardTable
{
  static final int XOFFSET = 1;
  static final int YOFFSET = 2;

  TablePiece table[][];
  int width, height;
  public Pool fallDitherPool, splashPool;

  public BoardTable (int width, int height)
    {
      this.width = width;
      this.height = height;
      table = new TablePiece[width+2][height+3];
      fallDitherPool = new Pool ();
      splashPool = new Pool ();

      TablePiece wall = new TablePiece (Piece.WALL);
      int i;
      for (i = 0; i < width; i++)
	setTableEntry (i, height, wall);
      for (i = -1; i < height; i++)
	{
	  setTableEntry (-1, i, wall);
	  setTableEntry (width, i, wall);
	}

      for (int y = -2; y < height; y++)
	for (int x = 0; x < width; x++)
	  setTableEntry (x, y, new TablePiece (Piece.EMPTY));

      for (int y = -1; y < height; y++)
	for (int x = 0; x < width; x++)
	  tableEntry (x, y).
	    setSidePieces (tableEntry (x, y-1), tableEntry (x+1, y),
			   tableEntry (x, y+1), tableEntry (x-1, y));
    }

  public TablePiece tableEntry (int x, int y)
    { return table[x + XOFFSET][y + YOFFSET]; }

  public TablePiece setTableEntry (int x, int y, TablePiece p)
    {
      TablePiece old = table[x + XOFFSET][y + YOFFSET];
      table[x + XOFFSET][y + YOFFSET] = p;
      return old;
    }

  public boolean isEmpty (int x, int y)
    { return tableEntry (x, y).isEmpty (); }

  public void flushup ()
    {
      for (int y = 0; y < height; y++)
	for (int x = 0; x < width; x++)
	  tableEntry (x, y).makeEmpty ();
    }

  public void clear (Graphics g, Board board)
    {
      for (int y = 0; y < height; y++)
	for (int x = 0; x < width; x++)
	  {
	    TablePiece p = tableEntry (x, y);
	    p.clear (g, x * board.pieceWidth, y * board.pieceHeight, board);
	  }
    }
  public void forcePaint (Graphics g, Board board)
    {
      for (int y = 0; y < height; y++)
	for (int x = 0; x < width; x++)
	  {
	    TablePiece p = tableEntry (x, y);
	    p.forcePaint (g, x*board.pieceWidth, y*board.pieceHeight, board);
	  }
    }
  public void paint (Graphics g, Board board)
    {
      for (int y = 0; y < height; y++)
	for (int x = 0; x < width; x++)
	  {
	    TablePiece p = tableEntry (x, y);
	    p.paint (g, x*board.pieceWidth, y*board.pieceHeight, board);
	  }
    }

  private static final int STEP_INTERVAL = 30;

  public int relaxing (Board board)
    throws InterruptedException
    {
      Thread me = Thread.currentThread ();
      Enumeration e;
      int score = 0;

      for (int chain = 1; ; chain++)
	{
	  while (!fallDitherPool.isEmpty ())
	    {
	      for (e = fallDitherPool.elements (); e.hasMoreElements(); )
		{
		  TablePiece p = (TablePiece) e.nextElement ();
		  p.stepFallDither (this);
		}
	      board.repaint ();
	      me.sleep (STEP_INTERVAL);
	    }
	  
	  if (splashPool.isEmpty ())
	    break;

	  int groups = splashPool.size ();
	  int bonus = 0;

	  Pool pool = (Pool) splashPool.clone ();
	  for (e = splashPool.elements (); e.hasMoreElements(); )
	    {
	      TablePiece p = (TablePiece) e.nextElement (); 
	      int links = p.links ();
	      if (links > 4)   bonus += links - 3;
	      if (links == 11) bonus += 2;

	      p.collectConnectedPiece (pool);
	    }
	  int puyos = pool.size ();

	  splashPool = (Pool) pool.clone ();
	  for (e = pool.elements (); e.hasMoreElements(); )
	    {
	      TablePiece p = (TablePiece) e.nextElement ();
	      p.collectObstaclePiece (splashPool);
	    }

	  score += computeShotScore (chain, groups, bonus, puyos);

	  board.repaint ();
	  me.sleep (STEP_INTERVAL * 5);

	  for (e = splashPool.elements (); e.hasMoreElements(); )
	    {
	      TablePiece p = (TablePiece) e.nextElement ();
	      p.startSplashing ();
	    }

	  while (!splashPool.isEmpty ())
	    {
	      for (e = splashPool.elements (); e.hasMoreElements(); )
		{
		  TablePiece p = (TablePiece) e.nextElement ();
		  p.stepSplash (this);
		}
	      board.repaint ();
	      me.sleep (STEP_INTERVAL);
	    }
	}
      return score;
    }

  public void obstacleFalling (Board board, int obstacles, Random rand)
    throws InterruptedException
    {
      Thread me = Thread.currentThread ();
      Enumeration e;
      while (true)
	{
	  if (obstacles > 0 && dressingAreaEmpty ())
	    obstacles -= putObstacles (obstacles, rand);

	  if (fallDitherPool.isEmpty ())
	    break;
	  for (e = fallDitherPool.elements (); e.hasMoreElements(); )
	    {
	      TablePiece p = (TablePiece) e.nextElement ();
	      p.stepFallDither (this);
	    }
	  board.repaint ();
	  me.sleep (STEP_INTERVAL);
	}
    }

  private int computeShotScore (int chain, int groups, int bonus, int puyos)
    {
      int base = puyos * 10;
      if (groups > 1) bonus += 3 * (1 << groups) / 4;
      if (chain > 1)  bonus += 1 << (chain + 1);
      if (bonus == 0) bonus = 1;

      return base * bonus;
    }

  static final int dressingAreaY = -1;

  private int putObstacles (int obstacles, Random rand)
    {
      int n = 0, x = 0;
      if (obstacles > width)
	obstacles = width;

      while (n < obstacles && x < width)
	{
	  int rest_width = width - x - (obstacles - n);
	  if (rest_width > 0)
	    x += Math.abs (rand.nextInt ()) % rest_width;
	  TablePiece p = tableEntry (x, dressingAreaY);
	  if (p.isEmpty ())
	    {
	      p.setSpecie (Piece.OBSTACLE);
	      p.startFalling ();
	      fallDitherPool.add (p);
	      n++;
	    }
	  x++;
	}
      return n;
    }

  private boolean dressingAreaEmpty ()
    {
      int x;
      for (x = 0; x < width; x++)
	if (!tableEntry (x, dressingAreaY).isEmpty ())
	  return false;
      return true;
    }

  public boolean filledup ()
    {
      return ! tableEntry (2, 1).isEmpty ();
    }


  public void allPieceFalling (Board board)
    throws InterruptedException
    {
      Thread me = Thread.currentThread ();
      Enumeration e;
      for (int y = 0; y < height; y++)
	for (int x = 0; x < width; x++)
	  {
	    TablePiece p = tableEntry (x, y);
	    if (!p.isEmpty ())
	      p.startFalling (this);
	  }

      while (true)
	{
	  if (fallDitherPool.isEmpty ())
	    break;
	  for (e = fallDitherPool.elements (); e.hasMoreElements(); )
	    {
	      TablePiece p = (TablePiece) e.nextElement ();
	      p.stepFallDither (this);
	    }
	  clearBottom ();
	  board.repaint ();
	  me.sleep (STEP_INTERVAL);
	}
    }

  private void clearBottom ()
    {
      for (int x = 0; x < width; x++)
	{
	  TablePiece p = tableEntry (x, height - 1);
	  p.makeEmpty ();
	  fallDitherPool.remove (p);
	}
    }
}
