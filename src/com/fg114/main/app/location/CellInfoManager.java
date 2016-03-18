package com.fg114.main.app.location;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

/**
 * 基站信息
 * @author zhangyifan
 *
 */
public class CellInfoManager {
	
//	private Context context;
	//基站状态监听
    private final PhoneStateListener listener;
    //电话管理
    private TelephonyManager telManager;
    //是否是CDMA
    private boolean isCdma;
    //是否是GSM
    private boolean isGsm;
    //信号情况
	private int asu;
	//gsm基站Id
	private int cid;
	//基站区域Code
	private int lac;
    //移动国家码
    private int mcc;
    //移动网络代码
    private int mnc;
    // cdma的场合用
    //cdma基站id
    private int bid;
    //cdma系统id
    private int sid;
    //cdma网络id
    private int nid;
    //是否已更新
    private boolean valid;
    
    public boolean isAvailable = true;

	public CellInfoManager(Context context) {
		this.listener = new CellInfoListener(this);
		this.telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (this.telManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
			isAvailable = false;
		} else {
			//监听位置和信号状态
			this.telManager.listen(this.listener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
		}
	}
	
    /**
     * 更新基站信息
     */
    private void update() throws Exception {
 	   
 	   this.isGsm = false;
 	   this.isCdma = false;
 	   this.cid = 0;
 	   this.lac = 0;
 	   this.mcc = 0;
 	   this.mnc = 0;
 	   //获得电话类型
 	   int nPhoneType = this.telManager.getPhoneType();
 	   if (nPhoneType == TelephonyManager.PHONE_TYPE_GSM) {
 		   //gsm的场合
 		   this.isGsm = true;
 	 	   //获得基站信息
 		   GsmCellLocation gsmCellLocation = (GsmCellLocation) this.telManager.getCellLocation();
 		  GsmCellLocation.requestLocationUpdate();
 		   //获得基站id及基站区域id
 		   int nGSMCID = gsmCellLocation.getCid();
 		   if (nGSMCID > 0) {
 			   if (nGSMCID != 65535) {
 				   this.cid = nGSMCID;
 				   this.lac = gsmCellLocation.getLac();
 			   }
 		   }
 	   } else if (nPhoneType == TelephonyManager.PHONE_TYPE_CDMA) {
		   //cdma的场合
		   this.isCdma = true;
		   this.valid = true;
		   //获得基站信息
		   CdmaCellLocation location  = (CdmaCellLocation) this.telManager.getCellLocation();
		   //获得基站id及基站区域id
		   this.bid = location.getBaseStationId();//基站小区号  cellId
		   this.sid = location.getSystemId();//系统标识 
		   this.nid = location.getNetworkId();//网络标识  
 	   }
 	   
 	   //获得移动国家码. MCC. 移动网络代码. MNC
	   String strNetworkOperator = this.telManager.getNetworkOperator();
	   int nNetworkOperatorLength = strNetworkOperator.length();
	   if (nNetworkOperatorLength == 5 || nNetworkOperatorLength == 6) {
		   this.mcc = Integer.parseInt(strNetworkOperator.substring(0, 3));
		   this.mnc = Integer.parseInt(strNetworkOperator.substring(3, nNetworkOperatorLength));
	   } 
    }
    
    /**
     * 获得所有gsm基站列表
     * @return
     */
	private int[] dump() throws Exception {
		int[] arrayCells;
		if (getCid() == -1) {
			arrayCells = new int[0];
			return arrayCells;
		}
		//获得附近基站情报列表
		List<NeighboringCellInfo> cellInfoList = this.telManager.getNeighboringCellInfo();
		if (cellInfoList == null || cellInfoList.size() == 0) {
			//当没有邻近基站的情报的场合，列表中只放入当前基站id
			arrayCells = new int[1];
			arrayCells[0] = getCid();
			return arrayCells;
		}
		//建立临时列表 第一项放基站id之后的一项放该基站的信号状态
		int[] tempArrayCells = new int[2 + cellInfoList.size() * 2];
		//下一项存放的序号
		int index = 0;
		tempArrayCells[index] = getCid();
		index = index + 1;
		tempArrayCells[index] = getAsu();
		index = index + 1;
		//将邻近基站id及信号强度放入临时基站
		Iterator<NeighboringCellInfo> iter = cellInfoList.iterator();
		while (iter.hasNext()) {
			NeighboringCellInfo localNeighboringCellInfo = (NeighboringCellInfo) iter.next();
			
			//判断信号值是否合法
			int rssi = localNeighboringCellInfo.getRssi();
			if (rssi < 0 || rssi > 31) {
				continue;
			}
			
			int cid = localNeighboringCellInfo.getCid();
			if ((cid <= 0) || (cid == 65535)) {
				continue;
			}
			tempArrayCells[index] = cid;
			index = index + 1;
			tempArrayCells[index] = rssi;
			index = index + 1;
		}

		int[] tempArrayCells2 = new int[index];
		System.arraycopy(tempArrayCells, 0, tempArrayCells2, 0, index);
		arrayCells = tempArrayCells2;
		return arrayCells;
	}

	/**
	 * 获得信号强度
	 * @param i
	 * @return
	 */
	private int getDBm(int i) {
        int j;
        if (i >= 0 && i <= 31) {
      	  j = i * 2 - 113;
        } else {
      	  j = 0;
        }
        return j;
	}

	/**
	 * 获得信号状态
	 * @return
	 */
	private int getAsu() {
		return this.asu;
	}

	/**
     * 获得gsm基站id
     * @return
     */
	public int getCid() throws Exception {
		if (!this.valid) {
			update();
		}
		return this.cid;
	}
	
	/**
	 * 获得cdma基站id
	 * @return
	 */
	public int getBid() throws Exception {
		if (!this.valid) {
			update();
		}
		return this.bid;
	}

	/**
	 * 是否是cdma
	 * @return
	 */
	public boolean isCdma() throws Exception {
		if (!this.valid) {
			update();
		}
		return this.isCdma;
	}


	/**
	 * 是否是gsm
	 * @return
	 */
	public boolean isGsm() throws Exception {
		if (!this.valid){
			update();
		}
		return this.isGsm;
	}
	
	/**
	 * 获得gsm基站区域code
	 * @return
	 */
	public int getLac() throws Exception {
		if (!this.valid){
			update();
		}
        return this.lac;
	}
	
	/**
	 * 获得移动国家码
	 * @return
	 */
	public int getMcc() throws Exception {
		if (!this.valid){
			update();
		}
        return this.mcc;
	}

	/**
	 * 获得移动网络代码
	 * @return
	 */
	public int getMnc() throws Exception {
		if (!this.valid){
			update();
		}
        return this.mnc;
	}

	/**
	 * 获得cdma网络id
	 * @return
	 */
	public int getNid() throws Exception {
		if (!this.valid){
			update();
		}
        return this.nid;
	}
 
	/**
	 * 获得cdma系统id
	 * @return
	 */
	public int getSid() throws Exception {
		if (!this.valid){
			update();
		}
        return this.sid;
    }

    /**
     * cellTowers信息to jsonArray
     * @return
     */
	public JSONArray cellTowersToJSONArray() throws Exception {

		JSONArray jsonarray = new JSONArray();
		if (isCdma) {
			try {
				JSONObject jsonobject = new JSONObject();
				jsonobject.put("cell_id", getBid());
				jsonobject.put("location_area_code", getNid());
				jsonobject.put("mobile_country_code", getMcc());
				jsonobject.put("mobile_network_code", getSid());
				jsonobject.put("signal_strength", getDBm(1));
				jsonobject.put("age", 0);
				jsonarray.put(jsonobject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			int lac = getLac();
			int mcc = getMcc();
			int mnc = getMnc();
			int aryCell[] = dump();
			if (aryCell == null || aryCell.length < 2) {
				aryCell = new int[2];
				aryCell[0] = getCid();
				aryCell[1] = -60;
			}
	
			for (int i = 0; i < aryCell.length; i += 2) {
				try {
//					int j2 = getDBm(i + 1);
					int j2 = getDBm(aryCell[i + 1]);
					JSONObject jsonobject = new JSONObject();
					jsonobject.put("cell_id", aryCell[i]);
					jsonobject.put("location_area_code", lac);
					
					jsonobject.put("mobile_country_code", mcc);
					jsonobject.put("mobile_network_code", mnc);
					jsonobject.put("signal_strength", j2);
					jsonobject.put("age", 0);
					jsonarray.put(jsonobject);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return jsonarray;
	}

    /**
     * 基站状态监听
     * @author zhangyifan
     *
     */
	class CellInfoListener extends PhoneStateListener {

		public CellInfoListener(CellInfoManager manager) {
		
		}
		
		/**
		 * 当位置变化时调用
		 */
		@Override
		public void onCellLocationChanged(CellLocation location) {
			//状态变为未更新
			CellInfoManager.this.valid = false;
		}

		/**
		 * 当信号强度变化时调用
		 */
		@Override
		public void onSignalStrengthChanged(int asu) {
			CellInfoManager.this.asu = asu;
		}
	}
}
