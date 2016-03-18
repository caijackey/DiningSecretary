package com.fg114.main.app.activity.takeaway;

import com.fg114.main.R;
import com.fg114.main.R.layout;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.activity.usercenter.APPSettingActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutIndexPageData;
import com.fg114.main.service.dto.TakeoutMenuData;
import com.fg114.main.service.dto.TakeoutMenuData2;
import com.fg114.main.service.dto.TakeoutMenuGradeData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.google.xiaomishujson.Gson;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 外卖菜品详情 foodId
 * @author 孙权 2014-03-31
 *
 */
public class NewTakeAwayFoodDetailActivity extends MainFrameActivity {
	// 传入参数
	private String foodId;

	private String menuSelPack;
	// 界面组件
	private LayoutInflater mInflater;
	private MyImageView ivFoodPic;
	private View contextView;
	private TextView tvFoodName;

	private TextView tvFoodIsSpecial;
	private TextView tvFoodIsAcridity;
	private TextView tvFoodPrice;

	private Button likebutton;
	private RatingBar rbFoodScroe;
	private LinearLayout takeaway_newfooddetail_layout;
	//需要后续获得id
//	private String restaurantId="";
	// 数据
	//TakeoutMenuData currentFood = null;
	TakeoutMenuData2 currentFood;
	private TextView tvDetail;
	private boolean isclick=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖菜品详情", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		foodId = bundle.getString(Settings.BUNDLE_FOOD_ID);
		menuSelPack= bundle.getString(Settings.BUNDLE_menuSelPack);
		this.initComponent();
		this.executeTask();

	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖菜品详情", "");
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
		this.getTvTitle().setText("美味详情");
		this.getBtnGoBack().setText("返回");
	    this.getBtnOption().setVisibility(View.INVISIBLE);
	    
	    likebutton = new Button(this);
		likebutton.setBackgroundResource(R.drawable.newtakeawayfood_save);
		//likebutton.setWidth(UnitUtil.dip2px(25));
		likebutton.setWidth(88);
		likebutton.setHeight(88);
		//likebutton.setHeight(UnitUtil.dip2px(25));
		//+ UnitUtil.dip2px(20)
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(likebutton.getLeft() - UnitUtil.dip2px(20), likebutton.getTop(), likebutton.getRight(), likebutton.getBottom());
		likebutton.setLayoutParams(lp);
		this.getTitleLayout().addView(likebutton);
		
//		this.getBtnOption().setBackgroundResource(R.drawable.newtakeawayfood_save);
//		this.getBtnOption().setVisibility(View.VISIBLE);
//  	Drawable drawable = getResources().getDrawable(R.drawable.takeawaylike_icon);
//		this.getBtnOption().setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//		this.getBtnOption().setText("");
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.newtakeaway_food_detail, null);
		ivFoodPic = (MyImageView) contextView.findViewById(R.id.takeaway_newfooddetail_ivPic);
		tvFoodName = (TextView)contextView.findViewById(R.id.takeaway_newfooddetail_foodName);
		tvFoodPrice = (TextView)contextView.findViewById(R.id.takeaway_newfooddetail_foodPrice);
		takeaway_newfooddetail_layout=(LinearLayout) contextView.findViewById(R.id.takeaway_newfooddetail_layout);
		tvDetail = (TextView)contextView.findViewById(R.id.takeaway_newfooddetail);
		rbFoodScroe = (RatingBar)contextView.findViewById(R.id.takeaway_newfooddetail_foodScroe);
		
		this.getBtnGoBack().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				backActivity();
			}
		});
		// 点击喜欢按钮
		likebutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				isclick=true;
				if (!currentFood.favTag) {
					// ----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					NewTakeAwayFoodDetailActivity.this.getBtnOption().setSelected(false);
					ServiceRequest request = new ServiceRequest(API.addTakeoutMenuToFav);
					request.addData("uuid", foodId);// 餐馆ID

					CommonTask.request(request, "收藏中...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							currentFood.favTag=true;
							likebutton.setBackgroundResource(R.drawable.takeawayliked_icon);
							// -----
							NewTakeAwayFoodDetailActivity.this.getBtnOption().setSelected(true);
							DialogUtil.showToast(NewTakeAwayFoodDetailActivity.this, "收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

//						private void doTest_confirm() {
//							String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
//							SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
//							onSuccess(data);

//						}
					});

				} else {
					NewTakeAwayFoodDetailActivity.this.getBtnOption().setSelected(true);
					ServiceRequest request = new ServiceRequest(API.delTakeoutMenuFromFav);
					request.addData("uuid", foodId);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "取消收藏...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							currentFood.favTag=false;
							likebutton.setBackgroundResource(R.drawable.takeawaylike_icon);
							// -----
							NewTakeAwayFoodDetailActivity.this.getBtnOption().setSelected(false);
							DialogUtil.showToast(NewTakeAwayFoodDetailActivity.this, "取消收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

//						private void doTest_cancel() {
//							String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"取消收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
//							SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
//							onSuccess(data);

//						}
					});

				}

			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

	}

	/**
	 * 获取外卖菜品详情
	 * **/
	private void executeTask() {
		ServiceRequest request = new ServiceRequest(API.getTakeoutMenuInfo2);
		request.addData("uuid", foodId);
		// ----------------------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// ----------------------------
		CommonTask.request(request, new CommonTask.TaskListener<TakeoutMenuData2>() {

			@Override
			protected void onSuccess(TakeoutMenuData2 dto) {
				// ----------------------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------------------
				currentFood = dto;
				setView(currentFood);

			}

			@Override
			protected void onError(int code, String message) {
				// ----------------------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------------------
				super.onError(code, message);
				//finish();
//				test();
				
			}
			// -----------------测试数据---------------------------
						private void test() {
							TakeoutMenuData2 takeoutMenuListPackDTO = new TakeoutMenuData2();
							String json = "{\"uuid\":\"1111\",\"favTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"name\":\"最高级霜降雪花和牛四喜锅\",\"pinyin\":\"sunquan\",\"pinyinCap\":\"s\",\"price\":\"299\",\"overallNum\":\"5.0\",\"detail\":\"纳是可泪如按键精灵按键大啊看见大啊看到就爱看按键打开啊大家爱卡adjacent垃圾点卡啊看得见啊大家爱卡就破案地魄啊看得见阿克江啊大家爱卡啊大家爱卡卡德加爱卡啊看到就爱看阿里假大空\",\"propertyTypeList\":[{\"uuid\":\"a111\",\"name\":\"a111\",\"list\":[{\"uuid\":\"a1111\",\"name\":\"a1111\",\"price\":\"1111\"}]}]}";
							takeoutMenuListPackDTO = new Gson().fromJson(json, TakeoutMenuData2.class);
							onSuccess(takeoutMenuListPackDTO);
						}
			//					@Override
			//					protected void onCancel() {
			//						finish();
			//					}

		});
		
//		currentFood =test();
//		setView(currentFood);
	}
	
	
	private void backActivity(){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		if (!CheckUtil.isEmpty(menuSelPack)) {
				bundle.putString(Settings.BUNDLE_menuSelPack, menuSelPack);
		}
		bundle.putBoolean("isclick", isclick);
		intent.putExtras(bundle);
		NewTakeAwayFoodDetailActivity.this.setResult(205, intent);
		NewTakeAwayFoodDetailActivity.this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	/*
	 * 绑定数据
	 */
	private void setView(TakeoutMenuData2 currentFood) {
		// 如果菜品对象对空，则直接返回
		if (currentFood == null) {
			return;
		}
		if(currentFood.favTag){
			likebutton.setBackgroundResource(R.drawable.takeawayliked_icon);
		}else{
			likebutton.setBackgroundResource(R.drawable.takeawaylike_icon);
		}
		if(currentFood.bigPicWidth==0){
			currentFood.bigPicWidth=1;
		}
		if(currentFood.bigPicHeight==0){
			currentFood.bigPicHeight=1;
		}
		if (CheckUtil.isEmpty(currentFood.bigPicUrl)) {
			this.ivFoodPic.setVisibility(View.GONE);
		} else {
			this.ivFoodPic.setVisibility(View.VISIBLE);
			int screenWidth = UnitUtil.getScreenWidthPixels();
			int itemPicWidth = (screenWidth - UnitUtil.dip2px(20));
			double scale = 1.0000 * itemPicWidth / currentFood.bigPicWidth;
			int itemPicHeight = (int) (currentFood.bigPicWidth * scale);
			this.ivFoodPic.setLayoutParams(new LinearLayout.LayoutParams(itemPicWidth, itemPicHeight));
			this.ivFoodPic.setImageByUrl(currentFood.bigPicUrl, false, 0, ScaleType.FIT_XY);
		}
		this.tvFoodName.setText(currentFood.name);
		this.tvFoodPrice.setText("￥" + currentFood.price);
		
		this.rbFoodScroe.setRating((float) currentFood.overallNum);
		//---
		if (CheckUtil.isEmpty(currentFood.detail)) {
			takeaway_newfooddetail_layout.setVisibility(View.GONE);
			this.tvDetail.setVisibility(View.GONE);
		} else {
			takeaway_newfooddetail_layout.setVisibility(View.VISIBLE);
			this.tvDetail.setVisibility(View.VISIBLE);
			this.tvDetail.setText(currentFood.detail);
		}

	}
	
}

