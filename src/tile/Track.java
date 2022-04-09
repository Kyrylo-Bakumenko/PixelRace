package tile;

import main.Display;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Track {
    int nRow;
    int nCol;
    BufferedImage[][] grid = new BufferedImage[Display.WIDTH/imgDim + 1][Display.HEIGHT/imgDim]; // screen ratio: 4x3, realized as 12x9 grid
    static final int imgDim = 128; // images are 128x128

    public Track() throws IOException {
        for(int row = 0; row < grid.length; row++){
            for(int col = 0; col < grid[0].length; col++){
                grid[row][col] = ImageIO.read(new File("res/imgs/tiles/grass.png"));
            }
        }
    }
    
    public void setSquare(int x, int y, BufferedImage image){
        grid[x/128][y/128] = image;
    }

    public void render(Graphics g){
        for(int row = 0; row < grid.length; row++){
            for(int col = 0; col < grid[0].length; col++){
                g.drawImage(grid[row][col], row*imgDim, col*imgDim, null);
            }
        }
    }
}
