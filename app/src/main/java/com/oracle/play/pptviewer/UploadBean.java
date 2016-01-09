package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadBean {

	private Integer docId;
	private String pptName;
	private boolean isFav;
	private ArrayList<Rating> ratings = new ArrayList<Rating>();
	private long numViewers;
	private float totalViewTime;  // in minutes
	private ArrayList<ArrayList<Float>> accessTimes = new ArrayList<ArrayList<Float>>();  // This is an arraylist of access times

	
	public UploadBean(String pptName,boolean isFav) {
		this.pptName = pptName;
		this.isFav = isFav;
		this.numViewers = 0L;
		this.totalViewTime = 0L;
	}
	
	public UploadBean() {
		// TODO Auto-generated constructor stub
	}
	
	public String getPptName() {
		return pptName;
	}
	public void setPptName(String pptName) {
		this.pptName = pptName;
	}

	public boolean isFav() {
		return isFav;
	}
	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}

  public ArrayList<Rating> getRatings() {
    return ratings;
  }

  public void setRatings(ArrayList<Rating> ratings) {
    this.ratings = ratings;
  }
	
  public long getNumViewers() {
    return numViewers;
  }
  
  public void setNumViewers(long viewers) {
    this.numViewers = viewers;
  }
  
  public float getTotalViewTime() {
    return totalViewTime;
  }
  
  public void setTotalViewTime(float totalViewTime) {
    this.totalViewTime = totalViewTime;
  }
  
  public ArrayList<ArrayList<Float>> getAccessTimes() {
    return accessTimes;
  }
  
  public void setAccessTimes(ArrayList<ArrayList<Float>> accessTimes) {
    this.accessTimes = accessTimes;
  }

public Integer getDocId() {
	return docId;
}

public void setDocId(Integer docId) {
	this.docId = docId;
}
}
