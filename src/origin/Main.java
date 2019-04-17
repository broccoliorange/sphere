package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.VerletParticle3D;
import toxi.physics3d.VerletPhysics3D;
import toxi.physics3d.VerletSpring3D;


public class Main extends PApplet {

    VerletPhysics3D physics;
    Jelly j;

    PImage loadImg;
    PShape halfSphere;


    PeasyCam cam;



    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        loadImg = loadImage("colorbar.jpg");

        cam = new PeasyCam(this,500);

        j = new Jelly();
    }


    public void draw(){
        physics.update();

        background(0);

        lights();
        //translate(width/2,height/2);
        j.display();

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

    public class Jelly {

        int total = 20;
        int[] vertexes2;
        PVector[] plain;
        Node[] nodes;
        Connection[] connections;

        Jelly() {

            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new Connection[total * total * 4];
            float strength = 0.1F;

            //半球の定義：ここから
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
                nodes[j] = new Node(new Vec3D(x,y,z));
                float v = map(floor(vertexes2[i]/(total+1)),0, total, 0,loadImg.height);
                float u = map(vertexes2[i] % (total+1),0, total,0, loadImg.width);
                plain[j] = new PVector(u,v);
            }
            //半球の定義：ここまで

            for (int i = 0; i < vertexes2.length; i += 4) {
                int j = vertexes2[i];
                int k = vertexes2[i + 1];
                int l = vertexes2[i + 2];
                int m = vertexes2[i + 3];

                physics.addParticle(nodes[j]);
                physics.addParticle(nodes[k]);
                physics.addParticle(nodes[l]);
                physics.addParticle(nodes[m]);

                Vec3D left = nodes[k].sub(nodes[j]);
                float dleft = left.magnitude();
                Connection sleft = new Connection(nodes[j], nodes[k], dleft, strength);
                Vec3D down = nodes[m].sub(nodes[j]);
                float ddown = down.magnitude();
                Connection sdown = new Connection(nodes[j], nodes[m], ddown, strength);

                physics.addSpring(sleft);
                physics.addSpring(sdown);

                if(k % total == 0){
                    Vec3D down2 = nodes[l].sub(nodes[k]);
                    float ddown2 = down2.magnitude();
                    Connection sdown2 = new Connection(nodes[k], nodes[l], ddown2, strength);
                    physics.addSpring(sdown2);
                }

                if(m % total == 0){
                    Vec3D left2 = nodes[l].sub(nodes[m]);
                    float dleft2 = left2.magnitude();
                    Connection sleft2 = new Connection(nodes[l], nodes[m], dleft2, strength);
                    physics.addSpring(sleft2);
                }
            }
        }

        void display(){

                halfSphere.beginShape(QUADS);
                for (int i = 0; i < vertexes2.length; i++) {
                    if (i % 4 == 0) {
                        halfSphere.texture(loadImg);
                    }
                    int j = vertexes2[i];
                    halfSphere.vertex(nodes[j].x, nodes[j].y, nodes[j].z, plain[j].x, plain[j].y);
                }
                halfSphere.endShape();


                pushMatrix();
                rotateX(HALF_PI);
                rotateY(HALF_PI);
                shape(halfSphere);
                popMatrix();
        }

    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}