package com.rectfy.flip;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jerin on 18-03-2018.
 */

public class ImageModel implements Parcelable {
    private String name,url;

    public ImageModel() {
    }

    public ImageModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    protected ImageModel(Parcel in) {
        name = in.readString();
        url = in.readString();
    }
    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
    }
}
