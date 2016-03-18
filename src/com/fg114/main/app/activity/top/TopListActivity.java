package com.fg114.main.app.activity.top;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.DistrictChoosingActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantHotsaleActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.SelectionLinkListView;
import com.fg114.main.app.view.SelectionListView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.RfTypeListPackDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SessionManager;

/**
 * 榜单选择页面
 * @author nieyinyin
 */
public class TopListActivity extends MainFrameActivity{
	
	private static final String TAG = "TopListActivity";
	/** 榜单缓存 */
	//缓存KEY
	private static final String KEY_TOP_LIST = "key_top_list";
	//缓存时间
	private static final int TOP_LIST_CACHE_TIME = 12 * 60; // 缓存时间半天，即12*60分钟
	
	private Context mCtx = this;
	private SelectionLinkListView slv; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("榜单", "");
		// ----------------------------
		
		initComponent();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("榜单", "");
		// ----------------------------
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	void initComponent(){
		// 顶部
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getTvTitle().setText("榜单");
		
		LinearLayout contentView = new LinearLayout(mCtx);
		slv = new SelectionLinkListView(mCtx);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		contentView.addView(slv,lp);
		slv.setVisibility(View.GONE);
		
		slv.setOnSelectedListener(new OnSelectedListener() {
			
			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if(subData == null){
					return;
				}
				
				// -----
				OpenPageDataTracer.getInstance().addEvent("榜单按钮", subData.getUuid());
				// -----

				
				// 设置筛选条件
				SessionManager.getInstance().getFilter().setMainTopRestTypeId(mainData.getUuid());
				SessionManager.getInstance().getFilter().setSubTopRestTypeId(subData.getUuid());
				//去热门榜单详细页面
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_TYPEID, subData.getUuid());
				ActivityUtil.jump(mCtx, RestaurantHotsaleActivity.class, 0,bundle);
				
				// 去餐厅美食列表页
//				ActivityUtil.jump(mCtx, ResAndFoodListActivity.class, 0, null);
			}
		});
		
		
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		
		ServiceRequest request = new ServiceRequest(API.getTopRestListTypeInfo);
		
		//获得榜单类别信息，返回RfTypeListPackDTO
		//RfTypeListDTO  中  u  n:   榜单父类的uuid,name    list :子类列表
		//RfTypeDTO  中  u  n : 商区的uuid,name
		CommonTask.request(request, new CommonTask.TaskListener<RfTypeListPackDTO>() {
			protected void onSuccess(RfTypeListPackDTO dto) { 
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				
				if(dto == null){
					finish();
					return;
				}
				List<RfTypeListDTO> listData = dto.list;
				if(listData != null){
					if(!isSelectTag(listData)){
						//TAG没有选中 默认第一个选中
					listData.get(0).setSelectTag(true);
					}
					slv.setData(listData);
					
					slv.setVisibility(View.VISIBLE);
				}
				
			};
			
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
			}
			@Override
			protected void defineCacheKeyAndTime(
					CommonTask.TaskListener.CacheKeyAndTime keyAndTime) {
				CityInfo city=SessionManager.getInstance().getCityInfo(TopListActivity.this);
				keyAndTime.cacheKey = KEY_TOP_LIST+"|"+city.getId();
				keyAndTime.cacheTimeMinute = TOP_LIST_CACHE_TIME; // 缓存时间半天，即12*60分钟
			}
			
		});
		
		this.getMainLayout().addView(contentView,LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		
	}
	
	//判断数据TAG是否有选中
	private Boolean isSelectTag(List<RfTypeListDTO> list){
		for(int i=0;i<list.size();i++){
			if(list.get(i).isSelectTag()){
				return true;
			}
		}
		return false;
	}
}

















