package tile;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

public class Tile {

    public BufferedImage image;
    public boolean collision = false;

    public Tile(String filePath, int scale){
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            this.image = scale(image, scale);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static BufferedImage scale(BufferedImage before, double scale) {
        int w = before.getWidth();
        int h = before.getHeight();
        // Create a new image of the proper size
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        scaleOp.filter(before, after);
        return after;
    }
}
