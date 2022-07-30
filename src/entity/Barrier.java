package entity;

import main.CollisionChecker;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Barrier {

    public Polygon bounds;
    public double stiffness;
    private static final double DEFAULT_STIFFNESS = 0.99;
    public ArrayList<Particle> sparks;
    private double[] normalAngles;

    public Barrier(Polygon bounds){
        this.bounds = bounds;
        setDefaultValues();
    }
    public Barrier(Rectangle bounds){
        this.bounds = RectangleToPolygon(bounds);
        setDefaultValues();
    }
    public Barrier(Polygon bounds, double stiffness){
        this.bounds = bounds;
        this.stiffness = stiffness;
        setDefaultValues();
    }
    public void setStiffness(double stiffness){
        this.stiffness = stiffness;
    }
    private void setDefaultValues(){
        int npoints = this.bounds.npoints;
        this.normalAngles = new double[npoints];

        int prevX = this.bounds.xpoints[npoints-1];
        int prevY = this.bounds.ypoints[npoints-1];
        for(int i = 0; i < npoints; i++) {
            int ux = this.bounds.xpoints[i] - prevX;
            int uy = this.bounds.ypoints[i] - prevY;
            prevX = this.bounds.xpoints[i];
            prevY = this.bounds.ypoints[i];

            this.normalAngles[i] = (angleOfVector(ux, uy) + 270) % 360;
            System.out.println(normalAngles[i]);
        }

        this.stiffness = DEFAULT_STIFFNESS;
        this.sparks = new ArrayList<>();
    }

    public static double angleOfVector(double ux, double uy){
        double den = (Math.sqrt(Math.pow(1, 2) + Math.pow(0, 2)) * (Math.sqrt(Math.pow(ux, 2) + Math.pow(uy, 2))));
        double cos = ux / den;
        double angle = Math.toDegrees(Math.acos(cos));
        if(uy < 0) angle = 360 - angle;
        return angle;
    }

    public static double angleBetweenVectors(double vx, double vy, double ux, double uy){
        double num = vx * ux + vy * uy;
        double den = (Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)) * (Math.sqrt(Math.pow(ux, 2) + Math.pow(uy, 2))));
        double cos = num / den;
        return Math.toDegrees(Math.acos(cos));
    }

    public double findCollisionNormal(int x, int y, int centerx, int centery) {
        // the index for getting normal to line that is closest to point
        int npoints = this.bounds.npoints;
        int x1 = this.bounds.xpoints[npoints - 1];
        int y1 = this.bounds.ypoints[npoints - 1];

        // for every barrier-side, see if this intersects entity point-to-center cord
        for (int i = 0; i < npoints; i++) {
            int x2 = this.bounds.xpoints[i];
            int y2 = this.bounds.ypoints[i];

            if(CollisionChecker.linesIntersect(x, y, centerx, centery, x1, y1, x2, y2))
                return normalAngles[i];

            x1 = this.bounds.xpoints[i];
            y1 = this.bounds.ypoints[i];
        }
        return findCollisionNormal(x, y);
    }

    public double findCollisionNormal(double x, double y) {
        // the index for getting normal to line that is closest to point
        int contactLineIdx = 0;
        double minDistanceToLine = Integer.MAX_VALUE;

        int npoints = this.bounds.npoints;
        int x1 = this.bounds.xpoints[npoints - 1];
        int y1 = this.bounds.ypoints[npoints - 1];

        for (int i = 0; i < npoints; i++) {
            int x2 = this.bounds.xpoints[i];
            int y2 = this.bounds.ypoints[i];

            // draw vector from x1, y1 to next point in boundary and target x, y
            // project <x-x1, y-y1> (vector A) onto <x2-x1, y2-y1> (vector B), call it vector A1
            double ax = x - x1;
            double ay = y - y1;
            double bx = x2 - x1;
            double by = y2 - y1;
            double ab_dot = ax * bx + ay * by;
            double b_len = Math.sqrt(Math.pow(bx, 2) + Math.pow(by, 2));
            double a1_len = ab_dot / b_len;
            // if ||A1|| < 0 or ||A1|| > ||B||, then the closest point of contact will off the boundary
            double dist;
            if(a1_len < 0 || a1_len > b_len){
                // dist between x, y and x1, y1 is ||A||
                // dist between x, y and x2, y2 is ||B^ - A^|| or simply ||<x-x2, y-y2>||
                dist = Math.min(Math.pow(ax, 2) + Math.pow(x - x2, 2), + Math.pow(y - y2, 2));
            }else{
                dist = distanceBetweenVectors(ax, ay, bx, by);
            }
            if(dist < minDistanceToLine){
                contactLineIdx = i;
                minDistanceToLine = dist;
            }

            x1 = this.bounds.xpoints[i];
            y1 = this.bounds.ypoints[i];
        }

        return normalAngles[contactLineIdx];
    }

    public double distanceBetweenVectors(double ax, double ay, double bx, double by){
        // position of point rel one end of line
        // ax, ay
        // vector along line
        // bx, by
        // orthogonal vector
        // -by, bx

        double dot = ax * -by + ay * bx;
        double len_sq = -by * -by + bx * bx;

        return Math.abs(dot) / Math.sqrt(len_sq);
    }

    public static Polygon RectangleToPolygon(Rectangle rect) {
        int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
        int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};
        return new Polygon(xpoints, ypoints, 4);
    }

    public void debugRender(Graphics g, int screenX, int screenY){
        Graphics2D g2d = (Graphics2D) g.create();
        int npoints = this.bounds.npoints;
        int x1 = this.bounds.xpoints[npoints - 1];
        int y1 = this.bounds.ypoints[npoints - 1];

        for (int i = 0; i < npoints; i++) {
            int x2 = this.bounds.xpoints[i];
            int y2 = this.bounds.ypoints[i];

            Color temp = g2d.getColor();
            if(normalAngles[i] == 0.0){
                g2d.setColor(Color.YELLOW);
            }else if(normalAngles[i] == 90.0){
                g2d.setColor(Color.GREEN);
            }else if(normalAngles[i] == 180.0){
                g2d.setColor(Color.RED);
            }else if(normalAngles[i] == 270.0){
                g2d.setColor(Color.BLUE);
            }
            int lineThickness = 3;
            g2d.setStroke(new BasicStroke(lineThickness));
            g2d.drawLine(x1 + screenX, y1 + screenY, x2 + screenX, y2 + screenY);
            g2d.setColor(temp);

            x1 = this.bounds.xpoints[i];
            y1 = this.bounds.ypoints[i];
        }
    }




}
