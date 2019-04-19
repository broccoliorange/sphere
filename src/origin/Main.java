package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.VerletParticle3D;
import toxi.physics3d.VerletPhysics3D;
import toxi.physics3d.VerletSpring3D;
import toxi.physics3d.behaviors.AttractionBehavior3D;

import java.util.ArrayList;


public class Main extends PApplet {

    VerletPhysics3D physics;
    Jelly j;

    PImage loadImg;

    Attractor attractor;
    float aStrength;
    float angle;

    //PeasyCam cam;



    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        loadImg = loadImage("colorbar.jpg");

        j = new Jelly();


        //cam = new PeasyCam(this,500);
    }


    public void draw(){
        background(0);
        physics.update();
        attractor = new Attractor( new Vec3D(0, 0, 0), aStrength);

        lights();
        translate(width/2,height/2);
        j.display();

        aStrength = -0.1F;
        //aStrength = map(sin(angle), -1,1,-0.001F, 0.001F);
        //angle += 0.01;


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

    public class Attractor extends VerletParticle3D{

        float aStrength;

        Attractor (Vec3D loc, float aStrength){
            super(loc);
            this.aStrength = aStrength;
            physics.addParticle(this);
            physics.addBehavior(new AttractionBehavior3D(this,width, aStrength));
        }

        void display(){

        }
    }

    public class Jelly {

        PShape halfSphere;

        int total = 20;
        int[] vertexes2;
        PVector[] plain;
        Node[] nodes;
        ArrayList<Connection> connections;

        Jelly() {

            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new ArrayList<Connection>();
            float cStrength = 0.95F;

            //頂点の定義(CW)
            float r = 200;

            for (int i = 0, j = 0; i < vertexes2.length; i += 4, j++) {
                vertexes2[i + 0] = j + floor(j / total);
                vertexes2[i + 1] = j + floor(j / total) + 1;
                vertexes2[i + 2] = j + floor(j / total) + 2 + total;
                vertexes2[i + 3] = j + floor(j / total) + 1 + total;
            }

            //半頂点番号→球表面の座標、テクスチャ画像の座標
            for(int i = 0; i < vertexes2.length; i++){
                float lat = map(floor(vertexes2[i]/(total+1)), 0, total, 0, PI);
                float lon = map(vertexes2[i] % (total+1),0, total,0, PI);
                float x = r * sin(lon) * cos(lat);
                float y = r * sin(lon) * sin(lat);
                float z = r * cos(lon);
                int j = vertexes2[i];
                nodes[j] = new Node( new Vec3D(x,y,z));

                float v = map(floor(vertexes2[i]/(total+1)),0, total, 0,loadImg.height);
                float u = map(vertexes2[i] % (total+1),0, total,0, loadImg.width);
                plain[j] = new PVector(u,v);
            }

            //半球頂点とノード、ノード間コネクションの関連付け、半球切り口のロック
            for(int j = 0; j < (total+1)*(total+1); j++){
                physics.addParticle( nodes[j]);
            }

            for (int i = 0; i < vertexes2.length; i += 4) {
                int j = vertexes2[i];
                int k = vertexes2[i + 1];
                int l = vertexes2[i + 2];
                int m = vertexes2[i + 3];

                Vec3D left = nodes[k].sub(nodes[j]);
                float dleft = left.magnitude();
                Connection sleft = new Connection(nodes[j], nodes[k], dleft, cStrength);
                Vec3D down = nodes[m].sub(nodes[j]);
                float ddown = down.magnitude();
                Connection sdown = new Connection(nodes[j], nodes[m], ddown, cStrength);

                physics.addSpring(sleft);
                connections.add(sleft);
                physics.addSpring(sdown);
                connections.add(sdown);

                if(j >= 0 && j < total || j % (total +1) == 0){
                    nodes[j].lock();
                }

                if((k + 1) % (total + 1) == 0){
                    Vec3D down2 = nodes[l].sub(nodes[k]);
                    float ddown2 = down2.magnitude();
                    Connection sdown2 = new Connection(nodes[k], nodes[l], ddown2, cStrength);
                    physics.addSpring(sdown2);
                    connections.add(sdown2);
                    nodes[k].lock();
                }

                if(m >= total*(total + 1)){
                    Vec3D left2 = nodes[l].sub(nodes[m]);
                    float dleft2 = left2.magnitude();
                    Connection sleft2 = new Connection(nodes[l], nodes[m], dleft2, cStrength);
                    physics.addSpring(sleft2);
                    connections.add(sleft2);
                    nodes[m].lock();
                }

                nodes[(total + 1)*(total +1)-1].lock();

            }
        }



        void display(){

                halfSphere = createShape();

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