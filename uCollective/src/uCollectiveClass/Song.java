package uCollectiveClass;

import android.graphics.Bitmap;

public class Song {
	private String songUrl;
	private Bitmap image;
	private String artistName;
	private String songTitle;
	
	public Song(String songUrl, Bitmap image, String artistName, String songTitle){
		this.songUrl = songUrl;
		this.image = image;
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
}
