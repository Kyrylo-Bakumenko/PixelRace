package entity;

import java.awt.*;
import java.util.Arrays;

public class Particle {
    private Color[] cycle;
    private int idx;
    private boolean active;
    // global
    private int x, y;

    public Particle(){
        this.cycle = new Color[]{Color.BLACK};
        this.idx = 0;
        this.active = false;
        this.x = 0;
        this.y = 0;
    }

    public Particle(int x, int y){
        this.cycle = new Color[]{Color.BLACK};
        this.idx = 0;
        this.active = false;
        this.x = x;
        this.y = y;
    }

    public void setCycleToGradient(Color color, int length){
        cycle = new Color[length];
        int delta = color.getAlpha()/length;

        for(int i = 0; i < length; i++){
            cycle[i] = color;
            color = new Color(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha()-delta);
        }
    }

    public void render(Graphics g, Car car){
        if(!active) return;

        Color temp = g.getColor();
        g.setColor(cycle[idx]);
        g.drawRect(car.screenX + (int)(this.x - car.worldX), car.screenY + (int)(this.y - car.worldY), 2, 2);
        g.setColor(temp);

        iterate();
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public boolean isActive(){
        return this.active;
    }

    private void iterate(){
        if(idx + 1 < cycle.length) idx++;
        else{
            idx = 0;
            active = false;
        }
    }
}
