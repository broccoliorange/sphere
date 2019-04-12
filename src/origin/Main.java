package origin;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Main extends PApplet {

    PGraphics bufrImg;
    PImage srcImg;  //原画
    PImage clipImg;  //処理範囲のクリップ画
    PImage backImg;  //背景画
    PGraphics distImg;  //レンズ歪みをかけた画
    PGraphics prevSphereImg;  //
    //PShape distSphere;  //distImgを貼り付ける球
    //PGraphics maskImg;  //仕上げスムージングのためのマスク

    int scrnShortSide;
    int lensWidth;
    int lensHeight;
    int _clipEdgeWidth;
    int _clipEdgeHeight;

    float[] posX;
    float[] posY;
    float[] posU;
    float[] posV;

    float[] posXi;
    float[] posYi;

    int[] vertexes;

    int num = 24;  //distImg分割数

    PeasyCam cam;
    PVector [][] globe;
    int total = 50;

    public void settings(){
        size(600,600, P3D);
    }

    public void setup(){
        bufrImg = createGraphics(capImg.width, capImg.height, P2D);
        srcImg = createImage(width, height, RGB);
        backImg = createImage(width, height, RGB);

        scrnShortSide = min(width, height);
        lensWidth = scrnShortSide;
        lensHeight = scrnShortSide;

        clipImg = createImage(scrnShortSide, scrnShortSide, RGB);
        distImg = createGraphics(scrnShortSide * 2, scrnShortSide, P3D);
        prevSphereImg = createGraphics(scrnShortSide, scrnShortSide, P3D);
        distSphere = createShape(SPHERE, scrnShortSide * 0.5F);
        //maskImg = createGraphics(width,height,P2D);
        saveImg = createImage(width, height, RGB);

        generateMesh();

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
            beginShape(QUAD_STRIP);
            for(int j = 0; j < total+1; j++){
                PVector v1 = globe[i][j];
                PVector v2 = globe[i+1][j];
                stroke(255);
                strokeWeight(2);

                if(i % 2 == 0){
                    vertex(v1.x, v1.y, v1.z);
                    vertex(v2.x, v2.y, v2.z);
                }else{
                    vertex(v2.x, v2.y, v2.z);
                    vertex(v1.x, v1.y, v1.z);
                }


            }
            endShape();
        }

    }


    public void generateMesh() {
        posX = new float[(num + 1) * (num + 1)];
        posY = new float[(num + 1) * (num + 1)];
        posU = new float[(num + 1) * (num + 1)];
        posV = new float[(num + 1) * (num + 1)];
        vertexes = new int[num * num * 4];

        for (int i = 0; i < posX.length; i++) {
            posX[i] = ((float) (i % (num + 1)) / (float) num);
            posU[i] = posX[i];
        }

        for (int i = 0; i < posY.length; i++) {
            posY[i] = ((float) (int) (i / (num + 1)) / (float) num);
            posV[i] = posY[i];
        }

        for (int i = 0, j = 0; i < num * num * 4; i += 4, j++) {
            vertexes[i    ] = j + floor(j / num);                    //CCW
            vertexes[i + 1] = j + floor(j / num) + 1 + num;
            vertexes[i + 2] = j + floor(j / num) + 2 + num;
            vertexes[i + 3] = j + floor(j / num) + 1;
            //vertexes[i    ] = j + floor(j / num);                  //CW
            //vertexes[i + 1] = j + floor(j / num) + 1;
            //vertexes[i + 2] = j + floor(j / num) + 2 + num;
            //vertexes[i + 3] = j + floor(j / num) + 1 + num;
        }

        posXi = new float[(num + 1) * (num + 1)];   //posX[],posY[],posU[],posV[]は上書きされて
        posYi = new float[(num + 1) * (num + 1)];   //使われるので、初期値をとっておく

        for (int i = 0; i < posX.length; i++) {
            posXi[i] = posX[i];
        }

        for (int i = 0; i < posY.length; i++) {
            posYi[i] = posY[i];
        }
    }

    public void initMesh() {

        for (int i = 0; i < posX.length; i++) {
            posX[i] = posXi[i];
            posU[i] = posXi[i];
        }

        for (int i = 0; i < posY.length; i++) {
            posY[i] = posYi[i];
            posV[i] = posYi[i];
        }

    }

    public void modelBrown() {

           // kは半径方向、pは円周方向の歪み
           // outRは出力イメージのサイズ比
           //     （小さくすると広い範囲が玉に映るが画像の外（黒）が入る危険あり）


        float k1 =  0.2F;
        float k2 = -0.02F;
        float p1 = 0;
        float p2 = 0;
        float outR = 1;

        for (int i = 0; i < posX.length; i++) {
            posX[i] = (posX[i] - 0.5F) * 2;
            posY[i] = (posY[i] - 0.5F) * 2;
            posU[i] *= _clipEdgeWidth;
            posV[i] *= _clipEdgeHeight;
            float xx = pow(posX[i], 2);
            float yy = pow(posY[i], 2);
            float b = k2 * pow(xx + yy, 2) + k1 * (xx + yy) + 1;
            float distX = p2 * (3 * xx + yy) + posX[i] * b + 2 * p1 * posX[i] * posY[i];
            float distY = p1 * (3 * yy + xx) + posY[i] * b + 2 * p2 * posX[i] * posY[i];
            posX[i] = (1 + distX * outR) * scrnShortSide / 2;
            posY[i] = (1 + distY * outR) * scrnShortSide / 2;
        }
    }

    public void drawDistortion() {
        distImg.beginDraw();
        distImg.background(0);
        distImg.noStroke();
        distImg.beginShape(QUADS);
        for (int i = 0; i < vertexes.length; i++) {
            if (i % 4 == 0) {
                distImg.texture(clipImg);
            }
            int j = vertexes[i];
            distImg.vertex(posX[j], posY[j], posU[j], posV[j]);
        }
        distImg.endShape();
        distImg.endDraw();
    }



    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}