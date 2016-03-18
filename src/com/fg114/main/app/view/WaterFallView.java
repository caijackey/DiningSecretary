package com.fg114.main.app.view;

import com.fg114.main.R;

import com.fg114.main.service.dto.ResPicData2;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DragArea;
import com.fg114.main.util.ViewUtils;

import android.content.Context;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import android.view.Gravity;
import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * 餐厅图片界面包含底部评论信息的 瀑布流 内容控件
 * 
 * @author liwenjie
 * 
 */
public class WaterFallView extends WaterFallBaseView
{

	private WaterFallImageView ResImage;
	private TextView ResInfoTv;
	private TextView HotNumTv;
	private TextView PicNumTv;
	private MyImageView UserPic;
	private EllipsizeText UserCommentTv;
	private Button MoreInfoBtn;
	private View lineView;
	private RelativeLayout mainBottom; // 底部内容区的布局
	private RelativeLayout userCommentLayout; // 包含用户头像和评论的布局
//	public static int BOTTOM_HIEGHT = 130; // 第一种包含用户头像和评论的布局的高度
//	public static int NO_CENTER_HIEGHT = 70;// 第二种隐藏了用户头像和评论的布局高度
	
	// ----- 隐藏 箭头按钮所需要的高度
	public static int BOTTOM_HIEGHT = 90; // 第一种包含用户头像和评论的布局的高度
	public static int NO_CENTER_HIEGHT = 40;// 第二种隐藏了用户头像和评论的布局高度

	public WaterFallView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public WaterFallView(Context context)
	{
		super(context);
	}
	/**
	 * 加载view显示信息
	 */
	@Override
	public void LoadViewData()
	{
		try {
			// 初始化view
			HotNumTv = (TextView) getContentView().findViewById(R.id.flow_content_HotNumTv);
			PicNumTv = (TextView) getContentView().findViewById(R.id.flow_content_PicNum);
			UserPic = (MyImageView) getContentView().findViewById(R.id.flow_content_userPic);
			UserCommentTv = (EllipsizeText) getContentView().findViewById(R.id.flow_content_user_comment);
			MoreInfoBtn = (Button) getContentView().findViewById(R.id.flow_content_more_infoBtn);
			ResImage = (WaterFallImageView) getContentView().findViewById(R.id.flow_content_WaterFallImageView);
			ResInfoTv = (TextView) getContentView().findViewById(R.id.flow_content_picTextView);
			userCommentLayout = (RelativeLayout) getContentView().findViewById(R.id.flow_content_centerID);
			lineView = getContentView().findViewById(R.id.flow_content_div2);
			mainBottom = (RelativeLayout) getContentView().findViewById(R.id.flow_content_main_layout);
			// 设置数据
			RestPicData data = getDto();
			if (data != null) {
				int width = data.getSmallPicWidth();
				int height = data.getSmallPicHeight();
				int ImageHeight = (height * getItemWidth()) / width;// 调整高度

				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getItemWidth(), ImageHeight);
				ResImage.setLayoutParams(layoutParams);
				ResImage.setMinimumHeight(ImageHeight);
				ResImage.setMinimumWidth(getItemWidth());

				ResImage.setBackgroundResource(R.drawable.bg_index_new_block);
				ResImage.setImageByUrl(data.getSmallPicUrl(), true, this.getId(), ScaleType.FIT_XY);
				ResInfoTv.setText(data.getName());

				FrameLayout.LayoutParams layoutParamsTv = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParamsTv.gravity = Gravity.BOTTOM;
				ResInfoTv.setLayoutParams(layoutParamsTv);
				ResInfoTv.setGravity(Gravity.CENTER_VERTICAL);

				HotNumTv.setText("人气:" + data.getHotNum());
				
				//逻辑修改，这里本来显示的是图片数量，现在修改为显示价格-------------------------
//				if(data.getGroupPicNum()>0){
//					PicNumTv.setText("图片:" + data.getGroupPicNum() + "张");
//				}else{					
//					PicNumTv.setText("暂无图片");
//				}
				
				if(!CheckUtil.isEmpty(data.getPrice())){
					PicNumTv.setText(data.getPrice());
				}else{					
					PicNumTv.setText("暂无价格");
				}
				//---------------------------------------------------------------------------
				MoreInfoBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						ViewUtils.preventViewMultipleClick(v, 1000);
						getContentView().performClick();
					}
				});
//				if ("".equals(data.getCommentUserName()) || "".equals(data.getCommentDetail())) {
					if (TextUtils.isEmpty(data.getCommentUserName()) || TextUtils.isEmpty(data.getCommentDetail())) {
					userCommentLayout.setVisibility(View.GONE);
					lineView.setVisibility(View.GONE);
					setItemHeight(ImageHeight + DragArea.dip2px(ContextUtil.getContext(), NO_CENTER_HIEGHT));
					LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(getItemWidth(), DragArea.dip2px(ContextUtil.getContext(), NO_CENTER_HIEGHT));
					mainBottom.setLayoutParams(lay);
				} else {
					userCommentLayout.setVisibility(View.VISIBLE);
					lineView.setVisibility(View.VISIBLE);
					setItemHeight(ImageHeight + DragArea.dip2px(ContextUtil.getContext(), BOTTOM_HIEGHT));
					LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(getItemWidth(), DragArea.dip2px(ContextUtil.getContext(), BOTTOM_HIEGHT));
					mainBottom.setLayoutParams(lay);

					UserPic.setImageByUrl(data.getCommentUserPicUrl(), true, this.getId(), ScaleType.FIT_XY);
					UserCommentTv.setMaxLines(2);
					UserCommentTv.setText(data.getCommentUserName() + ":" + data.getCommentDetail());
				}

				LinearLayout.LayoutParams lin = new LinearLayout.LayoutParams(getItemWidth(), getItemHeight());
				lin.setMargins(4, 8, 4, 8);
				lin.gravity = Gravity.CENTER;
				this.setLayoutParams(lin);

			}
		} catch (Exception e) {
			Log.e("bug", "dto" + getDto().getName() + getDto().getSmallPicUrl() + e.getMessage());
		}

	}
	@Override
	protected int getContentResId()
	{
		// TODO Auto-generated method stub
		return R.layout.flow_content_view;
	}

}
