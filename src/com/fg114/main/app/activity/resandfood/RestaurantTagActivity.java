package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅标签页 传入参数restaurantId toPage（1 查看标签 2打标签）
 * 
 * @author dengxiangyu
 * 
 */
public class RestaurantTagActivity extends MainFrameActivity {
	private String restaurantId; // 餐厅ID
	private int toPage = 1;
	private LayoutInflater mInflater;
	private View contextView;
	private LinearLayout res_tag_toptext;
	private LinearLayout lay_ok_bt;
	private LinearLayout restaurant_tag_layout;
	private Button okbutton;
	private ScrollView res_tag_scrollBar;
	// 缓存
	private List<CommonTypeDTO> tagListData;
	private CommonTypeDTO commonTypeDTO;
//	private boolean isAddTaguccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 获取参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		toPage = bundle.getInt("toPage");

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅标签", "");
		// ----------------------------
		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
			this.finish();
		} else {
			executeGetTagResInfoDataTask();
		}
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅标签", "");
		// ----------------------------
	}

	private void initComponent() {
		// 设置标题栏
		this.getBtnGoBack().setText(R.string.text_button_back);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_tag_act, null);

		res_tag_toptext = (LinearLayout) contextView.findViewById(R.id.res_tag_toptext);
		lay_ok_bt = (LinearLayout) contextView.findViewById(R.id.lay_ok_bt);
		restaurant_tag_layout = (LinearLayout) contextView.findViewById(R.id.restaurant_tag_layout);
		okbutton = (Button) contextView.findViewById(R.id.okbutton);
		res_tag_scrollBar=(ScrollView) contextView.findViewById(R.id.res_tag_scrollBar);
		

		if (toPage == 2) {
			this.getTvTitle().setText("打标签");
			this.getBtnOption().setText("新增标签");
			res_tag_toptext.setVisibility(View.VISIBLE);
			lay_ok_bt.setVisibility(View.VISIBLE);
//			res_tag_scrollBar.setPadding(0, 0, 0, UnitUtil.dip2px(30));
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
			lp.setMargins(0, 0, 0, UnitUtil.dip2px(43));
			res_tag_scrollBar.setLayoutParams(lp);
		} else {
			this.getTvTitle().setText("餐厅标签");
			this.getBtnOption().setVisibility(View.INVISIBLE);
			res_tag_toptext.setVisibility(View.GONE);
			lay_ok_bt.setVisibility(View.GONE);
		}

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				showDialog("我来说个新标签");
			}
		});

		okbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				// executePostTagResInfoDataTask();
				ViewUtils.preventViewMultipleClick(view, 1000);
					executePostTagResInfoDataTask(getListTagSelect(tagListData));
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/**
	 * 获取标签列表
	 */
	private void executeGetTagResInfoDataTask() {
		ServiceRequest request1 = new ServiceRequest(ServiceRequest.API.getRestLabelList);
		// 是否用于添加修改标签
		if(toPage == 2){
		request1.addData("forPostTag", true);
		}else{
		request1.addData("forPostTag", false);
		}
		request1.addData("restId", restaurantId);
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		CommonTask.request(request1, "正在获取标签信息，请等待...", new CommonTask.TaskListener<CommonTypeListDTO>() {

			@Override
			protected void onSuccess(CommonTypeListDTO dto) {
				// TODO Auto-generated method stub
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				tagListData = new ArrayList<CommonTypeDTO>();
				tagListData = dto.getList();
				if (dto.getList() == null) {
					// 标签列表为空 取消标签框

				} else {
					refreshUI(dto.getList());
				}

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
//				doTest();
				finish();
			}

			private void doTest() {
				String json = "{\"uuid\":\"12345\",\"name\":\"jio\",\"selectTag\":\"true\",\"list\":[{\"uuid\":\"1234\",\"parentId\":\"11111\",\"name\":\"看江景啊啊\",\"num\":\"131\",\"succTag\":\"true\",\"phone\":\"1231123\",\"memo\":\"1234\",\"selectTag\":\"false\",\"isFirst\":\"true\",\"keywords\":\"a\",\"firstLetters\":\"a\",\"firstLetter\":\"a\"},{\"uuid\":\"1235\",\"parentId\":\"11111\",\"name\":\"江景\",\"num\":\"28\",\"succTag\":\"true\",\"phone\":\"1231123\",\"memo\":\"1234\",\"selectTag\":\"true\",\"isFirst\":\"true\",\"keywords\":\"a\",\"firstLetters\":\"a\",\"firstLetter\":\"a\"},{\"uuid\":\"1236\",\"parentId\":\"11111\",\"name\":\"看江景\",\"num\":\"1311\",\"succTag\":\"true\",\"phone\":\"1231123\",\"memo\":\"1234\",\"selectTag\":\"true\",\"isFirst\":\"true\",\"keywords\":\"a\",\"firstLetters\":\"a\",\"firstLetter\":\"a\"},{\"uuid\":\"1237\",\"parentId\":\"11111\",\"name\":\"江景\",\"num\":\"28\",\"succTag\":\"false\",\"phone\":\"1231123\",\"memo\":\"1234\",\"selectTag\":\"false\",\"isFirst\":\"true\",\"keywords\":\"a\",\"firstLetters\":\"a\",\"firstLetter\":\"a\"}]}";
				CommonTypeListDTO dto = JsonUtils.fromJson(json, CommonTypeListDTO.class);
				onSuccess(dto);
			}
		});

	}

	/**
	 * 新增标签对话框
	 */

	public void showDialog(String title) {
		DialogUtil.showDialog(RestaurantTagActivity.this, R.layout.add_tag_dialog_layout, new DialogUtil.DialogEventListener() {

			@Override
			public void onInit(View contentView, final PopupWindow dialog) {
				// TODO Auto-generated method stub
				TextView add_tag_title = (TextView) contentView.findViewById(R.id.add_tag_title);
				final EditText add_tag_content = (EditText) contentView.findViewById(R.id.add_tag_content);
				Button add_tag_no = (Button) contentView.findViewById(R.id.add_tag_no);
				Button add_tag_ok = (Button) contentView.findViewById(R.id.add_tag_ok);

				add_tag_title.setText("我来说个新标签");
				
				

				add_tag_ok.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(view, 1000);
						String input_message = add_tag_content.getText().toString().trim();

						if (TextUtils.isEmpty(input_message)) {
							DialogUtil.showToast(RestaurantTagActivity.this, "输入的信息不能为空");
						} else if (input_message.length() > 4) {
							DialogUtil.showToast(RestaurantTagActivity.this, "标签字数不能超过4字");
						} else {

							executePostAddTagResInfoDataTask(input_message) ;
//								refreshUI
								dialog.dismiss();
							
						}
					}

				});

				add_tag_no.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(view, 1000);
						DialogUtil.showToast(RestaurantTagActivity.this, "您取消了操作");
						dialog.dismiss();
					}
				});
			}

		});
	}

	/**
	 * 添加标签请求
	 */
	private void executePostAddTagResInfoDataTask(final String labelName) {
		ServiceRequest request = new ServiceRequest(ServiceRequest.API.addRestLabel);
		request.addData("restId", restaurantId);
		request.addData("labelName", labelName);
		// -----
		OpenPageDataTracer.getInstance().addEvent("新增按钮");
		// -----
		CommonTask.request(request, "正在添加标签，请等待...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				// TODO Auto-generated method stub
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("新增按钮");
				// -----
				
				Settings.NEED_TAG_REST_COMMENT = true;
//				isAddTaguccess = true;
				commonTypeDTO = new CommonTypeDTO();
				commonTypeDTO.setName(labelName);
				commonTypeDTO.setUuid(dto.getUuid());
				commonTypeDTO.setSelectTag(true);
				commonTypeDTO.setNum(1);
				tagListData.add(commonTypeDTO);
				restaurant_tag_layout.removeAllViews();
				refreshUI(tagListData);

			}

			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("新增按钮");
				// -----
//				isAddTaguccess = false;
				DialogUtil.showToast(RestaurantTagActivity.this, message);

				// TODO
//				doTest_confirm();

			};

			private void doTest_confirm() {
				String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
				SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
				onSuccess(data);
			}

		});
	}

	/**
	 * 提交所选标签请求
	 */
	private void executePostTagResInfoDataTask(String labelIdList) {
		ServiceRequest request = new ServiceRequest(ServiceRequest.API.postSelectRestLabel);
		request.addData("restId", restaurantId);
		request.addData("labelIdList", labelIdList);// 111;222;333
		// -----
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----
		CommonTask.request(request, new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
//				DialogUtil.showAlert(RestaurantTagActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialoginterface, int i) {
//						// TODO Auto-generated method stub
//						
//
//					};
//
//				});
				
				Settings.NEED_TAG_REST_COMMENT = true;
				finish();
			}

			protected void onError(int code, String message) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
				DialogUtil.showToast(RestaurantTagActivity.this, message);
//				doTest_confirm();
			}
			
			private void doTest_confirm() {
				String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"标签提交成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
				SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
				onSuccess(data);
			}

		});

	}

	private void refreshUI(final List<CommonTypeDTO> tagListData) {
		int length = tagListData.size();

		if (length % 2 == 0) {// 判断标签是偶
			for (int i = 0; i < length / 2; i++) {
				LayoutInflater inflater = LayoutInflater.from(RestaurantTagActivity.this);

				View view = inflater.inflate(R.layout.res_tag_click_act, null);
				final ImageButton tag_bt_one_ib = (ImageButton) view.findViewById(R.id.tag_bt_one_ib);
				final ImageButton tag_bt_two_ib = (ImageButton) view.findViewById(R.id.tag_bt_two_ib);
				TextView res_one_tag1_name = (TextView) view.findViewById(R.id.tag_bt_one_name);
				final TextView res_one_tag1_num = (TextView) view.findViewById(R.id.tag_bt_one_num);
				TextView res_one_tag2_name = (TextView) view.findViewById(R.id.tag_bt_two_name);
				final TextView res_one_tag2_num = (TextView) view.findViewById(R.id.tag_bt_two_num);

				LinearLayout tag_bt_one = (LinearLayout) view.findViewById(R.id.tag_bt_one);
				LinearLayout tag_bt_two = (LinearLayout) view.findViewById(R.id.tag_bt_two);
				
				LinearLayout tag_one_layout = (LinearLayout) view.findViewById(R.id.tag_one_layout);
				LinearLayout tag_two_layout = (LinearLayout) view.findViewById(R.id.tag_two_layout);

				if (tagListData.get(i * 2).getName() != null) {
					res_one_tag1_name.setText(textNamesize(tagListData.get(i * 2).getName()));
					res_one_tag1_num.setTextScaleX(textNumsize("+" + tagListData.get(i * 2).getNum()));
					res_one_tag1_num.setText("+" + tagListData.get(i * 2).getNum());
					// res_one_tag1_num.setTextScaleX(size)
					final int j = i;
					tag_bt_one_ib.setSelected(tagListData.get(j * 2).isSelectTag());
					if(toPage==2){
						//打标签页才能点击
					tag_bt_one.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							if (tagListData.get(j * 2).isSelectTag()) {
								tagListData.get(j * 2).setSelectTag(false);
								tag_bt_one_ib.setSelected(false);
								
								if(tagListData.get(j * 2).getNum()!=0){
									tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()-1);
									res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
								}
							} else {
								tagListData.get(j * 2).setSelectTag(true);
								tag_bt_one_ib.setSelected(true);
								
								tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()+1);
								res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
							}
						}
					});
					}

					//

				}
				if (tagListData.get(i * 2 + 1).getName() != null) {
					res_one_tag2_name.setText(textNamesize(tagListData.get(i * 2 + 1).getName()));
					res_one_tag2_num.setTextScaleX(textNumsize("+" + tagListData.get(i * 2 + 1).getNum()));
					// res_one_tag2_num.setTextScaleX(textNumsize("+"+dto.getList().get(i
					// * 2+1).getNum()));
					res_one_tag2_num.setText("+" + tagListData.get(i * 2 + 1).getNum());
					//
					final int j = i;
					tag_bt_two_ib.setSelected(tagListData.get(j * 2 + 1).isSelectTag());
					if(toPage==2){
						//打标签页才能点击
					tag_bt_two.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							ViewUtils.preventViewMultipleClick(view, 1000);
							// TODO Auto-generated method stub
							if (tagListData.get(j * 2 + 1).isSelectTag()) {
								tagListData.get(j * 2 + 1).setSelectTag(false);
								tag_bt_two_ib.setSelected(false);
								
								if(tagListData.get(j * 2+1).getNum()!=0){
									tagListData.get(j * 2+1).setNum(tagListData.get(j * 2+1).getNum()-1);
									res_one_tag2_num.setText("+" + (tagListData.get(j * 2+1).getNum()));
								}
							} else {
								tagListData.get(j * 2 + 1).setSelectTag(true);
								tag_bt_two_ib.setSelected(true);
								
								tagListData.get(j * 2+1).setNum(tagListData.get(j * 2+1).getNum()+1);
								res_one_tag2_num.setText("+" + (tagListData.get(j * 2+1).getNum()));
							}
						}
					});
					}
				}
				if (toPage == 1) {
					tag_bt_one_ib.setVisibility(View.GONE);
					tag_bt_two_ib.setVisibility(View.GONE);
				} else {
					tag_bt_one_ib.setVisibility(View.VISIBLE);
					tag_bt_two_ib.setVisibility(View.VISIBLE);
				}
				restaurant_tag_layout.addView(view, i);

			}

		} else {// 判断标签是单
			for (int i = 0; i < length / 2 + 1; i++) {
				LayoutInflater inflater = LayoutInflater.from(RestaurantTagActivity.this);

				View view = inflater.inflate(R.layout.res_tag_click_act, null);
				final ImageButton tag_bt_one_ib = (ImageButton) view.findViewById(R.id.tag_bt_one_ib);
				final ImageButton tag_bt_two_ib = (ImageButton) view.findViewById(R.id.tag_bt_two_ib);
				TextView res_one_tag1_name = (TextView) view.findViewById(R.id.tag_bt_one_name);
				final TextView res_one_tag1_num = (TextView) view.findViewById(R.id.tag_bt_one_num);
				TextView res_one_tag2_name = (TextView) view.findViewById(R.id.tag_bt_two_name);
				final TextView res_one_tag2_num = (TextView) view.findViewById(R.id.tag_bt_two_num);

				LinearLayout tag_bt_one = (LinearLayout) view.findViewById(R.id.tag_bt_one);
				LinearLayout tag_bt_two = (LinearLayout) view.findViewById(R.id.tag_bt_two);


				// 最后一排只有一个标签
				if (i == length / 2) {
					tag_bt_two.setVisibility(View.INVISIBLE);
					if (tagListData.get(i * 2).getName() != null) {
						res_one_tag1_name.setText(textNamesize(tagListData.get(i * 2).getName()));
						res_one_tag1_num.setTextScaleX(textNumsize("+" + tagListData.get(i * 2).getNum()));
						res_one_tag1_num.setText("+" + tagListData.get(i * 2).getNum());

						final int j = i;
						tag_bt_one_ib.setSelected(tagListData.get(j * 2).isSelectTag());
						if(toPage==2){
							//打标签页才能点击
						tag_bt_one.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								if (tagListData.get(j * 2).isSelectTag()) {
									tagListData.get(j * 2).setSelectTag(false);
									tag_bt_one_ib.setSelected(false);
									
									if(tagListData.get(j * 2).getNum()!=0){
										tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()-1);
										res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
									}
								} else {
									tagListData.get(j * 2).setSelectTag(true);
									tag_bt_one_ib.setSelected(true);
									
									tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()+1);
									res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
								}
							}
						});
						}

					}

				} else {
					// 一排有2个标签
					if (tagListData.get(i * 2).getName() != null) {
						res_one_tag1_name.setText(textNamesize(tagListData.get(i * 2).getName()));
						res_one_tag1_num.setTextScaleX(textNumsize("+" + tagListData.get(i * 2).getNum()));
						res_one_tag1_num.setText("+" + tagListData.get(i * 2).getNum());
						final int j = i;
						tag_bt_one_ib.setSelected(tagListData.get(j * 2).isSelectTag());
						if(toPage==2){
							//打标签页才能点击
						tag_bt_one.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								if (tagListData.get(j * 2).isSelectTag()) {
									tagListData.get(j * 2).setSelectTag(false);
									tag_bt_one_ib.setSelected(false);
									
									if(tagListData.get(j * 2).getNum()!=0){
										tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()-1);
										res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
									}
								} else {
									tagListData.get(j * 2).setSelectTag(true);
									tag_bt_one_ib.setSelected(true);
									
									tagListData.get(j * 2).setNum(tagListData.get(j * 2).getNum()+1);
									res_one_tag1_num.setText("+" + (tagListData.get(j * 2).getNum()));
								}
							}
						});
						}

					}
					if (tagListData.get(i * 2 + 1).getName() != null) {
						res_one_tag2_name.setText(textNamesize(tagListData.get(i * 2 + 1).getName()));
						res_one_tag2_num.setTextScaleX(textNumsize("+" + tagListData.get(i * 2 + 1).getNum()));
						res_one_tag2_num.setText("+" + tagListData.get(i * 2 + 1).getNum());

						final int j = i;
						tag_bt_two_ib.setSelected(tagListData.get(j * 2 + 1).isSelectTag());
						if(toPage==2){
							//打标签页才能点击
						tag_bt_two.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								if (tagListData.get(j * 2 + 1).isSelectTag()) {
									tagListData.get(j * 2 + 1).setSelectTag(false);
									tag_bt_two_ib.setSelected(false);
									
									if(tagListData.get(j * 2 + 1).getNum()!=0){
										tagListData.get(j * 2 + 1).setNum(tagListData.get(j * 2 + 1).getNum()-1);
										res_one_tag2_num.setText("+" + (tagListData.get(j * 2 + 1).getNum()));
									}
								} else {
									tagListData.get(j * 2 + 1).setSelectTag(true);
									tag_bt_two_ib.setSelected(true);
									
										tagListData.get(j * 2 + 1).setNum(tagListData.get(j * 2 + 1).getNum()+1);
										res_one_tag2_num.setText("+" + (tagListData.get(j * 2 + 1).getNum()));
									
								}
							}
						});
						}

					}
				}
				if (toPage == 1) {
					tag_bt_one_ib.setVisibility(View.GONE);
					tag_bt_two_ib.setVisibility(View.GONE);
				} else {
					tag_bt_one_ib.setVisibility(View.VISIBLE);
					tag_bt_two_ib.setVisibility(View.VISIBLE);
				}
				restaurant_tag_layout.addView(view, i);
			}
		}

	}

	// 判断字体不超过4个字符 超过后面用...表示
	private String textNamesize(String name) {
		StringBuffer stringBuffer = new StringBuffer();
		if (name.length() > 4) {
			stringBuffer.append(name.substring(0, 4));
			stringBuffer.append("...");
		} else {
			stringBuffer.append(name);
		}
		return stringBuffer.toString();
	}

	// 当num是2位数 缩放比例1.0f num为3位数0.8f 4位数0.6
	private float textNumsize(String num) {
		float size = 1.0f;
		if (num.length() <= 3) {
			size = 1.0f;
		} else if (num.length() == 4) {
			size = 0.8f;
		} else if (num.length() == 5) {
			size = 0.6f;
		}
		return size;
	}

	// 获取标签选中list // 111;222;333
	private String getListTagSelect(List<CommonTypeDTO> tagListData) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < tagListData.size(); i++) {
			if (tagListData.get(i).isSelectTag()) {
				stringBuffer.append(tagListData.get(i).getUuid());
				stringBuffer.append(";");
			}
		}
		if(!stringBuffer.toString().equals("")){
		stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());
		}
		return stringBuffer.toString();

	}

}
