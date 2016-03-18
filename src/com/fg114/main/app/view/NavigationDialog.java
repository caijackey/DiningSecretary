//package com.fg114.main.app.view;
//
//import com.fg114.main.R;
//import com.fg114.main.analytics.OpenPageDataTracer;
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.HotDistrictActivity;
//import com.fg114.main.app.activity.IndexActivity;
//import com.fg114.main.app.activity.MainFrameActivity;
//import com.fg114.main.app.activity.mealcombo.MealComboListActivity;
//import com.fg114.main.app.activity.order.FastBookingActivity;
//import com.fg114.main.app.activity.top.TopListActivity;
//import com.fg114.main.service.dto.MainPageInfoPack4DTO;
//import com.fg114.main.service.dto.MainPageInfoPackDTO;
//import com.fg114.main.util.ActivityUtil;
//import com.fg114.main.util.CommonObservable;
//import com.fg114.main.util.CommonObserver;
//import com.fg114.main.util.SessionManager;
//
//import android.app.Activity;
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.LinearLayout.LayoutParams;
//
///**
// * 功能菜单
// * 
// * @author nieyinyin
// * 
// */
//public class NavigationDialog {
//
//	private CustomDialog navigation;
//
//	private LinearLayout llMainMenu;
//	private Button btIndex;
//	private Button btBenefits;
//	private Button btTakeaway;
//	private Button btFastBooking;
//	// private Button btTopList;
//	private Button btFindRest;
//	private RelativeLayout ivCircle;
//
//	private Context context;
//
//	public NavigationDialog(Context context) {
//		if (context == null) {
//			throw new NullPointerException();
//		}
//		this.context = context;
//		init(context);
//	}
//
//	/**
//	 * 
//	 * @param context
//	 */
//	public void init(final Context context) {
//
//		// --- 暂时用传进来的context，正式的时候，用ContextUtil.getContext()
//		View contentView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_content, null, false);
//		llMainMenu = (LinearLayout) contentView.findViewById(R.id.index2_btn_fac);
//		btIndex = (Button) contentView.findViewById(R.id.item_btIndex);
//		btBenefits = (Button) contentView.findViewById(R.id.item_btBenefits);
//		btTakeaway = (Button) contentView.findViewById(R.id.item_btTakeaway);
//		btFastBooking = (Button) contentView.findViewById(R.id.item_btFastBooking);
//
//		btFindRest = (Button) contentView.findViewById(R.id.item_btFindRest);// 找餐厅
//
//		// btTopList = (Button) contentView.findViewById(R.id.item_btTopList);
//		ivCircle = (RelativeLayout) contentView.findViewById(R.id.main_menu_header_circle);
//
//		llMainMenu.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				makeItemInvisible(context);
//				// navigation.dismiss();
//			}
//		});
//
//		// 找餐厅
//		btFindRest.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// 商圈
//				// ----------------------------
//				OpenPageDataTracer.getInstance().addEvent("导航栏商圈按钮");
//				// ----------------------------
//
//				if ((context.getClass() == IndexActivity.class)) { // 首页直接跳转
//					ActivityUtil.jump(context, HotDistrictActivity.class, 0);
//				} else {// 非首页下的情况,先跳转首页再跳转相关页面
//					CommonObservable.getInstance().addObserver(new CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//
//						@Override
//						public void run() {
//							ActivityUtil.jump(context, HotDistrictActivity.class, 0);
//						}
//					}));
//					MainFrameActivity.returnToActivity(IndexActivity.class);
//					((Activity) context).finish();
//				}
//				dismiss();
//			}
//		});
//
//		// 外卖
//		btTakeaway.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// ----------------------------
//				OpenPageDataTracer.getInstance().addEvent("导航栏外卖按钮");
//				// ----------------------------
//
//				if ((context.getClass() == IndexActivity.class)) {
//					Bundle data = new Bundle();
//					data.putString(Settings.BUNDLE_FUNC_NAME, "叫外卖");
//					ActivityUtil.isLocExist((Activity) context, Settings.STATUTE_CHANNEL_TAKEAWAY, 0, data);
//				} else { // 非首页下的情况,先跳转首页再跳转相关页面
//					CommonObservable.getInstance().addObserver(new CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//
//						@Override
//						public void run() {
//							Bundle data = new Bundle();
//							data.putString(Settings.BUNDLE_FUNC_NAME, "叫外卖");
//
//							// 注意这里所传递的context，没办法，被逼的！（因为这里的逻辑是先跳到首页）
//							//ActivityUtil.isLocExist(IndexActivity.indexActivity, Settings.STATUTE_CHANNEL_TAKEAWAY, 0, data);
//						}
//					}));
//					MainFrameActivity.returnToActivity(IndexActivity.class);
//					((Activity) context).finish();
//				}
//				dismiss();
//			}
//		});
//
//		btIndex.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// ----------------------------
//				OpenPageDataTracer.getInstance().addEvent("导航栏首页按钮");
//				// ----------------------------
//
//				if (context.getClass() != IndexActivity.class) {
//					CommonObservable.getInstance().addObserver(new CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//
//						@Override
//						public void run() {
//							ActivityUtil.jump(context, IndexActivity.class, 0);
//						}
//					}));
//					MainFrameActivity.returnToActivity(IndexActivity.class);
//					((Activity) context).finish();
//				}
//				dismiss();
//			}
//		});
//
//		btBenefits.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// ----------------------------
//				OpenPageDataTracer.getInstance().addEvent("导航栏优惠按钮");
//				// ----------------------------
//
//				if (context.getClass() == IndexActivity.class) {
//					Bundle bundle = new Bundle();
//					// bundle.putBoolean(Settings.BUNDLE_KEY_IS_MEALCOMBO,
//					// false);
//					bundle.putBoolean(Settings.BUNDLE_KEY_IS_QUICK_JUMP, false);
//					ActivityUtil.jump(context, MealComboListActivity.class, 0, bundle);
//				} else {
//					CommonObservable.getInstance().addObserver(new CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//
//						@Override
//						public void run() {
//							Bundle bundle = new Bundle();
//							// bundle.putBoolean(Settings.BUNDLE_KEY_IS_MEALCOMBO,
//							// false);
//							bundle.putBoolean(Settings.BUNDLE_KEY_IS_QUICK_JUMP, false);
//							ActivityUtil.jump(context, MealComboListActivity.class, 0, bundle);
//						}
//					}));
//					MainFrameActivity.returnToActivity(IndexActivity.class);
//					((Activity) context).finish();
//				}
//				dismiss();
//			}
//		});
//
//		// 快捷预定
//		btFastBooking.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// ----------------------------
//				OpenPageDataTracer.getInstance().addEvent("导航栏快捷预订按钮");
//				// ----------------------------
//
//				if (context.getClass() == IndexActivity.class) {
//					ActivityUtil.jump(context, FastBookingActivity.class, 0, null);
//				} else {
//					CommonObservable.getInstance().addObserver(new CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//
//						@Override
//						public void run() {
//							ActivityUtil.jump(context, FastBookingActivity.class, 0, null);
//						}
//					}));
//					MainFrameActivity.returnToActivity(IndexActivity.class);
//					((Activity) context).finish();
//				}
//				dismiss();
//			}
//		});
//
//		// 榜单
//		/*
//		 * btTopList.setOnClickListener(new OnClickListener() {
//		 * 
//		 * @Override
//		 * public void onClick(View v) {
//		 * 
//		 * // ----------------------------
//		 * OpenPageDataTracer.getInstance().addEvent("导航栏榜单按钮");
//		 * // ----------------------------
//		 * 
//		 * if (context.getClass() == IndexActivity.class) {
//		 * ActivityUtil.jump(context, TopListActivity.class, 0, null);
//		 * } else {
//		 * CommonObservable.getInstance().addObserver(new
//		 * CommonObserver.ReturnToActivityFinishedObserver(new Runnable() {
//		 * 
//		 * @Override
//		 * public void run() {
//		 * ActivityUtil.jump(context, TopListActivity.class, 0, null);
//		 * }
//		 * }));
//		 * MainFrameActivity.returnToActivity(IndexActivity.class);
//		 * ((Activity) context).finish();
//		 * }
//		 * dismiss();
//		 * }
//		 * });
//		 */
//
//		navigation = new CustomDialog(context, R.style.Main_Menu_Dialog);
//		navigation.addContentView(contentView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		navigation.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//
//		contentView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				makeItemInvisible(context);
//			}
//		});
//	}
//
//	/**
//	 * 
//	 * @param context
//	 * @param parent
//	 */
//	public void show(final Context context, View parent) {
//		if (navigation == null) { //
//			init(context);
//		}
//
//		try {
//			// 根据当前选中城市disable一些菜单按钮
//			disableSomeItemByCity();
//
//			makeItemVisible(context);
//
//			if (!navigation.isShowing()) {
//				navigation.show();
//			}
//
//		} catch (Exception e) {
//			// Nothing to do
//		}
//	}
//
//	/**
//	 * 使功能菜单item不可见
//	 * 
//	 * @param context
//	 */
//	public void makeItemInvisible(Context context) {
//		setItemDismissAnim(context);
//		btBenefits.setVisibility(View.INVISIBLE);
//		btFindRest.setVisibility(View.INVISIBLE);
//		btTakeaway.setVisibility(View.INVISIBLE);
//		ivCircle.setVisibility(View.INVISIBLE);
//		btIndex.setVisibility(View.INVISIBLE);
//		btFastBooking.setVisibility(View.INVISIBLE);
//		// btTopList.setVisibility(View.INVISIBLE);
//	}
//
//	/**
//	 * 使功能菜单item可见
//	 * 
//	 * @param context
//	 */
//	public void makeItemVisible(Context context) {
//		setItemShowAnim(context);
//		btBenefits.setVisibility(View.VISIBLE);
//		btFindRest.setVisibility(View.VISIBLE);
//		btTakeaway.setVisibility(View.VISIBLE);
//		ivCircle.setVisibility(View.VISIBLE);
//		btIndex.setVisibility(View.VISIBLE);
//		btFastBooking.setVisibility(View.VISIBLE);
//		// btTopList.setVisibility(View.VISIBLE);
//	}
//
//	/**
//	 * 给功能菜单item加上显示动画
//	 * 
//	 * @param context
//	 */
//	public void setItemShowAnim(Context context) {
//		Animation btIndexShowAnim = AnimationUtils.loadAnimation(context, R.anim.shrink_inside_to_outside_for_index);
//		btIndexShowAnim.setAnimationListener(itemShowListener);
//		btIndex.setAnimation(btIndexShowAnim);
//		btBenefits.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_inside_to_outside));
//		btFindRest.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_inside_to_outside));
//		btTakeaway.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_inside_to_outside));
//		btFastBooking.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_inside_to_outside));
//		// btTopList.setAnimation(AnimationUtils.loadAnimation(context,
//		// R.anim.shrink_inside_to_outside));
//		ivCircle.setAnimation(AnimationUtils.loadAnimation(context, R.anim.panel_show_from_left_to_right));
//	}
//
//	/**
//	 * 给功能菜单item加上消失动画
//	 * 
//	 * @param context
//	 */
//	public void setItemDismissAnim(Context context) {
//		Animation btIndexDissmiss = AnimationUtils.loadAnimation(context, R.anim.shrink_outside_to_inside);
//		btIndexDissmiss.setAnimationListener(listener);
//		btIndex.setAnimation(btIndexDissmiss);
//		btBenefits.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_outside_to_inside));
//		btFindRest.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_outside_to_inside));
//		btTakeaway.setAnimation(AnimationUtils.loadAnimation(context, R.anim.shrink_outside_to_inside));
//	}
//
//	/**
//	 * dismiss 弹出框
//	 */
//	public void dismiss() {
//		navigation.dismiss();
//	}
//
//	/**
//	 * 判断功能菜单弹出框是否可见
//	 * 
//	 * @return
//	 */
//	public boolean isShowing() {
//		if (navigation == null)
//			return false;
//		return navigation.isShowing();
//	}
//
//	// 动画监听
//	Animation.AnimationListener listener = new Animation.AnimationListener() {
//
//		@Override
//		public void onAnimationEnd(Animation animation) {
//			try {
//				dismiss();
//			} catch (Exception e) {
//				// Nothing to do
//			}
//		}
//
//		@Override
//		public void onAnimationRepeat(Animation animation) {
//
//		}
//
//		@Override
//		public void onAnimationStart(Animation animation) {
//
//		}
//	};
//
//	Animation.AnimationListener itemShowListener = new Animation.AnimationListener() {
//
//		@Override
//		public void onAnimationEnd(Animation animation) {
//			try {
//				ivCircle.setAnimation(AnimationUtils.loadAnimation(context, R.anim.panel_show_from_left_to_right));
//				ivCircle.setVisibility(View.VISIBLE);
//			} catch (Exception e) {
//				// Nothing to do
//			}
//		}
//
//		@Override
//		public void onAnimationRepeat(Animation animation) {
//
//		}
//
//		@Override
//		public void onAnimationStart(Animation animation) {
//
//		}
//	};
//
//	void disableSomeItemByCity() {
//		// 根据城市动态确定各个按钮是否可以点击
//		btIndex.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("首页"));
//		btBenefits.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("优惠"));
//		btFindRest.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("商圈"));
//		btTakeaway.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("外卖"));
//		// btTopList.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("榜单"));
//		btFastBooking.setEnabled(SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("快捷预订"));
//
//		/** 测试disable状态 */
//		// btIndex.setEnabled(false);
//		// btBenefits.setEnabled(false);
//		// btNearby.setEnabled(false);
//		// btTakeaway.setEnabled(false);
//		// btBusinessDistrict.setEnabled(false);
//		// btFastBooking.setEnabled(false);
//		// btTopList.setEnabled(false);
//	}
//}
