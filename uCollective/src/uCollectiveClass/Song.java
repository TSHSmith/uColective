package uCollectiveClass;

import android.graphics.Bitmap;
import android.view.View;

public class Song {
	private String songUrl;
	private Bitmap image;
	private String artistName;
	private String songTitle;
	private Boolean hasImage = false;
	private String imageUrl;
	
	public Song(String songUrl, String imageUrl, String artistName, String songTitle){
		this.songUrl = songUrl;
		this.imageUrl = imageUrl;
		this.artistName = artistName;
		this.songTitle = songTitle;
	}
	
	public String getSongTitle(){
		return this.songTitle;
	}
	public String getSongUrl(){
		return this.songUrl;
	}
	
	public Bitmap getImage(){
		return this.image;
	}
	
	public String getArtistName(){
		return this.artistName;
	}
	
	public String getImageUrl(){
		return this.imageUrl;
	}
	
	public Boolean getHasImage(){
		return hasImage;
	}
	
	public void setHasImage(Boolean hasImage){
		this.hasImage = hasImage;
	}
	
	public void saveDownloadedImage(Bitmap image){
		this.image = image;
	}
}
