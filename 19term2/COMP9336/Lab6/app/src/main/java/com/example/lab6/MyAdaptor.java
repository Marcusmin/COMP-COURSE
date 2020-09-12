package com.example.lab6;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyAdaptor extends RecyclerView.Adapter<MyAdaptor.MyViewHolder> {
    private List<String[]> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView vendor;
        public TextView version;
        public TextView maxRange;
        public TextView minDelay;
        public MyViewHolder(View view){
            super(view);
            this.name = view.findViewById(R.id.name);
            this.vendor = view.findViewById(R.id.vendor);
            this.version = view.findViewById(R.id.version);
            this.maxRange = view.findViewById(R.id.range);
            this.minDelay = view.findViewById(R.id.delay);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdaptor(List<String[]> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdaptor.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String[] infoList = mDataset.get(position);
        holder.name.setText(infoList[0]);
        holder.version.setText(infoList[1]);
        holder.vendor.setText(infoList[2]);
        holder.maxRange.setText(infoList[3]);
        holder.minDelay.setText(infoList[4]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}