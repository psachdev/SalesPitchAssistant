package com.oracle.play.pptviewer;

public class Rating {
  private int stars;
  private String comment;
  private String username;
  
  public Rating(int stars, String comment,String username) {
    this.stars = stars;
    this.comment = comment;
    this.username = username;
  }
  
  public Rating() {
    // TODO Auto-generated constructor stub
    stars = 0;
    comment = new String();
  }
  
  public int getStars() {
    return stars;
  }
  
  public void setStars(int stars) {
    this.stars = stars;
  }
  
  public String getComment() {
    return comment;
  }
  
  public void setComment(String comment) {
    this.comment = comment;
  }

public String getUsername() {
	return username;
}

public void setUsername(String username) {
	this.username = username;
}
 
}
