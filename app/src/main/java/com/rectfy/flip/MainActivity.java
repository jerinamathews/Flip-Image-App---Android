package com.rectfy.flip;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY = 10;
    private static final int CAMERA = 11;
    private static final String IMAGE_DIRECTORY = "/Flip pic";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 12;
    private static final String TAG = "Main";
    ImageView imageview;
    Boolean perm=false;
    BottomNavigationView navigationView;
    String image_path = "";
    Vibrator vib;
    Boolean isAnimate = false;
    Boolean imageLoaded = false;
    TextView add_image_text;
    Boolean hapticFeed = true;
    Dialog pDialog;

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public void onBackPressed() {
        if(imageLoaded)
            clearImage();
        else
            super.onBackPressed();
    }

    @Override
    protected void onStart() {
        onSharedIntent();
        if(pDialog!=null)
            pDialog.dismiss();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);
        if(checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
            perm=true;

        }
        SharedPreferences pref = getSharedPreferences("Flip_Pref",MODE_PRIVATE);
        hapticFeed = pref.getBoolean("hapticFeed",true);


        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        navigationView = findViewById(R.id.bottomNavigationView);

        imageview = findViewById(R.id.pic);
        add_image_text = findViewById(R.id.add_pic_text);

        MobileAds.initialize(this, "ca-app-pub-3534792353435863~8198722054");

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_verti:
                        if(imageLoaded && imageview!=null){
                            if(hapticFeed)
                                vib.vibrate(50);
                            System.gc();
                            try {
                                Bitmap image_bitmap=null;
                                float rot = imageview.getRotation();
                                Bitmap myBitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
                                Matrix matrix = new Matrix();
                                matrix.postRotate(rot);
                                matrix.postScale(1.0f, -1.0f);

                                image_bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                                imageview.setRotation(0);
                                imageview.setImageBitmap(image_bitmap);
                            }catch (Exception e){
                                Toast.makeText(MainActivity.this, "This image is larger to process in original size. Please choose a scaled down version", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.action_horiz:
                        if(imageLoaded && imageview!=null){
                            if(hapticFeed)
                                vib.vibrate(50);
                            float rot = imageview.getRotation();
                            System.gc();
                            Bitmap image_bitmap=null;
                            try {
                                Bitmap myBitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
                                Matrix matrix = new Matrix();
                                matrix.postRotate(rot);
                                matrix.postScale(-1.0f, 1.0f);

                                image_bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                                imageview.setRotation(0);
                                imageview.setImageBitmap(image_bitmap);
                            }
                            catch (Exception e){
                                Toast.makeText(MainActivity.this, "This image is larger to process in original size. Please choose a scaled down version", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.action_gallery:
                        if(hapticFeed)
                            vib.vibrate(50);
                        startActivity(new Intent(MainActivity.this, GalleryActivity.class));
                        break;
                }
                return true;
            }
        });
        findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(perm) choosePhotoFromGallary();
            }
        });
        add_image_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(perm) choosePhotoFromGallary();
            }
        });
    }
    // Shared intent method
    private void onSharedIntent() {
        Intent receiverdIntent = getIntent();// Receive intent
        String receivedAction = receiverdIntent.getAction();// Get Action from
        // receive intent
        String receivedType = receiverdIntent.getType();// Get type from receive
        // intent

        if(receivedType!=null) {
            // If action is equal to action send
            if (receivedAction.equals(Intent.ACTION_SEND)) {

                if (receivedType.startsWith("image/")) {

                    Uri receiveUri = (Uri) receiverdIntent
                            .getParcelableExtra(Intent.EXTRA_STREAM);

                    if (receiveUri != null) {
                        imageLoaded = true;
                        add_image_text.setVisibility(View.GONE);
                        imageview.setImageURI(receiveUri);
                    } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
                        clearImage();
                    }
                }

            } else if (receivedAction.equals(Intent.ACTION_EDIT)) {
                if (receivedType.startsWith("image/")) {

                    Uri receiveUri = (Uri) receiverdIntent
                            .getData();

                    if (receiveUri != null) {
                        imageLoaded = true;
                        add_image_text.setVisibility(View.GONE);
                        imageview.setImageURI(receiveUri);
                    } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
                        clearImage();
                    }
                }
            }
        }
    }
    public void choosePhotoFromGallary() {

        if(hapticFeed)
            vib.vibrate(50);
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(galleryIntent, GALLERY);
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");


        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        try {
            startActivityForResult(pickIntent, GALLERY);
        }catch (Exception ignored){
            startActivityForResult(getIntent, GALLERY);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                imageLoaded = true;
                add_image_text.setVisibility(View.GONE);
                imageview.setImageURI(contentURI);

            }

        }
    }
    public String saveImage() {
        float rot = imageview.getRotation();
        Bitmap myBitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(rot);
        try {
            Bitmap image_bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File wallpaperDirectory = new File(
                    Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
            // have the object build the directory structure, if needed.
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            try {
                File f = new File(wallpaperDirectory, Calendar.getInstance()
                        .getTimeInMillis() + ".jpg");
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                MediaScannerConnection.scanFile(this,
                        new String[]{f.getPath()},
                        new String[]{"image/jpeg"}, null);
                fo.close();
                Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());


                return f.getAbsolutePath();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "This image is larger to process in original size. Please choose a scaled down version", Toast.LENGTH_SHORT).show();
        }
        return "";
    }







    private  boolean checkAndRequestPermissions() {
//        int camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

//        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.CAMERA);
//        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        Log.d(TAG, "sms & location services permission granted");
                        // process the normal flow
                        perm = true;
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Service Permissions are required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
    private void explain(String msg){
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        //  permissionsclass.requestPermission(type,code);
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.rectfy.flip")));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    public void saveImage(View view) {
        if(imageLoaded) {
            progressDialog();
            if(hapticFeed)
                vib.vibrate(50);
            image_path = saveImage();
            startActivity(new Intent(MainActivity.this,EditSavedActivity.class).putExtra("path",image_path));
//            Toast.makeText(this, "Image Saved.", Toast.LENGTH_SHORT).show();

            clearImage();
        }
        else{
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void rotateLeft(View view) {
        if(imageLoaded){
            if(hapticFeed)
                vib.vibrate(50);
            if(!isAnimate) {
                imageview.animate().rotation(imageview.getRotation() - 90).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        isAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAnimate = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }

        }
        else{
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void rotateRight(View view) {
        if(imageLoaded) {
            if(hapticFeed)
                vib.vibrate(50);
            if(!isAnimate) {
                imageview.animate().rotation(imageview.getRotation() + 90).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        isAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAnimate = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }
        }
        else{
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearImage(View view) {
        if(hapticFeed)
            vib.vibrate(50);
        clearImage();
    }
    public void clearImage(){
        if(imageLoaded && imageview!=null){
            imageLoaded=false;
            imageview.setImageBitmap(null);
            add_image_text.setVisibility(View.VISIBLE);
        }
    }

    public void showMenu(View view) {
        if(hapticFeed)
            vib.vibrate(50);
        try {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.more_menu,
                    (ViewGroup) findViewById(R.id.options));
            final PopupWindow pwindo = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            int[] pos = new int[2];
            view.getLocationInWindow(pos);
            int x = pos[0];
            int y = pos[1] + view.getHeight();
            pwindo.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pwindo.setOutsideTouchable(true);
            pwindo.setFocusable(true);
            pwindo.showAtLocation(layout, Gravity.START | Gravity.TOP, x, y);

            Switch switch1 = layout.findViewById(R.id.hapticFeed);
            switch1.setChecked(hapticFeed);
            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    hapticFeed = b;
                    SharedPreferences pref = getSharedPreferences("Flip_Pref",MODE_PRIVATE);
                    pref.edit().putBoolean("hapticFeed",hapticFeed).apply();
                }
            });
            TextView rate = layout.findViewById(R.id.rate_us);
            rate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.rectfy.flip")));
                }
            });

        }
        catch (Exception ignored){}
    }

    public void progressDialog(){
        pDialog = new Dialog(this);
        pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setContentView(R.layout.progress_dialog);
        pDialog.show();
    }

}
