package com.sukshi.sukshicamerademo;

/*
 * Vishwam Corp CONFIDENTIAL

 * Vishwam Corp 2018
 * All Rights Reserved.

 * NOTICE:  All information contained herein is, and remains
 * the property of Vishwam Corp. The intellectual and technical concepts contained
 * herein are proprietary to Vishwam Corp
 * and are protected by trade secret or copyright law of U.S.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vishwam Corp
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

import static com.sukshi.sukshicamerademo.FaceDect.onFaceLandMarkDestectedListener;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private Bitmap marker;

    private BitmapFactory.Options opt;
    private Resources resources;

    private int faceId;
    Context context1;

    float isSmilingProbability = -1;
    float eyeRightOpenProbability = -1;
    float eyeLeftOpenProbability = -1;

    public static Paint mHintOutlinePaint;

    public Paint mHintTextPaint;

    private volatile Face mFace;

    public static Float faceArea;

    public static boolean faceIsInTheBox =false , faceRatioOk;

    public FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        this.context1 = context;
        opt = new BitmapFactory.Options();
        opt.inScaled = false;
        resources = context.getResources();
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt);
        initializePaints(resources);

    }

    public void setId(int id) {
        faceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    private void initializePaints(Resources resources) {
        mHintTextPaint = new Paint();
        mHintTextPaint.setTextAlign(Paint.Align.CENTER);
        mHintTextPaint.setTextSize(48);
    }

    public void goneFace() {
        mFace = null;
    }

    @Override
    public void draw(Canvas canvas) {

        Face face = mFace;
        if(face == null) {

            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            isSmilingProbability = -1;
            eyeRightOpenProbability= -1;
            eyeLeftOpenProbability = -1;
            return;
        }

        float centerX = translateX(face.getPosition().x + face.getWidth() / 2.0f);
        float centerY = translateY(face.getPosition().y + face.getHeight() / 2.0f);
        float offsetX = scaleX(face.getWidth() / 2.0f);
        float offsetY = scaleY(face.getHeight() / 2.0f);

        // Draw a box around the face.
        float left = centerX - offsetX * 0.75f;
        float right = centerX + offsetX * 0.75f;
        float top = centerY - offsetY*1.5f ;
        float bottom = centerY + offsetY * 1f;

         int hintArea = ((canvas.getWidth()-200) - 200) * ((canvas.getHeight()-200) - 200);

        faceIsInTheBox = !(left < 200) && !(right > canvas.getWidth()-200)
                && !(top < 200) && !(bottom > canvas.getHeight()-200);

        faceArea = (right - left) * (bottom - top);

        faceRatioOk = faceArea > hintArea / 3;

        String error = "";
        if (isFaceStraight(face.getEulerY(),face.getEulerZ()) && faceIsInTheBox && faceRatioOk) {
            mHintTextPaint.setColor(Color.GREEN);
            error = "Straight face found.";
            onFaceLandMarkDestectedListener.onFaceLandMarkDetected(true);
        } else {
            mHintTextPaint.setColor(Color.RED);
            if(!faceIsInTheBox){
                error = "Face is outside the green frame.";
            }else if(!faceRatioOk){
                error = "Face ratio is small. Bring phone close to face";
            }else if(!isFaceStraight(face.getEulerY(),face.getEulerZ())){
                error = "Face is not straight.";
            }
            onFaceLandMarkDestectedListener.onFaceLandMarkDetected(false);
        }

        if(!error.isEmpty() && mHintTextPaint != null){
            canvas.drawText(error, canvas.getWidth()/2, canvas.getHeight() - 100, mHintTextPaint);
        }
    }

    /**
     * For face to be straight Y axis and Z axis eular angle should lie between -12 to 12.
    * */
    private boolean isFaceStraight(float eularY, float eularZ){
        return eularY > -12 && eularY < 12 && eularZ > -12 && eularZ < 12;
    }
}