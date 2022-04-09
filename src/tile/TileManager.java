package tile;

import main.Display;

import java.awt.*;
import java.io.*;

public class TileManager {

    Display gp;
    Tile[] tile;
    int[][] mapTileNum;

    public TileManager(Display gp){
        this.gp = gp;

        this.tile = new Tile[gp.tileNames.length];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("res/maps/map01.txt");
    }

    public void getTileImage() {
        for(int i = 0; i < gp.tileNames.length; i++) {

            String filePath = "res/imgs/tiles/" + gp.tileNames[i];
            tile[i] = new Tile(filePath, Display.scale);
        }
    }

    public void loadMap(String filePath){
        try{
            InputStream is = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while(col < gp.maxWorldCol && row < gp.maxWorldRow){

                String line = br.readLine();

                while(col < gp.maxWorldCol){
                    String[] numbers = line.split(" ");

                    int num = Integer.parseInt(numbers[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }
                if(col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void render(Graphics g){
        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * Display.tileSize;
            int worldY = worldRow * Display.tileSize;
            int screenX = (int) (worldX - gp.car.worldX + gp.car.screenX);
            int screenY = (int) (worldY - gp.car.worldY + gp.car.screenY);

            // only render tiles that are visible on screen
            if(worldX + gp.tileSize > gp.car.worldX - gp.car.screenX &&
               worldX - gp.tileSize < gp.car.worldX + gp.car.screenX &&
               worldY + gp.tileSize> gp.car.worldY - gp.car.screenY &&
               worldY - gp.tileSize < gp.car.worldY + gp.car.screenY) {

               g.drawImage(tile[tileNum].image, screenX, screenY, Display.tileSize, Display.tileSize, null);
            }
            worldCol++;

            if(worldCol == gp.maxWorldCol){
                worldCol = 0;
                worldRow++;
            }
        }
    }

}
