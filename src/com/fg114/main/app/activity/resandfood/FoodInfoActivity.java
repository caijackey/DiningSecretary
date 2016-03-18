package com.fg114.main.app.activity.resandfood;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.DishBaseActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.SessionManager;

/**
 * 菜品信息界面
 * @author xujianjun
 *
 */
public class FoodInfoActivity extends DishBaseActivity {
	
	//传入参数
	private	String foodId;	//菜品ID
	private	String foodName;	//菜品名称
	private	String restName = "菜品信息";	//餐厅名称
	private	String foodInfo;	//菜品详细
	


	//界面组件
	private LayoutInflater mInflater;
	private View contextView;
	
	private TextView tvDetail;
	private TextView tvFoodName;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		String[] idAndName=bundle.getStringArray(Settings.BUNDLE_KEY_ID);
		foodId = idAndName[0];
		foodName = idAndName[1];
		
		if (idAndName.length == 3) {
			restName = idAndName[2];
		}
		
		foodInfo=bundle.getString(Settings.BUNDLE_FOOD_INFO);
		//foodInfo="哎呀，好好吃啊，蟹里面的脂膏金黄油亮，几乎整个覆于后盖，特别饱满厚实，而且膏质坚挺，一吃便知是好蟹。除了蟹，里面还有基围虾、蛤蜊等海鲜，这样煲出的粥，格外鲜美。砂锅粥的粥底也很重要，只选正宗的东北米，生米下锅，明火熬煮，且大厨得不停地用勺子边煮边搅，煮出的粥才特别稠，米粒颗颗圆润，香气扑鼻。";
		
		
		//初始化界面
		initComponent();
		
		//检查网络是否连通
        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
        if (!isNetAvailable) {
        	//没有网络的场合，去提示页
        	Bundle bund = new Bundle();
        	bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
	    	ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
        }

	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		
		//设置标题栏
		this.getTvTitle().setText(restName);
//		this.getTvTitle().setText(R.string.text_title_restaurant_info);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		
		//内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.food_info, null);
		tvDetail = (TextView) contextView.findViewById(R.id.food_info_detail);
		tvFoodName = (TextView) contextView.findViewById(R.id.food_name);

	    this.setFunctionLayoutGone();
		((ViewGroup)this.getMainLayout().findViewById(R.id.dishbase_main_layout)).addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);	
//		((ViewGroup)this.getMainLayout()).addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);	
		tvDetail.setText(foodInfo);
		tvFoodName.setText(foodName);
		
	}
	

	

}
