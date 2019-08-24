package com.ttl.callforhelp.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.ttl.callforhelp.R;

public class SliderAdapterExample extends SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

    private Context context;

    public SliderAdapterExample(Context context) {
        this.context = context;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {


        switch (position) {
            case 0:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.opioid_stats)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("The overall objective of the project is to make naloxone more widely available for opioid users.");

                break;

            case 1:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.opioid_signs)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("Every day, more than 130 people in the United States die after overdosing on opioids.");
                break;

            case 2:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.reversing_overdose_naloxone)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("Naloxone is a medication used to block the effects of opioids, especially in overdose.");

                break;
            default:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.opioid_epidemic)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("Every day, more than 130 people in the United States die after overdosing on opioids.");

                break;

        }

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return 4;
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }
}