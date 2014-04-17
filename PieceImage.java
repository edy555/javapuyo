import java.applet.Applet;
import java.awt.MediaTracker;
import java.net.URL;
import java.awt.Image;
import java.awt.image.*;

class PieceImage {
	private static final int NORMAL_FRAMES = 16;
	private static final int DITHER_FRAMES = 2;
	private static final int SPLASH_FRAMES = 5;
	private static final int TOTAL_FRAMES = NORMAL_FRAMES+DITHER_FRAMES+SPLASH_FRAMES;
	private static final int TOTAL_SPECIES = 6;
	private static final int SP_OBSTACLE = 5;
	
	Image ary[][];
	int pieceWidth, pieceHeight;
	MediaTracker tracker;
	
	PieceImage (URL url, Applet applet, int width, int height) {
		pieceWidth = width;
		pieceHeight = height;
      	tracker = new MediaTracker (applet);

		Image img = applet.getImage (url);
		tracker.addImage (img, 0);
		
		ary = new Image[TOTAL_SPECIES][TOTAL_FRAMES];

		ImageProducer source = img.getSource ();
		for (int y = 0; y < TOTAL_FRAMES; y++)
			for (int x = 0; x < TOTAL_SPECIES; x++) {
				ImageFilter filter = new CropImageFilter (x*pieceWidth, y*pieceHeight, pieceWidth, pieceHeight);
				ImageProducer prod = new FilteredImageSource (source, filter);
				ary[x][y] = applet.createImage (prod);
				tracker.addImage (ary[x][y], 1);
			}
		
		tracker.checkAll (true); // start loading
	}
	
	public Image normalImage (int specie, int n) {
		return ary[specie][n];
	}
	public Image getImage (int specie, int n) {
		return ary[specie][n];
	}
	
	public Image getObstacleImage (int n) {
		return ary[SP_OBSTACLE][n];
	}

	public Image getImage (int specie) {
		return getImage (specie, 0);
	}

	public int ditherImageFrames () {
		return DITHER_FRAMES;
	}
	
	public Image ditherImage (int specie, int n) { 
		return ary[specie][n+NORMAL_FRAMES];
	}
	public int splashImageFrames () {
		return SPLASH_FRAMES;
	}
	public Image splashImage (int specie, int n) {
		return ary[specie][n+NORMAL_FRAMES+DITHER_FRAMES]; 
	}
	
	public int getWidth () {
		return pieceWidth;
	}
	public int getHeight () {
		return pieceHeight;
	}
}


