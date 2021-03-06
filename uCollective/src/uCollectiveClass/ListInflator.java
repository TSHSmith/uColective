package uCollectiveClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;






import uCollectiveClass.gen.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListInflator extends ArrayAdapter<Song>{

	private final Activity context;
	private final ArrayList<Song> songList;
	
	public ListInflator(Activity context,
			List<Song> objects) {
		super(context, R.layout.list_single, objects);
		this.context = context;
		this.songList = (ArrayList<Song>) objects;
	}
	
	@Override
	public View getView(final int position, View view, ViewGroup parent){
		LayoutInflater inflater = context.getLayoutInflater();
		View listView;
		if (position >= 1){
		listView = inflater.inflate(R.layout.list_single, null, true);
		
		TextView textSong = (TextView) listView.findViewById(R.id.txt);
		TextView textArtist = (TextView) listView.findViewById(R.id.txt2);
		ImageView avatar = (ImageView) listView.findViewById(R.id.img);
		ProgressBar progressBar = (ProgressBar) listView.findViewById(R.id.progressBar);
		
		if(!songList.get(position).getHasImage())
			new getImage(songList.get(position - 1), avatar, progressBar, view.GONE).execute();
		else{
			avatar.setImageBitmap(songList.get(position - 1).getImage());
			progressBar.setVisibility(view.GONE);
			avatar.setBackground(null);
		}
		
		textSong.setText(songList.get(position).getSongTitle());
		textArtist.setText(songList.get(position).getArtistName());
		} else {
			listView = inflater.inflate(R.layout.empty_item, null, true);
		}
		
		return listView;
	}
	
	public void refill(ArrayList<Song> songs){
		this.songList.clear();
		this.songList.addAll(songs);
		notifyDataSetChanged();
	}
	
	class getImage extends AsyncTask<Void, Void, Void>{

		private Song currSong;
		private Bitmap bmp;
		private ImageView imageView;
		private ProgressBar progressBar;
		private int progressBarVisiblity;
		
		public getImage(Song currSong, ImageView imageView, ProgressBar progressBar, int progressBarVisibility){
			this.currSong = currSong;
			this.imageView = imageView;
			this.progressBar = progressBar;
			this.progressBarVisiblity = progressBarVisibility;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				URL url = new URL(currSong.getImageUrl());
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
			currSong.saveDownloadedImage(bmp);
			imageView.setImageBitmap(currSong.getImage());
			currSong.setHasImage(true);
			this.progressBar.setVisibility(this.progressBarVisiblity);
		}

	}
	

}
