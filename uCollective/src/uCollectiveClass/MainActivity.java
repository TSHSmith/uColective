package uCollectiveClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uCollectiveClass.gen.R;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class MainActivity extends Activity {
	private MediaPlayer mp;
	private ArrayList<Song> songList = new ArrayList<Song>();
	private ListView list;
	private Button playButton, nextButton, previousButton;
	private Boolean stop, update = false, startPlay = false, randomMode = false;
	private int x = 1;
	private int count = 1;
	private int currentSong = 0, position = 0;
	private View v;
	private Dialog searchPopup;
	private Boolean searchMode = false;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * Configure the titlebar to use a custom setup.
		 */
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);

		/**
		 * Creates the inflater.
		 */
		LayoutInflater inflater = LayoutInflater.from(this);
		/**
		 * Intialises the view using the inflater.
		 */
		v = inflater.inflate(R.layout.titleview, null);
		
		/**
		 * Sets the colour of the text to white.
		 */
		((TextView)v.findViewById(R.id.title)).setTextColor(Color.WHITE);
		
		/**
		 * Assigns the vie to the action bar.
		 */
		this.getActionBar().setCustomView(v);
		
		
		
		/**
		 * Sets up the local instances of the buttons and the list.
		 */
		list = (ListView)findViewById(R.id.list);
		playButton = (Button)findViewById(R.id.playButton);
		previousButton = (Button) findViewById(R.id.previousSong);
		nextButton = (Button) findViewById(R.id.nextSong);
		
		playButton.setClickable(false);
		previousButton.setClickable(false);
		nextButton.setClickable(false);
		
		/**
		 * Sets up the default visual elements for the buttons
		 */
		playButton.setBackgroundResource(R.drawable.playpressed);
		previousButton.setBackgroundResource(R.drawable.previouspressed);
		nextButton.setBackgroundResource(R.drawable.nextpressed);
		
		/**
		 * ListView configurations to improve performance.
		 */
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setFastScrollEnabled(true);
		list.setScrollingCacheEnabled(false);
		
		/**
		 * Beings the Asynchronous task to populate the list.
		 */
		new PopulateList(this, list).execute();
		
		/**
		 * Initialises the MediaPlayer variable mp.
		 */
		mp = new MediaPlayer();
		
		/**
		 * Calls the methods that set up the listeners.
		 */
		this.setMediaPlayerListeners();
		this.setListListeners();
		this.setPlayButtonListeners();
		this.setNextButtonListeners();
		this.setPreviousSongListeners();
		
		this.searchPopup = new Dialog(MainActivity.this, android.R.style.Theme_Light_NoTitleBar);
		this.searchPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.searchPopup.setCancelable(true);
		this.searchPopup.setContentView(R.layout.search_popup);
		
		this.setSearchListeners();
		
		ListInflator adapter = new ListInflator(this, songList);
		list.setAdapter(adapter);
		list.setOnScrollListener(new EndlessScrollListener(getActionBar()));
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		boolean toReturn = false;
		
		switch (item.getItemId()){
			case R.id.random_mode:
				new getRandomSong(MainActivity.this).execute();
				randomMode = true;
				previousButton.setBackgroundResource(R.drawable.previouspressed);
				previousButton.setClickable(false);
				playButton.setClickable(true);
				nextButton.setClickable(true);				
				playButton.setBackgroundResource(R.drawable.pause);
				nextButton.setBackgroundResource(R.drawable.next);
				toReturn = true;
				break;
			case R.id.refresh:
				x = 1;
				position = 0;
				songList.clear();
				new PopulateList(MainActivity.this, list).execute();
				toReturn = true;
				searchMode = false;
				break;
			case R.id.search_button:
				searchPopup.show();
		}
		
		return toReturn;
	}
	
	private void setSearchListeners(){
			
		Button cancleButton = (Button) searchPopup.findViewById(R.id.cancelBtn);
		cancleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchPopup.dismiss();
			}
		});
		
		Button searchButton = (Button) searchPopup.findViewById(R.id.searchBtn);
		searchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText searchInput = (EditText) searchPopup.findViewById(R.id.searchInput);
				String search = "https://ucollective.org/api/?request=audio&scope=search&title=" + searchInput.getText().toString().replace(" ", "%20");
				position = 0;
				searchMode = true;
				songList.clear();
				searchPopup.dismiss();
				new PopulateList(MainActivity.this, list, search).execute();
			}
		});
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
							//If down and not playing the button is set to the "playpressed" state.
							playButton.setBackgroundResource(R.drawable.playpressed);
						}else{
							//If down and is playing the button is set to a "pausepressed" state.
							playButton.setBackgroundResource(R.drawable.pausepressed);
						}
						break;
					case MotionEvent.ACTION_UP:
						if (!mp.isPlaying()){
							//If released and is not playing the button is set to "pause" state.
							playButton.setBackgroundResource(R.drawable.pause);
							mp.start();
						} else {
							//if released and is playing the music is paused and the button is set to play.
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
				if(randomMode){
					switch(event.getAction()){
						case MotionEvent.ACTION_DOWN:
							nextButton.setBackgroundResource(R.drawable.nextpressed);
							break;
						case MotionEvent.ACTION_UP:
							nextButton.setBackgroundResource(R.drawable.next);
							new getRandomSong(MainActivity.this).execute();
							break;
					}
				}else{
					if(mp != null){
						switch(event.getAction()){
						case MotionEvent.ACTION_CANCEL:
							break;
						case MotionEvent.ACTION_DOWN:
							//Whilst the button is held the image is changed to a blue version of the original.
							nextButton.setBackgroundResource(R.drawable.nextpressed);
							break;
						case MotionEvent.ACTION_UP:
							//When the button is release it reverts to it's original state.
							nextButton.setBackgroundResource(R.drawable.next);
							//Increments the current song pointer.
							currentSong++;
							//checks to see if the song is in the bounds of the current list.
							if (currentSong < songList.size()){
								//Plays the song if in bounds.
								playSong(currentSong);
							} else {
								//sets start play to true and calls populate list - becuase this is true the next song will play once the list is updated.
								startPlay = true;
								new PopulateList(MainActivity.this, list).execute();
							}
							
							if (currentSong > 0){
								previousButton.setEnabled(true);
								previousButton.setBackgroundResource(R.drawable.previous);
							}
							break;
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
				if(randomMode){
					new getRandomSong(MainActivity.this).execute();
				}else{
					//Increments the currentSong pointer.	
					currentSong++;
					//Checks to see
					if (currentSong < songList.size()){
						playSong(currentSong);
					} else {
						startPlay = true;
						new PopulateList(MainActivity.this, list).execute();
					}
					
					if (currentSong < 0){
						previousButton.setEnabled(true);
						previousButton.setBackgroundResource(R.drawable.next);
					}
				}
			}
		});
	}
	
	/**
	 * sets up the listeners for the list
	 */
	private void setListListeners(){
		//Single tap will play the selected song
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {	

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				randomMode = false;
				playButton.setClickable(true);
				nextButton.setClickable(true);
				
				playButton.setBackgroundResource(R.drawable.pause);
				nextButton.setBackgroundResource(R.drawable.next);
				
				openDrawer();
				
				if(position > 0){
					previousButton.setBackgroundResource(R.drawable.previous);
					previousButton.setClickable(true);
				}
				else{
					previousButton.setBackgroundResource(R.drawable.previouspressed);
					previousButton.setClickable(false);
				}
				
				currentSong = position;
				playSong(position);
			}
		});
		
		//A hold will open a new dialog that will show the songs info.
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				mp.pause();
				playButton.setBackgroundResource(R.drawable.pause);
				
				openDrawer();
				
				Intent intent = new Intent(MainActivity.this, SongActivity.class);
				Song currSong = new Song(songList.get(position));
				intent.putExtra("song", currSong);
			    startActivity(intent);

				return false;
			}
		});
		

	}
	
	/**
	 * Opens the drawer and displays the pause/play, next and previous songs.
	 */
	private void openDrawer(){
		//Don't like the use of a sliding drawer because it's an depreciated class - looking into alternatives.
		SlidingDrawer sd = (SlidingDrawer)findViewById(R.id.slidingDrawer1);
		sd.open();
	}
	
	
	private void setPreviousSongListeners(){
		previousButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						previousButton.setBackgroundResource(R.drawable.previouspressed);
						break;
					case MotionEvent.ACTION_UP:
						previousButton.setBackgroundResource(R.drawable.previous);
						if(currentSong - 1 >= 0 ){
							currentSong--;
							playSong(currentSong);
							if(currentSong == 0){
								previousButton.setEnabled(false);
								previousButton.setBackgroundResource(R.drawable.previouspressed);
							}
						} 
						break;
				}
				return false;
			}
		});
	}
	
	private void playSong(int index){
		try {
			mp.stop();
			mp.reset();
			mp.setDataSource(songList.get(index).getSongUrl());
			((TextView)v.findViewById(R.id.title)).setText(songList.get(index).getSongTitle());
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
		String url = "https://ucollective.org/api/?request=audio&scope=all&page=" + x;
		ListView list;
		
		public PopulateList(MainActivity mainActivity, ListView list){
			this.mainActivity = mainActivity;
			this.context = mainActivity;
			this.progressDialog = new ProgressDialog(this.mainActivity);
			this.list = list;
		}
		
		public PopulateList(MainActivity mainActivity, ListView list, String url){
			this.mainActivity = mainActivity;
			this.context = mainActivity;
			this.progressDialog = new ProgressDialog(this.mainActivity);
			this.list = list;
			this.url = url;
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
			
			list.setSelection(position);
			
			if(startPlay){	
				playSong(currentSong);
			}
			
			this.progressDialog.dismiss();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
					position = list.getFirstVisiblePosition() + 1;
					getJSON getjson = new getJSON();
					String json = getjson.startThread(url);
					x++;
					this.jsonArray = new JSONArray(json);
					for (int y = 0; y < 40; y++){
						JSONObject currObj = this.jsonArray.getJSONObject(y);
						Song currSong = new Song(currObj.getString("file"), currObj.getString("avatar"), currObj.getString("author"), currObj.getString("title"), currObj.getString("description"));
						songList.add(currSong);
					}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	class getRandomSong extends AsyncTask<Void, Void, Void>{
		ProgressDialog progressDialog;
		JSONObject obj;
		public getRandomSong(MainActivity mainActivity){
			progressDialog = new ProgressDialog(mainActivity);
		}
		
		@Override
		protected void onPreExecute(){
			this.progressDialog.setMessage("Getting a random song!");
			this.progressDialog.show();
			this.progressDialog.setCancelable(false);
		}
		

		
		@Override
		protected Void doInBackground(Void... params){
			try {
				//gets a random json from url and saves it as a string;
				getJSON gj = new getJSON();
				String text = gj.startThread("https://ucollective.org/api/?request=audio&scope=rand");
				obj = new JSONObject(text);
				mp.reset();
				//setMediaPlayerListeners();
				//sets the source of the data to that in the JSON object under the name "file"
				mp.setDataSource(obj.getString("file"));
				//prepares the mediaplayer
				mp.prepare();
				//starts streaming the audio
				mp.start();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override 
		protected void onPostExecute(Void result){
			try {
				((TextView)v.findViewById(R.id.title)).setText(this.obj.getString("title") + " - " + this.obj.getString("author"));
				this.progressDialog.dismiss();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
		
	class EndlessScrollListener implements OnScrollListener {

	    private int visibleThreshold = 0;
	    private int currentPage = 0;
	    private int previousTotal = 0;
	    private boolean loading = true;
	    private int prevVisibleItem = 0;
	    private ActionBar actionBar;

	    public EndlessScrollListener(ActionBar actionBar) {
	    	this.actionBar = actionBar;
	    }
	    
	    public EndlessScrollListener(int visibleThreshold, ActionBar actionBar) {
	        this.visibleThreshold = visibleThreshold;
	        this.actionBar = actionBar;
	    }

	    @Override
	    public void onScroll(AbsListView view, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {
	    	if(!searchMode){
		        if (loading) {
		            if (totalItemCount > previousTotal) {
		                loading = false;
		                previousTotal = totalItemCount;
		                currentPage++;
		            }
		        }
		        if (!loading & (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
		            new PopulateList(MainActivity.this, list).execute();
		            loading = true;
		        }
	    	}
	    	
	    	 if(prevVisibleItem != firstVisibleItem){
	    		    if(prevVisibleItem < firstVisibleItem)
	    		    	actionBar.hide();
	    		    else
	    		    	actionBar.show();

	    		  prevVisibleItem = firstVisibleItem;
	    	 }
	    }

	    @Override
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	    }
	}
		
}
