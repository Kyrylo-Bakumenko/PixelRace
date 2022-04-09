package Buttons;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class ButtonArray {
    Button[] buttons;
    int x1, x2, y1, y2; // bounding rectangle coordinates

    public ButtonArray() {
        this.x1 = 0;
        this.x2 = 0;
        this.y1 = 0;
        this.y2 = 0;
    }

    public ButtonArray(int x, int y, File file){

    }

    public ButtonArray(int x, int y, Button[] buttons){
        this.buttons = buttons;
    }

    public void render(Graphics g){
        for(Button button: buttons) button.render(g);
    }

    public void deselectAll(){

    }

    public boolean contains(MouseEvent e){
        int x = e.getX(), y = e.getY();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

//    public void initPochmannButton(){
//        this.pochmannButton = new Buttons.Button((main.Display.WIDTH-pButtonWidth)/2, main.Display.edgePad, pButtonWidth, pButtonHeight);
//    }
//    public void drawPochmannButton(Graphics g){
//        Color temp = g.getColor();
//        g.setColor(main.Display.background);
//        g.fillRoundRect((main.Display.WIDTH-pButtonWidth)/2, main.Display.edgePad, pButtonWidth, pButtonHeight, 30, 30);
//        g.setColor(Color.GRAY);
//        g.drawRoundRect((main.Display.WIDTH-pButtonWidth)/2, main.Display.edgePad, pButtonWidth, pButtonHeight, 30, 30);
//        g.setColor(temp);
//    }
}
