package com.rectfy.flip;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.adclient.android.sdk.nativeads.view.SmartBannerAdView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;

    public ArrayList<ImageModel> data = new ArrayList<>();
    int pos;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Vibrator vib;
    private boolean hapticFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_detail);
        if(getSharedPreferences("Flip_Pref",MODE_PRIVATE).getBoolean("remove_ad",false)){
            remove_ad();
        }
        else {

            final SmartBannerAdView smartBannerAdView =
                    findViewById(R.id.show_banner_ad_view);
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

        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        SharedPreferences pref = getSharedPreferences("Flip_Pref",MODE_PRIVATE);
        hapticFeed = pref.getBoolean("hapticFeed",true);
        data = getIntent().getParcelableArrayListExtra("data");
        pos = getIntent().getIntExtra("pos", 0);

        setTitle(data.get(pos).getName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pos);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //noinspection ConstantConditions
                setTitle(data.get(position).getName());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void remove_ad() {
        SmartBannerAdView ad = findViewById(R.id.show_banner_ad_view);
        ad.setVisibility(View.GONE);
    }

    public void goBack(View view) {

        if(hapticFeed)
            vib.vibrate(50);
        finish();
    }

    public void onShare(View view) {
        if(hapticFeed)
            vib.vibrate(50);
        try {
            File imageFile = new File(data.get(mViewPager.getCurrentItem()).getUrl());
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");

            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(intent, "Share your Image"));
        } catch (Exception e) {
            Toast.makeText(DetailActivity.this, "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onEdit(View view) {
        if (hapticFeed)
            vib.vibrate(50);
        PackageManager pm = getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("image/*");

            File imageFile = new File(data.get(mViewPager.getCurrentItem()).getUrl());
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

            PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            waIntent.setPackage(getPackageName());
            waIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            waIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(waIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(DetailActivity.this, "App not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void onDelete(View view) {
        if(hapticFeed)
            vib.vibrate(50);
        int p = mViewPager.getCurrentItem();
        if(mSectionsPagerAdapter.getCount()>0) {
            File fdelete = new File(data.get(p).getUrl());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    mSectionsPagerAdapter.deleteImage(p);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("-->", "file not Deleted :");
                }
            }
        }
        else{
            finish();
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public ArrayList<ImageModel> data = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<ImageModel> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, data.get(position).getName(), data.get(position).getUrl());
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return data.size();
        }
        void deleteImage(int position) {
            data.remove(position);
            if(data.size()>0)
                notifyDataSetChanged();
            else
                finish();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return data.get(position).getName();
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        String name, url;
        int pos;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_IMG_TITLE = "image_title";
        private static final String ARG_IMG_URL = "image_url";

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            this.pos = args.getInt(ARG_SECTION_NUMBER);
            this.name = args.getString(ARG_IMG_TITLE);
            this.url = args.getString(ARG_IMG_URL);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String name, String url) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_IMG_TITLE, name);
            args.putString(ARG_IMG_URL, url);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onStart() {
            super.onStart();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            Glide.with(getActivity()).load(url).thumbnail(0.1f).into(imageView);

            return rootView;
        }

    }
}



