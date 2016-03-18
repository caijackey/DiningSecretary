package com.fg114.main.app.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.app.Settings;

import com.fg114.main.app.activity.resandfood.RecommendRestaurantGalleryActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommonPicData;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 免担保图片列表适配器
 * 
 * 
 * 
 */
public class MdbRestDetailPicAdapter extends BaseAdapter {

	private static final String TAG = "AdvertisementImgAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	// 最多显示的广告条目
	private int maxShowingCount = 20;
	private List<CommonPicData> list;
	private LayoutInflater mInflater;
	private Context context;

	// 一般列表用
	public MdbRestDetailPicAdapter(Context c, List<CommonPicData> advList) {
		super();
		this.list = advList;
		// this.list = doTest();

		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	// private List<MainPageAdvData> doTest() {
	// List<MainPageAdvData> advList = new ArrayList<MainPageAdvData>();
	// for (int i = 0; i < 3; i++) {
	// MainPageAdvData mainPageAdvData = new MainPageAdvData();
	// mainPageAdvData.title = "广告条" + i;
	// advList.add(mainPageAdvData);
	// }
	// return advList;
	// }

	@Override
	public int getCount() {
		if (list == null) {
			return 0;
		}
		if (list.size() > maxShowingCount) {
			return maxShowingCount;
		}
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public MyImageView advimg; // 广告内容
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_adv_img, null);
			holder.advimg = (MyImageView) convertView.findViewById(R.id.advertisement_image_view);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final CommonPicData data = list.get(position);
		holder.advimg.setImageByUrl(data.picUrl, false, 0, ScaleType.FIT_XY);
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_ID, position);
				bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, (Serializable) getList());
				ActivityUtil.jump(context, RecommendRestaurantGalleryActivity.class, 0, bundle);
			}
		});

		convertView.setTag(holder);
		return convertView;
	}

	private List<RestRecomPicData> getList() {
		List<RestRecomPicData> picList = new ArrayList<RestRecomPicData>();
		for (int i = 0; i < list.size(); i++) {
			RestRecomPicData recomPicData = new RestRecomPicData();
			recomPicData.bigPicUrl = list.get(i).bigPicUrl;
			recomPicData.picUrl = list.get(i).picUrl;
			recomPicData.picWidth = list.get(i).picWidth;
			recomPicData.picHeight = list.get(i).picHeight;
			recomPicData.detail = list.get(i).detail;

			picList.add(recomPicData);

		}
		return picList;
	}
}
