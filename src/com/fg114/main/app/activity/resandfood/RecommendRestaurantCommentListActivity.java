package com.fg114.main.app.activity.resandfood;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.RestRecomCommentData;
import com.fg114.main.service.dto.RestRecomCommentListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅评论
 * 
 * @author lijian
 * 
 */
public class RecommendRestaurantCommentListActivity extends MainFrameActivity {
	private LayoutInflater mInflater;
	private View contextView;

	private ListView recomRestcommentlv;

	private String uuid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("推荐评论列表", "");
		// ----------------------------

		Bundle bundle = this.getIntent().getExtras();

		if (bundle == null) {
			DialogUtil.showToast(ContextUtil.getContext(), "数据请求异常");
			finish();
		}
		uuid = bundle.getString(Settings.BUNDLE_REST_ID);

		// 初始化界面
		initComponent();
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("餐厅评论");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.recommend_restaurant_comment_detail, null);

		recomRestcommentlv = (ListView) contextView.findViewById(R.id.recommend_restaurant_comment_listview);

		recomRestcommentlv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----
					
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initAdapter();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("推荐评论列表", "");
		// ----------------------------
	}

	List<RestRecomCommentData> restRecomCommentDataList;

	private void initAdapter() {

		ListViewAdapter<RestRecomCommentData> adapter = new ListViewAdapter<RestRecomCommentData>(R.layout.list_item_res_rescommed_comment,
				new ListViewAdapter.OnAdapterListener<RestRecomCommentData>() {

					@Override
					public void onLoadPage(final ListViewAdapter<RestRecomCommentData> adapter, final int startIndex, int pageSize) {

						// ----
						OpenPageDataTracer.getInstance().addEvent("页面查询");
						// -----

						RestRecomCommentListDTO dto = null;
						ServiceRequest request = new ServiceRequest(API.getRestRecomCommentList);
						request.addData("uuid", uuid);
						request.addData("pageSize", pageSize);
						request.addData("startIndex", startIndex);
						CommonTask.request(request, "正在加载数据...", new CommonTask.TaskListener<RestRecomCommentListDTO>() {

							@Override
							protected void onSuccess(RestRecomCommentListDTO dto) {

								// ----
								OpenPageDataTracer.getInstance().endEvent("页面查询");
								// -----

								restRecomCommentDataList = dto.list;
								ListViewAdapter.AdapterDto<RestRecomCommentData> adapterDto = new ListViewAdapter.AdapterDto<RestRecomCommentData>();
								adapterDto.setList(restRecomCommentDataList);
								adapterDto.setPageInfo(dto.pgInfo);
								adapter.onTaskSucceed(adapterDto);
							};

							protected void onError(int code, String message) {

								// ----
								OpenPageDataTracer.getInstance().endEvent("页面查询");
								// -----

								DialogUtil.showToast(getApplicationContext(), message);
								adapter.onTaskFail();
								this.onSuccess(getTest());
							};
						});
					}

					@Override
					public void onRenderItem(ListViewAdapter<RestRecomCommentData> adapter, ViewHolder holder, final RestRecomCommentData data) {
						MyImageView userPicUrl = holder.$myIv(R.id.user_pic_url_img);
						TextView userNickName = holder.$tv(R.id.user_nick_name_tv);
						TextView createTime = holder.$tv(R.id.create_time_tv);
						TextView detail = holder.$tv(R.id.detail_tv);
						LineView line = (LineView) holder.$(R.id.horizontal_line);
						userPicUrl.setImageByUrl(data.userPicUrl, true, 0, ScaleType.FIT_XY);
						userNickName.setText(data.userNickName);
						createTime.setText(data.createTime);
						detail.setText(data.detail);
						if (restRecomCommentDataList.size() == (adapter.getCount() + 1)) {
							line.setVisibility(View.GONE);
						}
						
						userPicUrl.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(v, 1000);
								String path = "member/resrecommend";
								String title = data.userNickName + "的主页";
								NameValuePair pair = new BasicNameValuePair("userId", data.userId);
								ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title, pair);
							}
						});
						

					};

				});
		adapter.setExistPage(true);
		adapter.setmCtx(RecommendRestaurantCommentListActivity.this);
		adapter.setListView(recomRestcommentlv);

	}

	// 获得餐厅推荐信息，返回RestRecomCommentListDTO
	private RestRecomCommentListDTO getTest() {
		String json = "{\"list\":[{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://www.xiaomishu.com/shop/D22I15N56303/pic/\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"},{\"userNickName\":\"一朵熊\",\"userPicUrl\":\"http://g.hiphotos.baidu.com/album/w%3D2048/sign=969ec776aec379317d688129dffcb645/b999a9014c086e06f3fb3a3b03087bf40ad1cb6b.jpg\",\"detail\":\"价格挺贵的哦，还蛮不错的啦，服务各方面都挺好 难办来吃吃还可以哦\",\"createTime\":\"2013-02-12 16:50\"}],\"needUpdateTag\":\"true\",\"timestamp\":\"0\",\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\"true\"}}";
		RestRecomCommentListDTO restRecomInfoData = JsonUtils.fromJson(json, RestRecomCommentListDTO.class);
		return restRecomInfoData;
	}

}
