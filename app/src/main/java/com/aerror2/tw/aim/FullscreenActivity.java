package com.aerror2.tw.aim;


import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;



/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements OnClickListener, SurfaceHolder.Callback {

    private  static final   String TAG="AIM";
    private SurfaceView surfaceView;
    private Camera camera;
    private Camera.Parameters parameters;

   // private Button btnLarge, btnSmall;
    private SeekBar thebar;
    private Button  switchCam;
    private SurfaceHolder surfaceHolder;
    boolean mIsSupportZoom=false;
    int cameraPosition=1;
    boolean permissionGranted = false;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.classes_activity_takephoto);
        initView();
        requestpermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permissionGranted)
        {
            initCamera();
        }
        else
        {
            requestpermission();;
        }
    }

    private void initView() {

        switchCam = (Button)findViewById(R.id.switch_camera);
        switchCam.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });


        thebar = (SeekBar) findViewById(R.id.seekBar);
        thebar.setMax(100);
        thebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setZoom(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

        try
        {
            if(camera!=null)
            camera.setPreviewDisplay(surfaceHolder);
        }
        catch (IOException e)
        {
            Log.e(TAG,"setPreviewDisplay failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        Log.d(TAG, "surfaceChanged " + width + " height " + height);
        // 实现自动对焦
        camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    if(camera!=null) {
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦
                        doAutoFocus();
                    }
                }
            }
        });
    }

    // 相机参数的初始化设置
    private void initCamera() {
        if (null == camera) {
            camera = Camera.open();
        }
        if(surfaceView==null) {
            surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
            surfaceView.setFocusable(true);
            surfaceView.setOnClickListener(this);
            surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceHolder.setKeepScreenOn(true);
            surfaceHolder.addCallback(this);
        }


        parameters = camera.getParameters();
      //  parameters.setPictureFormat(PixelFormat.JPEG);

        if (!Build.MODEL.equals("KORIDY H30")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        setDispaly(camera);
        camera.setParameters(parameters);
        try
        {
            camera.setPreviewDisplay(surfaceHolder);
        }
        catch (IOException e)
        {
            Log.e(TAG,"setPreviewDisplay failed");
        }
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
        mIsSupportZoom = isSupportZoom();
        camera.startPreview();




    }

    // 控制图像的正确显示方向
    private void setDispaly(Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }

    }

    // 实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e(TAG, "setDisplayOrientation error");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.surfaceView:
                doAutoFocus();
                break;
            default:
                break;
        }
    }

    // handle button auto focus
    private void doAutoFocus() {
        if(camera==null) return;

        if(cameraPosition==0)
            return ;

        parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);
        camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    if (!Build.MODEL.equals("KORIDY H30")) {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        camera.setParameters(parameters);
                    } else {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(parameters);
                  }
                }
            }
        });
    }

    private void takePicture() {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    // define shutterCallback
    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            // TODO Do something when the shutter closes.
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Do something with the image RAW data.
        }
    };

    // stroe the picture in format jpeg
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };



    public boolean isSupportZoom()
    {
        boolean isSuppport = true;
        if (camera.getParameters().isSmoothZoomSupported())
        {
            isSuppport = false;
        }
        return isSuppport;
    }


    public void setZoom(int iz)
    {
        if (mIsSupportZoom)
        {
            try
            {
                Parameters params = camera.getParameters();
                final int MAX = params.getMaxZoom();

                if(MAX==0)return;

                int zoomValue = iz*MAX/100;

                params.setZoom(zoomValue);
                camera.setParameters(params);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }



    final static int PermissionRquestCode = 6554;
    final static int PermissionResultCode = 6555;

    /** 在手机设置中打开的应用权限 */
    private static void PermissionSetting(final Activity activity, final String permission)
    {
        if (permission.trim().equals("")) return;

        // 获取权限对应的标题和详细说明信息
        String permissionLabel = "";
        String permissionDescription = "";

        try
        {
            PackageManager packageManager = activity.getPackageManager();
            // Tools.showText("permission -> " + permission);

            PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);

            // PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
            // Tools.showText("permission组 -> " + permissionGroupInfo.loadLabel(packageManager).toString());

            permissionLabel = permissionInfo.loadLabel(packageManager).toString();
            // Tools.showText("permission名称 -> " + permissionLabel);

            permissionDescription = permissionInfo.loadDescription(packageManager).toString();
            // Tools.showText("permission描述 -> " + permissionDescription);

        }
        catch (Exception ex)
        {
            return;
        }

        // 自定义Dialog弹窗，显示权限请求
        permissionLabel = "Application need ：" + permissionLabel + "\r\n" + permission;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(permissionLabel);
        builder.setMessage(permissionDescription);
        builder.setPositiveButton("Add permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        // 打开应用对应的权限设置界面
                        String action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
                        Intent intent = new Intent(action);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivityForResult(intent, PermissionResultCode);	// 从应用设置界面返回时执行OnActivityResult
                    }
                }
        );

        builder.setNegativeButton("Deny and Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // 若拒绝了所需的权限请求，则退出应用
                activity.finish();
                System.exit(0);
            }
        });
        builder.show();
    }


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
        initCamera();
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

    void switchCamera()
    {
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for(int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(cameraPosition == 1) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    setDispaly(camera);
                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    setDispaly(camera);
                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 1;
                    break;
                }
            }

        }

    }
}
