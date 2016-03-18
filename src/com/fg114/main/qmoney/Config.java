package com.fg114.main.qmoney;

public class Config {

	/**
	 * 商户配置参数
	 * @author wangjun
	 * 2013-12-2
	 */
		
		// 集成测试环境
//		public static final String MEMBERCODE = "10011548156";
//		public static final String MERCHANTID = "999923124421312";
//		public static final String URL_TEST_SERVER = "https://218.242.247.11:8441/payment";
//		public static final String PRIVATE_KEY_PATH = "PrivateKey_jckf.pem";
		
		// MOCK环境
		// public static final String MEMBERCODE = "10011548156";
		// public static final String MERCHANTID = "999923124421312";
		// public static final String URL_TEST_SERVER =
		// "https://218.242.247.6:13443/payment";
		// public static final String PRIVATE_KEY_PATH = "keys/PrivateKey_Mock.pem";
		
	// 生产环境
		 public static final String MEMBERCODE = "10022450592";
		 public static final String MERCHANTID = "812310054110022";
		 public static final String URL_TEST_SERVER =
		 "https://sandbox.99bill.com:8441/payment";
		 //PrivateKey_jckf.pem为测试证书  正式证书为：
		 public static final String PRIVATE_KEY_PATH =
		 "PrivateKey_jckf.pem";
		
	// NewSandbox环境
//		 public static final String MEMBERCODE = "10012139869";
//		 public static final String MERCHANTID = "104110045110202";
//		 public static final String URL_TEST_SERVER =
//		 "https://218.242.247.3:8441/payment";
//		 public static final String PRIVATE_KEY_PATH =
//		 "PrivateKey_NewSandbox.pem";
		
	// 开发环境
	//   public static final String MEMBERCODE = "11029832128";
	//   public static final String MERCHANTID = "999002311233255";
	//   public static final String URL_TEST_SERVER = "https://218.242.247.7:11443/payment";
	//   public static final String PRIVATE_KEY_PATH = "PrivateKey_Develop.pem";
		
		
	}

