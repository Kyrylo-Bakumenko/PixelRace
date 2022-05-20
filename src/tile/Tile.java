package tile;

import entity.Barrier;
import main.Display;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

public class Tile {

    public BufferedImage image;
    public boolean collision = false;
    public Barrier barrier;
    final int layer;

    public Tile(String filePath, int scale){
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            this.image = scale(image, scale);
            // generic collision bounds
            this.barrier = new Barrier(new Rectangle(0, 0, Display.tileSize, Display.tileSize));
            // create tile specific collision bounds
            int type = -1;
            if(filePath.contains("c")) type = Integer.parseInt(filePath.substring(filePath.indexOf("c") + 1, filePath.indexOf(".")));
            if(filePath.contains("top") || type == 1 || type == 12)
                this.barrier = new Barrier(new Rectangle(0, 80*scale, Display.tileSize*scale, 16*scale));
            else if(filePath.contains("bot") || type == 6 || type == 7)
                this.barrier = new Barrier(new Rectangle(0, 44*scale, Display.tileSize*scale, 16*scale));
            else if(filePath.contains("vr") || type == 3 || type == 4)
                this.barrier = new Barrier(new Rectangle(40*scale, 0, 4*scale, Display.tileSize*scale));
            else if(filePath.contains("vl") || type == 9 || type == 10)
                this.barrier = new Barrier(new Rectangle(84*scale, 0, 4*scale, Display.tileSize*scale));
            else if(type == 2)
                this.barrier = new Barrier(new Rectangle(0, Display.tileSize/2, Display.tileSize/2, Display.tileSize/2));
            else if(type == 5)
                this.barrier = new Barrier(new Rectangle(0, 0, Display.tileSize/2, Display.tileSize/2));
            else if(type == 8)
                this.barrier = new Barrier(new Rectangle(Display.tileSize/2, 0, Display.tileSize/2, Display.tileSize/2));
            else if(type == 11)
                this.barrier = new Barrier(new Rectangle(Display.tileSize/2, Display.tileSize/4, Display.tileSize/2, Display.tileSize/2));

        }catch (Exception e){
            e.printStackTrace();
        }
        this.layer = 0;
    }

    public Tile(String filePath, int scale, int layer){
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            this.image = scale(image, scale);
        }catch (Exception e){
            e.printStackTrace();
        }
        this.layer = layer;
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
