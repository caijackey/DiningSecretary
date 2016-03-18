package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.DragLoadingView;
import com.fg114.main.service.dto.TopRestListDTO;
import com.fg114.main.service.dto.TopRestListData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
/**
 * 热门榜单界面
 * @author zhaozuoming
 *
 */
public class RestaurantHotsaleActivity extends MainFrameActivity {
	private View contextView;
	private ListView lv;
	private LayoutInflater mInflater;
	private ListViewAdapter<TopRestListData> adapter;
	private String RestTypeId = "";	
	private String leftGoBackBtn = ""; // 返回按钮内容
	private List<TopRestListData> list;
	public static float ScreenW, ScreenH;
	
	private int screenWidth;
	private int itemPicWidth = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 //传入参数Id
		//==
		OpenPageDataTracer.getInstance().enterPage("榜单餐厅列表", "");
		//==
		screenWidth = UnitUtil.getScreenWidthPixels();
		itemPicWidth = (screenWidth - UnitUtil.dip2px(20));
		
		Bundle bundleId = this.getIntent().getExtras();
		if (bundleId !=null && bundleId.containsKey(Settings.BUNDLE_KEY_LEFT_BUTTON)) {
			leftGoBackBtn = bundleId.getString(Settings.BUNDLE_KEY_LEFT_BUTTON);	
		}
		if (TextUtils.isEmpty(leftGoBackBtn)) {
			leftGoBackBtn = getString(R.string.text_button_back);
		}
		if (bundleId != null && bundleId.containsKey(Settings.BUNDLE_REST_TYPEID)) {
			RestTypeId = bundleId.getString(Settings.BUNDLE_REST_TYPEID);
		}
		list =  new ArrayList<TopRestListData>();
		initComment();
		//判断网络是否连接
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0,bundle);			
		}
		
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		//==
		OpenPageDataTracer.getInstance().enterPage("榜单餐厅列表", "");
		//==
		// ----------------------------
	}
	
	
	@Override
	public void finish() {
		super.finish();
	}
	
	//初始化组件
	private void initComment() {
		//获取一下屏幕分辨率
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ScreenH = metrics.heightPixels;
		ScreenW = metrics.widthPixels;
		this.getTvTitle().setText(R.string.text_hotsale);
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.hotsale_layout_list, null);
		lv = (ListView) contextView.findViewById(R.id.hotsale_listview);
		lv.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//不滚动时的状态
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");						
				}					
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {									
		  }
		});		
		
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);					
		initHotsaleListview();
	}
	 //初始化热门榜单ListView，加载数据的方法
	    private void initHotsaleListview() {
	    	 if (lv != null && adapter !=null) {
	    		 lv.removeFooterView(adapter.getFooterView());
			}
	    	 ListViewAdapter.OnAdapterListener<TopRestListData> adapterListener = new ListViewAdapter.OnAdapterListener<TopRestListData>() {
				@Override
				public void onRenderItem(ListViewAdapter<TopRestListData> adapter,
						ViewHolder holder, final TopRestListData data) {	
					//餐馆名					
					holder.$tv(R.id.hotsale_rest_name).setText(data.restName);					
					//餐厅地址及其他					
					holder.$tv(R.id.text).setText(data.restAddress);					
					//餐厅图片显示					
					double scale = 1.0000 * itemPicWidth / 360;
					int itemPicHeight = (int) (240 * scale);
					holder.$myIv(R.id.hotsale_picture).setLayoutParams(new RelativeLayout.LayoutParams(itemPicWidth, itemPicHeight));
					holder.$myIv(R.id.hotsale_picture).setImageByUrl(data.restPicUrl, true, 0, ScaleType.FIT_XY);					
					//设置人均价
					holder.$tv(R.id.holtsale_avdprice).setText(data.avgPrice);
					//餐厅介绍														
				    holder.$tv(R.id.tv_restaurant_comment).setText(data.detail);
				  //在这里跳转，不用listView Item 跳转
					View hotsale_item_layout=holder.$(R.id.hotsale_item_layout);
					hotsale_item_layout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(v, 1000);
							// ----
							OpenPageDataTracer.getInstance().addEvent("选择行",data.restId+"-"+RestTypeId);
							// -----
							
							
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_ID, data.restId);
							bundle.putString(Settings.BUNDLE_REST_TYPE_ID,RestTypeId);	
							bundle.putInt(Settings.BUNDLE_showTypeTag,3);	
				            ActivityUtil.jump(RestaurantHotsaleActivity.this, RestaurantDetailMainActivity.class, 0,bundle);
						}
					});
				}
				@Override
				public void onLoadPage(final ListViewAdapter<TopRestListData> adapter,final int startIndex, int pageSize) {
						
					ServiceRequest request=new ServiceRequest(ServiceRequest.API.getTopRestList);
					request.addData("typeId", RestTypeId);
					request.addData("pageSize", pageSize);
					request.addData("startIndex", startIndex);
					//--
					OpenPageDataTracer.getInstance().endEvent("页面查询");
					//封装Commtask 请求
					CommonTask.request(request, "数据正在加载...",new CommonTask.TaskListener<TopRestListDTO>() {
						@Override
						protected void onSuccess(TopRestListDTO dto) {
							OpenPageDataTracer.getInstance().endEvent("页面查询");
							RestaurantHotsaleActivity.this.getTvTitle().setText(dto.typeName);
							ListViewAdapter.AdapterDto<TopRestListData> adapterDto = new ListViewAdapter.AdapterDto<TopRestListData>();
							adapterDto.setList(dto.list);
							list = dto.list;
							adapterDto.setPageInfo(dto.pgInfo);
							adapter.onTaskSucceed(adapterDto);						
						}

						@Override
						protected void onError(int code, String message) {
							// TODO Auto-generated method stub
							//--
							OpenPageDataTracer.getInstance().endEvent("页面查询");
							//--
							super.onError(code, message);
						}						
						
					});									
				}};
				//为item设置适配
				adapter = new ListViewAdapter<TopRestListData>(R.layout.hotsale_list_item,adapterListener);
				adapter.setExistPage(true);
				adapter.setmCtx(RestaurantHotsaleActivity.this);
				adapter.setListView(lv);
		
	     }
}
