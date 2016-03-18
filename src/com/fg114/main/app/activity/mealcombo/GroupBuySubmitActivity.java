package com.fg114.main.app.activity.mealcombo;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.CouponApplyFormData;
import com.fg114.main.service.dto.CouponApplyInputData;
import com.fg114.main.service.dto.CouponInfoData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
/**
 * 团购报名表单
 * @author dengxiangyu
 *
 */
public class GroupBuySubmitActivity extends MainFrameActivity {
	private String uuid;
	
	private View contextView;
	private Button chbShareToSina;
	private Button chbShareToTX;
	private boolean isSINAWBbinding=false;
	private boolean isQQWBbinding=false;
	private LinearLayout group_buy_info_view;
	private LinearLayout group_buy_submit_share;
	private TextView group_buy_submit_hint;
	private String couponApplyFormDataJson="";
	private CouponApplyFormData couponApplyFormData;
	private Button group_buy_submit_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购报名表单", "");
		// ----------------------------
		
		
		Bundle bundle = this.getIntent().getExtras();
	    uuid=bundle.getString(Settings.UUID);
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

		 executeTask();

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购报名表单", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	private void initComponent() {
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getTvTitle().setText("我要报名");
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.group_buy_sumbit_activiry, null);
		contextView.setVisibility(View.GONE);
		chbShareToSina = (Button) contextView.findViewById(R.id.group_buy_submit_chkShareSina);
		chbShareToTX = (Button) contextView.findViewById(R.id.group_buy_submit_chkShareTX);
		group_buy_info_view = (LinearLayout) contextView.findViewById(R.id.group_buy_info_view);
		group_buy_submit_share= (LinearLayout) contextView.findViewById(R.id.group_buy_submit_share);
		group_buy_submit_hint=(TextView) contextView.findViewById(R.id.group_buy_submit_hint);
		group_buy_submit_bt=(Button)contextView.findViewById(R.id.group_buy_submit_bt);

		// TODO
		chbShareToSina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isSINAWBbinding) {
					isSINAWBbinding = false;
					chbShareToSina.setBackgroundResource(R.drawable.weibo_icon_1_gray);
				} else {
					isSINAWBbinding = true;
					chbShareToSina.setBackgroundResource(R.drawable.weibo_icon_1_hl);
				}
			}
		});

		chbShareToTX.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isQQWBbinding) {
					isQQWBbinding = false;
					chbShareToTX.setBackgroundResource(R.drawable.weibo_icon_2_gray);
				} else {
					isQQWBbinding = true;
					chbShareToTX.setBackgroundResource(R.drawable.weibo_icon_2_hl);
				}

			}
		});
		
		group_buy_submit_bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				postCouponApplyFromInfo();
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void executeTask() {
		ServiceRequest request = new ServiceRequest(API.getCouponApplyFormInfo);
		request.addData("uuid", uuid);
		// -----------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----------------
		CommonTask.request(request, "数据获取中，请稍候...", new CommonTask.TaskListener<CouponApplyFormData>() {

			@Override
			protected void onSuccess(CouponApplyFormData dto) {
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				if(dto!=null){
					couponApplyFormData=dto;
				}
				contextView.setVisibility(View.VISIBLE);
				setView(dto);
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				 finish();
//				 doTest_cancel();
				

			}

			 void doTest_cancel() {
			 String json ="{\"inputList\":[{\"uuid\":\"11\",\"name\":\"姓 名\",\"placeHolder\":\"请输入姓名\",\"text\":\"\",\"canEmptyTag\":\"false\"},{\"uuid\":\"11\",\"name\":\"手机号\",\"placeHolder\":\"请输入手机号\",\"text\":\"\",\"canEmptyTag\":\"false\"},{\"uuid\":\"11\",\"name\":\"地址\",\"placeHolder\":\"请输入地址\",\"text\":\"\",\"canEmptyTag\":\"false\"},{\"uuid\":\"11\",\"name\":\"备注\",\"placeHolder\":\"请输入备注\",\"text\":\"\",\"canEmptyTag\":\"true\"}],\"showSharePanelTag\":\"true\",\"shareHint\":\"分享到其它网站可提高抽奖率\"}";
			 CouponApplyFormData data = JsonUtils.fromJson(json,CouponApplyFormData.class);
			 onSuccess(data);
			 }
		});
	}

	private void postCouponApplyFromInfo() {
		if(!getCouponApplyFormDataJson(couponApplyFormData)){
			return;
		}
		ServiceRequest request = new ServiceRequest(API.postCouponApply);
		request.addData("uuid", uuid);
		request.addData("shareTo", getShareString());
		request.addData("formData", couponApplyFormDataJson);//表单信息  jsonData  CouponApplyFormData  json
		request.setCanUsePost(true);
		// -----------------
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----------------
		CommonTask.request(request, "数据提交中，请稍候...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {
				if(dto!=null){
				DialogUtil.showAlert(GroupBuySubmitActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// ----------------
						OpenPageDataTracer.getInstance().endEvent("提交按钮");
						// ----------------
						Bundle bundle=new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, dto.getUuid());
						ActivityUtil.jump(GroupBuySubmitActivity.this, GroupBuySubmitSuccessActivity.class, 0,bundle);
					}
				});
				}
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// ----------------
				// finish();
				// doTest_cancel();

			}

			// void doTest_cancel() {
			// String json =
			// "{\"uuid\":\"111\",\"typeTag\":\"2\",\"name\":\"有滋有味千团套餐\",\"statusName\":\"正在进行\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picList\":[{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}],\"nowPrice\":\"100\",\"oldPrice\":\"40\",\"applyShortHint\":\"报名简短提示 标红\",\"btnEnabledTag\":\"true\",\"btnName\":\"购买购买\",\"describeList\":[{\"name\":\"描述名\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"actionXmsUrl\"},{\"name\":\"描述名\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"actionXmsUrl\"},{\"name\":\"描述名\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"actionXmsUrl\"}],\"limitTitle\":\"limitTitle\",\"remainSeconds\":\"121313131\",\"limitHint\":\"limitHintlimitHintlimitHint\",\"limitRangeList\":[{\"name\":\"12:00\",\"statusTag\":\"1\"},{\"name\":\"14:00\",\"statusTag\":\"1\"},{\"name\":\"16:00\",\"statusTag\":\"2\"},{\"name\":\"18:00\",\"statusTag\":\"3\"}],\"hintTitle\":\"hintTitlehintTitle\",\"hintDetail\":\"hintDetailhintDetailhintDetailhintDetailhintDetailhintDetailhintDetailhintDetail\",\"restList\":[{\"restId\":\"11\",\"restName\":\"restName\",\"restAddress\":\"restAddress\",\"distanceMeter\":\"200米\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"\",\"branch\":\"\"}]},{\"restId\":\"11\",\"restName\":\"restName\",\"restAddress\":\"restAddress\",\"distanceMeter\":\"200米\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"\",\"branch\":\"\"}]}],\"couponDetail\":\"couponDetailcouponDetailcouponDetailcouponDetail\",\"couponDetailWapUrl\":\"couponDetailWapUrlcouponDetailWapUrl\",\"canAnytimeRefundTag\":\"true\",\"anytimeRefundHint\":\"是否支持随时退款\",\"canOvertimeRefundTag\":\"true\",\"overtimeRefundHint\":\"是否支持过期退款\",\"soldNumHint\":\"200\",\"remainTimeHint\":\"4天20小时30分\"}";
			// CouponInfoData data = JsonUtils.fromJson(json,
			// CouponInfoData.class);
			// onSuccess(data);

			// }
		});
	}

	
	private void setView(CouponApplyFormData dto){
		if(dto==null){
			return;
		}
		if(dto.showSharePanelTag){
			group_buy_submit_share.setVisibility(View.VISIBLE);
			group_buy_submit_hint.setVisibility(View.VISIBLE);
		}else{
			group_buy_submit_share.setVisibility(View.GONE);
			group_buy_submit_hint.setVisibility(View.GONE);
		}
		if(!CheckUtil.isEmpty(dto.shareHint)){
		group_buy_submit_hint.setText(dto.shareHint);
		}
		
		addGroupBuyInfoView(dto.inputList);
		
	}
	/**
	 * 构建分享参数
	 * 
	 * @return
	 */
	// TODO
	private String getShareString() {
		StringBuffer sbShare = new StringBuffer();
		if (isSINAWBbinding) {
			sbShare.append("sina:1;");
		} else {
			sbShare.append("sina:0;");
		}
		if (isQQWBbinding) {
			sbShare.append("qq:1");
		} else {
			sbShare.append("qq:0");
		}

		return sbShare.toString();
	}

	private void addGroupBuyInfoView(List<CouponApplyInputData> inputList) {
		if (inputList == null) {
			return;
		}
		if(group_buy_info_view.getChildCount()!=0){
			group_buy_info_view.removeAllViews();	
		}
		for (int i = 0; i < inputList.size(); i++) {
			LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = mInflater.inflate(R.layout.group_buy_sumbit_info_item, null);
			TextView group_buy_sumbit_name = (TextView) view.findViewById(R.id.group_buy_sumbit_name);
			EditText group_buy_sumbit_detail_ed = (EditText) view.findViewById(R.id.group_buy_sumbit_detail_ed);
			View group_buy_sumbit_line = (View) view.findViewById(R.id.group_buy_sumbit_line);

			group_buy_sumbit_name.setText(inputList.get(i).name);
			
			if (!CheckUtil.isEmpty(inputList.get(i).placeHolder)) {
				group_buy_sumbit_detail_ed.setHint(inputList.get(i).placeHolder);
			}
			if (!CheckUtil.isEmpty(inputList.get(i).text)) {
				group_buy_sumbit_detail_ed.setHint(inputList.get(i).text);
			}
			if(i==(inputList.size()-1)){
				group_buy_sumbit_line.setVisibility(View.GONE);
			}
			group_buy_info_view.addView(view);
		}

	}
	
	
	private boolean getCouponApplyFormDataJson(CouponApplyFormData data){
		if(group_buy_info_view.getChildCount()==0&&group_buy_info_view==null){
			return false;
		}
		if(data==null){
			return false;
		}
		for(int i=0;i<data.inputList.size();i++){
			EditText group_buy_sumbit_detail_ed=(EditText) group_buy_info_view.getChildAt(i).findViewById(R.id.group_buy_sumbit_detail_ed);
			String s=group_buy_sumbit_detail_ed.getText().toString();
			if(CheckUtil.isEmpty(s)&&!data.inputList.get(i).canEmptyTag){
				DialogUtil.showToast(GroupBuySubmitActivity.this, data.inputList.get(i).name+"不能为空");
				return false;
			}
			data.inputList.get(i).text=s;
		}
		
		couponApplyFormDataJson = JsonUtils.toJson(data, CouponApplyFormData.class);
		return true;
		
	}
}
