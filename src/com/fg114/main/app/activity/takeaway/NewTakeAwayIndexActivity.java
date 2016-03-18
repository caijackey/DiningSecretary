package com.fg114.main.app.activity.takeaway;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.HomeActivity;
import com.fg114.main.app.activity.HotDistrictActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.AdvertisementAdapter;
import com.fg114.main.app.adapter.AdvertisementImgAdapter;
import com.fg114.main.app.adapter.TakeAwayIndexListAdapter;
import com.fg114.main.app.adapter.TakeAwaySearchRestListAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.TakeoutIndexPageData;
import com.fg114.main.service.dto.TakeoutListDTO;
import com.fg114.main.service.dto.TakeoutListData;
import com.fg114.main.service.dto.TakeoutListData2;
import com.fg114.main.service.dto.TakeoutMenuListPack2DTO;
import com.fg114.main.service.dto.TakeoutMenuListPackDTO;
import com.fg114.main.service.dto.TakeoutTypeData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.CommonTask.TaskListener.CacheKeyAndTime;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.google.xiaomishujson.Gson;

/**
 * 
 * 外卖首页
 * 
 * @author sunquan,2014-03-31
 * 
 */
public class NewTakeAwayIndexActivity extends MainFrameActivity {
	private static final String TAG = "NewTakeAwayIndexActivity";
	// 缓存数据
	private TakeAwayFilter filter; // 查询条件
	private View contextView;
	private ListView listView;
	// 广告位
	public List<MainPageAdvData> advList;
	// 类别列表
	public List<TakeoutTypeData> typeList;
	// 广告组件
	private RelativeLayout new_take_away_adv_layout;
	private View new_take_away_adv_line;
	private ViewFlow advViewFlowimg;
	private CircleFlowIndicator advimgCircleIndicator;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。
	private TakeAwayIndexListAdapter lvadapter;
	private AdvertisementImgAdapter advadapter;
	private boolean needHideBackButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖首页", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			needHideBackButton = bundle.getBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, false);
		}

		// 初始化界面
		initComponent();
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖首页", "");
		// ----------------------------
		// executeGetIndexRestListTask();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// //防止没有数据的时候
		// if(listView!=null&&listView.getChildCount()==0){
		// executeGetIndexRestListTask();
		// }
		// 显示广告
		tryDisplayAdvertisement();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	// 广告是否是被主动关闭过
	private boolean hasAdvertisementBeenClosed() {
		// 通过判断是否是同一天来控制广告位的显隐
		long timeStamp = SessionManager.getInstance().getAdvCloseTimeStamp(NewTakeAwayIndexActivity.this);
		if (!CalendarUtil.isToday(timeStamp)) {
			return false;
		} else {
			return true;
		}

	}

	private synchronized void tryDisplayAdvertisement() {
		// List<MainPageAdvData> advList =
		// SessionManager.getInstance().getMainPageAdvDataList();
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0) {
			new_take_away_adv_layout.setVisibility(View.VISIBLE);
			new_take_away_adv_line.setVisibility(View.VISIBLE);
			if (advList.size() == 1) {
				advimgCircleIndicator.setVisibility(View.GONE);
			} else {
				advimgCircleIndicator.setVisibility(View.VISIBLE);
			}

			// 确保只有一个运行的线程
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			// ---------------------
			advViewFlowimg.setAdapter(new AdvertisementImgAdapter(this, advList));
			// 广告自动滚动的线程，４秒
			playAdvertisement = new Thread(new Runnable() {
				int i = 0;

				@Override
				public void run() {
					try {
						int count = advViewFlowimg.getAdapter().getCount();
						while (count > 1) {
							Thread.sleep(4000);
							if (playCoolingTime > System.currentTimeMillis()) {
								continue;
							}
							i = advViewFlowimg.getSelectedItemPosition();
							i = (i + 1) % count;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									advViewFlowimg.setSelection(i);

								}
							});
							count = advViewFlowimg.getAdapter().getCount();
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			});
			playAdvertisement.start();
			// 广告手动滑动
			advViewFlowimg.setOnTouchListener(advTouchListener);
			// --
		} else {
			// 没有广告时，撤消线程，清除数据
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}

			AdvertisementImgAdapter adapter = new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>());
			advViewFlowimg.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	// 控制自动播放的手势
	OnTouchListener advTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				playCoolingTime = System.currentTimeMillis() + 2000; // 马上冷却
			} else {
				playCoolingTime = System.currentTimeMillis() + 200000; // 几乎不冷却　
			}
			return false;
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	// 页面切换动画
	@Override
	public void finish() {
		super.finish();
		if (mUpdateCityThread != null) {
			mUpdateCityThread.interrupt();
		}

		Settings.CURRENT_PAGE = "";
		// 页面跳转过程平滑的滑动
		// overridePendingTransition(R.anim.left_slide_in,
		// R.anim.left_slide_out);
		ActivityUtil.overridePendingTransition(this, R.anim.left_slide_in, R.anim.left_slide_out);
	}

	/**
	 * 初始化界面
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("叫外卖");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		if (needHideBackButton) {
			this.getBtnGoBack().setVisibility(View.INVISIBLE);
		}
		this.setLocationLayoutVisibility(View.GONE);
		// 内容部分
		contextView = View.inflate(this, R.layout.takeawayindex_activity, null);
		// ------------外面首页图片广告-----------------------------------------------------------------------
		advViewFlowimg = (ViewFlow) contextView.findViewById(R.id.viewflow_img);
		new_take_away_adv_layout = (RelativeLayout) contextView.findViewById(R.id.new_take_away_adv_layout);
		new_take_away_adv_line = (View) contextView.findViewById(R.id.new_take_away_adv_line);
		advimgCircleIndicator = (CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator_img);
		advViewFlowimg.setFlowIndicator(advimgCircleIndicator);
		listView = (ListView) contextView.findViewById(R.id.list_view_takeawayindex);
		listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<TakeoutTypeData> list = ((TakeAwayIndexListAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					TakeoutTypeData data = list.get(index);
					if (data != null) {
						if (("" + Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
							// 控制项不处理
							return;
						}
						// -----
						OpenPageDataTracer.getInstance().addEvent("选择行", data.uuid);
						// 去外卖列表

						Bundle bundle = new Bundle();
						bundle.putString(Settings.UUID, data.uuid);
						ActivityUtil.jump(NewTakeAwayIndexActivity.this, NewTakeAwaySearchRestListActivity.class, 0, bundle);
					}
				}
			}
		});

		executeGetIndexRestListTask();
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

	}

	/**
	 * 获得外卖首页外卖类型列表
	 */
	private void executeGetIndexRestListTask() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		ServiceRequest request = new ServiceRequest(API.getTakeoutIndexPageInfo);
		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<TakeoutIndexPageData>() {

			@Override
			protected void onSuccess(TakeoutIndexPageData dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// 缓存数据 防止因为网络 导致空白页面
				SessionManager.getInstance().setTakeoutIndexPageData(dto);

				if (dto.advList != null && dto.advList.size() > 0) {
					new_take_away_adv_layout.setVisibility(View.VISIBLE);
					new_take_away_adv_line.setVisibility(View.VISIBLE);
					advList = dto.advList;
				}
				typeList = dto.typeList;

				// -----------ListView 外卖分类
				advadapter = new AdvertisementImgAdapter(NewTakeAwayIndexActivity.this, advList);
				advViewFlowimg.setAdapter(advadapter);
				// 有重试逻辑的adapter
				lvadapter = new TakeAwayIndexListAdapter(NewTakeAwayIndexActivity.this, typeList);
				lvadapter.setList(typeList);
				listView.setAdapter(lvadapter);
				
				
				tryDisplayAdvertisement();

			}

			@Override
			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// super.onError(code, message);
				// 服务器数据获取失败 查看是否有缓存数据 有 加载缓存数据 没有 重新请求

				TakeoutIndexPageData dto = SessionManager.getInstance().getTakeoutIndexPageData();
				if (dto == null) {
					// 没有 重新请求
					DialogUtil.showAlert(NewTakeAwayIndexActivity.this, false, true, "提示", "获取数据失败！", "重试", "", new DialogInterface.OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							// 如果是返回 那么弹出dialog询问是否要退出
							if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
								finish();
							}
							return false;
						}

					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							executeGetIndexRestListTask();
						}
					});
				} else {
					onSuccess(dto);
				}
				// test();

			}

			// -----------------测试数据---------------------------
			private void test() {
				TakeoutIndexPageData takeoutMenuListPackDTO = new TakeoutIndexPageData();
				String json = "{\"advList\":[{\"uuid\":\"1111\",\"typeTag\":\"1\",\"title\":\"广告1\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"},"
						+ "{\"uuid\":\"1121\",\"typeTag\":\"1\",\"title\":\"广告2\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://upload2.9517.cn/albumpicimages/20140409/c706bcb9-9e98-4ab9-af93-8fe53d7c509a.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"},"
						+ "{\"uuid\":\"1131\",\"typeTag\":\"1\",\"title\":\"广告3\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"},"
						+ "{\"uuid\":\"1141\",\"typeTag\":\"1\",\"title\":\"广告4\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"},"
						+ "{\"uuid\":\"1151\",\"typeTag\":\"1\",\"title\":\"广告5\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\","
						+ "\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"}],"
						+ "\"typeList\":[{\"uuid\":\"a111\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a222\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a333\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a555\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a666\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a777\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},"
						+ "{\"uuid\":\"a444\",\"name\":\"中餐厅\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"}]}";
				takeoutMenuListPackDTO = JsonUtils.fromJson(json, TakeoutIndexPageData.class);
				onSuccess(takeoutMenuListPackDTO);
			}

		});

	}

}
