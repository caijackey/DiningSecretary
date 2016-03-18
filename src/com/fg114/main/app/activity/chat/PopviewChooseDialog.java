package com.fg114.main.app.activity.chat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.util.ViewUtils;

/**
 * @author caijie
 * 
 */
public class PopviewChooseDialog extends Dialog {
	private Context context;
	private Button hideButton;
	private Button closeButton;
	private Button cancelButton;
	private XiaomishuChat.FscadeListen fscade;

	public PopviewChooseDialog(final Context context,
			XiaomishuChat.FscadeListen listen) {
		super(context);

		fscade = listen;
		this.context = context;
		this.setContentView(R.layout.chat_choose_layout);

		// this.setCanceledOnTouchOutside(true);
		this.getWindow().setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		this.getWindow().setGravity(Gravity.CENTER);
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));

		hideButton = (Button) findViewById(R.id.hideId);
		closeButton = (Button) findViewById(R.id.closeId);
		cancelButton = (Button) findViewById(R.id.cancelId);

		hideButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				dismiss();
				fscade.hideChatdialog();

			}
		});

		closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				dismiss();
				fscade.exitChatDialog();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				dismiss();
			}
		});
	}

}
