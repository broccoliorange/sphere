package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.*;
import toxi.physics3d.behaviors.AttractionBehavior3D;

import java.util.ArrayList;


public class Main extends PApplet {

    VerletPhysics3D physics;
    Jelly j;

    PImage loadImg;

    Attractor attractor;
    float aStrength;
    float angle;

    float ax,ay;

    PeasyCam cam;



    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        loadImg = loadImage("colorbar.jpg");

        j = new Jelly();
        ax = 0;
        ay = 0;

        cam = new PeasyCam(this,500);
    }


    public void draw(){
        background(0);
        physics.update();

        attractor = new Attractor( new Vec3D(0, 0, 0), aStrength);
        ax += random(5);
        ay += random(5);
        aStrength = -0.0001F;
        //aStrength = map(sin(angle), -1,1,0.0F, 0.1F);
        //angle += HALF_PI/1000;
        //attractor.lock();
        lights();
        //translate(width/2,height/2);
        j.display();


    }

    public class Node extends VerletParticle3D{

        Node(Vec3D loc){
            super(loc);

        }
    }



    public  class  Connection extends VerletConstrainedSpring3D {
        float x1,y1,z1,x2,y2,z2;

        Connection(Node n1, Node n2, float len, float strength){

            super(n1, n2, len, strength);

            x1 = n1.x;
            y1 = n1.y;
            z1 = n1.z;
            x2 = n2.x;
            y2 = n2.y;
            z2 = n2.z;
        }

        void display(){
            beginShape();
            stroke(255);
            vertex(x1,y1,z1);
            vertex(x2,y2,z2);
            endShape();
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

        int total = 7;
        int[] vertexes2;
        PVector[] plain;
        Node[] nodes;
        ArrayList<Connection> connections;

        Jelly() {

            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new ArrayList<Connection>();
            float cStrength = 0.1F;

            //頂点の定義(CW)
            float r = 200;

            for (int i = 0, j = 0; i < vertexes2.length; i += 4, j++) {
                vertexes2[i + 0] = j + floor(j / total);
                vertexes2[i + 1] = j + floor(j / total) + 1;
                vertexes2[i + 2] = j + floor(j / total) + 2 + total;
                vertexes2[i + 3] = j + floor(j / total) + 1 + total;
            }

            //頂点番号→球表面の座標、テクスチャ画像の座標
            for(int i = 0; i < vertexes2.length; i++){
                float lat = map(floor(vertexes2[i]/(total+1)), 0, total, 0, PI);
                float lon = map(vertexes2[i] % (total+1),0, total,0, TWO_PI);
                float x = r * sin(lat) * cos(lon);
                float y = r * sin(lat) * sin(lon);
                float z = r * cos(lat);
                int j = vertexes2[i];
                nodes[j] = new Node( new Vec3D(x,y,z));

                float v = map(floor(vertexes2[i]/(total+1)),0, total, 0,loadImg.height);
                float u = map(vertexes2[i] % (total+1),0, total,0, loadImg.width);
                plain[j] = new PVector(u,v);
            }

            //球頂点とノード、ノード間コネクションの関連付け
            for(int j = 0; j < (total+1)*(total+1); j++){
                physics.addParticle( nodes[j]);

            }

            for (int i = 0; i < vertexes2.length; i += 4) {
                int j = vertexes2[i];
                int k = vertexes2[i + 1];
                int l = vertexes2[i + 2];
                int m = vertexes2[i + 3];

                if((k + 1) % (total + 1) == 0){
                    nodes[k] = nodes[k - total];
                    nodes[l] = nodes[l - total];
                }

                Vec3D left = nodes[k].sub(nodes[j]);
                float dleft = left.magnitude();
                Connection sleft = new Connection(nodes[j], nodes[k], dleft, cStrength);
                physics.addSpring(sleft);
                connections.add(sleft);
                println("j=",j," k=",k,"nodes:",nodes[j],nodes[k]);
                sleft.display();

                Vec3D down = nodes[m].sub(nodes[j]);
                float ddown = down.magnitude();
                Connection sdown = new Connection(nodes[j], nodes[m], ddown, cStrength);
                physics.addSpring(sdown);
                connections.add(sdown);

                Vec3D diag = nodes[m].sub(nodes[k]);
                float ddiag = diag.magnitude();
                Connection sdiag = new Connection(nodes[k], nodes[m], ddiag, cStrength);
                physics.addSpring(sdiag);
                connections.add(sdiag);

                if(m >= total*(total +1)){
                    Vec3D leftbtm = nodes[l].sub(nodes[m]);
                    float dleftbtm = leftbtm.magnitude();
                    Connection sleftbtm = new Connection(nodes[l], nodes[m], dleftbtm, cStrength);
                    physics.addSpring(sleftbtm);
                    connections.add(sleftbtm);
                }

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
                    //if((j + 1) % (total +1) == 0){
                    //    j = j - total;
                    //}
                    halfSphere.vertex(nodes[j].x, nodes[j].y, nodes[j].z, plain[j].x, plain[j].y);
                }
                halfSphere.endShape();

                //strokeWeight(0);
                //pushMatrix();
                //rotateX(HALF_PI);
                //rotateY(HALF_PI);
                shape(halfSphere);
                //popMatrix();
        }

    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}