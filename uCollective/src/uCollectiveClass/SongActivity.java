package uCollectiveClass;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import uCollectiveClass.gen.R;
import uCollectiveClass.gen.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SongActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song);
		
		Intent intent = getIntent();
		Song song = (Song) intent.getSerializableExtra("song");
		
		this.getActionBar().hide();
		
		ImageView avatar = (ImageView) findViewById(R.id.avartar);
		ProgressBar pd = (ProgressBar) findViewById(R.id.iconDownload);
		TextView songTitle = (TextView) findViewById(R.id.songTitle);
		TextView artistName = (TextView) findViewById(R.id.artistName);
		TextView description = (TextView) findViewById(R.id.Description);
		
		if(!song.getHasImage())
			new getSingleImage(avatar, pd, song).execute();
		else
			avatar.setImageBitmap(song.getImage());
		
		songTitle.setText(song.getSongTitle());
		artistName.setText(song.getArtistName());
		description.setText(song.getDescription());

	}
	
	class getSingleImage extends AsyncTask<Void, Void, Void>{
		private ImageView avatar;
		private ProgressBar pd;
		private Song song;
		private Bitmap bmp;
		
		public getSingleImage(ImageView avatar, ProgressBar pd, Song song){
			this.avatar = avatar;
			this.pd = pd;
			this.song = song;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(song.getImageUrl());
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void unused){
			this.song.saveDownloadedImage(bmp);
			this.avatar.setImageBitmap(bmp);
			this.song.setHasImage(true);
			this.pd.setVisibility(View.INVISIBLE);
			this.avatar.setVisibility(View.VISIBLE);
		}
		
	}
	
}
