package com.oracle.play.pptviewer;

import java.util.HashMap;

public class UploadBeanListWrapper {
	
		private HashMap<Integer,UploadBean> uploadBeansJsonWrapper;
		private String userName;
	//	private UploadBean[] uploadBeans;

		public HashMap<Integer,UploadBean> getUploadBeans() {
			return uploadBeansJsonWrapper;
		}

		public void setUploadBeans(HashMap<Integer,UploadBean> uploadBeans) {
			this.uploadBeansJsonWrapper = uploadBeans;
		}

		public String getUsername() {
			return userName;
		}

		public void setUsername(String username) {
			this.userName = username;
		}


}
