package origin;

import peasy.PeasyCam;
import processing.core.PApplet;

public class Main extends PApplet {
    PeasyCam cam;

    public void settings(){
        size(600,600, P3D);
    }

    public void setup(){
        cam = new PeasyCam(this,500);
    }

    public void draw(){
        background(0);
        fill(255);
        lights();
        //translate(width/2,height/2);
        //sphere(200);

        float r = 200;
        int total = 20;
        for(int i = 0; i < 100; i++){
            float lon = map(i, 0, total, -PI, PI);
            for(int j = 0; j < 100; j++){
                float lat = map(j, 0, total, -HALF_PI, HALF_PI);

                float x = r * sin(lon) * cos(lat);
                float y = r * sin(lon) * sin(lat);
                float z = r * cos(lon);
                stroke(255);
                strokeWeight(4);
                point(x,y,z);
            }
        }


    }

    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}