package origin;

import peasy.PeasyCam;
import processing.core.*;

public class Main extends PApplet {


    PGraphics bufrImg;
    PImage srcImg;  //原画
    PImage clipImg;  //処理範囲のクリップ画
    PImage backImg;  //背景画
    PGraphics distImg;  //レンズ歪みをかけた画
    PGraphics prevSphereImg;  //
    PShape distSphere;  //distImgを貼り付ける球
    //PGraphics maskImg;  //仕上げスムージングのためのマスク
    //PImage saveImg;
    PImage loadImg;

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
    float sphereDiaR = 1;
    int backBlur = 8;
    boolean upright = true;
    float clipPosX;
    float clipPosY;

    PeasyCam cam;
    PVector [][] globe;
    int total = 75;
    float m = 0;
    float mchange = 0;

    public void settings(){
        size(1280,578, P3D);
    }

    public void setup(){
        loadImg = loadImage("colorbar.jpg");
        bufrImg = createGraphics(width, height, P2D);
        srcImg = createImage(width, height, RGB);
        backImg = createImage(width, height, RGB);

        scrnShortSide = min(width, height);
        lensWidth = scrnShortSide;
        lensHeight = scrnShortSide;

        clipImg = createImage(scrnShortSide, scrnShortSide, RGB);
        distImg = createGraphics(scrnShortSide * 2, scrnShortSide, P3D);
        prevSphereImg = createGraphics(scrnShortSide, scrnShortSide, P3D);



        //maskImg = createGraphics(width,height,P2D);
        //saveImg = createImage(width, height, RGB);

        clipPosX = (width - lensWidth - 1) / 2;
        clipPosY = (height - lensHeight - 1) / 2;

        generateMesh();

        cam = new PeasyCam(this,500);
        globe = new PVector[total+1][total+1];
    }

    float a = 1;
    float b = 1;

    public float supershape(float theta, float m, float n1, float n2, float n3){
       float t1 = abs((1/a) * cos(m * theta / 4));
       t1 = pow(t1, n2);
       float t2 = abs((1/b) * sin(m * theta / 4));
       t2 = pow(t2, n3);
       float t3 = t1 + t2;
       float r = pow(t3, -1 / n1);
        return r;
    }

    public void draw(){

        m = map(sin(mchange), -1, 1, 0, 7);
        mchange += 0.05;

        background(0);

        lights();
        //translate(width/2,height/2);
        //sphere(200);

        //球の定義：ここから
        distSphere = createShape();
        float r = 200;
        for(int i = 0; i < total+1; i++){
            float lat = map(i, 0, total, -HALF_PI, HALF_PI);
            float r2 = supershape(lat,m,0.2F, 1.7F, 1.7F);
            for(int j = 0; j < total+1; j++){
                float lon = map(j, 0, total, -PI, PI);
                float r1 = supershape(lon,m,0.2F, 1.7F, 1.7F);

                float x = r * r1 * cos(lon) * r2 * cos(lat);
                float y = r * r1 * sin(lon) * r2 *cos(lat);
                float z = r * r2 * sin(lat);
                globe[i][j] = new PVector(x,y,z);
            }
        }

        distSphere.stroke(255);
        distSphere.fill(255);
        for(int i = 0; i < total; i++){
            distSphere.beginShape(QUAD_STRIP);
            for(int j = 0; j < total+1; j++){
                PVector v1 = globe[i][j];
                PVector v2 = globe[i+1][j];
                distSphere.stroke(255);
                distSphere.strokeWeight(2);

                if(i % 2 == 0){
                    distSphere.vertex(v1.x, v1.y, v1.z);
                    distSphere.vertex(v2.x, v2.y, v2.z);
                }else{
                    distSphere.vertex(v2.x, v2.y, v2.z);
                    distSphere.vertex(v1.x, v1.y, v1.z);
                }

            }
            distSphere.endShape();
        }
        //球の定義：ここまで
        shape(distSphere);


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

    public void updateSourceImage(){
        bufrImg.beginDraw();
        bufrImg.clear();  //clear()はPGraphicsクラスのメソッド
        bufrImg.imageMode(CENTER);
        bufrImg.image(loadImg,bufrImg.width/2, bufrImg.height/2);
        //if(isLoaded){
        //    bufrImg.image(loadImg,bufrImg.width/2, bufrImg.height/2);
        //}else{
        //    bufrImg.image(capImg,bufrImg.width/2,bufrImg.height/2);
        //}
        bufrImg.endDraw();

        srcImg = bufrImg.get();
        //if(capImg.width > capImg.height){
        //    srcImg.resize(width,0);  //resize()はPImageクラスのメソッド
        //}else {
        //    srcImg.resize(0, height);
        //}
        ////hint(DISABLE_DEPTH_TEST);  //test code
        ////image(srcImg,width/2,height/2);  //test code
    }

    public void drawSoratama() {


        background(0);  //デバッグのときコメントアウト

        updateSourceImage();

        backImg = srcImg.get();
        backImg.filter(BLUR, backBlur);
        image(backImg, width / 2, height / 2);  //背景画をスクリーンに描く

        clippingImage();

        initMesh();
        modelBrown();

        drawDistortion();                   //clipImgからレンズ歪みの画像を作ってdistImgとする

        float ratio = sphereDiaR;
        distSphere.scale(ratio);
        distSphere.setTexture(distImg);           //球にdistImgを貼ってスクリーンに上書きする
        distSphere.setStrokeWeight(0);
        pushMatrix();
        translate(width / 2, height / 2);
        rotateY(PI);
        if (!upright) {
            rotateZ(PI);
        }
        sphereDetail(24);
        shape(distSphere);
        popMatrix();
        distSphere.scale(1 / ratio);

/*
        maskImg.beginDraw();
        maskImg.fill(0);
        float maskDia = scrnShortSide*sphereDiaR.getValue()*1.05F;
        maskImg.ellipse(width/2,height/2,maskDia,maskDia);
        maskImg.endDraw();

        mask(maskImg);
*/

        //if (!lp_finishBlur.getState()) {
        //    filter(BLUR, 1);
        //}

    }

    public void clippingImage(){

        int left = (int) map(clipPosX - (width - bufrImg.width)/2,
                0, bufrImg.width, 0, srcImg.width);
        int top = (int) map(clipPosY - (height - bufrImg.height)/2,
                0, bufrImg.height, 0, srcImg.height);
        _clipEdgeWidth = (int) map( lensWidth,
                0,  bufrImg.width, 0, srcImg.width );  //（注）modelBrown()でも使用
        _clipEdgeHeight = (int) map( lensHeight,
                0, bufrImg.height, 0, srcImg.height);  //（注）modelBrown()でも使用

        clipImg = srcImg.get(left, top, _clipEdgeWidth, _clipEdgeHeight);

        //hint(DISABLE_DEPTH_TEST);  //test code
        //image(clipImg,width/2, height/2);  //test code
    }

    public static void main(String[] args){
        PApplet.main("origin.Main");
    }
}