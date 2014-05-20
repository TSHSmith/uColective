package uCollectiveClass;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import learn2crack.customlistview.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;


public class MainActivity extends Activity {
	MediaPlayer mp;
	ArrayList<Song> songList = new ArrayList<Song>();
	ListView list;
	Button playButton, nextButton, previousButton;
	Boolean stop, update = false;
	int x = 1;
	int count = 1;
	int currentSong = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		list = (ListView)findViewById(R.id.list);
		playButton = (Button)findViewById(R.id.playButton);
		previousButton = (Button) findViewById(R.id.previousSong);
		nextButton = (Button) findViewById(R.id.nextSong);
		
		playButton.setBackgroundResource(R.drawable.playpressed);
		previousButton.setBackgroundResource(R.drawable.previouspressed);
		nextButton.setBackgroundResource(R.drawable.nextpressed);
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setFastScrollEnabled(true);
		list.setScrollingCacheEnabled(false);
		
		new PopulateList(this, list).execute();
		mp = new MediaPlayer();
		
		this.setMediaPlayerListeners();
		this.setListListeners();
		this.setPlayButtonListeners();
		this.setNextButtonListeners();
	}
	
	/**
	 * Sets up the listener for the playButton
	 */
	private void setPlayButtonListeners(){
		playButton.setOnTouchListener(new View.OnTouchListener() {
					
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mp != null){
					switch(event.getAction()){
					case MotionEvent.ACTION_CANCEL:
						break;
					case MotionEvent.ACTION_DOWN:
						if(!mp.isPlaying()){
							playButton.setBackgroundResource(R.drawable.playpressed);
						}else{
							playButton.setBackgroundResource(R.drawable.pausepressed);
						}
						break;
					case MotionEvent.ACTION_UP:
						if (!mp.isPlaying()){
							playButton.setBackgroundResource(R.drawable.pause);
							mp.start();
						} else {
							playButton.setBackgroundResource(R.drawable.play);
							mp.pause();
						}
						break;
					}
					
				}
				
				return false;
			}
		});
	}
	
	/**
	 * Sets up the listener for the next Button
	 */
	private void setNextButtonListeners(){
		nextButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mp != null){
					switch(event.getAction()){
					case MotionEvent.ACTION_CANCEL:
						break;
					case MotionEvent.ACTION_DOWN:
						nextButton.setBackgroundResource(R.drawable.nextpressed);
						break;
					case MotionEvent.ACTION_UP:
						try {
							nextButton.setBackgroundResource(R.drawable.next);
							mp.stop();
							mp.reset();
							currentSong++;
							mp.setDataSource(songList.get(currentSong).getSongUrl());
							getActionBar().setTitle(songList.get(currentSong).getSongTitle());
							mp.prepare();
							mp.start();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return false;
			}
		});
	}
	
	/**
	 * Sets up the listeners for the mediaPlayer
	 */
	private void setMediaPlayerListeners(){
		mp.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				try {
					mp.stop();
					mp.reset();
					currentSong++;
					mp.setDataSource(songList.get(currentSong).getSongUrl());
					getActionBar().setTitle(songList.get(currentSong).getSongTitle());
					mp.prepare();
					mp.start();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}
	
	/**
	 * sets up the listeners for the list
	 */
	private void setListListeners(){
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					currentSong = position;
					
					playButton.setBackgroundResource(R.drawable.pause);
					
					if(position > 0)
						previousButton.setBackgroundResource(R.drawable.previous);
					else
						previousButton.setBackgroundResource(R.drawable.previouspressed);
					
					nextButton.setBackgroundResource(R.drawable.next);
					
					try {
						mp.stop();
						mp.reset();
						mp.setDataSource(songList.get(position).getSongUrl());
						getActionBar().setTitle(songList.get(currentSong).getSongTitle());
						mp.prepare();
						update = true;
						mp.start();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
	}
	
	/**
	 * Class that pulls down and populates the list view with data.
	 * @author Thomas
	 *
	 */
	class PopulateList extends AsyncTask<Void, Void, Void>{
		
		ProgressDialog progressDialog;
		Context context;
		MainActivity mainActivity;
		JSONArray jsonArray;

		ListView list;
		
		public PopulateList(MainActivity mainActivity, ListView list){
			this.mainActivity = mainActivity;
			this.context = mainActivity;
			this.progressDialog = new ProgressDialog(this.mainActivity);
			this.list = list;
		}
		
		@Override
		protected void onPreExecute(){
			this.progressDialog.setCancelable(false);
			this.progressDialog.setTitle("Loading");
			this.progressDialog.setMessage("Getting you some more songs!");
			this.progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void unused){
			ListInflator adapter = new ListInflator(this.mainActivity, songList);
			list.setAdapter(adapter);
			list.setOnScrollListener(new EndlessScrollListener());
			list.setSelection(currentSong);
			currentSong += 40;
			this.progressDialog.dismiss();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {

					getJSON getjson = new getJSON();
					String json = getjson.startThread("https://ucollective.org/api/?request=audio&scope=all&page=" + x);
					x++;
					this.jsonArray = new JSONArray(json);
					for (int x = 0; x < 40; x++){
						JSONObject currObj = this.jsonArray.getJSONObject(x);
						try {
							URL url = new URL(currObj.getString("avatar"));
							Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
							songList.add(new Song(currObj.getString("file"), bmp, currObj.getString("author"), currObj.getString("title")));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	
	class EndlessScrollListener implements OnScrollListener {

	    private int visibleThreshold = 5;
	    private int currentPage = 0;
	    private int previousTotal = 0;
	    private boolean loading = true;

	    public EndlessScrollListener() {
	    }
	    
	    public EndlessScrollListener(int visibleThreshold) {
	        this.visibleThreshold = visibleThreshold;
	    }

	    @Override
	    public void onScroll(AbsListView view, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {
	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	                currentPage++;
	            }
	        }
	        if (!loading & (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
	            new PopulateList(MainActivity.this, list).execute();
	            loading = true;
	        }
	    }

	    @Override
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	    }
	}
				
}
