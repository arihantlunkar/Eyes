package com.eyes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

import java.util.EnumSet;

@SuppressLint("NewApi")
public class CameraPreviewActivity extends Activity implements Camera.PreviewCallback {

    static boolean cameraSwitch = false;
    Camera cameraObj;
    FrameLayout preview;
    FacialProcessing facialProcessing;
    FaceData[] faceArray = null;
    View myView;
    Canvas canvas = new Canvas();
    boolean landScapeMode = false;
    int cameraIndex;
    int leftEyeBlink = 0;
    int rightEyeBlink = 0;
    int surfaceWidth = 0;
    int surfaceHeight = 0;
    int deviceOrientation;
    int presentOrientation;
    Display display;
    int displayAngle;
    int dummyLeftEye=0,dummyRightEye=0;
    Boolean isLeftEyeBlink = false;
    Boolean isRightEyeBlink = false;
    Boolean isLeftEyeClosed = false;
    Boolean isRightEyeClosed = false;
    private CameraSurfacePreview mPreview;
    private DrawView drawView;
    static long currentTime = System.currentTimeMillis();
    static long previousTime = System.currentTimeMillis();
    static long currentTime1 = System.currentTimeMillis();
    static long previousTime1 = System.currentTimeMillis();
    int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        myView = new View(CameraPreviewActivity.this);
        preview = (FrameLayout) findViewById(R.id.camera_preview);

        if (facialProcessing == null) {
            facialProcessing = FacialProcessing.getInstance();
            facialProcessing.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        } else {
            return;
        }

        cameraIndex = Camera.getNumberOfCameras() - 1;

        try {
            cameraObj = Camera.open(cameraIndex);
            } catch (Exception e) {
        }

        mPreview = new CameraSurfacePreview(CameraPreviewActivity.this, cameraObj, facialProcessing);
        preview.removeView(mPreview);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        blackScreenWhenNotInView();
        cameraObj.setPreviewCallback(CameraPreviewActivity.this);
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        mode = getSharedPreferences("eye_data", Context.MODE_PRIVATE).getInt(getString(R.string.storedSelectDifficulty), 0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraObj != null) {
            stopCamera();
        }

        if (!cameraSwitch)
            startCamera(1);
        else
            startCamera(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_preview, menu);
        return true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera arg1) {

        presentOrientation = (90 * Math.round(deviceOrientation / 90)) % 360;
        int dRotation = display.getRotation();
        PREVIEW_ROTATION_ANGLE angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;

        switch (dRotation) {
            case 0:
                displayAngle = 90;
                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_90;
                break;

            case 1:
                displayAngle = 0;
                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;
                break;

            case 2:
                break;

            case 3:
                displayAngle = 180;
                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_180;
                break;
        }

        if (facialProcessing == null) {
            facialProcessing = FacialProcessing.getInstance();
        }

        Parameters params = cameraObj.getParameters();
        Size previewSize = params.getPreviewSize();
        surfaceWidth = mPreview.getWidth();
        surfaceHeight = mPreview.getHeight();

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !cameraSwitch) {
            facialProcessing.setFrame(data, previewSize.width, previewSize.height, true, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = true;
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && cameraSwitch) {
            facialProcessing.setFrame(data, previewSize.width, previewSize.height, false, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = true;
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                && !cameraSwitch) {
            facialProcessing.setFrame(data, previewSize.width, previewSize.height, true, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = false;
        }
        else {
            facialProcessing.setFrame(data, previewSize.width, previewSize.height, false, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = false;
        }

        int numFaces = facialProcessing.getNumFaces();

        if (numFaces == 0) {
            blackScreenWhenNotInView();
            canvas.drawColor(0, Mode.CLEAR);
        } else {
            faceArray = facialProcessing.getFaceData(EnumSet.of(FacialProcessing.FP_DATA.FACE_RECT,
                    FacialProcessing.FP_DATA.FACE_COORDINATES, FacialProcessing.FP_DATA.FACE_CONTOUR,
                    FacialProcessing.FP_DATA.FACE_SMILE, FacialProcessing.FP_DATA.FACE_ORIENTATION,
                    FacialProcessing.FP_DATA.FACE_BLINK, FacialProcessing.FP_DATA.FACE_GAZE));
            if (faceArray == null) {
                blackScreenWhenNotInView();
            } else {
                facialProcessing.normalizeCoordinates(surfaceWidth, surfaceHeight);

                for (int j = 0; j < numFaces; j++) {
                    leftEyeBlink = faceArray[j].getLeftEyeBlink();
                    rightEyeBlink = faceArray[j].getRightEyeBlink();
                }

                isLeftEyeBlink = false;
                isRightEyeBlink = false;

                currentTime = System.currentTimeMillis();
                currentTime1 = System.currentTimeMillis();

                if (leftEyeBlink >= 70 && dummyLeftEye == 0) {
                    dummyLeftEye = 1;

                } else if (leftEyeBlink < 40 && dummyLeftEye == 1 && (currentTime - previousTime) > 200) {
                    isLeftEyeBlink = true;
                    previousTime = currentTime;
                    dummyLeftEye = 0;
                }

                if (rightEyeBlink >= 70 && dummyRightEye == 0) {
                    dummyRightEye = 1;
                } else if (rightEyeBlink < 40 && dummyRightEye == 1 && (currentTime1 - previousTime1) > 200) {
                    isRightEyeBlink = true;
                    previousTime1 = currentTime1;
                    dummyRightEye = 0;
                }

                if((isLeftEyeBlink || isRightEyeBlink) && Math.abs(rightEyeBlink-leftEyeBlink) <= 15 && ((isLeftEyeBlink && dummyRightEye == 1) || (isRightEyeBlink && dummyLeftEye == 1)))
                {
                    dummyLeftEye = 0;
                    dummyRightEye = 0;
                    isLeftEyeBlink = true;
                    isRightEyeBlink = true;
                }

                if(((leftEyeBlink >= 70 && dummyLeftEye == 1 && rightEyeBlink>= 60) || (rightEyeBlink >= 70 && dummyRightEye == 1 && leftEyeBlink >= 60)) && Math.abs(rightEyeBlink-leftEyeBlink) <= 15)
                {
                    dummyLeftEye = 1;
                    dummyRightEye = 1;
                }

                preview.removeView(drawView);
                drawView = new DrawView(this, true, isLeftEyeBlink,isRightEyeBlink,isLeftEyeClosed,isRightEyeClosed, mode);
                preview.addView(drawView);
            }
        }
    }

    public void blackScreenWhenNotInView(){
        if (drawView != null) {
            isRightEyeClosed = false;
            isLeftEyeClosed = false;
            isLeftEyeBlink = false;
            isRightEyeBlink = false;
            preview.removeView(drawView);
            drawView = new DrawView(this, false, isLeftEyeBlink,isRightEyeBlink,isLeftEyeClosed,isRightEyeClosed, mode);
            preview.addView(drawView);
        } else {
            isRightEyeClosed = false;
            isLeftEyeClosed = false;
            isLeftEyeBlink = false;
            isRightEyeBlink = false;
            drawView = new DrawView(this, false, isLeftEyeBlink,isRightEyeBlink,isLeftEyeClosed,isRightEyeClosed,mode);
            preview.addView(drawView);
        }
    }

    public void stopCamera() {
        if (cameraObj != null) {
            cameraObj.stopPreview();
            cameraObj.setPreviewCallback(null);
            preview.removeView(mPreview);
            cameraObj.release();
            facialProcessing.release();
            facialProcessing = null;
        }
        cameraObj = null;
    }

    public void startCamera(int cameraIndex) {

        if (facialProcessing == null) {
            facialProcessing = FacialProcessing.getInstance();
        }

        try {
            cameraObj = Camera.open(cameraIndex);
            } catch (Exception e) {
        }

        mPreview = new CameraSurfacePreview(CameraPreviewActivity.this, cameraObj, facialProcessing);
        preview.removeView(mPreview);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        cameraObj.setPreviewCallback(CameraPreviewActivity.this);

    }
}