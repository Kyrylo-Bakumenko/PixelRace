package main;

import Buttons.Button;
import entity.Car;
import entity.MidiTest;
import tile.TileManager;
import tile.Track;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Goals:
    // design Pochmann algorithm
        // remember to add corner validation
    // display Pochmann solution
    // simplify solution
    // optimize algorithm
    // correct for un-desired rotation
    // color palette
    // label mode to display lettering system


public class Display extends Canvas implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
    private Thread thread;
    private JFrame frame;
    private MidiTest midiTest;

    // game settings
    private final int frameRate = 100; //fps
    private static final String title = "Pixel Race";
    private static boolean running = false;
    // screen settings
    final static int originalTileSize = 128;
    public static final int scale = 2;
    public static final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public static int WIDTH = 1526;//128*12
    public static int HEIGHT = 1152;//128*9
    // world settings
    public final int maxWorldCol = 21;
    public final int maxWorldRow = 16;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;
    // ??? idk do this later
    public static int edgePad = (int) (Math.min(WIDTH, HEIGHT)*0.02);
    public String[] tileNames = new String[]{"grass.png", "road_12.png", "road_16.png", "road_20.png", "road_24.png",
            "road_20V.png", "road_20_WS90.png", "road_20_SE90.png", "road_20_EN90.png", "road_20_NW90.png", "new_sand.png", "road_20_16.png"};

    public static final Color background = new Color(30, 30, 30); // screen background color
    public static final Color text = new Color(170, 170, 170); // screen text color
    // flags
    // w, a, s, d, spacebar, i,         showSideMenu
    boolean[] flags = new boolean[9];

    public Car car;
    public Track track;
    public Button[] sideMenu;
    public BufferedImage selectedTrack;
    TileManager tileM = new TileManager(this);

    public Display() {
        this.frame = new JFrame();

        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
    }
    
    public static void main(String[] args) throws IOException {
        Display display = new Display();
        display.frame.setTitle(title);
        display.frame.add(display);
        display.frame.pack();
        display.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        display.frame.setIconImage(ImageIO.read(new File("res/imgs/cars/red_car.png")));
        display.frame.setLocationRelativeTo(null);
        display.frame.setResizable(true);
        display.frame.setVisible(true);

        display.start();
    }
    
    public synchronized void start(){
        running = true;
        this.thread = new Thread(this, "Display");
        this.thread.start();
    }
    public synchronized void stop(){
        try{
            this.thread.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1_000_000_000.0 / frameRate;
        double delta = 0;
        int frames = 0;

        init();

        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1){
                update();   //  <-------
                delta--;
                render();   //  <-------
                frames++;
            }

            if(System.currentTimeMillis() - timer > 1_000){
                timer += 1_000;
                this.frame.setTitle(title + " | " + frames + " fps");
                frames = 0;
            }
        }
         stop();
    }

    private void init() {
        try {
            this.car = new Car();
            this.track = new Track();
            this.sideMenu = initSideMenu();
            this.midiTest = new MidiTest();
        }catch(Exception ignored){

        }
    }

    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        Font font = new Font("Tahoma", Font.PLAIN, 18);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,WIDTH, HEIGHT);

//        track.render(g);
        tileM.render(g);
        car.render(g);
        renderSideMenu(g);

        g.dispose();
        bs.show();
    }
    private void update(){
        car.update(flags);
        if(car.getRunning()) midiTest.playNote(car.getRPM());
    }

//    Point old = null;
    @Override
    public void mouseDragged(MouseEvent e) {
//        Point current = new Point(e.getX() - main.Display.WIDTH / 2, e.getY() - main.Display.HEIGHT / 2);
//        old = current;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // checks if a color has been selected and applies it to the target face
//        if(old==null && e.getButton()==1) {
//
//        }
//
//        old = null; // resets frag from rotation drag action
//        System.out.println(e.getX() + ", " + e.getY());
    }
    // flag #8
//    boolean showSideMenu = false;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println(car);
        // if menu is visible
        if(flags[8]){
            // and a button was clicked
            Button selectedButton = getButton(e);
            if(selectedButton != null){
                // save the track
                selectedTrack = selectedButton.getImage();
                // ensure all others are unselected
                for(Button button : sideMenu) button.setSelection(false);
                // toggle selection
                selectedButton.toggleSelected();
            }
        }else if(selectedTrack != null){
            // if track is selected, update clicked grid
            this.track.setSquare(e.getX(), e.getY(), selectedTrack);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
//        midiTest.playNote(car.getRPM());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        flags[8] = showSideMenu(e);
    }

    // event flags
    // boolean toggleEngine = false; flag#5

    @Override
    public void keyTyped(KeyEvent e) {
        if(e.getKeyChar()=='i'){
            flags[5] = true;
        }
    }

    // event flags #0-#3
//    boolean wKey = false, aKey = false, sKey = false, dKey = false;
//    event flag #4
//    boolean spacebar = false;

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar()=='w'){
            flags[0] = true;
        }else if(e.getKeyChar() == 'a'){
            flags[1] = true;
        }else if(e.getKeyChar() == 's'){
            flags[2] = true;
        }else if(e.getKeyChar() == 'd'){
            flags[3] = true;
        }else if(e.getKeyChar() == ' '){
            flags[4] = true;
        }

        // event flag #6 & flag #7
        // shift -> upShift, ctrl -> downShift
        if(e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK){
            flags[6] = true;
        }
        if(e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK){
            flags[7] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyChar()=='w'){
            flags[0] = false;
        }else if(e.getKeyChar() == 'a'){
            flags[1] = false;
        }else if(e.getKeyChar() == 's'){
            flags[2] = false;
        }else if(e.getKeyChar() == 'd'){
            flags[3] = false;
        }else if(e.getKeyChar() == ' '){
            flags[4] = false;
            car.resetHandbrake();
        }
    }

    public Button[] initSideMenu() throws IOException {
        sideMenu = new Button[5];
        int size = 100;
        int imgDim = 64; // 64x64 thumbnails
        int x = (size-imgDim)/2, y;

        for(int i = 0; i < sideMenu.length; i++){
            String path = "res/imgs/thumbnails/" + tileNames[i];
            BufferedImage thumbnail = ImageIO.read(new File(path));

            y = (size-thumbnail.getHeight())/2 + size*i;
            sideMenu[i] = new Button(x, y, thumbnail, path);
        }

//        selectedTrack = ImageIO.read(new File("res/tracks/grass.png"));
        return sideMenu;
    }

    public boolean showSideMenu(MouseEvent e){
        return e.getX() < 210;
    }

    public Button getButton(MouseEvent e){
        for(Button button : sideMenu)
            if(button.contains(e)) return button;
        return null;
    }

    public void renderSideMenu(Graphics g){
        if(!flags[8]) return;
        Color temp = g.getColor();
        g.setColor(Color.black);

        g.fillRect(0, 0, 200, HEIGHT);
        for(Button button : sideMenu) button.render(g);

        g.setColor(temp);
    }
}
