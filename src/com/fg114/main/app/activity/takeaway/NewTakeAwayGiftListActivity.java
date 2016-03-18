package com.fg114.main.app.activity.takeaway;

import com.fg114.main.R;
import com.fg114.main.R.layout;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.activity.usercenter.APPSettingActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.adapter.NewTakeAwaySearchRestListAdapter;
import com.fg114.main.app.adapter.NewTakeawayGiftAdapter;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutListData2;
import com.fg114.main.service.dto.TakeoutMenuData;
import com.fg114.main.service.dto.TakeoutMenuData2;
import com.fg114.main.service.dto.TakeoutMenuGradeData;
import com.fg114.main.service.dto.TakeoutMenuList2DTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 *赠品列表 参数 uuid typeid
 * @author 孙权 2014-04-08
 *
 */
public class NewTakeAwayGiftListActivity extends MainFrameActivity {
	

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView giftlv;
	//需要后续获得id
	private String restaurantId="";
	// 数据
	TakeoutMenuList2DTO currentFood = null;
    private String takeoutId;
    private String typeId;
    private String dataIdentifer;
    private NewTakeawayGiftAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖赠品选择", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		takeoutId=bundle.getString(Settings.UUID);
		typeId=bundle.getString(Settings.BUNDLE_typeId);
		dataIdentifer=bundle.getString(Settings.BUNDLE_KEY_ID);
		
		this.initComponent();
		this.executeTask();

	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖赠品选择", "");
		// ----------------------------
	}

	@Override
	public void finish() {
		super.finish();
	}

	/**
	 * 初始化
	 * */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("选择赠品");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.VISIBLE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.new_takeaway_gift_layout, null);
		giftlv=(ListView) contextView.findViewById(R.id.newtakeaway_gift_listview);
		adapter=new NewTakeawayGiftAdapter(NewTakeAwayGiftListActivity.this); 
		
		giftlv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {				
				// ----------------------------
				OpenPageDataTracer.getInstance().addEvent("选择行");
				// ----------------------------
				TakeoutMenuData2 data =currentFood.list.get(arg2);

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, data.uuid);
				bundle.putString(Settings.BUNDLE_FOOD_NAME, data.name);
				bundle.putString(Settings.BUNDLE_KEY_ID, dataIdentifer);
				
				intent.putExtras(bundle);
				NewTakeAwayGiftListActivity.this.setResult(980, intent);
				NewTakeAwayGiftListActivity.this.finish();
			}
		});
		giftlv.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----

				}
			}
		
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

	}

	/**
	 * 获取外卖赠品
	 * **/
	private void executeTask() {
		ServiceRequest request = new ServiceRequest(API.getTakeoutGiftMenuList);
		request.addData("takeoutId", takeoutId);
		request.addData("typeId", typeId);
		// ----------------------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// ----------------------------
		CommonTask.request(request, new CommonTask.TaskListener<TakeoutMenuList2DTO>() {

			@Override
			protected void onSuccess(TakeoutMenuList2DTO dto) {
				// ----------------------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				
				// ----------------------------
				currentFood = dto;   
				adapter.setList(currentFood.list);
				giftlv.setAdapter(adapter);
			}

			@Override
			protected void onError(int code, String message) {
				// ----------------------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------------------
				super.onError(code, message);
				finish();
			}

			//					@Override
			//					protected void onCancel() {
			//						finish();
			//					}

		});
	}
}

