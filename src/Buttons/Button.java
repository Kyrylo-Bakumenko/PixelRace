package Buttons;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class Button {
    int x;
    int y;
    int width;
    int height;
    BufferedImage image;
    String path;
    boolean selected;
    int selectionPad = 8; // pixel pad for white border

    public Button(){
        int x, y = 0;
        int width, height = 100;
        this.selected = false;
    }
    public Button(BufferedImage image, String path){
        int x, y = 0;
        this.selected = false;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.path = path.replace("thumbnails", "tiles");
    }
    public Button(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected = false;
    }

    public Button(int x, int y, BufferedImage image, String path){
        this.x = x;
        this.y = y;
        this.selected = false;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.path = path.replace("thumbnails", "tiles");
    }

    public void render(Graphics g){
        if(selected){
            Color temp = g.getColor();
            g.setColor(Color.WHITE);
            g.fillRect(x-selectionPad, y-selectionPad, width+2*selectionPad, height+2*selectionPad);
            g.setColor(temp);
        }
        if(image != null){
            g.drawImage(image, x, y, null);
            return;
        }
        g.fillRect(x, y, width, height);
    }

    public boolean contains(MouseEvent e){
        int mx = e.getX();
        int my = e.getY();
        return mx >= x && my >= y && mx <= x+width && my <= y+height;
    }

    // return fullsize image of thumbnail
    public BufferedImage getImage(){
        try{
            return ImageIO.read(new File(path));
        }
        catch(Exception ignore){
            return null;
        }
    }

    public void toggleSelected(){
        this.selected = !this.selected;
    }

    public void setSelection(boolean selected){
        this.selected = selected;
    }

    public boolean getSelection(){
        return selected;
    }
}
