/*
 * Piece
 *   written by tak (takahasi@huee.hokudai.ac.jp)
 */

import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.util.Random;

public class Piece
{
  static final int WALL = -2;
  static final int EMPTY = -1;
  static final int OBSTACLE = 5;
  static final int NSPECIES = 6;
  static PieceImage pi;
	
	static void setPieceImage (PieceImage pieceImage) {
		pi = pieceImage;
	}
	
  int specie;
  double xoffset, yoffset;

  public Piece ()
    {
      this.specie = EMPTY;
      xoffset = 0;
      yoffset = 0;
    }

  public Piece (int specie)
    {
      this.specie = specie;
      xoffset = 0.;
      yoffset = 0.;
    }

  public void setSpecie (int specie)
    { this.specie = specie; }

  public void setSpecieRandomly (Random rand)
    {
      specie = Math.abs (rand.nextInt ()) % 5;
    }

  public void copyFrom (Piece p)
    {
      specie = p.specie;
      xoffset = p.xoffset;
      yoffset = p.yoffset;
    }

  public boolean isEmpty ()
    {
      return specie == EMPTY;
    }

  public Image getImage ()
    {
      if (specie >= 0)
		return pi.getImage (specie);
      else
		return null;
    }

  public int getWidth (ImageObserver observer)
    { 
		return pi.getWidth ();
    }

  public int getHeight (ImageObserver observer)
    {
		return pi.getHeight ();
    }

  public void clear (Graphics g, int x, int y, ImageObserver observer)
    {
      int w = getWidth (observer);
      int h = getHeight (observer);
      g.fillRect (x + (int)(xoffset * w), y + (int)(yoffset * w), w, h);
    }

  public void paint (Graphics g, int x, int y, ImageObserver observer)
    {
      if (specie >= 0)
	{
	  int w = getWidth (observer);
	  int h = getHeight (observer);
	  g.drawImage (getImage (), x+(int)(xoffset * w), y+(int)(yoffset * h),
		       observer);
	}
    }

  public boolean isSameSpiece (Piece p)
    {
      return p.specie == specie;
    }
}

class TablePiece extends Piece
{
  private static final int NUM_SIDES = 4;
  static final int SPLASHING_THRESHOLD = 4;

  TablePiece sides[];
  boolean mark = false;
  int links;
  int connection;
  boolean falling, splashing, dithering; // animation status
  int count;  // skip count for animation
  int frame;  // frame # of animation
  boolean changed; // true if redraw is needed
  Piece previous; // backup for redraw

  public TablePiece (int specie)
    {
      sides = new TablePiece[NUM_SIDES];
      previous = new Piece (specie);
      this.specie = specie;
      changed = true;
      falling = false;
    }

  public void setSidePieces (TablePiece n, TablePiece e,
			     TablePiece s, TablePiece w)
    {	
      sides[0] = n; sides[1] = e; sides[2] = s; sides[3] = w;
    }

  public void copyFrom (Piece p)
    {
      super.copyFrom (p);
      changed = true;
      falling = false;
      dithering = false;
      splashing = false;
      frame = 0;
      previous.copyFrom (this);
    }
  public void copyFrom (TablePiece p)
    {
      super.copyFrom (p);
      changed = true;
      falling = p.falling;
      dithering = p.dithering;
      splashing = p.splashing;
      frame = p.frame;
      count = p.count;
      previous.copyFrom (this);
    }

  public void setSpecie (int specie)
    {
      if (this.specie != specie)
	{
	  this.specie = specie;
	  changed = true;
	}
    }

  public void makeEmpty ()
    {
      changed = true;
      splashing = false;
      dithering = false;
      frame = 0;
      count = 0;
      mark = false;
      setSpecie (EMPTY);
      connection = 0;
    }

  public void clear (Graphics g, int x, int y, ImageObserver observer)
    {
      if (changed)
	{
	  previous.clear (g, x, y, observer);
	  previous.copyFrom (this);
	}
    }

  public void forcePaint (Graphics g, int x, int y, ImageObserver observer)
    {
      if (specie >= 0)
	{
	  if (getWidth (observer) != 0)
	    changed = false;
	  super.paint (g, x, y, observer);
	}
    }

  public void paint (Graphics g, int x, int y, ImageObserver observer)
    {
      if (specie >= 0 && changed)
	forcePaint (g, x, y, observer);
    }

  public Image getImage ()
    {
      if (frame < 0)
	return pi.normalImage (specie, 0);
      else if (dithering)
	return pi.ditherImage (specie, frame);
      else if (splashing)
	return pi.splashImage (specie, frame);
      else
	return pi.normalImage (specie, connection);
    }

  private void computeConnection ()
    {
      int c = 0;
      if (specie >= 0 && specie != OBSTACLE)
	for (int i = 0; i < NUM_SIDES; i++)
	  if (isSameSpiece (sides[i]))
	    c |= 1<<i;

      if (connection != c)
	{
	  connection = c;
	  changed = true;
	}
    }

  private int markCount ()
    {
      if (mark) return 0;
      mark = true;
      int n = 0;
      for (int i = 0; i < NUM_SIDES; i++)
	if ((connection & (1<<i)) != 0)
	  n += sides[i].markCount ();
      return n + 1;
    }

  private void propagateCount (int n)
    {
      if (!mark) return;
      mark = false;
      links = n;
      for (int i = 0; i < NUM_SIDES; i++)
	if ((connection & (1<<i)) != 0)
	  sides[i].propagateCount (n);
    }

  public int links ()
    { return links; }
  private void computeLinks (BoardTable table)
    {
      if (specie >= 0 && specie != OBSTACLE && !mark)
	{
	  int c = markCount ();
	  propagateCount (c);
	  if (checkToSplash ())
	    {
	      table.splashPool.add (this);
	      propagateMark ();
	    }
	}
    }

  private void propagateMark ()
    {
      if (!mark)
	{
	  mark = true;
	  for (int i = 0; i < NUM_SIDES; i++)
	    if ((connection & (1<<i)) != 0)
	      sides[i].propagateMark ();
	}
    }

  public void collectConnectedPiece (Pool pool)
    {
      for (int i = 0; i < NUM_SIDES; i++)
	if ((connection & (1<<i)) != 0)
	  {
	    TablePiece p = sides[i];
	    if (!pool.contains (p))
	      { pool.add (p); p.collectConnectedPiece (pool); }
	  }
    }
  public void collectObstaclePiece (Pool pool)
    {
      for (int i = 0; i < NUM_SIDES; i++)
	if (sides[i].specie == OBSTACLE && !pool.contains (sides[i]))
	  pool.add (sides[i]);
    }

  private void computeConnectionAndLinksWithAroundPiece (BoardTable table)
    {
      computeConnection ();
      for (int n = 0; n < NUM_SIDES; n++)
	sides[n].computeConnection ();

      computeLinks (table);
      for (int n = 0; n < NUM_SIDES; n++)
	sides[n].computeLinks (table);
    }

  public void stepFallDither (BoardTable table)
    {
      if (falling)        stepFalling (table);
      else if (dithering) stepDithering (table);
      else table.fallDitherPool.remove (this);
    }
  public void stepSplash (BoardTable table)
    {
      if (splashing) stepSplashing (table);
      else table.splashPool.remove (this);
    }

  public void startFalling ()
    {
      TablePiece south = sides[Direction.SOUTH];
      if (!isEmpty () && (south.isEmpty () || south.falling))
	{
	  falling = true;
	  count = 0;
	}
      else
	startDithering ();
    }
  public void startFalling (BoardTable table)
    {
      if (!isEmpty ())
	{
	  falling = true;
	  count = 0;
	  table.fallDitherPool.add (this);
	}
    }
  private void stepFalling (BoardTable table)
    {
      TablePiece south = sides[Direction.SOUTH];
      double step = 0.1 * count;
      if (step > 1.0) step = 1.0;

      yoffset += step;
      count++;
      changed = true;
      if (yoffset >= 0.)
	{
	  if (south.isEmpty ())
	    {
	      yoffset -= 1.0;
	      south.copyFrom (this);
	      makeEmpty ();
	      computeConnectionAndLinksWithAroundPiece (table);
	      table.fallDitherPool.remove (this);
	      table.fallDitherPool.add (south);
	    }
	  else if (south.falling)
	    {
	      yoffset = 0;
	    }
	  else
	    {
	      yoffset = 0.0;
	      falling = false;
	      connection = 0;
	      startDithering ();
	    }
	}
    }

  private void startDithering ()
    {
      dithering = true;
      frame = -1;
      count = 0;
    }

  private static final int DITHERING_COUNT = 3;

  private void stepDithering (BoardTable table)
    {
      if (specie >= 0 && dithering)
	{
	  if (count < DITHERING_COUNT)
	    { 
	      count++;
	      return;
	    }
	  else
	    {
	      count = 0;
	      ++frame;
	      changed = true;
	      if (frame < pi.ditherImageFrames())
		return;
	    }
	}
      frame = 0;
      dithering = false;
      table.fallDitherPool.remove (this);
      computeConnectionAndLinksWithAroundPiece (table);
    }

  private boolean checkToSplash ()
    {
      return !isEmpty () && links >= SPLASHING_THRESHOLD;
    }
  public void startSplashing ()
    {
      splashing = true;
      count = 0;
      frame = -1;
    }
  private static final int SPLASHING_COUNT = 5;
  private static final int SURPRISING_COUNT = 20;
  private void stepSplashing (BoardTable table)
    {
      if (frame != 0 ? ++count >= SPLASHING_COUNT
	             : ++count >= SURPRISING_COUNT)
	{
	  ++frame;
	  count = 0;
	  changed = true;
	  if (frame < pi.splashImageFrames())
	    return;
	  makeEmpty ();
	  table.splashPool.remove (this);
	  startFallingPieceNorthSide (table);
	}
    }

  private void startFallingPieceNorthSide (BoardTable table)
    {
      if (specie >= 0 && !falling)
	startFalling (table);
      TablePiece north = sides[Direction.NORTH];
      if (north != null)
	north.startFallingPieceNorthSide (table);
    }
}
