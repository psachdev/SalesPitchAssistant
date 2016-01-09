package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.HashMap;

public class PptBean {

	private Integer docId;
	private String pptName;
	private String[] thumbnailURL;
	private String[] imageURL;
	private boolean isFav;
	private ArrayList<Rating> ratings = new ArrayList<Rating>();
	private long numViewers;
	private float totalViewTime;  // in minutes
	private ArrayList<ArrayList<Float>> accessTimes = new ArrayList<ArrayList<Float>>();  // This is an arraylist of access times
	private int slideCount;

	
	public PptBean(String pptName, String[] thumbnailURL, String[] imageURL,
			boolean isFav, int slideCount) {
		this.pptName = pptName;
		this.thumbnailURL = thumbnailURL;
		this.imageURL = imageURL;
		this.isFav = isFav;
		this.numViewers = 0L;
		this.totalViewTime = 0L;
		this.slideCount = slideCount;
	}
	
	public PptBean() {
		// TODO Auto-generated constructor stub
	}
	
	public String getPptName() {
		return pptName;
	}
	public void setPptName(String pptName) {
		this.pptName = pptName;
	}
	public String[] getThumbnailURL() {
		return thumbnailURL;
	}
	public void setThumbnailURL(String[] thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}
	public String[] getImageURL() {
		return imageURL;
	}
	public void setImageURL(String[] imageURL) {
		this.imageURL = imageURL;
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
	
 /* public Rating getUserRating(String user) {
    if (user == null || user.length() == 0)
      return null;
    
    return ratings.get(user);
  }
  
  public void setUserRating(String user, Rating rating) {   
    if (user == null || user.length() == 0)
      return;
    
    if (!ratings.containsKey(user))  {
        ratings.put(user,rating);
    } else {
      Rating foundRating = getUserRating(user);
      foundRating.setStars(rating.getStars());
      foundRating.setComment(rating.getComment());
    }
  }
  
  public int getUserStarRating(String user) {
    int stars = 0;
    
    if (user != null && user.length() > 0) {    
      Rating foundRating = getUserRating(user);
      if (foundRating != null)
        stars = foundRating.getStars();
    }
    
    return stars;
  }
  
  public void setUserStarRating(String user, int stars) {   
    if (user == null || user.length() == 0)
      return;
    
    Rating rating;
    if (!ratings.containsKey(user))  {
        rating = new Rating();
        rating.setStars(stars);
        ratings.put(user,rating);
    } else {
      rating = getUserRating(user);
      rating.setStars(stars);    
      setUserRating(user, rating);
    }
  }
  
  public String getUserCommentRating(String user) {
    String comment = new String();
    
    if (user != null && user.length() > 0) {    
      Rating foundRating = getUserRating(user);
      if (foundRating != null)
        comment = foundRating.getComment();
    }
    
    return comment;
  }
  
  public void setUserCommentRating(String user, String comment) {   
    if (user == null || user.length() == 0)
      return;
    
    Rating rating;
    if (!ratings.containsKey(user))  {
        rating = new Rating();
        rating.setComment(comment);
        ratings.put(user,rating);
    } else {
      rating = getUserRating(user);
      rating.setComment(comment);    
      setUserRating(user, rating);
    }
  }
  */
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

public int getDocId() {
	return docId;
}

public void setDocId(int docId) {
	this.docId = docId;
}

public int getSlideCount() {
	return slideCount;
}

public void setSlideCount(int slideCount) {
	this.slideCount = slideCount;
}
}
