package uCollectiveClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import learn2crack.customlistview.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
		View listView = inflater.inflate(R.layout.list_single, null, true);
		
		TextView textSong = (TextView) listView.findViewById(R.id.txt);
		TextView textArtist = (TextView) listView.findViewById(R.id.txt2);
		final ImageView imageView = (ImageView) listView.findViewById(R.id.img);
		
		textSong.setText(songList.get(position).getSongTitle());
		textArtist.setText(songList.get(position).getArtistName());
		imageView.setImageBitmap(songList.get(position).getImage());
		
		
		/*
		*/
			
		return listView;
	}
	

}
