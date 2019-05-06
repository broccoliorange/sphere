package origin;

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.*;
import java.util.ArrayList;


public class Main extends PApplet {

    VerletPhysics3D physics;
    JellyBall jb;

    //float r;

    int fc = 0;

    PImage backImg;
    PImage blurImg;
    PImage texImg;
    PGraphics maskImg; //仕上げスムージングのためのマスク

    PeasyCam cam;



    public void settings(){
        size(1080,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);

        backImg = loadImage("colorbar.jpg");
        backImg.filter(BLUR,8);
        texImg = loadImage("gaju1024.jpg");


        image(backImg,0,0);

        jb = new JellyBall();

        //r = 120;

        maskImg=createGraphics(width,height,P2D);
        maskImg.beginDraw();
        maskImg.fill(0);
        maskImg.ellipse(width/2,height/2,120*2*0.9F,120*2*0.9F);
        maskImg.endDraw();

        //cam = new PeasyCam(this,500);
    }


    public void draw(){
        background(0);
        physics.update();


        lights();
        translate(width/2,height/2); //PeasyCam有効の時はコメントアウト
        jb.display();
        if(fc % 20==0){
            jb.squash();
        }
        fc ++;
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

    public  class  Connection2 extends VerletConstrainedSpring3D {

        Connection2(Node n1, Node n2, float len, float strength){

            super(n1, n2, len, strength);
        }
    }



    public class JellyBall {

        PShape ball;

        int total = 100;
        int[] vertexes2;
        PVector[] plain;
        Node[] nodes;
        Node core;
        ArrayList<Connection> connections;
        ArrayList<Connection2> connections2;

        JellyBall() {

            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new ArrayList<Connection>();
            connections2 = new ArrayList<Connection2>();
            float cStrength = 0.001F;
            float cStrength2 = 0.005F;

            //頂点番号の並び方の定義(CW　右端＝左端)

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

            float r = 120;
            for(int i = 0; i < vertexes2.length; i++){
                float lat = map(floor(vertexes2[i]/(total+1)), 0, total, 0, PI);
                float lon = map(vertexes2[i] % (total+1),0, total,0, TWO_PI);
                float x = r * sin(lat) * cos(lon);
                float y = r * sin(lat) * sin(lon);
                float z = r * cos(lat);
                int j = vertexes2[i];
                nodes[j] = new Node( new Vec3D(x,y,z));

                float v = map(floor(vertexes2[i]/(total+1)),0, total, 0,texImg.height);
                float u = map(vertexes2[i] % (total+1),0, total,0, texImg.width);
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

            //コネクションを生成、物理系へ登録（中心と球表面のノードをつなぐ）
            core = new Node(new Vec3D(0,0,0));
            physics.addParticle(core);

            for(int j = 0; j < (total+1)*(total+1); j++) {
                if (!((j + 1) % (total + 1) == 0)) {
                    Vec3D center = nodes[j].sub(core);
                    float dcenter = center.magnitude();
                    Connection2 scenter = new Connection2(nodes[j], core, dcenter, cStrength2);
                    physics.addSpring(scenter);
                    connections2.add(scenter);
                }
            }


            //nodes[total+1].lock();
            //nodes[floor((total+1)*total/2)].lock();
            //nodes[total*total].lock();
            core.lock();
        }

        void display(){
            image(backImg,0,0);

            ball = createShape();

            ball.beginShape(QUADS);
            for (int i = 0; i < vertexes2.length; i++) {
                if (i % 4 == 0) {
                    ball.texture(texImg);
                }
                int j = vertexes2[i];
                if((j + 1) % (total + 1) == 0){ j -= total; }
                ball.vertex(nodes[j].x, nodes[j].y, nodes[j].z, plain[j].x, plain[j].y);
            }
            ball.endShape();

            strokeWeight(0);
            pushMatrix();
            rotateZ(HALF_PI);
            rotateY(HALF_PI);
            shape(ball);
            popMatrix();

            mask(maskImg);
            filter(BLUR,2);
        }

        void squeeze(){
            //赤道を絞ってみる
            for(int j = 0; j < (total+1)*(total+1); j++){
                if( !((j+1)%(total+1)==0)){
                    nodes[j].lock();
                    Vec3D v = nodes[j].getNormalized();
                    v.scaleSelf(map(abs(v.z),0,1,-50,0));
                    v.rotateZ( map(v.z,-1,1,-0.08F,0.08F) );
                    v.setZ(0);
                    nodes[j].addSelf(v);
                    nodes[j].unlock();
                }
            }
        }

        void squash(){
            //縦につぶしてみる
            for(int j = 0; j < (total+1)*(total+1); j++){
                if( !((j+1)%(total+1)==0)){
                    nodes[j].lock();
                    Vec3D v = nodes[j].getNormalized();
                    v.scaleSelf(map(abs(v.z),0,1,0,-20));
                    //v.x -= v.z;
                    //v.y -= v.z;
                    nodes[j].addSelf(v);
                    nodes[j].unlock();
                }
            }
        }

    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}