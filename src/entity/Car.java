package entity;

import main.Display;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// regulation data from:
// https://www.fia.com/sites/default/files/2022_formula_1_technical_regulations_-_iss_6_-_2021-09-13_0.pdf
// gear ratios from RB16 F1 Game (2021)
//    <GearRatio1st value="2.8" />
//    <GearRatio2nd value="2.29" />
//    <GearRatio3rd value="1.93" />
//    <GearRatio4th value="1.583" />
//    <GearRatio5th value="1.375" />
//    <GearRatio6th value="1.19" />
//    <GearRatio7th value="1.05" />
//    <GearRatio8th value="0.93" />


public class Car extends Entity{
    // images
    BufferedImage car_image;
    BufferedImage rpm_background_image;
    BufferedImage rpm_meter_image;
//    // position
//    double x, y;
    // center in screen
    public final int screenX;
    public final int screenY;
    // velocity
    double v;
    // top speed
    double vMax;
    // angle
    double angle;
    // RPM passed through gears
    double cur_rpm;
    // RPM at which the car idles
    double idle_rpm;
    int max_rpm;
    // handbrake length
    int handbrakeLength = 0;
    // bounding box
    int x1, y1, x2, y2;

    // for debugging
    boolean debug;

    // car qualities
    // mass Kg
    int mass = 790;
    // max power, Newtons
    double maxThrust = 7900.0;
    // gear ratios
    double[] gearRatios;
    // index of selected gear, 8 foward gears, -1 is reverse?, neutral is absence of gearing -> raw_rpm;
    int gearIdx;
    boolean engineOn;

    // Physics constants
    double g = 9.806;
    // rolling coefficient
    double Cr = 0.02;
    // density of dry air at sea level & pressure at 15*C
    double dragConstant = 0.166; // 1/2*Cd*A*rho (Kg/m)

    // particle trail
    ArrayList<Particle> particles = new ArrayList<>();


    public Car() throws IOException {
        this.car_image = ImageIO.read(new File("res/imgs/cars/red_car.png"));
        this.rpm_meter_image = ImageIO.read(new File("res/imgs/ui/rpm_meter.png"));
        this.rpm_background_image = ImageIO.read(new File("res/imgs/ui/rpm_outline.png"));

        this.screenX = Display.WIDTH/2 - car_image.getWidth()/2;
        this.screenY = Display.HEIGHT/2 - car_image.getHeight()/2;

        setDefaultValues();
    }

    public void setDefaultValues() {
//        this.x = Display.WIDTH/2.0 - this.car_image.getWidth()/2.0;
//        this.y = Display.HEIGHT/2.0 - this.car_image.getHeight()/2.0;
        this.worldX = Display.tileSize*6;
        this.worldY = Display.tileSize*3.25;
        this.v = 0.0;
        this.vMax = 6.0;
        this.angle = 0.0;
        this.cur_rpm = 0.0;
        this.idle_rpm = 3000.0;
        this.max_rpm = 13000;

        this.gearIdx = 0;
        this.gearRatios = new double[]{
                2.8,
                2.29,
                1.93,
                1.583,
                1.375,
                1.19,
                1.05,
                0.93
        };

        this.engineOn = false;
        this.debug = false;
    }

    public void render(Graphics g){
        drawTrail(g);
        if(debug) renderDebugging(g);
        rotate(g);
        drawRpmMeter(g);
        drawGearIndicator((Graphics2D) g);
    }

//    public void accelerate(){
//        if(v < vMax) v += 0.04;
//    }
//
//    public void decelerate(){
//        if(v > -vMax/2) v -= 0.04;
//    }

    public void turnLeft(){
        angle-=1;
    }

    public void turnLeft(boolean handbrake){
        if(handbrake){
            angle -= Math.min(handbrakeLength/20.0, 2);
//            angle -= 1;
//            drift(false);
            return;
        }
        angle-=1;
    }
    public void turnRight(){
        angle+=1;
    }

    public void turnRight(boolean handbrake){
        if(handbrake){
            angle+=Math.min(handbrakeLength/20.0, 2);
//            angle += 1;
//            drift(true);
            return;
        }
        angle+=1;
    }

    public void handbrake(){
        v *= (1-handbrakeLength++/40.0/100.0);
        if(v > 0) v -= 0.02;
    }

//    public void drift(boolean right){
//        handbrakeLength++;
//    }

    public void update(boolean[] flags){
        // debugging
        if(debug) System.out.println(toString(flags));
        // toggle engine ignition
        if(flags[5]){
            toggleEngine();
            flags[5] = false;
        }
        // handle gas & brakes
        updateSpeed(flags[0], flags[2]);
        // turning left
        if(flags[1]) turnLeft(flags[4]);
        // turning right
        if(flags[3]) turnRight(flags[4]);
        // handbrake (unused)
        if(flags[4]) handbrake();

        // upShift, reset flag
        if(flags[6]){
            shiftUp();
            flags[6] = false;
        }
        // downShift, reset flag
        else if(flags[7]){
            shiftDown();
            flags[7] = false;
        }
        // toggle debugger, reset flag
        if(flags[9]){
            debug = !debug;
            flags[9] = false;
        }

//        updateRpm(flags[0], flags[2]);
        // update car coordinates
        updatePosition();
    }

    private void updateSpeed(boolean throttleUp, boolean throttleDown){
        updateRpm(throttleUp, throttleDown);

        // apply braking force
        if(throttleDown) v -= 0.06;

        v += ( (enginePower()-drag())/(double)(mass) )/100.0;
        if(v < 0) v = 0;
        if(v > vMax) v = vMax;

//        System.out.println("Velocity: " + v + ", Power: " + enginePower() + ", Drag: " + drag() + ", RPM: " + cur_rpm);
    }

    private void updatePosition(){
//        this.x += (Math.cos(Math.toRadians(angle)) * v);
//        this.y += (Math.sin(Math.toRadians(angle)) * v);
        this.worldX += (Math.cos(Math.toRadians(angle)) * v);
        this.worldY += (Math.sin(Math.toRadians(angle)) * v);
        this.v *= 0.995; // fraction between 1 & 0.99, 0.99 at max speed (drag increases with velocity)
        if(Math.abs(this.v) < 1) this.v *=0.995; // compensation for constant friction (versus drag)
    }

    public void rotate(Graphics g) {
        // The required drawing location
//        int drawLocationX = (int) this.x;
//        int drawLocationY = (int) this.y;
        int drawLocationX = this.screenX;
        int drawLocationY = this.screenY;

        // Rotation information
        double rotationRequired = Math.toRadians(angle);
        double locationX = car_image.getWidth() / 2.0;
        double locationY = car_image.getHeight() / 2.0;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

        // Drawing the rotated image at the required drawing locations
        g.drawImage(op.filter(car_image, null), drawLocationX, drawLocationY, null);
    }

    public void resetHandbrake(){
//        System.out.println("HANDBRAKE: " + handbrakeLength);
        this.handbrakeLength = 0;
    }

    // playing sounds
    private static void playClip(File clipFile) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        class AudioListener implements LineListener {
            private boolean done = false;
            @Override public synchronized void update(LineEvent event) {
                LineEvent.Type eventType = event.getType();
                if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }
            public synchronized void waitUntilDone() throws InterruptedException {
                while (!done) { wait(); }
            }
        }
        AudioListener listener = new AudioListener();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(listener);
            clip.open(audioInputStream);
            try {
                clip.start();
                listener.waitUntilDone();
            } finally {
                clip.close();
            }
        } finally {
            audioInputStream.close();
        }
    }

    public void shiftUp(){
        // change gear +1
        if(gearIdx == gearRatios.length-1) return;

        gearIdx++;
        cur_rpm *= (gearRatios[gearIdx]/gearRatios[gearIdx-1]);
    }

    public void shiftDown(){
        // change gear -1
        if(gearIdx == 0) return;

        gearIdx--;
        cur_rpm *= (gearRatios[gearIdx]/gearRatios[gearIdx+1]);
    }

    // returns F of friction (rolling + air) in Newtons
    public double drag(){
        double airFriction = Math.pow(v, 2) * dragConstant;
        double rollingFriction = mass*g*Cr;
        return airFriction + rollingFriction;
    }

    public double enginePower(){
        return maxThrust * rpmCurve(cur_rpm);
    }

    public void updateRpm(boolean throttleUp, boolean throttleDown){
        // make sure we dont have below idle rpm with running engine
        if(engineOn && cur_rpm < idle_rpm) cur_rpm = idle_rpm;

        // rpm change due to braking or acceleration
        if(throttleUp) cur_rpm += 20 * gearRatios[gearIdx];
        if(throttleDown) cur_rpm -= 40 * gearRatios[gearIdx];
        // RPM decay due to internal friction
        cur_rpm -= 10;
        // randomness induced from throttle sensitivity
        cur_rpm += Math.random()*50 - 25;
        // never below 0 or above 13000
        if(cur_rpm < 0) cur_rpm = 0;
        if(cur_rpm > max_rpm) cur_rpm = max_rpm;
    }

    public double rpmCurve(double rpm){
        // https://imgur.com/a/bBNZeTU, limit at 13,000 RPM
        // peak power at 11,000 RPM
        if(rpm < 8260){
            return ( (rpm-8260)/(double)(11800) ) + 0.7;
        }
        return 1 - Math.pow((rpm-11000)/(double)(5000), 2);
    }

    public void drawRpmMeter(Graphics g){
        int x = Display.WIDTH - this.rpm_background_image.getWidth() - this.rpm_background_image.getWidth()/4;
        int y = Display.HEIGHT - this.rpm_background_image.getHeight() + this.rpm_meter_image.getHeight()/4;
        g.drawImage(this.rpm_background_image, x, y, null);

        x += (this.rpm_background_image.getWidth() - this.rpm_meter_image.getWidth())/2;
        y += (this.rpm_background_image.getHeight() - this.rpm_meter_image.getHeight())/2;
        g.drawImage(this.rpm_meter_image, x, y, null);

        int bar_fill = (int)( (cur_rpm/(double)(max_rpm))*(rpm_meter_image.getWidth() - 16) );
        x += 8;
        y += rpm_meter_image.getHeight()/2 - 8;
        Color temp = g.getColor();
        g.setColor(Color.BLACK);
        g.fillRect(x + bar_fill, y, rpm_meter_image.getWidth() - bar_fill - 16, 16);
        g.setColor(temp);
    }

    public void drawGearIndicator(Graphics2D g){
        // box height, width
        int width = 128;
        int height = 64;

        int x = Display.WIDTH - this.rpm_background_image.getWidth() - this.rpm_background_image.getWidth()/4;
        int y = Display.HEIGHT - this.rpm_background_image.getHeight() + this.rpm_meter_image.getHeight()/2;
        
        g.drawRect(x, y, width, height);
        // text time
        Font font = new Font("Tacoma", Font.BOLD, 32);
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        String gearState = String.format("Gear %d", gearIdx + 1);
        g.drawString(gearState, x, y);
    }

    public void toggleEngine(){
        if(engineOn)
            cur_rpm = 0;
        else
            cur_rpm = idle_rpm;

        engineOn = !engineOn;
    }

    public double getRPM(){
        return cur_rpm;
    }

    public boolean getRunning(){
        return engineOn;
    }

    private void drawTrail(Graphics g){
        // probability of trail appearing
        if(( (int)(cur_rpm) < 4000 && (int)(cur_rpm) % 5 == 0 ) || Math.random()*(cur_rpm/1_000) < 1){
            int lengthOfCar = 48;
            int distanceBetweenWheels = 28;

            int px = (int)(worldX + car_image.getWidth()/2);
            // car is asymmetrical for some reason, -4 offset
            int py = (int)(worldY + car_image.getHeight()/2);
            px -= (Math.cos(Math.toRadians(angle)) * lengthOfCar/2);
            py -= (Math.sin(Math.toRadians(angle)) * lengthOfCar/2);
            // add variation to tire skip locations
            double trailLocVar = 5.0;
            px += (Math.random()*trailLocVar) - trailLocVar/2;
            py += (Math.random()*trailLocVar) - trailLocVar/2;

            // wheel coords for first pixel
            int px1 = (int) (px + Math.sin(Math.toRadians(angle)) * distanceBetweenWheels/2);
            int py1 = (int) (py - Math.cos(Math.toRadians(angle)) * distanceBetweenWheels/2);
            // wheel coords for second pixel
            int px2 = (int) (px - Math.sin(Math.toRadians(angle)) * (distanceBetweenWheels - 2)/2);
            int py2 = (int) (py + Math.cos(Math.toRadians(angle)) * (distanceBetweenWheels - 6)/2);

            int trailDurVar = 100;
            Particle p1 = new Particle(px1, py1);
            p1.setCycleToGradient(Color.DARK_GRAY, 50 + (int)(Math.random()*trailDurVar));
            p1.setActive(true);

            Particle p2 = new Particle(px2, py2);
            p2.setCycleToGradient(Color.DARK_GRAY, 50 + (int)(Math.random()*trailDurVar));
            p2.setActive(true);

            particles.add(p1);
            particles.add(p2);

//            System.out.println("THETA: " + angle);
        }

        particles.removeIf(p -> !p.isActive());

        particles.forEach(p -> p.render(g, this));
    }

    public void renderDebugging(Graphics g){
        int lineThickness = 4;
        Color temp = g.getColor();
        g.setColor(Color.BLACK);

        for(int x = 0; x < Display.WIDTH; x+=Display.tileSize){
            g.fillRect(x-lineThickness/2, 0, lineThickness, Display.HEIGHT);
        }
        for(int y = 0; y < Display.HEIGHT; y+=Display.tileSize){
            g.fillRect(0, y-lineThickness/2, Display.WIDTH, lineThickness);
        }

        g.setColor(temp);
    }

    public String toString(boolean[] flags){
        StringBuilder sb = new StringBuilder();
        sb.append("Position: ").append(worldX).append(", ").append(worldY).append("\n");
        sb.append("Velocity: ").append(v).append("\n");
        sb.append("Angle: ").append(angle);
        for(int i=0; i<flags.length; i++){
            sb.append("\n");
            sb.append("Flag ").append(i).append(" : ");
            if(flags[i]) sb.append("True");
            else sb.append("False");
        }

        return sb.toString();
    }
}
