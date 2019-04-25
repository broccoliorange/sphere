package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.*;
import toxi.physics3d.behaviors.AttractionBehavior3D;

import java.util.ArrayList;


public class Main extends PApplet {

    VerletPhysics3D physics;
    JellyBall jb;

    PImage loadImg;

    PeasyCam cam;



    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        loadImg = loadImage("colorbar.jpg");

        jb = new JellyBall();

        cam = new PeasyCam(this,500);
    }


    public void draw(){
        background(0);
        physics.update();


        lights();
        //translate(width/2,height/2);
        jb.display();


    }

    public class Node extends VerletParticle3D{

        Node(Vec3D loc){
            super(loc);

        }
    }



    public  class  Connection extends VerletMinDistanceSpring3D {

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

    public class JellyBall {

        PShape ball;

        int total = 50;
        int[] vertexes2;
        PVector[] plain;
        Node[] nodes;
        ArrayList<Connection> connections;

        JellyBall() {

            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new ArrayList<Connection>();
            float cStrength = 0.99F;

            //頂点番号の並び方の定義(CW　右端＝左端)
            float r = 200;

            for (int i = 0, j = 0; i < vertexes2.length; i += 4, j++) {
                vertexes2[i + 0] = j + floor(j / total);
                vertexes2[i + 1] = j + floor(j / total) + 1;
                vertexes2[i + 2] = j + floor(j / total) + 2 + total;
                vertexes2[i + 3] = j + floor(j / total) + 1 + total;
                if((vertexes2[i+1]+1) % (total+1) == 0){
                    vertexes2[i+1] -= total;
                }

                if((vertexes2[i+2]+1) % (total+1) == 0){
                    vertexes2[i+2] -= total;
                }
            }


            //頂点番号と球表面の座標、テクスチャ画像の座標の関連づけ
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

            //ノードを物理系へ登録
            for(int j = 0; j < (total+1)*(total+1); j++){
                if( !((j+1)%(total+1)==0)){
                    physics.addParticle( nodes[j]);
                }
            }

            //コネクションを生成、物理系へ登録
            for (int i = 0; i < vertexes2.length; i += 4) {
                int j = vertexes2[i];
                int k = vertexes2[i + 1];
                int l = vertexes2[i + 2];
                int m = vertexes2[i + 3];
                if((k+1)%(total+1)==0){ k-=total; }
                if((l+1)%(total+1)==0){ l-=total; }

                Vec3D left = nodes[k].sub(nodes[j]);
                float dleft = left.magnitude();
                Connection sleft = new Connection(nodes[j], nodes[k], dleft, cStrength);
                physics.addSpring(sleft);
                connections.add(sleft);

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
            //nodes[total+1].lock();
            //nodes[floor((total+1)*total/2)].lock();
            //nodes[total*total].lock();
        }



        void display(){

                ball = createShape();

                ball.beginShape(QUADS);
                for (int i = 0; i < vertexes2.length; i++) {
                    if (i % 4 == 0) {
                        ball.texture(loadImg);
                    }
                    int j = vertexes2[i];
                    if((j + 1) % (total + 1) == 0){ j -= total; }
                    ball.vertex(nodes[j].x, nodes[j].y, nodes[j].z, plain[j].x, plain[j].y);
                }
                ball.endShape();

                //strokeWeight(0);
                pushMatrix();
                rotateX(HALF_PI);
                shape(ball);
                popMatrix();
        }

    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}