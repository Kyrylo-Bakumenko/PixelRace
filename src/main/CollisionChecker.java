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
        double normalAngle = barrier.findCollisionNormal(x, y);
        switch ((int) (normalAngle / 90)){
            case 0: System.out.println("E");
            case 1: System.out.println("S");
            case 2: System.out.println("W");
            case 3: System.out.println("N");
        }
        double nx = Math.max(Math.abs(vx), 0.01) * Math.cos(Math.toRadians(normalAngle)) / barrier.stiffness;
        double ny = Math.max(Math.abs(vy), 0.01) * Math.sin(Math.toRadians(normalAngle)) / barrier.stiffness;
        System.out.println("NX: " + nx + ", NY: " + ny);
        // modify velocity with normal
        vx += nx;
        vy += ny;
        // update entity angle
        entity.angle = Barrier.angleOfVector(vx, vy);
        // apply velocity penalty due to collision
        entity.v = 0.95 * entity.v;
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
        Rectangle r = entity.solidArea;
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

//    public void checkTile(Entity entity){
//        int eLeftWorldX = (int) (entity.worldX + entity.solidArea.x);
//        int eRightWorldX = (int) (entity.worldX + entity.solidArea.x + entity.solidArea.width);
//        int eTopWorldY = (int) (entity.worldY + entity.solidArea.y);
//        int eBottomWorldY = (int) (entity.worldY + entity.solidArea.y + entity.solidArea.height);
//
//        int eLeftCol = eLeftWorldX / Display.tileSize;
//        int eRightCol =  eRightWorldX / Display.tileSize;
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
