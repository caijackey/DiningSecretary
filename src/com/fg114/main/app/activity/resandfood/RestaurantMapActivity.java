//package com.fg114.main.app.activity.resandfood;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Point;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.fg114.main.R;
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.ShowErrorActivity;
//import com.fg114.main.util.ActivityUtil;
//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapActivity;
//import com.google.android.maps.MapView;
//import com.google.android.maps.Overlay;
//
///**
// * 餐厅地图界面
// * @author zhangyifan
// *
// */
//public class RestaurantMapActivity extends MapActivity {
//	
//	private static final String TAG = "RestaurantMapActivity";
//	private static final boolean DEBUG = Settings.DEBUG;
//	
//	//传入参数
//	double longitude = 0;
//	double latitude = 0;
//
//	//界面组件
//	private Button btnLeft;
//	private TextView tvTitle;
//	private Button btnRight;
//	private MapView map;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		//设置返回页
//		this.setResult(Settings.RESTAURANT_DETAIL_ACTIVITY);
//		
//		//获得传入参数
//		Bundle bundle = this.getIntent().getExtras();
//		double[] local = bundle.getDoubleArray(Settings.BUNDLE_KEY_ID);
//		longitude = local[0];
//		latitude = local[1];
//		
//		if (DEBUG) Log.d(TAG, "latitude:" + latitude + "  longitude:" + longitude);
//		
//		//检查网络是否连通
//        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
//        if (!isNetAvailable) {
//        	//没有网络的场合，去提示页
//        	Bundle bund = new Bundle();
//        	bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
//	    	ActivityUtil.jump(this, ShowErrorActivity.class, Settings.RESTAURANT_IMAGE_ACTIVITY, bund);
//        }
//        
//		//初始化界面
//		setContentView(R.layout.restaurant_map);
//		btnLeft = (Button) findViewById(R.id.map_btnGoBack);
//		btnLeft.setText(R.string.text_title_restaurant_detail);
//		//返回按钮------------------------------------------------
//		btnLeft.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
//		tvTitle = (TextView) findViewById(R.id.map_tvTitle);
//		tvTitle.setText(R.string.text_title_map);
//		btnRight = (Button) findViewById(R.id.map_btnOption);
//		btnRight.setVisibility(View.INVISIBLE);
//		
//		map = (MapView) findViewById(R.id.map_gMap);//获得MapView对象  
//		double geoLatitude = latitude * 1E6;
//		double geoLongitude = longitude * 1E6;
//		GeoPoint p = new GeoPoint((int) geoLatitude, (int) geoLongitude);
//		MapOverlay myOverlay = new MapOverlay(p);
//		map.setStreetView(true);
//        map.getOverlays().add(myOverlay);
//        map.getController().setZoom(19);
//        map.getController().setCenter(p);
//        map.getController().animateTo(p);
//        //开启MapView对象内置的  UI交互处理
//        map.setBuiltInZoomControls(true);
//        map.setClickable(true);
//	}
//	
//    @Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		
//		if (requestCode != resultCode) {
//			this.setResult(resultCode);
//			this.finish();
//		}
//	}
//    
//	@Override
//	public void finish() {
//		super.finish();
//		overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);
//	}
//
//	@Override
//	protected boolean isRouteDisplayed() {
//		return false;
//	}
//	
//	/**
//	 * 自定义层
//	 * @author zhangyifan
//	 *
//	 */
//	class MapOverlay extends Overlay {
//		private GeoPoint gp;
//		
//		public MapOverlay(GeoPoint gp) {
//			super();
//			this.gp = gp;
//		}
//		
//        @Override
//        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
//            
//        	super.draw(canvas, mapView, shadow);                  
//            //—translate the GeoPoint to screen pixels—
//            Point screenPts = new Point();
//            mapView.getProjection().toPixels(this.gp, screenPts);
//            //—add the marker—
//            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pushpin);  
//            //减去的数值按照实际标签图片尺寸而定
//            canvas.drawBitmap(bmp, screenPts.x-15, screenPts.y-70, null);        
//            return true;
//        }
//    }
//}
