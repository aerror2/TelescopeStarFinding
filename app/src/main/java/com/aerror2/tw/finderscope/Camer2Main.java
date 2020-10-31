package com.aerror2.tw.finderscope;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Camer2Main extends AppCompatActivity  {
    private static final String TAG = "Camer2Activity";

    private boolean permissionGranted = false;
    private String[] mCameraIdList;
    private CameraDevice mCamDevice;
    private CameraCaptureSession mSession;
    private  Handler mMainHandler ;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private CameraCharacteristics mCharacteristics;

    private TextureView  mTexView;

    private void startPreview()
    {
        if(mSurfaceTexture==null)  return;
        if(mSurface!=null) return;


        mSurface = new Surface(mSurfaceTexture);
        List<Surface> sfl = new ArrayList<Surface>();
        sfl.add(mSurface);
        try {
            mCamDevice.createCaptureSession(sfl,sessionStateCb,mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    private final TextureView.SurfaceTextureListener mSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            mSurfaceTexture = surfaceTexture;
            startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            
        }
    };


    private CameraCaptureSession.CaptureCallback mPreviewCallback = new CameraCaptureSession
            .CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
          //  updateAfState(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
         //   updateAfState(result);
            //mCallback.onRequestComplete();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camer2);
        mTexView = findViewById(R.id.texture_preview);
        mTexView.setSurfaceTextureListener(mSurfaceListener);
        mSurfaceTexture = mTexView.getSurfaceTexture();
        mMainHandler  = new Handler(Looper.getMainLooper());
        requestpermission();

    }

    private void doEnumCamera() {
        CameraManager mgr = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
             mCameraIdList = mgr.getCameraIdList();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* ------------------------- private function------------------------- */
    private int getValidAFMode(int targetMode) {
        int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        for (int mode : allAFMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.i(TAG, "not support af mode:" + targetMode + " use mode:" + allAFMode[0]);
        return allAFMode[0];
    }

    private int getValidAntiBandingMode(int targetMode) {
        int[] allABMode = mCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        for (int mode : allABMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.i(TAG, "not support anti banding mode:" + targetMode
                + " use mode:" + allABMode[0]);
        return allABMode[0];
    }

    private boolean isMeteringSupport(boolean focusArea) {
        int regionNum;
        if (focusArea) {
            regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        } else {
            regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
        }
        return regionNum > 0;
    }

    private float getMinimumDistance() {
        Float distance = mCharacteristics.get(
                CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        if (distance == null) {
            return 0;
        }
        return distance;
    }

    private boolean isFlashSupport() {
        Boolean support = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        return support != null && support;
    }

    boolean canTriggerAf() {
        int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        return  allAFMode != null && allAFMode.length > 1;
    }


    public CaptureRequest getPreviewRequest(CaptureRequest.Builder builder) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        int antiBMode = getValidAntiBandingMode(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    //session callback
    private CameraCaptureSession.StateCallback sessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " session onConfigured id:" + session.getDevice().getId());
            mSession = session;
            try {
                CaptureRequest.Builder builder = mCamDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(mSurface);

                //   updateRequestFromSetting(builder);
                CaptureRequest request = getPreviewRequest(builder);
                mSession.setRepeatingRequest(request, mPreviewCallback, mMainHandler);
            } catch (CameraAccessException | IllegalStateException e) {
                Log.e(TAG, "send repeating request error:" + e.getMessage());
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "create session fail id:" + session.getDevice().getId());
        }
    };


    void initCharacteristics() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert manager != null;
            mCharacteristics= manager.getCameraCharacteristics(mCamDevice.getId());
        } catch (CameraAccessException e) {
            Log.e(TAG, "getCameraCharacteristics error:" + e.getMessage());
        }
    }

    private CameraDevice.StateCallback camStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "device opened :" + camera.getId());
            mCamDevice = camera;
            initCharacteristics();
            startPreview();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "onDisconnected");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "error occur when open camera :" + camera.getId() + " error code:" + error);
            camera.close();
        }
    };


    private void setupCamera() {
        if (mCameraIdList != null && mCameraIdList.length > 0) {

            try {
                CameraManager mgr = (CameraManager) getSystemService(CAMERA_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestpermission();
                    return;
                }



                mgr.openCamera(mCameraIdList[0], camStateCallback, mMainHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }




    final static int PermissionRquestCode = 6554;


    /** 处理权限请求结果逻辑，再次调用请求、或提示跳转设置界面 */
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,
                                           int[] grantResults) {

        Log.d(TAG,"onRequestPermissionsResult");
        if (requestCode == PermissionRquestCode) {
            int ret = this.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid());
            if (ret == PackageManager
                    .PERMISSION_GRANTED)
            {
                onPermissionGranted();
            }
        }
    }

    void onPermissionGranted()
    {
        permissionGranted = true;

        doEnumCamera();
        setupCamera();
    }

    void requestpermission()
    {
        int sdkVersion = this.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= 23 && sdkVersion >= 23) {
            int ret = this.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid());
            if (ret != PackageManager
                    .PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionRquestCode);
            }
            else
            {
                onPermissionGranted();
            }

        }
        else
        {
            onPermissionGranted();
        }

    }

}
