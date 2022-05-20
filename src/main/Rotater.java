package main;

public class Rotater {
    public static double[] rotateWithAnchor(double x, double y, double anchorx, double anchory, double theta){
        // vectorize
        double vx = anchorx - x;
        double vy = anchory - y;

        // clockwise rotation
        return new double[]{x + vx*Math.cos(Math.toRadians(theta)) + vy*Math.sin(Math.toRadians(theta)),
                            y - vx*Math.sin(Math.toRadians(theta)) + vy*Math.cos(Math.toRadians(theta))};
    }
}
