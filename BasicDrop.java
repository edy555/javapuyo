import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.util.Random;

class BasicDrop
{
  Piece center;
  Piece satellite;
  SubDirection dir;

  public BasicDrop ()
    {
      center = new Piece (0);
      satellite = new Piece (0);
      dir = new SubDirection (4);
      dir.setDirection (Direction.NORTH);
    }

  public void copyFrom (BasicDrop drop)
    {
      center.copyFrom (drop.center);
      satellite.copyFrom (drop.satellite);
      dir.copyFrom (drop.dir);
    }
  public void recreate (Random rand)
    {
      center.setSpecieRandomly (rand);
      satellite.setSpecieRandomly (rand);
    }

  public void clear (Graphics g, int x, int y, ImageObserver observer)
    {
      int w = center.getWidth (observer);
      int h = center.getHeight (observer);
      center.clear (g, x, y, observer);
      satellite.clear (g, x + (int)(dir.xoffsetf () * w),
		          y + (int)(dir.yoffsetf () * h), observer);
    }
  public void paint (Graphics g, int x, int y, ImageObserver observer)
    {
      int w = center.getWidth (observer);
      int h = center.getHeight (observer);
      center.paint (g, x, y, observer);
      satellite.paint (g, x + (int)(dir.xoffsetf () * w),
		          y + (int)(dir.yoffsetf () * h), observer);
    }
}
