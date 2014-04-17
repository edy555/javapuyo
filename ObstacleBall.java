

import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.MediaTracker;
import java.awt.Dimension;

class ObstacleBall
{
	static PieceImage pi;
	
	static void setPieceImage (PieceImage pieceImage) {
		pi = pieceImage;
	}
	
	int type;
	int pieces;
	
	public ObstacleBall (int type, int pieces) {
		this.type = type;
		this.pieces = pieces;
	}
	
	public int pieces () {
		return pieces;
	}
	public Image getImage () { 
		return pi.getObstacleImage (type); 
	}
	public int getWidth (ImageObserver obserber) { 
		return getImage ().getWidth (obserber); 
	}
}

class ObstacleArea extends Canvas
{
  int nObstacles;
  ObstacleBall redBall;
  ObstacleBall whiteBall;
  ObstacleBall miniBall;
  PuyoGame game;

  public ObstacleArea (PuyoGame game)
    {
      this.game = game;
      redBall   = new ObstacleBall (1, 30);
      whiteBall = new ObstacleBall (2, 6);
      miniBall  = new ObstacleBall (3, 1);
      resize (24*6, 24*2);
    }

  public Dimension preferedSize ()
    { return new Dimension (24*6, 24*2); }

  public void addObstacles (int n)
    {
      if (n != 0)
	{
	  nObstacles += n;
	  repaint ();
	}
    }

  public int getObstacles (int max)
    {
      if (nObstacles > max)
	{
	  nObstacles -= max;
	  repaint ();
	  return max;
	}
      int n = nObstacles;
      nObstacles = 0;
      repaint ();
      return n;
    }

  public int cancelObstacles (int n)
    {
      if (nObstacles < n)
	{
	  nObstacles = 0;
	  repaint ();
	  return n - nObstacles;
	}
      nObstacles -= n;
      repaint ();
      return 0;
    }

  public boolean empty ()
    {
      return nObstacles == 0;
    }

  public void makeEmpty ()
    {
      nObstacles = 0;
      repaint ();
    }
	  
  public void paint (Graphics g)
    {
      int x = 0;
      Dimension d = size ();
      g.fillRect (0, 0, d.width, d.height);
      for (int n = nObstacles; n > 0; )
	{
	  if (n >= redBall.pieces ())
	    {
	      g.drawImage (redBall.getImage (), x, 0, this);
	      x += redBall.getWidth (this);
	      n -= redBall.pieces ();
	    }
	  else if (n >= whiteBall.pieces ())
	    {
	      g.drawImage (whiteBall.getImage (), x, 0, this);
	      x += whiteBall.getWidth (this);
	      n -= whiteBall.pieces ();
	    }
	  else 
	    {
	      g.drawImage (miniBall.getImage (), x, 0, this);
	      x += miniBall.getWidth (this);
	      n--;
	    }
	}
    }
}
