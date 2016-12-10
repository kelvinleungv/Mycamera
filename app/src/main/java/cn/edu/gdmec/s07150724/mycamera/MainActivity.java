package cn.edu.gdmec.s07150724.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private ImageView mImageView;
    private  SurfaceHolder mSurfaceHolder;
    private  ImageView shutter;
    private android.hardware.Camera mcamera=null;
    private boolean mPreviewRunning;
    private static  final int MENU_START=1;
    private static  final int MENU_SENSOR=1;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mSurfaceView =(SurfaceView)findViewById(R.id.camera);
        mImageView=(ImageView)findViewById(R.id.image);
        shutter=(ImageView)findViewById(R.id.shutter);

        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder=mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraParams();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    try{
        if(mPreviewRunning){
            mcamera.stopPreview();
        }
        mcamera.setPreviewDisplay(holder);
        mcamera.startPreview();
        mPreviewRunning=true;
    }catch(Exception e){
        e.printStackTrace();
    }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mcamera!=null){
            mcamera.stopPreview();
            mPreviewRunning=false;
            mcamera.release();
            mcamera=null;
        }
    }

    @Override
    public void onClick(View v) {
        if(mPreviewRunning){
            shutter.setEnabled(false);
            mcamera.autoFocus(new android.hardware.Camera.AutoFocusCallback(){
                @Override
                public void onAutoFocus(boolean success, android.hardware.Camera camera) {
                    mcamera.takePicture(mShutterCallback,
                            null,mPictureCallback);
                }});
        }
    }
    android.hardware.Camera.PictureCallback mPictureCallback=new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            if (data != null) {
                saveAndShow(data);
            }
        }
    };

    android.hardware.Camera.ShutterCallback mShutterCallback=new android.hardware.Camera.ShutterCallback(){
        public void onShutter(){
            System.out.println("快照回调函数....");
        }
    };
    public void setCameraParams(){
        if(mcamera!=null){
            return;

        }
        mcamera= android.hardware.Camera.open();
        android.hardware.Camera.Parameters params=mcamera.getParameters();
        params.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
        params.setPreviewFrameRate(3);
        params.setPreviewFormat(PixelFormat.YCbCr_422_SP);
        params.set("jpeg-quality",85);
        List<android.hardware.Camera.Size> list=params.getSupportedPictureSizes();
        android.hardware.Camera.Size size=list.get(0);
        int w =size.width;
        int h=size.height;
        params.setPictureSize(w,h);
        params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_AUTO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_START,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==MENU_START){
            setRequestedOrientation(ActivityInfo.
                    SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.
                    SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if(item.getItemId()==MENU_SENSOR){
            Intent intent=new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void saveAndShow(byte[] data){
        try{
            String imageId=System.currentTimeMillis()+"";
            String pathName=android.os.Environment.getExternalStorageDirectory().getPath()
                    +"/mycamera";
            File file=new File(pathName);
            if(!file.exists()){
                file.mkdirs();
            }
            pathName+="/"+imageId+"jpeg";
            file=new File(pathName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album=new AlbumActivity();
            bitmap =album.loadImage(pathName);
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            if(mPreviewRunning){
                mcamera.stopPreview();
                mPreviewRunning=false;
            }
            shutter.setEnabled(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
