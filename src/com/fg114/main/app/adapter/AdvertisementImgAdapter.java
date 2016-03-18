package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.data.BaseData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;

/**
 * 外卖首页广告图片列表适配器
 * 
 * @author sunquan 2014-03-31
 * 
 */
public class AdvertisementImgAdapter extends BaseAdapter {

	private static final String TAG = "AdvertisementImgAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	//最多显示的广告条目
	private int maxShowingCount = 20;
	private List<MainPageAdvData> list;
	private LayoutInflater mInflater;
	private Context context;

	//一般列表用
	public AdvertisementImgAdapter(Context c, List<MainPageAdvData> advList) {
		super();
		this.list = advList;
		//		this.list = doTest();

		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

//	private List<MainPageAdvData> doTest() {
//		List<MainPageAdvData> advList = new ArrayList<MainPageAdvData>();
//		for (int i = 0; i < 3; i++) {
//			MainPageAdvData mainPageAdvData = new MainPageAdvData();
//			mainPageAdvData.title = "广告条" + i;
//			advList.add(mainPageAdvData);
//		}
//		return advList;
//	}

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
		public MyImageView advimg; //广告内容
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
		final MainPageAdvData data = list.get(position);
		holder.advimg.setImageByUrl(data.picUrl, false, 0, ScaleType.FIT_XY);
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				try {
					// -----
					OpenPageDataTracer.getInstance().addEvent("广告位按钮", data.uuid);
					// -----
					//广告类别  1:广告链接  2：本地连接  3:普通链接
					//普通链接跳转到webview页面, 本地链接使用url处理器
					if (data.typeTag == 1) {
						// 广告链接，使用内嵌的WebView打开
						Bundle bd = new Bundle();
						bd.putString(Settings.BUNDLE_KEY_WEB_URL, data.advUrl);
						bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, data.title);
						ActivityUtil.jump(context, SimpleWebViewActivity.class, 0, bd);

					} else if (data.typeTag == 2) {
						// 本地链接，跳转本地界面
						URLExecutor.execute(data.advUrl, context, 0);
					} else if (data.typeTag == 3) {
						// 普通链接，使用系统浏览器打开
						ActivityUtil.jumbToWeb((Activity) context, data.advUrl);
					} else if (data.typeTag ==4) { 
						// -----
						OpenPageDataTracer.getInstance().addEvent("软件更新按钮");
						// -----
						Bundle bundle=new Bundle();
						bundle.putString(Settings.BUNDLE_KEY_CONTENT, data.appDownloadUrl);
						bundle.putString(Settings.BUNDLE_UPDATE_APP_NAME, data.appName);
						ActivityUtil.jump((Activity) context, AutoUpdateActivity.class, 0,bundle,false, R.anim.frame_anim_from_bottom, 0);
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		convertView.setTag(holder);
		return convertView;
	}
}
