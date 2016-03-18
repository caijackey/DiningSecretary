package com.fg114.main.app.activity;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.os.Handler.Callback;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup.*;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.*;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.adapter.CityAdapter.ViewHolder;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;

/**
 * 选取多张照片
 * 
 * @author xujianjun, 2013-10-30
 */
public class SelectMultiplePictureActivity extends MainFrameActivity {

	private CityListDTO cityListDTO;

	private List<CityData> cityList = new ArrayList<CityData>(); // 城市列表
	private View contentView;
	private GridView picture_grid;
	private BaseAdapter adapter;
	private Button button_ok;
	//最大选择数量
	private int maxSelectedCount = 20;
	private int currentSelectedCount = 0;
	//最大允许图片选择数量，key
	public static final String KEY_MAX_ALLOWED_COUNT = "KEY_MAX_ALLOWED_COUNT";
	//当前已选择数量，key
	public static final String KEY_CURRENT_COUNT = "KEY_CURRENT_COUNT";
	/**
	 * 存放图片数据的二维表
	 * 每行依次：图id，图绝对路径，是否选中:1选中，0未选中
	 */
	public ArrayList<String[]> picture_data = new ArrayList<String[]>(256);
	public ArrayList<String[]> picture_data_selected = new ArrayList<String[]>(256);
	//当前屏幕里选中的ViewHolder
	public ArrayList<PictureAdapter.ViewHolder> picture_selected = new ArrayList<PictureAdapter.ViewHolder>(256);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		// OpenPageDataTracer.getInstance().enterPage("全部城市", "");
		// ----------------------------
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			maxSelectedCount = bundle.getInt(KEY_MAX_ALLOWED_COUNT, maxSelectedCount);
			currentSelectedCount = bundle.getInt(KEY_CURRENT_COUNT, currentSelectedCount);
		}

		// 初始化界面
		initComponent();
		executeGetPictureTask();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		// OpenPageDataTracer.getInstance().enterPage("全部城市", "");
		// ----------------------------
	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("相机胶卷");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getBtnOption().setText("取消");
		this.setFunctionLayoutGone();

		// 内容部分
		contentView = View.inflate(this, R.layout.select_mutiple_picture, null);
		picture_grid = (GridView) contentView.findViewById(R.id.picture_grid);
		button_ok = (Button) contentView.findViewById(R.id.button_ok);
		//--
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (picture_data_selected.size() == 0) {
					DialogUtil.showAlert(SelectMultiplePictureActivity.this, "提示", "请至少选择一张照片！");
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("picture_data_selected", picture_data_selected);
				setResult(-9999, intent);
				finish();
			}
		});
		//--
//		this.getBtnOption().setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				finish();
//			}
//		});
		this.getMainLayout().addView(contentView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		// ----
		adapter = new PictureAdapter();
		picture_grid.setAdapter(adapter);
	}

	//获取图片数据
	private void executeGetPictureTask() {
		new GetPictureTask("正在读取图片...").execute(new Runnable() {

			@Override
			public void run() {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
			}
		}, new Runnable() {

			@Override
			public void run() {
				finish();
			}
		});
	}

	// -------------------------------------
	class PictureAdapter extends BaseAdapter {

		class ViewHolder {
			public LinearLayout picture_container; // 图片容器
			public MyImageView picture; // 图片
			public TextView picture_selected_mark; // 选中标记
			public String[] data; //图片数据
		}

		//--
		int itemSide = (UnitUtil.getScreenWidthPixels() - 4 * UnitUtil.dip2px(5)) / 3;

		@Override
		public View getView(final int position, View convertView, ViewGroup viewgroup) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(SelectMultiplePictureActivity.this, R.layout.list_item_select_multiple_picture, null);
				holder.picture_container = (LinearLayout) convertView.findViewById(R.id.picture_container);
				holder.picture = (MyImageView) convertView.findViewById(R.id.picture);
				holder.picture.isThumbnail = true;
				holder.picture_selected_mark = (TextView) convertView.findViewById(R.id.picture_selected_mark);
				//设置大小
				holder.picture_container.setLayoutParams(new RelativeLayout.LayoutParams(itemSide, itemSide));
//				holder.picture_selected_mark.setLayoutParams(new RelativeLayout.LayoutParams(itemSide, itemSide));
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final String[] data = picture_data.get(position);
			holder.data = data;
			//
			holder.picture.setImageByUrl(data[1], true, position, ScaleType.FIT_XY);
			picture_selected.remove(holder);
			if ("1".equals(data[2])) {
				holder.picture_selected_mark.setVisibility(View.VISIBLE);
				//设置选择的索引数字
				int index = picture_data_selected.indexOf(data) + 1;
				holder.picture_selected_mark.setText(index + "");
				picture_selected.add(holder);//加入
			} else {
				holder.picture_selected_mark.setVisibility(View.INVISIBLE);
				picture_selected.remove(holder);
			}
			holder.picture.setTag(holder);
			holder.picture.setOnClickListener(listener);
			convertView.setTag(holder);
			//测试机，长按显示图片地址
			if (ActivityUtil.isTestDev(ContextUtil.getContext())) {
				holder.picture.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {

						DialogUtil.showAlert(getCurrentTopActivity(), "" + position, data[1]);
						return true;
					}
				});
			}
			return convertView;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public Object getItem(int i) {
			if (picture_data != null && i >= 0 && i < picture_data.size()) {
				return picture_data.get(i);
			}
			return null;
		}

		@Override
		public int getCount() {
			return picture_data.size();
		}

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 200);
				ViewHolder holder = (ViewHolder) v.getTag();
				if (holder == null) {
					return;
				}
				//当前是选中的，取消选中
				if ("1".equals(holder.data[2])) {
					holder.picture_selected_mark.setVisibility(View.INVISIBLE);
					holder.data[2] = "0";
					currentSelectedCount--;
					picture_selected.remove(holder);//移除文字
					picture_data_selected.remove(holder.data);

				} else {
					if (currentSelectedCount >= maxSelectedCount) {
						DialogUtil.showAlert(getCurrentTopActivity(), "提示", "已达最大选择数量!");
						return;
					}
					holder.picture_selected_mark.setVisibility(View.VISIBLE);
					holder.data[2] = "1";
					currentSelectedCount++;
					picture_selected.add(holder);//加入文字
					picture_data_selected.add(holder.data);
				}
				syncCount();
			}

			private void syncCount() {
				//刷新选中TextView的序号文字
				for (PictureAdapter.ViewHolder holder : picture_selected) {
					int index = picture_data_selected.indexOf(holder.data) + 1;
					holder.picture_selected_mark.setText(index + "");
				}
				//---
				if (currentSelectedCount > 0) {
					button_ok.setText("完成 (" + currentSelectedCount + "/" + maxSelectedCount + ")");
				} else {
					button_ok.setText("完成");
				}

			}
		};
	}

	// 借用task来异步获取图片信息
	class GetPictureTask extends BaseTask {
		public GetPictureTask(String preDialogMessage) {
			super(preDialogMessage, SelectMultiplePictureActivity.this);
			picture_data.clear();
			picture_data_selected.clear();
		}

		@Override
		public JsonPack getData() throws Exception {

			JsonPack jp = new JsonPack();
			jp.setRe(200);
			// ------------------------------
			try {
				String str[] = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };

				Cursor cursor = SelectMultiplePictureActivity.this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, str,
				//MediaStore.Images.Media.SIZE+">102400 or "+MediaStore.Images.Media.SIZE+" is null",
						null,
						//null, 
						null, MediaStore.Images.Media.DATE_ADDED + " DESC," + MediaStore.Images.Media.SIZE + " DESC");

				while (cursor.moveToNext()) {
					long size = 0;
					try {
						size = Long.parseLong(cursor.getString(1));
					} catch (Exception e) {
						//e.printStackTrace();
					}
					//如果转换失败，尝试直接从文件中获取文件大小
					if (size == 0) {
						try {
							File f = new File(cursor.getString(1));
							if (f.exists() && f.isFile() && f.length() >= 512000) {
								size = f.length();
							}
						} catch (Exception e) {
							//e.printStackTrace();
						}
					}
					if (size > 0) {
						picture_data.add(new String[] { cursor.getString(0), cursor.getString(1), "0" });
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				jp.setRe(300);
				jp.setMsg(e.getMessage());
			}
			// ------------------------------
			return jp;
		}

		@Override
		public void onPreStart() {
		}

		@Override
		public void onStateFinish(JsonPack result) {
		}

		@Override
		public void onStateError(JsonPack result) {
			DialogUtil.showToast(context, "获取图片数据时失败，请稍后重试!" + (result != null ? result.getMsg() : ""));
		}
	}

}
