package entity;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {

    public double worldX, worldY;
    // velocity
    public double v;

    public BufferedImage img;
    // angle
    public double angle;

    public int spriteCounter = 0;
    public int spriteNum = 1;

    public Rectangle solidArea;
    // bounding box
    public int x1, y1, x2, y2;
    // mid point (for rotations)
    public double mx, my;
    public boolean collisionOn = false;
}
