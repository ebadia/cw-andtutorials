package apt.tutorial.two;

import android.app.TabActivity ;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.jtwitter.Twitter;
import apt.tutorial.ITwitterListener;
import apt.tutorial.ITwitterMonitor;

public class Patchy extends TabActivity	{
	public static final String LATITUDE="apt.tutorial.latitude";
	public static final String LONGITUDE="apt.tutorial.longitude";
	public static final String STATUS_TEXT="apt.tutorial.statusText";
	private static ColorStateList allWhite=ColorStateList.valueOf(0xFFFFFFFF);
	private EditText status=null;
	private SharedPreferences prefs=null;
	private Twitter client=null;
	private List<TimelineEntry> timeline=new ArrayList<TimelineEntry>();
	private TimelineAdapter adapter=null;
	private List<String> friends=new ArrayList<String>();
	private ArrayAdapter<String> friendsAdapter=null;
	private ListView friendsList=null;
	private ITwitterMonitor service=null;
	private LocationManager locMgr=null;
	private View lastRow=null;
	private Pattern regexLocation=Pattern.compile("L\\:((\\-)?[0-9]+(\\.[0-9]+)?)\\,((\\-)?[0-9]+(\\.[0-9]+)?)");
	private ServiceConnection svcConn=new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
																		IBinder binder) {
			service=ITwitterMonitor.Stub.asInterface(binder);
			
			try {
				service.registerAccount(prefs.getString("user", null),
																prefs.getString("password", null),
																listener);
			}
			catch (Throwable t) {
				Log.e("Patchy", "Exception in call to registerAccount()", t);
				goBlooey(t);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			service=null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		status=(EditText)findViewById(R.id.status);
		
		Button send=(Button)findViewById(R.id.send);
		
		send.setOnClickListener(onSend);
		
		prefs=PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
		
		bindService(new Intent(ITwitterMonitor.class.getName()),
								svcConn, Context.BIND_AUTO_CREATE);
	
		adapter=new TimelineAdapter();
		
		ListView list=(ListView)findViewById(R.id.timeline);
		
//		list.addHeaderView(buildHeader());
		list.setAdapter(adapter);
		list.setOnItemClickListener(onStatusClick);
		list.setOnItemSelectedListener(selectionListener);
		
		TabHost.TabSpec spec=getTabHost().newTabSpec("tag1");
		
		spec.setContent(R.id.status_tab);
		spec.setIndicator("Status", getResources()
																.getDrawable(R.drawable.status));
		getTabHost().addTab(spec);
		
		spec=getTabHost().newTabSpec("tag2");
		spec.setContent(R.id.friends);
		spec.setIndicator("Friends", getResources()
																	.getDrawable(R.drawable.friends));
		getTabHost().addTab(spec);
		
		getTabHost().setCurrentTab(0);
		
		try {
			for (Twitter.User u : getClient().getFriends()) {
				friends.add(u.screenName);
			}
		}
		catch (Throwable t) {
			Log.e("Patchy",
						"Exception in JTwitter#getFriends()", t);
			goBlooey(t);
		}
		
		Collections.sort(friends);
		
		friendsList=(ListView)findViewById(R.id.friends);
		
		friendsAdapter=new ArrayAdapter<String>(this,
												android.R.layout.simple_list_item_multiple_choice,
												friends);
		friendsList.setAdapter(friendsAdapter);
		friendsList.setItemsCanFocus(false);
		friendsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		locMgr=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
																		10000,
																		10000.0f,
																		onLocationChange);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		locMgr.removeUpdates(onLocationChange);
		
		try {
			if (service!=null) {
				service.removeAccount(listener);
			}
		}
		catch (Throwable t) {
			Log.e("Patchy", "Exception in call to removeAccount()", t);
			goBlooey(t);
		}
		
		unbindService(svcConn);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication())
																	.inflate(R.menu.option, menu);

		return(super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.prefs) {
			startActivity(new Intent(this, EditPreferences.class));
			
			return(true);
		}
		else if (item.getItemId()==R.id.bff) {
			try {
				List<String> bff=new ArrayList<String>();
				
				for (int i=0;i<friends.size();i++) {
					if (friendsList.isItemChecked(i)) {
						bff.add(friends.get(i));
					}
				}
				
				service.setBestFriends(listener, bff);
			}
			catch (Throwable t) {
				Log.e("Patchy",
							"Exception in onOptionsItemSelected()", t);
				goBlooey(t);
			}
			
			return(true);
		}
		else if (item.getItemId()==R.id.location) {
			insertLocation();
			
			return(true);
		}
		else if (item.getItemId()==R.id.help) {
			startActivity(new Intent(this, HelpPage.class));
			
			return(true);
		}
		
		return(super.onOptionsItemSelected(item));
	}
	
	private void insertLocation() {
		Location loc=locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (loc==null) {
			Toast
				.makeText(this,
										"No location available",
										Toast.LENGTH_SHORT)
				.show();
		}
		else {
			StringBuffer buf=new StringBuffer(status
																				.getText()
																				.toString());

			buf.append(" L:");
			buf.append(String.valueOf(loc.getLatitude()));
			buf.append(",");
			buf.append(String.valueOf(loc.getLongitude()));
			
			status.setText(buf.toString());
		}
	}
	
	synchronized private Twitter getClient() {
		if (client==null) {
			client=new Twitter(prefs.getString("user", ""),
													prefs.getString("password", ""));
		}
		
		return(client);
	}
	
	synchronized private void resetClient() {
		client=null;
		
		try {
			service.removeAccount(listener);
			service.registerAccount(prefs.getString("user", ""),
														prefs.getString("password", ""),
														listener);
		}
		catch (Throwable t) {
			Log.e("Patchy", "Exception in resetClient()", t);
			goBlooey(t);
		}
	}
	
	private void updateStatus() {
		try {
			getClient().updateStatus(status.getText().toString());
		}
		catch (Throwable t) {
			Log.e("Patchy", "Exception in updateStatus()", t);
			goBlooey(t);
		}
	}
	
	private void goBlooey(Throwable t) {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		
		builder
			.setTitle("Exception!")
			.setMessage(t.toString())
			.setPositiveButton("OK", null)
			.show();
	}
	
	private View buildHeader() {
		Button btn=new Button(this);
		
		btn.setText("Update Me!");
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					service.updateTimeline();
				}
				catch (Throwable t) {
					Log.e("Patchy", "Exception in update-me button", t);
					goBlooey(t);
				}
			}
		});
		
		return(btn);
	}
	
	private View.OnClickListener onSend=new View.OnClickListener() {
		public void onClick(View v) {
			updateStatus();
		}
	};
	
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener=
		new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals("user") || key.equals("password")) {
				resetClient();
			}
		}
	};
	
	private ITwitterListener listener=new ITwitterListener.Stub() {
		public void newFriendStatus(final String friend,
																final String status,
																final String createdAt) {
			runOnUiThread(new Runnable() {
				public void run() {
					adapter.insert(new TimelineEntry(friend,
																						createdAt,
																						status),
													0);
				}
			});
		}
	};
	
	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location location) {
			// required for interface, not used
		}
		
		public void onProviderDisabled(String provider) {
			// required for interface, not used
		}
		
		public void onProviderEnabled(String provider) {
			// required for interface, not used
		}
		
		public void onStatusChanged(String provider, int status,
																	Bundle extras) {
			// required for interface, not used
		}
	};
	
	private AdapterView.OnItemClickListener onStatusClick=
										new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
														int position, long id) {
			TimelineEntry entry=timeline.get(position);
			Matcher r=regexLocation.matcher(entry.status);
			
			if (r.find()) {
				double latitude=Double.valueOf(r.group(1));
				double longitude=Double.valueOf(r.group(4));
				
				Intent i=new Intent(Patchy.this, StatusMap.class);
				
				i.putExtra(LATITUDE, latitude);
				i.putExtra(LONGITUDE, longitude);
				i.putExtra(STATUS_TEXT, entry.status);
				
				startActivity(i);
			}
		}
	};
	
	AdapterView.OnItemSelectedListener selectionListener=
		new AdapterView.OnItemSelectedListener() {
		View lastRow=null;
		
		public void onItemSelected(AdapterView<?> parent,
															 View view, int position,
															 long id) {
			if (lastRow!=null) {
				lastRow.setBackgroundColor(0x00000000);
			}
			
			view.setBackgroundColor(0xFFFF0000);
			lastRow=view;
		}
		
		public void onNothingSelected(AdapterView<?> parent) {
			if (lastRow!=null) {
				lastRow.setBackgroundColor(0x00000000);
				lastRow=null;
			}
		}
	};
	
	class TimelineEntry {
		String friend="";
		String createdAt="";
		String status="";
		
		TimelineEntry(String friend, String createdAt,
									String status) {
			this.friend=friend;
			this.createdAt=createdAt;
			this.status=status;
		}
	}
	
	class TimelineAdapter extends ArrayAdapter<TimelineEntry> {
		 TimelineAdapter() {
			super(Patchy.this, R.layout.row, timeline);
		}
		
		public View getView(int position, View convertView,
												ViewGroup parent) {
			View row=convertView;
			TimelineEntryWrapper wrapper=null;
			
			if (row==null) {													
				LayoutInflater inflater=getLayoutInflater();
				
				row=inflater.inflate(R.layout.row, null);
				wrapper=new TimelineEntryWrapper(row);
				row.setTag(wrapper);
			}
			else {
				wrapper=(TimelineEntryWrapper)row.getTag();
			}
			
			wrapper.populateFrom(timeline.get(position));
			
			return(row);
		}
	}
	
	class TimelineEntryWrapper {
		private TextView friend=null;
		private TextView createdAt=null;
		private TextView status=null;
		private View row=null;
		
		TimelineEntryWrapper(View row) {
			this.row=row;
			
			getFriend().setTextColor(allWhite);
			getCreatedAt().setTextColor(allWhite);
			getStatus().setTextColor(allWhite);
		}
		
		void populateFrom(TimelineEntry s) {
			getFriend().setText(s.friend);
			getCreatedAt().setText(s.createdAt);
			getStatus().setText(s.status);
		}
		
		TextView getFriend() {
			if (friend==null) {
				friend=(TextView)row.findViewById(R.id.friend);
			}
			
			return(friend);
		}
		
		TextView getCreatedAt() {
			if (createdAt==null) {
				createdAt=(TextView)row.findViewById(R.id.created_at);
			}
			
			return(createdAt);
		}
		
		TextView getStatus() {
			if (status==null) {
				status=(TextView)row.findViewById(R.id.status);
			}
			
			return(status);
		}
	}
}