package com.fg114.main.app.activity.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

import com.fg114.main.app.Settings;
import com.fg114.main.app.view.ChatWebView;
import com.fg114.main.util.ZipUtils;

/**
 * 异步下载
 * @author caijie
 *
 */
public class DownloadAsyncTask extends AsyncTask<String, Integer, String>{
	private ChatWebView.ReloadURL fscade;
	
	public DownloadAsyncTask(ChatWebView.ReloadURL event){
		super();
		fscade = event;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String src = Settings.LOCAL_HTML_PATH + Settings.zipPackageName;
		String dest = Settings.LOCAL_HTML_PATH + Settings.ZIP_PATH;
		
		//0.download
		downloadZip(params[0]);
		
		//1.delete direct
//		File destDir = new File(dest);
//		if(destDir.exists())
//			destDir.delete();
		
		//2.unzip
		try {
			ZipUtils.unZipFiles(src, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//3.delete zip
		File zipFile = new File(src, Settings.zipPackageName);
		if(zipFile.exists())
			zipFile.delete();
		
		return null;
	}
	
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		fscade.startReloadUrlParmas();
	}

	/**
	 * download zip包
	 * @param urlStr
	 */
	private void downloadZip(String urlStr) {
		OutputStream output = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			//dir:xiaomishu
			File filePath = new File(Settings.LOCAL_HTML_PATH);
			if (!filePath.exists())
				filePath.mkdir();
			//file
			String pathName = Settings.LOCAL_HTML_PATH + Settings.zipPackageName;
			File file = new File(pathName);
			InputStream input = conn.getInputStream();
			if (file.exists()) {
				file.delete();
			}
			
			file.createNewFile();
			output = new FileOutputStream(file);
			// 读取大文件
			byte[] buffer = new byte[4 * 1024];
			while (input.read(buffer) != -1) {
				output.write(buffer);
			}
			output.flush();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
