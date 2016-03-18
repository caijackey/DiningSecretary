package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.RecommendRestImageListAdapter;
import com.fg114.main.app.view.MyRecommendRestGalleryView;
import com.fg114.main.app.view.MyScaleImageView;
import com.fg114.main.app.view.MySurfaceLayout;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;

/**
 * 推荐餐厅图片相册界面
 * 
 * @author zhangyifan
 * 
 */
public class RecommendRestaurantGalleryActivity extends MainFrameActivity implements OnTouchListener{

	private static final String TAG = "RecommendRestaurantGalleryActivity";

	// 传入参数
	private int picIndex; // 图片序号
	private List<RestRecomPicData> imageList; // 图片列表

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private MyRecommendRestGalleryView imageGalleryView;
	private RecommendRestImageListAdapter adapter;
	private LinearLayout mMyTitleLayout;
	private Button mBackbtn, mTitlebtn, mOptionbtn;
	private TextView mTitleTv;
	private TextView mRecomRestDetail;

	// 存入前一个画面的position
	private int oldItemPosition;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		picIndex = bundle.getInt(Settings.BUNDLE_KEY_ID);
		imageList = (ArrayList<RestRecomPicData>) bundle.getSerializable(Settings.BUNDLE_KEY_CONTENT);

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		Settings.DISPLAY = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(Settings.DISPLAY);// 获取分辨率
		Settings.DISPLAY.heightPixels = Settings.DISPLAY.heightPixels - Settings.STATUS_BAR_HEIGHT;
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅图片大图", "");
		// ----------------------------
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		getWindowManager().getDefaultDisplay().getMetrics(Settings.DISPLAY);// 获取分辨率
		Settings.DISPLAY.heightPixels = Settings.DISPLAY.heightPixels - Settings.STATUS_BAR_HEIGHT;
		if (!(imageGalleryView.getSelectedView() instanceof MySurfaceLayout)) {
			if (imageGalleryView != null && imageGalleryView.getSelectedView() != null) {
				((MyScaleImageView) imageGalleryView.getSelectedView()).resetImageView();
			}

		}

		// 如果标题栏显示，则放到所有的view的前面
		if (mMyTitleLayout.isShown()) {
			mMyTitleLayout.bringToFront();
		}

	}

	@Override
	public void finish() {
		recycle();
		super.finish();
	}

	// 设置title中的当前页和总数
	public void setTitleState(int current, int total) {
		String s = current + "/" + total;
		mTitleTv.setText(s);
		mRecomRestDetail.setVisibility(View.VISIBLE);
		mRecomRestDetail.setText(imageList.get(current - 1).detail);
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置父类标题栏
		this.setFunctionLayoutGone();
		this.getTitleLayout().setVisibility(View.GONE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.recommend_restaurant_image_gallery, null);
		imageGalleryView = (MyRecommendRestGalleryView) contextView.findViewById(R.id.image_gallery_view);
		// 初始化自身标题栏
		mMyTitleLayout = (LinearLayout) contextView.findViewById(R.id.image_gallery_titlelayout);
		mBackbtn = (Button) contextView.findViewById(R.id.image_gallery_btnGoBack);
		mTitlebtn = (Button) contextView.findViewById(R.id.image_gallery_btnTitle);
		mOptionbtn = (Button) contextView.findViewById(R.id.image_gallery_btnOption);
		mTitleTv = (TextView) contextView.findViewById(R.id.image_gallery_tvTitle);
		mRecomRestDetail = (TextView) contextView.findViewById(R.id.recom_rest_detail);
		adapter = new RecommendRestImageListAdapter(this);
		adapter.setList(imageList);
		imageGalleryView.setAdapter(adapter);

		
		imageGalleryView.setVerticalFadingEdgeEnabled(false);// 取消竖直渐变边框
		imageGalleryView.setHorizontalFadingEdgeEnabled(false);// 取消水平渐变边框

		mBackbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮 ");
				// -----
				finish();

			}
		});

		//mBackbtn.setText(R.string.text_button_back);
		mOptionbtn.setVisibility(View.INVISIBLE);
		// imageGalleryView.seto
		imageGalleryView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				RestRecomPicData data = ((RecommendRestImageListAdapter) imageGalleryView.getAdapter()).getList().get(arg2);
				// 更新title显示浏览位置
				setTitleState(arg2 + 1, adapter.getCount());
				if (mMyTitleLayout.isShown()) {
					mMyTitleLayout.bringToFront();
				}
				oldItemPosition = arg2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		
		
		imageGalleryView.setSelection(picIndex);
		
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		LinkedList<Bitmap> list = ((RecommendRestImageListAdapter) imageGalleryView.getAdapter()).bitmapList;
		for (Bitmap b : list) {
			if (b != null) {
				b.recycle();
			}
		}
		System.gc();
	}

	float beforeLenght = 0.0f; // 两触点距离
	float afterLenght = 0.0f; // 两触点距离
	boolean isScale = false;
	float currentScale = 1.0f;// 当前图片的缩放比率
	int positionYFirst = 0;
	int positionXFirst = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		// Log.i("","touched---------------");
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:// 多点缩放
			positionXFirst = (int) event.getX();
			positionYFirst = (int) event.getY();
			beforeLenght = spacing(event);
			if (beforeLenght > 5f) {
				isScale = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isScale) {

				afterLenght = spacing(event);
				if (afterLenght < 5f)
					break;
				float gapLenght = afterLenght - beforeLenght;
				if (gapLenght == 0) {
					break;
				} else if (Math.abs(gapLenght) > 5f) {
					// FrameLayout.LayoutParams params =
					// (FrameLayout.LayoutParams) gallery.getLayoutParams();
					float scaleRate = gapLenght / 854;// 缩放比例

					// Log.i("",
					// "scaleRate："+scaleRate+" currentScale:"+currentScale);
					// Log.i("", "缩放比例：" +
					// scaleRate+" 当前图片的缩放比例："+currentScale);
					// params.height=(int)(800*(scaleRate+1));
					// params.width=(int)(480*(scaleRate+1));
					// params.height = 400;
					// params.width = 300;
					// gallery.getChildAt(0).setLayoutParams(new
					// Gallery.LayoutParams(300, 300));
					Animation myAnimation_Scale = new ScaleAnimation(currentScale, currentScale + scaleRate, currentScale, currentScale + scaleRate, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					// Animation myAnimation_Scale = new
					// ScaleAnimation(currentScale, 1+scaleRate,
					// currentScale,
					// 1+scaleRate);
					myAnimation_Scale.setDuration(100);
					myAnimation_Scale.setFillAfter(true);
					myAnimation_Scale.setFillEnabled(true);
					// gallery.getChildAt(0).startAnimation(myAnimation_Scale);

					// gallery.startAnimation(myAnimation_Scale);
					currentScale = currentScale + scaleRate;
					// gallery.getSelectedView().setLayoutParams(new
					// Gallery.LayoutParams((int)(480), (int)(800)));
					// Log.i("",
					// "===========:::"+gallery.getSelectedView().getLayoutParams().height);
					// gallery.getSelectedView().getLayoutParams().height=(int)(800*(currentScale));
					// gallery.getSelectedView().getLayoutParams().width=(int)(480*(currentScale));
					// imageGalleryView.getSelectedView().setLayoutParams(new
					// Gallery.LayoutParams((int) (480 * (currentScale)),
					// (int) (854 * (currentScale))));
					// gallery.getSelectedView().setLayoutParams(new
					// Gallery.LayoutParams((int)(320*(scaleRate+1)),
					// (int)(480*(scaleRate+1))));
					// gallery.getSelectedView().startAnimation(myAnimation_Scale);
					// isScale = false;
					beforeLenght = afterLenght;
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			isScale = false;

			int positionXSec = (int) event.getRawX();
			int positionYSec = (int) event.getRawY();
			if (Math.abs(positionYSec - positionYFirst) <= 5 && Math.abs(positionXSec - positionXFirst) <= 5) {
//				mRecomRestDetail.setVisibility(mRecomRestDetail.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
			}

			break;
		}

		return false;
	}

	/**
	 * 计算两点间的距离
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	public TextView getmMyTitleLayout() {
		return mRecomRestDetail;
	}


}
