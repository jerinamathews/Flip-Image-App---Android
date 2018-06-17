package com.rectfy.flip;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import com.adclient.android.sdk.nativeads.view.SmartBannerAdView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    RecyclerView galleryRecycler;
    GalleryAdapter adapter;
    private static final String IMAGE_DIRECTORY = "/Flip pic";
    List<ImageModel> imgList = new ArrayList<>();
    private String path;


    @Override
    protected void onStart() {
        super.onStart();
        imgList.clear();
        File file = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        File[] files = file.listFiles();
        int pos =-1;
        if(files != null){
            for(File f : files){ // loop and print all file
                imgList.add(new ImageModel(f.getName(),f.getPath()));
                if(path!=null){
                    if(path.equals(f.getPath())){
                        pos = imgList.size()-1;
                    }
                }

            }
            if(pos!=-1){
                path=null;
                Intent intent = new Intent(GalleryActivity.this, DetailActivity.class);
                intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) imgList);
                intent.putExtra("pos", pos);
                pos=-1;
                startActivity(intent);

            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_gallery);

        if(getSharedPreferences("Flip_Pref",MODE_PRIVATE).getBoolean("remove_ad",false)){
            remove_ad();
        }
        else {

            final SmartBannerAdView smartBannerAdView =
                    findViewById(R.id.gallery_banner_ad_view);
            smartBannerAdView.setListener(
                    new SmartBannerAdView.SmartBannerAdViewListener() {
                        @Override
                        public void onBannerLoading(SmartBannerAdView banner, String message) {
                            Log.d("TestApp", "onBannerLoading : loaded = " +
                                    banner.isLoaded());
                        }

                        @Override
                        public void onBannerRefreshed(SmartBannerAdView banned, String
                                message) {
                            Log.d("TestApp", "onBannerRefreshed");
                        }

                        @Override
                        public void onBannerImpression(SmartBannerAdView banner) {
                            Log.d("TestApp", "onBannerImpression");
                        }

                        @Override
                        public void onBannerFailed(SmartBannerAdView banner, String message) {
                            Log.d("TestApp", "onBannerFailed msg:" + message);
                        }

                        @Override
                        public void onBannerClicked(SmartBannerAdView banner) {
                            Log.d("TestApp", "onBannerClicked");
                        }
                    });
            smartBannerAdView.load(this);
        }


        galleryRecycler = findViewById(R.id.galleryRecycler);

        path = getIntent().getStringExtra("path");

        galleryRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        galleryRecycler.setHasFixedSize(true); // Helps improve performance

        adapter = new GalleryAdapter(GalleryActivity.this, imgList);
        galleryRecycler.setAdapter(adapter);
        galleryRecycler.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {

                        Intent intent = new Intent(GalleryActivity.this, DetailActivity.class);
                        intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) imgList);
                        intent.putExtra("pos", position);
                        startActivity(intent);

                    }
                }));

    }

    private void remove_ad() {
        SmartBannerAdView ad = findViewById(R.id.gallery_banner_ad_view);
        ad.setVisibility(View.GONE);
    }

    public void goBack(View view) {
        finish();
    }
}
