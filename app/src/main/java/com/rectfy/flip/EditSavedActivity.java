package com.rectfy.flip;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class EditSavedActivity extends AppCompatActivity {

    ImageView imgView;
    private AdView mAdView1,mAdView2;
    ImageButton wtsp,fb,instgrm,shr;
    Button viewImg;
    private Vibrator vib;
    private boolean hapticFeed=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_edit_saved);
        final String path = getIntent().getStringExtra("path");
        Log.e("Img Path",path);
        imgView= findViewById(R.id.imgThumb);
        if(path!=null){
            Glide.with(this).load(path)
                    .thumbnail(0.5f)
                    .into(imgView);
        }

        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        SharedPreferences pref = getSharedPreferences("Flip_Pref",MODE_PRIVATE);
        hapticFeed = pref.getBoolean("hapticFeed",true);
        int count = pref.getInt("save_count",0);

        if(count>5){
            pref.edit().putInt("save_count",0).apply();
            showRateDialog();
        }else if(count!=-1) {
            pref.edit().putInt("save_count",count+1).apply();
        }
        mAdView1 = findViewById(R.id.editPageBanner);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("B5C6779005B7324A3BB20FCA3B71F16B").build();
        mAdView1.loadAd(adRequest);

        mAdView2 = findViewById(R.id.editPageRectangle);
        AdRequest adRequest2 = new AdRequest.Builder().addTestDevice("B5C6779005B7324A3BB20FCA3B71F16B").build();
        mAdView2.loadAd(adRequest2);
        wtsp = findViewById(R.id.wtsp);
        fb = findViewById(R.id.facbk);
        instgrm = findViewById(R.id.instgrm);
        shr = findViewById(R.id.shareall);
        viewImg = findViewById(R.id.viewImg);
        final PackageManager pm = getPackageManager();

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                startActivity(new Intent(EditSavedActivity.this,GalleryActivity.class).putExtra("path",path));
            }
        });
        viewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                startActivity(new Intent(EditSavedActivity.this,GalleryActivity.class).putExtra("path",path));
            }
        });
        File imageFile = new File(path);
        final Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

        wtsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                try {
                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("image/*");
                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    waIntent.setPackage("com.whatsapp");
                    waIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(waIntent);
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(EditSavedActivity.this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType("image/*");
                sendIntent.setPackage("com.facebook.orca");
                try {
                    startActivity(sendIntent);
                }
                catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(EditSavedActivity.this,"Facebook Messenger Not installed", Toast.LENGTH_LONG).show();
                }
            }
        });
        instgrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType("image/*");
                sendIntent.setPackage("com.instagram.android");
                try {
                    startActivity(sendIntent);
                }
                catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(EditSavedActivity.this,"Instagram Not installed", Toast.LENGTH_LONG).show();
                }
            }
        });
        shr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hapticFeed)
                    vib.vibrate(50);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                try {
                    startActivity(Intent.createChooser(intent, "Share your Image"));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(EditSavedActivity.this, "No App Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
            


    }
    public void showRateDialog() {
        final SharedPreferences.Editor editor = getSharedPreferences("Flip_Pref",MODE_PRIVATE).edit();
        String APP_TITLE ="Flip Image";
        final Boolean[] first = {true};
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_layout);
        final Button yes = dialog.findViewById(R.id.rate_yes);
        final Button no = dialog.findViewById(R.id.rate_no);
        final TextView content = dialog.findViewById(R.id.rate_text);
        content.setText("Enjoying Flip Image App ?");
        yes.setText("Yes!");
        no.setText("Not Really");

        yes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(first[0]){
                    first[0] =false;
                    dialog.dismiss();
                    content.setText("How about rating on the Play Store, then?");
                    yes.setText("Ok, sure");
                    no.setText("No, thanks");
                    dialog.show();
                }
                else {
                    editor.putInt("save_count",-1).apply();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.rectfy.flip")));
                    dialog.dismiss();
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(first[0]){
                    Toast.makeText(EditSavedActivity.this, "Noted. We will make the app better in coming updates.", Toast.LENGTH_SHORT).show();
                    first[0]=false;
                }
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    public void goBack(View view) {
        finish();
    }
}
