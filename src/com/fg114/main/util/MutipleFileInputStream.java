package com.fg114.main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.fg114.main.app.Settings;
import com.fg114.main.app.data.ImageData;


/**
 * 支持多文件输入的inputStream
 * @author dengxiangyu
 *
 */
public class MutipleFileInputStream extends InputStream{

	private int readedCount;
	private int mark;
	private int available;
	private StringBuilder imageSize = new StringBuilder(256);
	private StringBuilder imageDescription = new StringBuilder(512);
	// 临时文件
	private String targetPic = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY + File.separator + "MutipleFileInputStream";
	private InputStream inner;

	public MutipleFileInputStream(ArrayList<ImageData> imageDataList) {
		ArrayList<String> tNames = new ArrayList<String>();
		ArrayList<Long> tSizes = new ArrayList<Long>();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(targetPic);
			// “描述”数据块
			for (int i = 0; i < imageDataList.size(); i++) {
				imageDescription.append(imageDataList.get(i).description.replace('|', '｜') + "|");
			}
			if (imageDescription.length() > 0) {
				imageDescription.deleteCharAt(imageDescription.length() - 1);
			}
			byte[] bdesc = imageDescription.toString().getBytes("utf-8");
			fout.write(bdesc);
			imageSize.append(bdesc.length);
			// ------
			for (int i = 0; i < imageDataList.size(); i++) {
				String fileName = imageDataList.get(i).imagePath;
				File f = new File(fileName);
				if (f.exists() && f.isFile() && f.canRead() && f.length() > 0) {
					FileInputStream fin = new FileInputStream(f);
					tNames.add(fileName);
					tSizes.add(f.length());
					available += f.length();
					byte[] data = new byte[(int) f.length()];
					int count = fin.read(data);
					fout.write(data);
					// --------------------
					imageSize.append(";" + f.length());

				}
			}
			fout.close();
			inner = new FileInputStream(targetPic);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getImageSizeList() {
		return imageSize.toString();
	}

	@Override
	public int read() throws IOException {
		int r = inner.read();
		return r;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int r = inner.read(b);
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = inner.read(b, off, len);
		return r;
	}

	@Override
	public long skip(long n) throws IOException {
		return inner.skip(n);
	}

	@Override
	public int available() throws IOException {
		return inner.available();
	}

	@Override
	public void close() throws IOException {
		inner.close();
	}

	@Override
	public void mark(int readlimit) {
		inner.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		inner.reset();
	}

	@Override
	public boolean markSupported() {
		return inner.markSupported();
	}

}
