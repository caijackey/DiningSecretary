package com.fg114.main.weibo.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.MyThreadPool;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.adapter.WeiboFriendSelectionAdapter;
import com.fg114.main.weibo.dto.User;
import com.fg114.main.weibo.task.GetFriendsListTask;
import com.google.xiaomishujson.reflect.TypeToken;

/**
 * 选取新浪好友页面
 * 
 * @author xujianjun, 2012-03-22
 * 
 */
public class FriendSelectionActivity extends MainFrameActivity {

	// 传入参数
	private int fromPage = 0;

	private List<User> list = new ArrayList<User>(); // 好友列表
	private WeiboFriendSelectionAdapter listAdapter;


	private LinearLayout main;
	private TextView searchBar;
	private ListView listView;
	private TextView overlay;
	private TextView noFriends;

	private UserInfoDTO user;
//	private GetUserFriendListTask getUserFriendListTask;
	private GetFriendsListTask task;
	
	public static WeiboUtil currentWeiboUtil;

	// 保证单线程性的池
	private static MyThreadPool searchThread=new MyThreadPool(1, -1, 50);

	// 提示字母
	private String hintLetter;
	// 首字母提示是否可见
	private boolean needHintLetter = true; 

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		if(currentWeiboUtil==null){
			DialogUtil.showToast(this, "没有设置微博类型!");
			finish();
		}
		
//		int weiboType=bundle.getInt(com.fg114.main.app.zy.Settings.BUNDLE_KEY_WEIBO_TYPE,-1);
//		if(weiboType==com.fg114.main.app.zy.Settings.SHARE_WITH_SINA_WEIBO){
//			currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO_SSO);
//		}else if(weiboType==com.fg114.main.app.zy.Settings.SHARE_WITH_TENXUN_WEIBO){
//			currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
//		}else{
//			currentWeiboUtil=null;
//		}
		
		user = SessionManager.getInstance().getUserInfo(this);

		// mUserFriendListDTO =
		// SessionManager.getInstance().getUserFriendList(this);

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		} else {
			executeTask(false);
		}

	}

	@Override
	public void finish() {
		currentWeiboUtil=null;
		super.finish();

	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("选择微博联系人");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
//		this.getBtnCall57().setVisibility(View.GONE);
		//

		// 内容部分
		main = (LinearLayout) View.inflate(this, R.layout.at_sina_weibo_select, null);
		searchBar = (TextView) main.findViewById(R.id.at_sina_weibo_search_textview);
		listView = (ListView) main.findViewById(R.id.at_sina_weibo_listview);
		overlay = (TextView) main.findViewById(R.id.pop_letter_layer);
		noFriends = (TextView) main.findViewById(R.id.at_sina_weibo_no_friends);
		
		listAdapter = new WeiboFriendSelectionAdapter(FriendSelectionActivity.this);

		searchBar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// //DialogUtil.showToast(CityMoreActivity.this, "好吧");
				// Bundle bundle = new Bundle();
				// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
				// Settings.CITY_MORE_ACTIVITY);
				// ActivityUtil.jump(AtSinaWeiboSelectActivity.this,
				// AtSinaWeiboSelectActivity.class, Settings.CITY_MORE_ACTIVITY,
				// bundle);
			}
		});
		//
		this.getBtnOption().setText("刷新");
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchBar.setText("");
				executeTask(true);

			}
		});

		// 搜索
		searchBar.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				makeSearch(s.toString());
			}

		});
		//
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				List<User> list = ((WeiboFriendSelectionAdapter) parent.getAdapter()).getList();
				if (list != null) {
					User user = list.get(position);
					if (user != null) {
						Bundle data = new Bundle();
						data.putSerializable("sinaUser", user);
						// 将选择的user存入“最近联系人”
						saveRecentPerson(user);
						Intent intent = new Intent();
						intent.putExtras(data);
						FriendSelectionActivity.this.setResult(fromPage, intent);
						// 本页面关闭
						finish();
					}
				}
				/*
				 * if (cbSelect.isChecked()) {
				 * mLocalAdapter.selectItem(position); } else {
				 * mLocalAdapter.unselectItem(position); }
				 */
			}

		});

		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// Log.i("-----",""+firstVisibleItem+","+visibleItemCount+","+totalItemCount);
				if (needHintLetter) {
					// 当是最近联系人时，不显示提示字符
					if (FriendSelectionActivity.this.listView.getCount() > 0 && !((User) view.getAdapter().getItem(firstVisibleItem)).isRecentPerson()) {
						FriendSelectionActivity.this.hintLetter = HanziUtil.getFirst(((User) view.getAdapter().getItem(firstVisibleItem)).getFirstLetter())
								.toUpperCase();
						showPopLetterLayer();
						return;
					}
					// String firstLetter =
					// HanziToPinyinUtil.getAlpha(mFilterLocalList
					// .get(firstVisibleItem).getName());
					/*
					 * String firstLetter =
					 * HanziToPinyinUtil.getFirst(mFilterLocalList
					 * .get(firstVisibleItem).getUuid());
					 */

				}
			}
		});

		this.getMainLayout().addView(main, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	// 提交一个线程去过滤
	private void makeSearch(final String word) {
		if (word == null) {
			return;
		}

		searchThread.submit(new MyThreadPool.Task() {

			@Override
			public void run() {
				try {
					// 如果是空字符串，则显示全部列表
					if (word.trim().equals("")) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								listAdapter.setList(list);
							}
						});
						return;
					}
					// 否则按照搜索的关键词过滤
					final List<User> tempList = new ArrayList<User>();
					for (User u : list) {
						//

						// if(u.getName()!=null&&u.getName().toUpperCase().contains(word.trim().toUpperCase())){
						// tempList.add(u);
						// }
						if (u.getName() != null && HanziUtil.doesStringMatchKeywords(u.getName().toLowerCase(), word.trim().toLowerCase())) {
							tempList.add(u);
						}
					}
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							listAdapter.setList(tempList);
						}
					});

				} catch (Exception e) {
					Log.e("AtSinaWeiboSelectActivity", "error in searchThread.run()", e);
				}
			}
		});

	}

	// user存入“最近联系人”，重复的user不存储
	private void saveRecentPerson(User user) {
		String token=SessionManager.getInstance().getUserInfo(this).getToken();
		//FriendListDTO dto = null;
		List<User> userList = null;
		user.setRecentPerson(true);
		// 从缓存中读取
		ValueObject value = ValueCacheUtil.getInstance(this).get(currentWeiboUtil.getWeiboName(), "recentFriendList-"+Settings.VERSION_NAME+"-" + token);
		if (value != null) {
			// 缓存中有
			try {
				userList = JsonUtils.fromJson(value.getValue(), new TypeToken<List<User>>(){});

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//dto = new FriendListDTO();
		}
		//
		if (userList != null) {
			boolean isExist = false;
			//userList = dto.getList();
			for (User u : userList) {
				if (u.getName()!=null && u.getName().equals(user.getName())){
					isExist = true;
					break;
				}
			}
			if (!isExist) {// 不存在才添加新的
				userList.add(0, user);
			}

			// 保持最多６个最近联系人
			for (int i = 6; i < userList.size(); i++) {
				userList.remove(i);
			}

			// 更新缓存。先删除，再添加
			ValueCacheUtil.getInstance(this).remove(currentWeiboUtil.getWeiboName(), "recentFriendList-"+Settings.VERSION_NAME+"-" + token );
			ValueCacheUtil.getInstance(this).add(currentWeiboUtil.getWeiboName(), "recentFriendList-"+Settings.VERSION_NAME+"-" + token, JsonUtils.toJson(userList), "", "", 10);// 缓存１０分钟
		}

	}


	//---
	private long timestamp=0;
	// 显示字母提示框
	private void showPopLetterLayer() {
		if ("".equals(this.hintLetter)) {
			return; // 不显示空字符
		}
		if (this.overlay != null) {
			this.overlay.setText(this.hintLetter);
			this.overlay.setVisibility(View.VISIBLE);
		}
		final long time=SystemClock.elapsedRealtime();
		timestamp=time;
		this.overlay.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				hidePopLetterLayer(time);
			}
		}, 500);
	}

	// 隐藏字母提示框
	private void hidePopLetterLayer(long time) {

		if (this.overlay != null && timestamp==time) {
			this.overlay.setVisibility(View.INVISIBLE);
		}
	}

	// isRefresh--是否是刷新，如果是刷新，则跳过缓存逻辑，直接去网络取数据
	private void executeTask(boolean isRefresh) {
		UserInfoDTO user = SessionManager.getInstance().getUserInfo(this);
		task = new GetFriendsListTask(this, "正在获取好友，请稍候...", isRefresh, currentWeiboUtil);
		task.execute(new Runnable() {

			@Override
			public void run() {
				task.closeProgressDialog();
				// 如果没有好友，显示信息
				if (task.friendList != null && task.friendList.size() == 0) {
					noFriends.setVisibility(View.VISIBLE);
					listView.setVisibility(View.GONE);
				} else {
					noFriends.setVisibility(View.GONE);
					listView.setVisibility(View.VISIBLE);
					listAdapter = new WeiboFriendSelectionAdapter(FriendSelectionActivity.this);
					listView.setAdapter(listAdapter);
					listAdapter.setList(task.friendList);
					list = task.friendList;
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}


}
