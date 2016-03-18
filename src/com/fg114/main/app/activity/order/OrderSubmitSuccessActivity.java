package com.fg114.main.app.activity.order;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.view.CommentImageHorizontalScrollView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.OrderStateHorizaontalScrollView;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.OrderStateInfoData;
import com.fg114.main.service.dto.TakeoutMenuListPack2DTO;
import com.fg114.main.service.dto.TakeoutPostOrderFormData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;

/**
 * 订座提交成功页 //传入orderID
 * 
 * @author dengxiangyu
 * 
 */
public class OrderSubmitSuccessActivity extends MainFrameActivity {
	// 缓存数据
	private String orderId;
	private OrderStateInfoData orderStateInfoData = null;

	private View contextView;
	private LayoutInflater mInflater;
	private MyImageView order_state_icon;
	private TextView order_state_hint;
	private View order_state_line;
	private TextView operate_hint;
	private OrderStateHorizaontalScrollView order_coin_imageScrollView;
	private Button share_mibi_bt;
	private View jump_mibi_wap;

	private Boolean forAddTag;// 是否是添加订单

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("下单完成提示", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
			forAddTag = bundle.getBoolean(Settings.BUNDLE_forAddTag);
		}
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

		executeOrderSubmitSuccess();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("下单完成提示", "");
		// ----------------------------
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return orderStateInfoData.shareInfo == null ? "" : orderStateInfoData.shareInfo.shareWeiboUuid;
	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("订座提交成功");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("查看订单");
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.order_submit_success_act, null);
		order_state_icon = (MyImageView) contextView.findViewById(R.id.order_state_icon);
		order_state_hint = (TextView) contextView.findViewById(R.id.order_state_hint);
		order_state_line = contextView.findViewById(R.id.order_state_line);
		operate_hint = (TextView) contextView.findViewById(R.id.operate_hint);
		order_coin_imageScrollView = (OrderStateHorizaontalScrollView) contextView.findViewById(R.id.order_coin_imageScrollView);
		share_mibi_bt = (Button) contextView.findViewById(R.id.share_mibi_bt);
		jump_mibi_wap = contextView.findViewById(R.id.jump_mibi_wap);

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// --------------
				OpenPageDataTracer.getInstance().addEvent("订单详情按钮");
				// --------------

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
				ActivityUtil.jump(OrderSubmitSuccessActivity.this, NewMyOrderDetailActivity.class, 0, bundle, true);
			}
		});
		
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void executeOrderSubmitSuccess() {
		ServiceRequest request = new ServiceRequest(API.getOrderStateInfo);
		request.addData("orderId", orderId);
		request.addData("forAddTag", forAddTag);		
		// --------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// --------------

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<OrderStateInfoData>() {

			@Override
			protected void onSuccess(OrderStateInfoData dto) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				setView(dto);
				if (dto != null) {
					orderStateInfoData = dto;
				}
			}

			protected void onError(int code, String message) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				super.onError(code, message);
				// doTest_confirm();
			}

			private void doTest_confirm() {
				String s = "{\"stateIconUrl\":\"http://www.baidu.com\",\"stateHint\":\"订座已提交成功\",\"showOperatePanelTag\":\"true\",\"operateHint\":\"你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，你是小秘书第1000，\",\"coinPicList\":[{\"name\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},{\"name\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}],\"coinWapUrl\":\"http://www.baidu.com\",\"showShareBtnTag\":\"true\",\"shareInfo\":{\"shareSmsDetail\":\"shareSmsDetail\",\"shareEmailDetail\":\"shareEmailDetail\",\"shareWeiboDetail\":\"shareWeiboDetail\",\"shareWeixinIconUrl\":\"shareWeixinIconUrl\",\"shareWeixinDetailUrl\":\"shareWeixinDetailUrl\",\"shareWeixinDetail\":\"shareWeixinDetail\",\"shareWeixinName\":\"shareWeixinName\",\"shareWeiboUuid\":\"shareWeiboUuid\"}}";
				OrderStateInfoData data = JsonUtils.fromJson(s, OrderStateInfoData.class);
				onSuccess(data);

			}
		});
	}

	private void setView(final OrderStateInfoData dto) {
		if (dto == null) {
			return;
		}
		
		order_state_icon.setImageByUrl(dto.stateIconUrl, true, 0, ScaleType.FIT_CENTER);
		order_state_hint.setText(Html.fromHtml(dto.stateHint));
		if (dto.showOperatePanelTag) {

			// order_coin_imageScrollView.setOnClickListener(new
			// OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// // TODO Auto-generated method stub
			// // --------------
			// OpenPageDataTracer.getInstance().addEvent("秘币按钮");
			// // --------------
			// ActivityUtil.jumpToWeb(dto.coinWapUrl, "");
			// }
			// });
			order_state_line.setVisibility(View.VISIBLE);
			operate_hint.setVisibility(View.VISIBLE);
			operate_hint.setText(Html.fromHtml(dto.operateHint));

			List<CommentPicData> picDatas = new ArrayList<CommentPicData>();
			if (dto.coinPicList != null) {
				for (int i = 0; i < dto.coinPicList.size(); i++) {
					CommentPicData commentPicData = new CommentPicData();
					commentPicData.smallPicUrl = dto.coinPicList.get(i).getName();
					commentPicData.picUrl = dto.coinWapUrl;
					picDatas.add(commentPicData);
				}
				if (picDatas.size() > 0 && order_coin_imageScrollView.getImageCount() == 0) {
					order_coin_imageScrollView.setVisibility(View.VISIBLE);
					order_coin_imageScrollView.setImageData(picDatas);
				}
			}
		} else {
			order_state_line.setVisibility(View.GONE);
			operate_hint.setVisibility(View.GONE);
			order_coin_imageScrollView.setVisibility(View.GONE);
		}

		if (dto.showShareBtnTag) {
			if (CheckUtil.isEmpty(dto.shareBtnName)) {
				share_mibi_bt.setText("分享得秘币");
			} else {
				share_mibi_bt.setText(dto.shareBtnName);
			}
			
			share_mibi_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					postSelectOrderStateShareBtn();
					showShareDialog(6);
				}
			});
		} else {
			share_mibi_bt.setVisibility(View.GONE);
		}

		if (order_coin_imageScrollView.getChildCount() != 0) {
			for (int i = 0; i < order_coin_imageScrollView.getChildCount(); i++) {
				order_coin_imageScrollView.getChildAt(i).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						// --------------
						OpenPageDataTracer.getInstance().addEvent("秘币按钮");
						// --------------
						ActivityUtil.jumpToWeb(dto.coinWapUrl, "");
					}
				});
			}
		}

	}

	private void postSelectOrderStateShareBtn() {
		ServiceRequest request = new ServiceRequest(API.postSelectOrderStateShareBtn);
		request.addData("orderId", orderId);
		CommonTask.requestMutely(request, new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
			}

			protected void onError(int code, String message) {
				super.onError(code, message);
			}

		});
	}
}
