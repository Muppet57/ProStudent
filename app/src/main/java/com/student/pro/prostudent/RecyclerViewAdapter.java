package com.student.pro.prostudent;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by jonnh on 3/14/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Disciplines>ucs = new ArrayList<>();

    private Context mContext;

    public RecyclerViewAdapter(ArrayList<Disciplines> ucs,Context mContext) {
        this.ucs = ucs;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.class_layout,parent,false);
        ViewHolder holder= new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.name.setText(ucs.get(position).getName().toString());
        holder.tag.setText(ucs.get(position).getTag().toString());
        holder.year.setText(ucs.get(position).getYear().toString());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Click on "+ucs.get(position).getName().toString());
            }
        });
        
    }

    @Override
    public int getItemCount() {
        return ucs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,tag,year;
        ConstraintLayout parentLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            year=itemView.findViewById(R.id.class_year);
            name=itemView.findViewById(R.id.class_name);
            tag=itemView.findViewById(R.id.class_tag);
            parentLayout=itemView.findViewById(R.id.class_view);
        }
    }
}
