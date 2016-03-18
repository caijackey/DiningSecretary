package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestRecomListDTO;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;

/**
 * 吃货荐店页面
 * 
 * @author zhaozuoming
 * 
 */
public class RestaurantFoodieListActivity extends MainFrameActivity {

	private View contextView;
	private LayoutInflater mInflater;
	private ListView lv;
	private String restId = "";
	private ListViewAdapter<RestRecomPicData> adapter;
	private List<RestRecomPicData> list = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 传入绑定参数为（BUNDLE_REST_ID）
		Bundle bundle = this.getIntent().getExtras();
		restId = bundle.getString(Settings.BUNDLE_REST_ID);
		OpenPageDataTracer.getInstance().enterPage("餐厅吃货荐店", "");
		initComent();
		list = new ArrayList<RestRecomPicData>();
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		initListview();
	}

	/**
	 * Activity life cycle
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onRestart() {

		OpenPageDataTracer.getInstance().enterPage("餐厅吃货荐店", "");

		super.onRestart();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Instantiate the component
	 */
	private void initComent() {
		// Setting title
		this.getTvTitle().setText(R.string.text_foodie);
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_foodielayout, null);
		lv = (ListView) contextView.findViewById(R.id.foodie_listview);

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// init Listview
	private void initListview() {
		if (lv != null && adapter != null) {
			lv.removeFooterView(adapter.getFooterView());
		}
		ListViewAdapter.OnAdapterListener<RestRecomPicData> adapterlisenter = new ListViewAdapter.OnAdapterListener<RestRecomPicData>() {

			@Override
			public void onRenderItem(ListViewAdapter<RestRecomPicData> adapter, ViewHolder holder, final RestRecomPicData data) {
				View res_food_list_item_foodieLayout = holder.$(R.id.res_food_list_item_foodieLayout);
				res_food_list_item_foodieLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// ----
						OpenPageDataTracer.getInstance().addEvent("选择行");
						// -----
						
						
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_REST_ID, data.uuid);
						bundle.putInt(Settings.BUNDLE_showTypeTag, 2);
						ActivityUtil.jump(RestaurantFoodieListActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
					}
				});
				holder.$tv(R.id.tv_usernameitem).setText(data.userNickName);
				// Add Item imgeview
				holder.$myIv(R.id.img).setImageByUrl(data.picUrl, true, 0, ScaleType.CENTER_CROP);
				// Set the creation time of the hired shop
				holder.$tv(R.id.tv_recommend_creatTime).setText(data.createTime);
				// set up a restaurant title bar
				holder.$tv(R.id.tv_foodieItem_explain).setText(data.title);
				// Set the display content
				if (TextUtils.isEmpty(data.detail)) {
					holder.$tv(R.id.tv_recommendItem_address).setText(R.string.text_layout_dish_no_comment);
				} else {
					holder.$tv(R.id.tv_recommendItem_address).setText(data.detail);
				}

			}

			@Override
			public void onLoadPage(final ListViewAdapter<RestRecomPicData> adapter, int startIndex, int pageSize) {
				ServiceRequest request = new ServiceRequest(ServiceRequest.API.getRestRecomList2);
				
				// -----------------
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----------------
				
				
				request.addData("restId", restId);// restaurant Id
				request.addData("topTag", false);// boolean selection
				request.addData("pageSize", pageSize);
				request.addData("startIndex", startIndex);// now page
				CommonTask.request(request, "正在加载数据...", new CommonTask.TaskListener<RestRecomListDTO>() {

					@Override
					protected void onSuccess(RestRecomListDTO dto) {
						OpenPageDataTracer.getInstance().endEvent("页面查询");

						ListViewAdapter.AdapterDto<RestRecomPicData> adapterDto = new ListViewAdapter.AdapterDto<RestRecomPicData>();
						adapterDto.setList(dto.list);
						list = dto.list;
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);

					}

					@Override
					protected void onError(int code, String message) {
						// TODO Auto-generated method stub
						super.onError(code, message);
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// doTest();
					}

					// Commend swathes shop test the JSON data
					private void doTest() {
						String json = "{\"recommData\":[{\"restId\":\"1314\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1315\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1316\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1317\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1318\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1319\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1320\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w% 3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"},{\"restId\":\"1321\",\"topTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%  3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"title\":\"上海顶级日料：位于浦东大道1227号\",\"userNikName\":\"张晓健\",\"creatTime\":\"2014.2.17\",\"detail\":\"上海顶级日料位于环境优雅，交通方便，高端大气的上海外滩...\"}]}";
						RestRecomListDTO dto = JsonUtils.fromJson(json, RestRecomListDTO.class);
						onSuccess(dto);
					}

				});

			}
		};
		adapter = new ListViewAdapter<RestRecomPicData>(R.layout.foodie_item, adapterlisenter);
		adapter.setExistPage(true);
		adapter.setmCtx(RestaurantFoodieListActivity.this);
		adapter.setListView(lv);

	}

}
