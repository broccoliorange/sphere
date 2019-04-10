package origin;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PVector;

public class Main extends PApplet {
    PeasyCam cam;
    PVector [][] globe;
    int total = 50;

    public void settings(){
        size(600,600, P3D);
    }

    public void setup(){
        cam = new PeasyCam(this,500);
        globe = new PVector[total+1][total+1];
    }

    public void draw(){
        background(0);

        lights();
        //translate(width/2,height/2);
        //sphere(200);

        float r = 200;

        for(int i = 0; i < total+1; i++){
            float lat = map(i, 0, total, 0, PI);
            for(int j = 0; j < total+1; j++){
                float lon = map(j, 0, total, 0, TWO_PI);

                float x = r * sin(lon) * cos(lat);
                float y = r * sin(lon) * sin(lat);
                float z = r * cos(lon);
                globe[i][j] = new PVector(x,y,z);
            }
        }

        for(int i = 0; i < total; i++){
            beginShape(TRIANGLE_STRIP);
            for(int j = 0; j < total+1; j++){
                PVector v1 = globe[i][j];
                stroke(255);
                strokeWeight(2);
                vertex(v1.x, v1.y, v1.z);
                PVector v2 = globe[i+1][j];
                vertex(v2.x, v2.y, v2.z);

            }
            endShape();
        }

    }

    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}