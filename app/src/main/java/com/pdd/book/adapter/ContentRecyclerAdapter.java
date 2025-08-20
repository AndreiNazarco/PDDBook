package com.pdd.book.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pdd.book.R;

import java.util.List;

public class ContentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<ContentInfo> contentInfoList;

	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView tvContentName, tvContentSubName, tvContentCode;

		public MyViewHolder(View view) {
			super(view);
			tvContentName = (TextView) view.findViewById(R.id.itvContentName);
			tvContentSubName = (TextView) view.findViewById(R.id.itvContentSubName);
			tvContentCode = (TextView) view.findViewById(R.id.itvContentCode);
		}
	}

	public ContentRecyclerAdapter(List<ContentInfo> pContentInfoList) { this.contentInfoList = pContentInfoList; }

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		SharedPreferences myPrefs = parent.getContext().getSharedPreferences(parent.getContext().getPackageName() + "_preferences", MODE_PRIVATE);
		boolean day_night  = myPrefs.getBoolean("day_night", true); // true - day, false - night
		View itemView;

		if (day_night == true)
		{
			itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_card_view_day, parent, false);
		}
		else
		{
			itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_card_view_night, parent, false);
		}

		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ContentInfo ciItem = contentInfoList.get(position);

		((MyViewHolder) holder).tvContentName.setText(ciItem.getContentName());
		((MyViewHolder) holder).tvContentSubName.setText(ciItem.getContentSubName());
		((MyViewHolder) holder).tvContentCode.setText(ciItem.getContentCode());
	}

	@Override
	public int getItemCount() {
		return contentInfoList.size();
	}
}
