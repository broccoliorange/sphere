package play;  //PDEではコメントアウト

import peasy.PeasyCam;
import processing.core.*;
import toxi.geom.Vec3D;
import toxi.physics3d.*;
import java.util.ArrayList;


public class Main extends PApplet {  //PDEではコメントアウト

    VerletPhysics3D physics;
    JellyBall jb;

    float r; //JellyBallの半径

    int fc = 0; //frame count
    int press = 0; //JellyBall圧縮の程度（ピクセル）

    PImage backImg; //背景
    PImage texImg; //テクスチャ
    PGraphics maskImg; //仕上げスムージングのためのマスク

    PeasyCam cam;



    public void settings(){
        size(1080,578, P3D);
    }

    public void setup(){
        physics = new VerletPhysics3D();

        imageMode(CENTER);
        strokeWeight(0); //シェイプにメッシュを描かない

        backImg = loadImage("colorbar.jpg"); //srcと同じフォルダに用意する
        backImg.filter(BLUR,8);
        texImg = loadImage("colorbar.jpg");

        r = 120;
        jb = new JellyBall(r);

        maskImg=createGraphics(width,height,P2D);
        maskImg.beginDraw();
        maskImg.fill(0);
        maskImg.ellipse(width/2,height/2,r*2*0.9F,r*2*0.9F);
        maskImg.endDraw();

        //cam = new PeasyCam(this,500);
    }


    public void draw(){
        //background(0);
        physics.update();


        lights();
        translate(width/2,height/2); //PeasyCam有効の時はコメントアウト

        if(fc % 105 < 15) {
            press = fc % 15 * 2;
            jb.nodesReset();
            jb.squash(press);
        }

        jb.display();
        fc ++;

        //saveFrame("frames/######.png"); //processingムービーメーカー用
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
        float r;

        int total = 100; //メッシュの縦横分割数
        int[] vertexes2; //頂点番号を配列として持つ
        PVector[] plain; //テクスチャの格子座標
        Node[] nodes; //球表面の頂点座標
        Node core; //球の中心座標
        ArrayList<Connection> connections; //球表面のノード同士のバネ
        ArrayList<Connection2> connections2; //coreと球表面ノードを結ぶバネ

        JellyBall( float r ) {

            this.r = r;
            vertexes2 = new int[total * total * 4];
            plain = new PVector[total * total * 4];
            nodes = new Node[total * total * 4];
            connections = new ArrayList<Connection>();
            connections2 = new ArrayList<Connection2>();
            float cStrength = 0.001F;
            float cStrength2 = 0.005F;

            //頂点番号の並び方の定義(CW　右端＝左端)
            //total=5の場合ならば、
            //
            //  1  2  3  4  5
            //  6  7  8  9 10
            // 11 12 13 14 15
            // 16 17 18 19 20
            // 21 22 23 24 25
            //
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

            //バネを生成、物理系へ登録
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

                Vec3D diag2 = nodes[l].sub(nodes[j]);
                float ddiag2 = diag2.magnitude();
                Connection sdiag2 = new Connection(nodes[j], nodes[l], ddiag2, cStrength);
                physics.addSpring(sdiag2);
                connections.add(sdiag2);

                if(m >= total*(total +1)){
                    Vec3D leftbtm = nodes[l].sub(nodes[m]);
                    float dleftbtm = leftbtm.magnitude();
                    Connection sleftbtm = new Connection(nodes[l], nodes[m], dleftbtm, cStrength);
                    physics.addSpring(sleftbtm);
                    connections.add(sleftbtm);
                }
            }

            //バネを生成、物理系へ登録（coreと球表面のノード）
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

            core.lock(); //coreをロックしてボールを空間につなぎ留めておく
        }

        void display(){
            //背景と球の描画
            image(backImg,0,0);

            ball = createShape();

            ball.beginShape(QUADS);
            //
            //  1  2  3  4  5
            //  6  7  8  9 10
            // 11 12 13 14 15
            // 16 17 18 19 20
            // 21 22 23 24 25
            //
            //(1,2,7,6),(2,3,8,7),(3,4,9,8),(4,1,6,9),(6,7,12,11),…,(19,16,21,24)
            //
            for (int i = 0; i < vertexes2.length; i++) {
                if (i % 4 == 0) {
                    ball.texture(texImg);
                }
                int j = vertexes2[i];
                if((j + 1) % (total + 1) == 0){ j -= total; }
                ball.vertex(nodes[j].x, nodes[j].y, nodes[j].z, plain[j].x, plain[j].y);
            }
            ball.endShape();

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

        void squash( int press ){
            //縦につぶしてみる
            for(int j = 0; j < (total+1)*(total+1); j++){
                if( !((j+1)%(total+1)==0)){
                    nodes[j].lock();
                    Vec3D v = nodes[j].getNormalized();

                    v.x *= press * cos(abs(v.z));
                    v.y *= press * cos(abs(v.z));
                    v.z *= -press * sin(abs(v.z));

                    nodes[j].addSelf(v);
                    nodes[j].unlock();
                }
            }
        }

        void nodesReset(){
            //ノード座標の初期化
            for(int j = 0; j < (total+1)*(total+1); j++){
                if( !((j+1)%(total+1)==0)){
                    nodes[j].lock();

                    float lat = map(floor(j/(total+1)), 0, total, 0, PI);
                    float lon = map(j % (total+1),0, total,0, TWO_PI);
                    nodes[j].x = r * sin(lat) * cos(lon);
                    nodes[j].y = r * sin(lat) * sin(lon);
                    nodes[j].z = r * cos(lat);

                    nodes[j].unlock();
                }
            }
        }

    }



    public static void main(String[] args){
        PApplet.main("play.Main");
    }  //PDEではコメントアウト
}  //PDEではコメントアウト
