


import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Dimension;

class NextDropArea extends Canvas
{
  BasicDrop nextDrop;
  PuyoGame game;

  public NextDropArea (PuyoGame game, BasicDrop nextDrop)
    {
      this.nextDrop = nextDrop;
      this.game = game;
      resize (24*3, 24*3);
    }
  
  public void paint (Graphics g)
    {
      Dimension d = size ();
      g.fillRect (0, 0, d.width, d.height);
      if (game.running ())
	nextDrop.paint (g, 24, 36, this);
    }
}


