package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.VerletParticle3D;
import toxi.physics3d.VerletPhysics3D;
import toxi.physics3d.VerletSpring3D;


public class Main extends PApplet {

    VerletPhysics3D physics;

    PImage loadImg;
    PShape halfSphere;


    PeasyCam cam;
    PVector [] globe;
    PVector [] plain;
    int total = 20;
    int[] vertexes2;


    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        loadImg = loadImage("colorbar.jpg");

        cam = new PeasyCam(this,500);

        vertexes2 = new int[total * total * 4];
        globe = new PVector[total * total * 4];
        plain = new PVector[total * total * 4];


    }


    public void draw(){
        physics.update();

        background(0);

        lights();
        //translate(width/2,height/2);
        //sphere(200);

        //球の定義：ここから
        halfSphere = createShape();
        float r = 200;


        for (int i = 0, j = 0; i < vertexes2.length; i += 4, j++) {
            vertexes2[i + 0] = j + floor(j / total);                  //CW
            vertexes2[i + 1] = j + floor(j / total) + 1;
            vertexes2[i + 2] = j + floor(j / total) + 2 + total;
            vertexes2[i + 3] = j + floor(j / total) + 1 + total;
        }

        for(int i = 0; i < vertexes2.length; i++){
            float lat = map(floor(vertexes2[i]/(total+1)), 0, total, 0, PI);
            float lon = map(vertexes2[i] % (total+1),0, total,0, PI);
            float x = r * sin(lon) * cos(lat);
            float y = r * sin(lon) * sin(lat);
            float z = r * cos(lon);
            int j = vertexes2[i];
            globe[j] = new PVector(x,y,z);
            float v = map(floor(vertexes2[i]/(total+1)),0, total, 0,loadImg.height);
            float u = map(vertexes2[i] % (total+1),0, total,0, loadImg.width);
            plain[j] = new PVector(u,v);
        }

        halfSphere.beginShape(QUADS);
        for (int i = 0; i < vertexes2.length; i++) {
            if (i % 4 == 0) {
                halfSphere.texture(loadImg);
            }
            int j = vertexes2[i];
            halfSphere.vertex(globe[j].x, globe[j].y, globe[j].z, plain[j].x, plain[j].y);
        }
        halfSphere.endShape();

        //球の定義：ここまで

        image(loadImg,0,0);
        pushMatrix();
        rotateX(HALF_PI);
        rotateY(HALF_PI);
        shape(halfSphere);
        popMatrix();

        //drawSoratama();

        //testSphere.setTexture(loadImg);
        //shape(testSphere);

        //noLoop();
    }

    public class Node extends VerletParticle3D{

        Node(Vec3D loc){
            super(loc);
        }
    }

    public  class  Connection extends VerletSpring3D {

        Connection(Node n1, Node n2, float len, float strength){
            super(n1, n2, len, strength);
        }
    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}