package com.fg114.main.util;



/** 
 * 自动更新
 * @author zhangyifan
 *
 */
public class AppUpdateUtil {   
	
//	private static final String TAG = "AppUpdateUtil"; 
//	private static final boolean DEBUG = Settings.DEBUG;
//	
//	private static final String TYPE_APK = "application/vnd.android.package-archive";
//	
//
//	private String fileEx = "";   
//    private String fileNa = "";   
// 
//    private String currentFilePath = "";   
//    private String currentTempFilePath = "";   
//      
//    private static ProgressDialog dialog;   
//    
//    /**
//     * 更新
//     */
//    public static void updateVersion(Context context, VersionChkDTO dto, String url, String msg) {   
//        if (!ActivityUtil.isNetWorkAvailable(context)) {
//        	//没有网络，不更新
//            return;   
//        } else {
//        	showUpdateDialog(context, dto, url, msg); 
//        }
//    }   
//    
//    /**
//     * 显示更新对话框
//     */
//    private static void showUpdateDialog(final Context context, VersionChkDTO dto, String url, String msg) { 
//    	
//    	DialogUtil.showAlert(context, true, msg, 
//    			new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						//确定
//						downloadTheFile(strURL);   
//						showWaitDialog(context);   
//					}
//				},
//				new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						//取消
//                        dialog.cancel();   
//					}
//				});
//    }   
//    
//    public static void showWaitDialog(Context context) {   
//        dialog = new ProgressDialog(context);   
//        dialog.setMessage(context.getString(R.string.text_info_loading_for_new_apk));   
//        dialog.setIndeterminate(true);   
//        dialog.setCancelable(false);   
//        dialog.show();   
//    }   
//    
//    /**
//     * 下载文件
//     * @param strPath
//     * @param url
//     */
//    private void downloadTheFile(Context context, String url, String path) {
//    	//获得文件名
//        fileEx = url.substring(url.lastIndexOf(".") + 1, url.length()).toLowerCase();   
//        fileNa = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));   
//        try {   
//            if (path.equals(currentFilePath)) {   
//                doDownloadTheFile(path);   
//            }   
//            currentFilePath = strPath;   
//            Runnable r = new Runnable() {   
//                public void run() {   
//                    try {   
//                        doDownloadTheFile(context, strPath);   
//                    } catch (Exception e) {   
//                        Log.e(TAG, e.getMessage(), e);   
//                    }   
//                }   
//            };   
//            new Thread(r).start();   
//        } catch (Exception e) {   
//            e.printStackTrace();   
//        }   
//    }   
//    
//    /**
//     * 下载新的apk
//     * @param strPath  apk下载地址
//     * @throws Exception
//     */
//    private void doDownloadTheFile(Context context, String url) throws Exception {   
//    	
//        if (DEBUG) Log.d(TAG, "doDownloadTheFile()");   
//        if (!URLUtil.isNetworkUrl(url)) {   
//        	if (DEBUG) Log.d(TAG, "doDownloadTheFile() It's a wrong URL!");   
//            return;
//        } else {   
//            URL myURL = new URL(url);   
//            URLConnection conn = myURL.openConnection();   
//            conn.connect();   
//            InputStream is = conn.getInputStream();   
//            if (is == null) { 
//            	//服务器端apk不存在的场合
//                throw new RuntimeException("");   
//            }   
//            File myTempFile = File.createTempFile(fileNa, "." + fileEx);   
//            currentTempFilePath = myTempFile.getAbsolutePath();   
//            FileOutputStream fos = new FileOutputStream(myTempFile);   
//            byte buf[] = new byte[128];   
//            do {   
//                int numread = is.read(buf);   
//                if (numread <= 0) {   
//                    break;   
//                }   
//                fos.write(buf, 0, numread);   
//            } while (true);   
//            if (DEBUG) Log.d(TAG, "doDownloadTheFile() Download  ok...");   
//            dialog.cancel();   
//            dialog.dismiss();   
//            openFile(myTempFile, context);   
//            try {   
//                is.close();   
//            } catch (Exception ex) {   
//                Log.e(TAG, "doDownloadTheFile() error: " + ex.getMessage(), ex);   
//            }   
//        }   
//    }   
//    
//    /**
//     * 安装apk
//     * @param f
//     */
//    private void openFile(File f, Context context) {   
//        Intent intent = new Intent();   
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
//        intent.setAction(android.content.Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(f), TYPE_APK);   
//        context.startActivity(intent);   
//    }   
//    
//    /**
//     * 删除安装文件
//     */
//    public void delFile() {   
//        Log.i(TAG, "The TempFile(" + currentTempFilePath + ") was deleted.");   
//        File myFile = new File(currentTempFilePath);   
//        if (myFile.exists()) {   
//            myFile.delete();   
//        }   
//    }   

} 