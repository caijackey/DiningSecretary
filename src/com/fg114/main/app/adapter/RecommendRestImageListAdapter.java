package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.Gallery;

import com.fg114.main.app.Settings;

import com.fg114.main.app.view.MyScaleImageView;
import com.fg114.main.app.view.MySurfaceLayout;
import com.fg114.main.service.dto.RestRecomPicData;

/**
 * 图片列表用适配器
 * 
 * @author zhangyifan
 * 
 */
public class RecommendRestImageListAdapter extends BaseAdapter {

	private static final String TAG = "ImageListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private ArrayList<RestRecomPicData> list = null;

	private Context context;

	// 相册用回收列表
	public LinkedList<Bitmap> bitmapList = new LinkedList<Bitmap>();

	public RecommendRestImageListAdapter(Context c) {
		super();
		this.context = c;
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RestRecomPicData data = (RestRecomPicData) list.get(position);
		synchronized (bitmapList) {
			if (bitmapList.size() > 3) {
				bitmapList.getLast().recycle();
				bitmapList.removeLast();
				System.gc();
			}
		}

		// 图片列表的场合
		MyScaleImageView myImageview = new MyScaleImageView(context);
		myImageview.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		myImageview.setImageByUrl(data.bigPicUrl);
		convertView = myImageview;

		return convertView;
	}

	public void setList(List<RestRecomPicData> list) {
		// list序列化
		this.list = (ArrayList<RestRecomPicData>) list;
		notifyDataSetChanged();
	}

	public ArrayList<RestRecomPicData> getList() {
		return list;
	}

	public void addList(List<RestRecomPicData> list) {
		this.list.addAll((ArrayList<RestRecomPicData>) list);
		notifyDataSetChanged();
	}
}
