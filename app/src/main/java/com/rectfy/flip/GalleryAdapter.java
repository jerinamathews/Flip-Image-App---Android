package com.rectfy.flip;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerin on 18-03-2018.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {
    Context ctx;
    List<ImageModel> list = new ArrayList<>();

    public GalleryAdapter(Context ctx, List<ImageModel> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(ctx).load(list.get(position).getUrl())
                .thumbnail(0.5f)
                .into(holder.imgView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imgView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.galleryIV);
        }
    }
}
