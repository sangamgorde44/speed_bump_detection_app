package com.example.speedbumpdetectionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class RecyclerBumpAdapter extends RecyclerView.Adapter<RecyclerBumpAdapter.ViewHolder> {
    Context context;
    ArrayList<BumpListModel> arrayBumpList;
    RecyclerBumpAdapter(Context context, ArrayList<BumpListModel> arrayList){
        this.context = context;
        this.arrayBumpList = arrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.speed_bump_row,parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.lat.setText(arrayBumpList.get(position).latitude);
        holder.longi.setText(arrayBumpList.get(position).longitude);
    }

    @Override
    public int getItemCount() {
        return arrayBumpList.size()-1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView lat, longi;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            lat = itemView.findViewById(R.id.textViewLatitude);
            longi = itemView.findViewById(R.id.textViewLongitude);
        }
    }
}

//---------------------------------------------- end -----------------------------------------------