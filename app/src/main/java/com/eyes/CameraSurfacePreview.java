/*
 * ======================================================================
 * Copyright � 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * QTI Proprietary and Confidential.
 * =====================================================================
 * @file: CameraSurfacePreview.java
 */

package com.eyes;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import java.io.IOException;
import java.util.List;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder mHolder;
    private final Camera mCamera;
    private final int MAX_NUM_BYTES = 1500000;
    Context mContext;
    FacialProcessing mFaceProc;

    @SuppressWarnings("deprecation")
    public CameraSurfacePreview(Context context, Camera camera, FacialProcessing faceProc) {
        super(context);
        mCamera = camera;
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFaceProc = faceProc;
        Camera.Parameters pm = mCamera.getParameters();

        int index = 0;
        List<Size> previewSize = pm.getSupportedPreviewSizes();
        for (int i = 0; i < previewSize.size(); i++) {
            int width = previewSize.get(i).width;
            int height = previewSize.get(i).height;
            int size = width * height * 3 / 2;
            if (size < MAX_NUM_BYTES) {
                index = i;
                break;
            }
        }
        pm.setPreviewSize(previewSize.get(index).width, previewSize.get(index).height);
        mCamera.setParameters(pm);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
