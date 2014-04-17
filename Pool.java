
import java.util.Vector;

class Pool extends Vector
{
  public void add (Object obj)
    {
      if (!contains (obj))
	addElement (obj);
    }
    
  public void remove (Object obj)
    {
      removeElement (obj);
    }

//  public Pool clone ()
//    {
//      return (Pool)super.clone ();
//    }
}
