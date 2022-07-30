package main;

import entity.Barrier;
import entity.Car;
import entity.Entity;
import tile.Tile;

import java.awt.*;

public class CollisionChecker {

    Display gp;

    public CollisionChecker(Display gp){
        this.gp = gp;
    }

    private void applyCollision(Entity entity, double x, double y, Barrier barrier){
        // get velocity components
        double vx = Math.cos(Math.toRadians(entity.angle));
        double vy = Math.sin(Math.toRadians(entity.angle));
        // determine reflection direction off of bounding rectangle shape
        double normalAngle = barrier.findCollisionNormal((int)x, (int)y, (int)(entity.worldX + entity.mx), (int)(entity.worldY + entity.my));
        switch ((int) (normalAngle / 90)){
//            case 0: System.out.println("E");
//            case 1: System.out.println("S");
//            case 2: System.out.println("W");
//            case 3: System.out.println("N");
        }
        double nx = Math.abs(vx) * Math.cos(Math.toRadians(normalAngle)) / barrier.stiffness;
        double ny = Math.abs(vy) * Math.sin(Math.toRadians(normalAngle)) / barrier.stiffness;
//        System.out.println("NX: " + nx + ", NY: " + ny);
        // modify velocity with normal
        vx += nx;
        vy += ny;
        // update entity angle
        entity.angle = Barrier.angleOfVector(vx, vy);
        // apply velocity penalty due to collision
        entity.v = Math.pow(entity.v, 0.75);
        // set collision flag to true
        entity.collisionOn = true;
    }

    private boolean checkPointCollision(Entity entity, double x, double y){
        boolean collision = false;
        double[] rotatedCoords = Rotater.rotateWithAnchor(x, y,
                entity.worldX + entity.mx, entity.worldY + entity.my, entity.angle);
        int row = (int)(rotatedCoords[0]/Display.tileSize);
        int col = (int)(rotatedCoords[1]/Display.tileSize);
        int tileNum = gp.tileM.mapLayers[1][row][col];
        // if tile is empty, skip
        if(tileNum == -1) return false;
        // translate bounds polygon into world-space for determining intersection
        Polygon p = gp.tileM.tile[tileNum].barrier.bounds;
        p.translate(row*Display.tileSize, col*Display.tileSize);
        Rectangle r = entity.solidArea.getBounds();
        r.translate((int) entity.worldX, (int) entity.worldY);
        if(p.intersects(r)){
            applyCollision(entity, x, y, gp.tileM.tile[tileNum].barrier);
            collision = true;
        }
        p.translate(-row*Display.tileSize, -col*Display.tileSize);
        r.translate(-((int)entity.worldX), -((int)entity.worldY));
        return collision;
    }

    public void checkTile(Entity entity){
        // is there currently a collision
        // TL corner
        if(checkPointCollision(entity, entity.worldX + entity.x1, entity.worldY + entity.y1)) return;
        // TR corner
        if(checkPointCollision(entity, entity.worldX + entity.x2, entity.worldY + entity.y1)) return;
        // BL corner
        if(checkPointCollision(entity, entity.worldX + entity.x1, entity.worldY + entity.y2)) return;
        // BR corner
        if(checkPointCollision(entity, entity.worldX + entity.x2, entity.worldY + entity.y2)) return;
        // see if collision will occur should the car move
        double xOffset = (Math.cos(Math.toRadians(entity.angle)) * entity.v);
        double yOffset = (Math.sin(Math.toRadians(entity.angle)) * entity.v);
        // if collision is applied return to avoid layering collisions
        // TL corner
        if(checkPointCollision(entity, entity.worldX + entity.x1 + xOffset, entity.worldY + entity.y1 + yOffset)) return;
        // TR corner
        if(checkPointCollision(entity, entity.worldX + entity.x2 + xOffset, entity.worldY + entity.y1 + yOffset)) return;
        // BL corner
        if(checkPointCollision(entity, entity.worldX + entity.x1 + xOffset, entity.worldY + entity.y2 + yOffset)) return;
        // BR corner
        if(checkPointCollision(entity, entity.worldX + entity.x2 + xOffset, entity.worldY + entity.y2 + yOffset)) return;
    }

//    private double polygonIntersection(Polygon p1, Polygon p2){
//        Polygon union = new Polygon(p1.xpoints, p1.ypoints, p1.npoints);
//        for(int i = 0; i < p2.npoints; i++)
//            union.addPoint(p2.xpoints[i], p2.ypoints[i]);
//
//        double areaDiff = polygonArea(union) - polygonArea(p1) - polygonArea(p2);
//        System.out.println(areaDiff);
//
//        return areaDiff < 0 ? areaDiff : 0;
//    }

//    private boolean polygonIntersection(Polygon p1, Polygon p2){
//        for(int i = 1; i < p1.npoints; i++){
//            int x0 = p1.xpoints[i-1];
//            int x1 = p1.xpoints[i];
//            int y0 = p1.ypoints[i-1];
//            int y1 = p1.ypoints[i];
//            for(int j = 1; j < p2.npoints; j++){
//                int x2 = p2.xpoints[j-1];
//                int x3 = p2.xpoints[j];
//                int y2 = p2.ypoints[j-1];
//                int y3 = p2.ypoints[j];
//                if(linesIntersect(x0, y0, x1, y1, x2, y2, x3, y3)) return true;
//            }
//        }
//
//        return false;
//    }

    public static boolean linesIntersect(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3){
        if( (x1-x0 == 0 && x3-x2 == 0) || (y1-y0 == 0 && y3-y2 == 0)){
            //parallel
            return false;
        }
        if(x1 - x0 == 0)
            return x2 <= x0 && x0 <= x3 || x3 <= x0 && x0 <= x2;
        if(x3 - x2 == 0)
            return x0 <= x2 && x2 <= x1 || x1 <= x2  && x2 <= x0;
        double x = (y2-y0)/( (y1-y0)/(double)(x1-x0) - (y3-y2)/(double)(x3-x2) );
        System.out.println((x >= x0 && x <= x1 || x >= x1 && x <= x0) && (x >= x2 && x <= x3 || x >= x3 && x <= x2));
        return (x >= x0 && x <= x1 || x >= x1 && x <= x0) && (x >= x2 && x <= x3 || x >= x3 && x <= x2);
    }

    private boolean polygonIntersection(Polygon p1, Polygon p2){
        for(int i = 0; i < p2.npoints; i++)
            if(p1.contains(p2.xpoints[i], p2.ypoints[i])) return true;

        return false;
    }

    private Polygon rotatePolygon(Polygon p, double anchorx, double anchory, double theta){
        int[] xpoints = new int[p.npoints];
        int[] ypoints = new int[p.npoints];

        for(int i = 0; i < p.npoints; i++) {
            double[] rotatedCoords = Rotater.rotateWithAnchor(p.xpoints[i], p.ypoints[i], anchorx, anchory, theta);
            xpoints[i] = (int)rotatedCoords[0];
            ypoints[i] = (int)rotatedCoords[1];
        }

        return new Polygon(xpoints, ypoints, p.npoints);
    }

    private double polygonArea(Polygon p){
        if(p.npoints < 1) return 0;
        double area = 0;
        for(int i = 0; i+1 < p.npoints/2 ; i++){
            // quadrilateral ABCD
            // find area by vectors
            // area = 1/2 (AB x AC + AC x AD)
            int ABx = p.xpoints[i+1] - p.xpoints[i];
            int ABy = p.ypoints[i+1] - p.ypoints[i];
            int ACx = p.xpoints[p.npoints-1 -(i+1)] - p.xpoints[i];
            int ACy = p.ypoints[p.npoints-1 -(i+1)] - p.ypoints[i];
            int ADx = p.xpoints[p.npoints-1 -i] - p.xpoints[i];
            int ADy = p.ypoints[p.npoints-1 -i] - p.ypoints[i];

            area += 0.5 * (Math.abs(ABx * ACy - ABy * ACx) + Math.abs(ACx * ADy - ACy * ADx));
        }
        return area;
    }

    public boolean intersects(int testx, int testy, int x1, int y1, int x2, int y2) {
        if ((testy >= y1 && testy >= y2) || (testy <= y1 && testy <= y2)) return false;
        int intersectX = (x1 + ((testy - y1) * (x2 - x1)) / (y2 - y1));
        System.out.println("test: " + testx + " tgt: " + intersectX);
        return intersectX > testx;
    }

    public boolean contains(Polygon p, int testx, int testy){
        if(!p.contains(testx, testy)) return false;
        int intersections = 0;
        for(int i = 1; i < p.npoints; i++) {
            int x1 = p.xpoints[i-1];
            int x2 = p.xpoints[i];
            int y1 = p.ypoints[i-1];
            int y2 = p.ypoints[i];
            if (intersects(testx, testy, x1, y1, x2, y2)) intersections++;
        }
        return intersections % 2 == 1;
    }

//    public void checkTile(Entity entity){
//        int eLeftWorldX = (int) (entity.worldX + entity.solidArea.x);
//        int eRightWorldX = (int) (entity.worldX + entity.solidArea.x + entity.solidArea.width);
//        int eTopWorldY = (int) (entity.worldY + entity.solidArea.y);
//        int eBottomWorldY = (int) (entity.worldY + entity.solidArea.y + entity.solidArea.height);
//
//        int eLeftCol = eLeftWorldX / Display.tileSize;
//        sssssssint eRightCol =  eRightWorldX / Display.tileSize;
//        int eTopRow = eTopWorldY / Display.tileSize;
//        int eBottomRow = eBottomWorldY / Display.tileSize;
//
//        // if outside of map boundries, ignore
//        int nCols = gp.tileM.mapLayers[1].length;
//        int nRows = gp.tileM.mapLayers[1][0].length;
//        if(eLeftCol >= nCols || eRightCol >= nCols || eTopRow >= nRows || eBottomRow >= nRows || eLeftCol < 0 || eRightCol < 0 || eTopRow < 0 || eBottomRow < 0) return;
//
//        int tileNum1, tileNum2;
//
//        int direction = ((int)entity.angle % 360) / 90;
//        // reminder, all layers > 0 are layered on top of base layer
//        // therefore we need to consider them for collision
//        int speed = (int) entity.v;
//        switch (direction){
//            // right
//            case 0:
//                eTopRow = (eTopWorldY - speed) / Display.tileSize;
//                tileNum1 = gp.tileM.mapLayers[1][eLeftCol][eTopRow];
//                tileNum2 = gp.tileM.mapLayers[1][eRightCol][eTopRow];
//                if( (tileNum1!=-1 && gp.tileM.tile[tileNum1].collision) || (tileNum2!=-1 && gp.tileM.tile[tileNum2].collision) ){
//                    entity.collisionOn = true;
//                    entity.angle = direction * 90.0;
//                }
//            // down
//            case 1:
//                eBottomRow = (eBottomWorldY + speed) / Display.tileSize;
//                tileNum1 = gp.tileM.mapLayers[1][eLeftCol][eBottomRow];
//                tileNum2 = gp.tileM.mapLayers[1][eRightCol][eBottomRow];
//                if( (tileNum1!=-1 && gp.tileM.tile[tileNum1].collision) || (tileNum2!=-1 && gp.tileM.tile[tileNum2].collision) ){
//                    entity.collisionOn = true;
//                    entity.angle = direction * 90.0;
//                }
//            // left
//            case 2:
//                eLeftCol = (eLeftWorldX - speed) / Display.tileSize;
//                tileNum1 = gp.tileM.mapLayers[1][eLeftCol][eTopRow];
//                tileNum2 = gp.tileM.mapLayers[1][eLeftCol][eBottomRow];
//                if( (tileNum1!=-1 && gp.tileM.tile[tileNum1].collision) || (tileNum2!=-1 && gp.tileM.tile[tileNum2].collision) ){
//                    entity.collisionOn = true;
//                    entity.angle = direction * 90.0;
//                }
//            // up
//            case 3:
//                eRightCol = (eRightWorldX + speed) / Display.tileSize;
//                tileNum1 = gp.tileM.mapLayers[1][eRightCol][eTopRow];
//                tileNum2 = gp.tileM.mapLayers[1][eRightCol][eBottomRow];
//                if( (tileNum1!=-1 && gp.tileM.tile[tileNum1].collision) || (tileNum2!=-1 && gp.tileM.tile[tileNum2].collision) ){
//                    entity.collisionOn = true;
//                    entity.angle = direction * 90.0;
//                }
//        }
//    }
}
