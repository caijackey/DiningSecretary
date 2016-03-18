package com.fg114.main.app.adapter.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.service.dto.PgInfo;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ViewUtils;

/**
 * ListView Adapter，支持
 * 1、普通的ListView 2、需要分页的ListView 3、需要分页时，footView可以自定义，若不自定义，会使用默认的
 * 注意：默认是需要分页的;  泛型DTO是ListView所需要的Object
 * 
 * 以下是以订单列表为例：
 * <pre>
 *	ListViewAdapter<OrderHintData> adapter = new ListViewAdapter<OrderHintData>(R.layout.order_list_item, new ListViewAdapter.OnAdapterListener<OrderHintData>() {
 *			
 *			@Override
 *			public void onRenderItem(ViewHolder holder, OrderHintData data) {
 *
 *				MyImageView iconUrl=(MyImageView) holder.$iv(R.id.icon_url).setImageByUrl(data.iconUrl, true, 0, ScaleType.CENTER_CROP);
 *				TextView reserveTime=holder.$tv(R.id.reserve_time);
 *				TextView peopleNum=holder.$tv(R.id.people_num);
 *				TextView roomTypeName=holder.$tv(R.id.room_type_name);
 *				TextView restName=holder.$tv(R.id.rest_name);
 *				TextView eaterName=holder.$tv(R.id.eater_name);
 *				TextView eaterTel=holder.$tv(R.id.eater_tel);
 *				TextView statusName=holder.$tv(R.id.status_name);
 *				iconUrl.setImageByUrl(data.iconUrl, true, 0, ScaleType.CENTER_CROP);
 *				reserveTime.setText(data.reserveTime);
 *				peopleNum.setText(data.peopleNum);
 *				roomTypeName.setText(data.roomTypeName);
 *				restName.setText(data.restName);
 *				eaterName.setText(data.eaterName);
 *				eaterTel.setText(data.eaterTel);
 *				statusName.setText(data.statusName);
 *			
 *			};
 *			
 *			@Override
 *			public void onLoadPage(final ListViewAdapter<OrderHintData> adapter, int startIndex, int pageSize) {
 *
 *				ServiceRequest request = new ServiceRequest(API.getOrderList);
 *				request.addData("statusId", "");
 *				request.addData("startIndex", startIndex);
 *				request.addData("pageSize", pageSize);
 *				
 *				CommonTask.request(request, new CommonTask.TaskListener<OrderListDTO>() {
 *					
 *					@Override
 *					protected void onSuccess(OrderListDTO dto) {
 *						ListViewAdapter.AdapterDto<OrderHintData> adapterDto = new ListViewAdapter.AdapterDto<OrderHintData>();
 *						adapterDto.setList(dto.list);
 *						adapterDto.setPageInfo(dto.pgInfo);
 *						adapter.onTaskSucceed(adapterDto);
 *					};
 *				} );
 *			
 *			};
 *		});
 *		adapter.setExistPage(true);   // 此句代码必须在"adapter.setListView(listview)"之前
 *		adapter.setmCtx(OrderListActivity.this); //若需要用到的Context是Activity，则需要手动设置mCtx，否则默认是Application，注:此句代码也必须在"adapter.setListView(listview)"之前
 *		adapter.setListView(listview); //
 * </pre>
 * 
 * @author nieyinyin
 * @since 2013-07-01
 * 
 */
public class ListViewAdapter<T> extends BaseAdapter {

	// ------------------------------------------------------- Constants
	private static final String TAG = "ListViewAdapter";
	private static final int PAGE_SIZE = 20;
	
	
	// ------------------------------------------------------- 私有属性
	
	private List<T> mList; // 数据源
	private Context mCtx = ContextUtil.getContext();  // 应用环境全局上下文
	private int layoutId; // 布局Id
	private LayoutInflater mInflater; 
	private OnAdapterListener<T> onAdapterListener;
	private PgInfo pgInfo; // 分页信息
	private ListView listView; //所绑定的ListView
	
	/**
	 * 控制是否分页,默认需要分页的
	 * 若不分页，需要手动设置setExistPage(false);
	 */
	private boolean isExistPage = true;
	/**
	 * ListView的FooterView，当ListView加载下一页的时候显示；
	 * 如果，您想用自己的footView,您可以自己set进来
	 */
	private View footerView;
	
	/**
	 * 第一次进入标志，控制“无分页情况”下，加载footerView的显示
	 */
	private volatile boolean isFirstInflate = true;
	
	/**
	 * 控制当前只有一个Task执行，保持线程安全；
	 * true --> 线程安全
	 * false --> 线程不安全
	 */
	private volatile boolean isThreadSafe = true;
	
	/**
	 * footView 相关View
	 */
	private ProgressBar pBar;  // 加载提示进度
	private TextView tvMsg;    // 加载提示信息
	private Button btnRetry;   // 重试按钮
	
	// ------------------------------------------------------- Constructors
	public ListViewAdapter(int layoutId,
			OnAdapterListener<T> onAdapterListener) {
		this.layoutId = layoutId;
		this.onAdapterListener = onAdapterListener;
	}
	
	public ListViewAdapter(int layoutId) {
		this.layoutId = layoutId;
	}

	// ------------------------------------------------------- 重写父类方法
	@Override
	public int getCount() {
		return mList != null ? mList.size() : 0;
	}

	@Override
	public T getItem(int position) {
		return (mList != null && position < mList.size()) ? mList
				.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = initConvertView();
		}
		// -- 
		holder = (ViewHolder)convertView.getTag();
		
		if (onAdapterListener == null) {
			throw new NullPointerException("getViewListener can not be null!");
		}
		T data = getItem(position);
		
		//绑定数据和事件 
		onAdapterListener.onRenderItem(this,holder, data);
		
		/**
		 * 分页获取数据，执行以下代码
		 * 
		 * 列表的最后一项，并且不是最后一页
		 */
		if (position == getCount() - 1 && isExistPage && pgInfo != null
				&& !pgInfo.lastTag) {
			if (this.onAdapterListener != null && isThreadSafe) {
				isThreadSafe = false;
				this.onAdapterListener.onLoadPage(this, pgInfo.nextStartIndex,
						PAGE_SIZE);
			}
		}
		return convertView;
	}

	// ------------------------------------------------------ AsyncTask callback
	/**
	 * 后台进程执行完成，获取数据成功
	 * @param dto 
	 */
	public void onTaskSucceed(AdapterDto<T> dto){
		try {
			isThreadSafe = true; //后台进程执行完成，标志位设为true
			
			if(dto == null){
				return;
			}
			pgInfo = dto.getPageInfo();
			if( isExistPage && pgInfo == null ){
				if(pBar != null) pBar.setVisibility(View.GONE);
				if(tvMsg != null){
					tvMsg.setVisibility(View.VISIBLE);
					tvMsg.setText("数据加载异常");
				}
				if(btnRetry != null)btnRetry.setVisibility(View.GONE);
				
			}
			
			// 控制footerView的显示
			if(!isExistPage){  //不需要分页
				if(!isFirstInflate){ //不是首次加载
//					listView.removeFooterView(footerView);
					listView.removeFooterView((View)listView.getTag(R.id.listviewadapter_key));
					
				}
			}else{ // 需要分页
				if(pgInfo != null && pgInfo.lastTag){ //最后一页
//					listView.removeFooterView(footerView);
					listView.removeFooterView((View)listView.getTag(R.id.listviewadapter_key));
				}
			}
			
			addList(dto.getList());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 后台进程执行完成，获取数据失败
	 * @param dto
	 */
	public void onTaskFail(){
		isThreadSafe = true;
		if(tvMsg != null) tvMsg.setVisibility(View.GONE);
		if(pBar != null) pBar.setVisibility(View.GONE);
		if(btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
	}
	
	// ------------------------------------------------------- getters and setters
	public List<T> getmList() {
		return mList;
	}

	public void setmList(List<T> mList) {
		this.mList = mList;
	}
	
	public Context getmCtx() {
		return mCtx;
	}

	public void setmCtx(Context mCtx) {
		if (mCtx == null) {
			return;
		}
		this.mCtx = mCtx;
	}

	public int getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	public LayoutInflater getmInflater() {
		return mInflater;
	}

	public void setmInflater(LayoutInflater mInflater) {
		this.mInflater = mInflater;
	}

	public boolean isExistPage() {
		return isExistPage;
	}

	public void setExistPage(boolean isExistPage) {
		this.isExistPage = isExistPage;
	}

	public PgInfo getPgInfo() {
		return pgInfo;
	}

	public void setPgInfo(PgInfo pgInfo) {
		this.pgInfo = pgInfo;
	}

	public OnAdapterListener<T> getOnAdapterListener() {
		return onAdapterListener;
	}

	public void setOnAdapterListener(OnAdapterListener<T> onAdapterListener) {
		this.onAdapterListener = onAdapterListener;
	}

	public ListView getListView() {
		return listView;
	}

	/**
	 * <p>用法如下：</p>
	 * <pre>
	 * 	ListViewAdapter foo = new ListViewAdapter();
	 * 	foo.setFooterView();
	 * 	foo.setExistPage();
	 * 	foo.setListView();
	 * </pre>
	 * 
	 * @param listView
	 */
	public void setListView(ListView listView) {
		this.listView = listView;
		View view = (View)listView.getTag(R.id.listviewadapter_key);
		if (view!=null) {
			listView.removeFooterView(view);
		}
		
		// 第一次加载的时候，显示加载框
		if(isFirstInflate){
			if(listView.getFooterViewsCount() == 0){
				if(this.footerView != null){
					this.listView.addFooterView(this.footerView);
				}else{
					footerView = this.getFooterView();
					this.listView.addFooterView(footerView);
				}
			}
			isFirstInflate = false; 
		}
		this.listView.setAdapter(this);
		this.listView.setTag(R.id.listviewadapter_key, footerView);
		
		// 首次进来加载数据
		if(mList == null){
			loadingPage();
		}else{
			this.listView.removeFooterView(this.footerView);
		}
	}

	void loadingPage(){
		if(pgInfo == null){
			pgInfo = new PgInfo();
		}
		this.onAdapterListener.onLoadPage(this, pgInfo.nextStartIndex, PAGE_SIZE);
	}
	/**
	 * 设置数据源	
	 * @param mList
	 * @see OnAdapterListener#onLoadPage()
	 */
	public void setList(List<T> mList) {
		if(mList == null){
			return;
		}
		if(this.mList == null){
			this.mList = new ArrayList<T>();
		}
		this.mList = mList;
		notifyDataSetChanged();
	}

	/**
	 * 增加数据源	
	 * @param mList
	 * @see OnAdapterListener#onLoadPage()
	 */
	public void addList(List<T> mList){
		if(mList == null){
			return;
		}
		if(this.mList == null){
			this.mList = new ArrayList<T>();
		}
		this.mList.addAll(mList);
		notifyDataSetChanged();
	}
	
	/**
	 * 必须在 setListView()之前调用
	 * @see ListViewAdapter#setListView() 
	 * @param footView
	 */
	public void setFooterView(View footerView){
		if(footerView == null){
			this.footerView = getFooterView();
		}
	}
	
	/**
	 * 默认的footView
	 */
	public View getFooterView(){
		if(mInflater == null){
			mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			footerView = mInflater.inflate(R.layout.loading_msg_item, null);
			pBar = (ProgressBar) footerView.findViewById(R.id.pBar);
			tvMsg = ((TextView)footerView.findViewById(R.id.tvMsg));
			btnRetry = (Button)footerView.findViewById(R.id.btnRetry);
			tvMsg.setText(createMsg());
			
			btnRetry.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// 重设footerView
					btnRetry.setVisibility(View.GONE);
					tvMsg.setVisibility(View.VISIBLE);
					tvMsg.setText(createMsg());
					pBar.setVisibility(View.VISIBLE);
					
					// 加载下一页
					loadingPage();
				}
			});
		}
		footerView.setClickable(false);
		return footerView;
	}
	
	/**
	 * 重置方法
	 */
	public void reset(){
		mList = null;
		isExistPage = true;
		listView.removeFooterView(footerView);
		isFirstInflate = true;
		isThreadSafe = true;
		pgInfo = null;
	}
	// ------------------------------------------------------- 内部类
	/**
	 * 
	 * @author nieyinyin
	 *
	 * @param <T>
	 */
	public static class AdapterDto<T>{
		//fields
		private List<T> list;
		private PgInfo pageInfo;
		
		//getters and setters
		public List<T> getList() {
			return list;
		}
		public void setList(List<T> list) {
			this.list = list;
		}
		public PgInfo getPageInfo() {
			return pageInfo;
		}
		public void setPageInfo(PgInfo pageInfo) {
			this.pageInfo = pageInfo;
		}
		
	}
	
	/**
	 * 加载数据监听
	 * @author nieyinyin
	 *
	 * @param <T>
	 */
	public static interface OnAdapterListener<T>{
		/**
		 * 绑定数据、事件到控件
		 * @param holder
		 * @param data
		 */
		public void onRenderItem(ListViewAdapter<T> adapter,ViewHolder holder,T data);
		
		/**
		 * 加载数据监听
		 * @param adapter
		 * @param startIndex
		 * @param pageSize
		 */
		public void onLoadPage(ListViewAdapter<T> adapter, int startIndex, int pageSize);
	}
	
	// ---------------------------------------------------------- 内部使用
	/**
	 * 初始化ViewHolder
	 */
	private View initConvertView() {
		if(mInflater == null){
			mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		ViewGroup viewGroup = (ViewGroup) mInflater.inflate(layoutId, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.getViewMap().put(viewGroup.getId(), viewGroup);
		
		// 递归获取所有的child
		getChildView(viewHolder,viewGroup);
		
		viewGroup.setTag(viewHolder);
		
		
		
		
		return viewGroup;
	}
	
	/**
	 * 
	 * @param parent
	 */
	private void getChildView(ViewHolder viewHolder,ViewGroup parent){
		int childCount = parent.getChildCount();
		if(childCount == 0){
			return;
		}
		for (int i = 0; i < childCount; i++) {
			try {
				ViewGroup child = (ViewGroup) parent.getChildAt(i);
				viewHolder.getViewMap().put(child.getId(), child);
				getChildView(viewHolder,child);
			} catch (ClassCastException e) {  // View 转成ViewGroup会报错吗？
				View child = parent.getChildAt(i);
				viewHolder.getViewMap().put(child.getId(), child);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 消息
	 * @return
	 */
	private String createMsg() {
		String msg = "";
		if (!ActivityUtil.isNetWorkAvailable(mCtx)) {// 检查网络是否连通
			// 没有网络时
			msg = "网络连接错误";
		} else {
			msg = mCtx.getString(R.string.text_info_loading);
		}
		return msg;
	}
}

