package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;




import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_EXTERNAL_STORAGE = 101;

    private static final int REQUEST_PERMISSION = 123;
    private static final String[] permissionArray = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static File imageFile;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限 √
                Log.d("mylog","require the camera & storage permission");
                requestPermissions(permissionArray,REQUEST_PERMISSION);
            } else {
                takePicture();
            }
        });

    }

    private void takePicture() {
        // todo 打开相机

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // get the image  for saving an image
        imageFile =Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if(imageFile != null){
            Uri fileUri = FileProvider.getUriForFile(this,"com.bytedance.camera.demo",imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("mylog","receive the result of REQUEST_IMAGE_CAPTURE");
            try {
                setPic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPic() throws Exception {
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();
        // Get the dimensions of the bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //不加载图片
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(),options);
        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;
        //todo 根据缩放比例读取文件，生成Bitmap
        int scalaFactor = Math.min(photoWidth/targetWidth,photoHeight/targetHeight); // 0 -->don't need scala  1-->need scala
        Log.d("mylog","scalaFactor:"+scalaFactor);
        options.inJustDecodeBounds = false; //加载图片
        options.inSampleSize = scalaFactor;
        options.inPurgeable = true;
        //todo 如果存在预览方向改变，进行图片旋转
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),options);
        bitmap = rotateImage(bitmap,imageFile.getAbsolutePath());

        //todo 展示图片
        imageView.setImageBitmap(bitmap);


    }
    //图片旋转
    public static Bitmap rotateImage(Bitmap bitmap,String path )throws Exception{
        ExifInterface scrExif = new ExifInterface(path);
        Matrix matrix = new Matrix();
        int angle=0;
        int orientation = scrExif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle= 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle= 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle= 270;
                break;
            default:
                break;
        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("mylog","onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                //todo 判断权限是否已经授予 √
                String resultStr ="";
                boolean allRight = true;
                for(int i=0;i<permissions.length;++i)
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        resultStr += permissions[i] + ": "+"success\n";
                    else {
                        resultStr += permissions[i] + ": " + "fail\n";
                        allRight = false;
                    }
                if(allRight == true){ //已获取全部权限
                    takePicture();
                }else { //存在权限未获取
                    Toast.makeText(this, resultStr, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}