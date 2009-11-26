package apt.tutorial.two;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import winterwell.jtwitter.Twitter;
import apt.tutorial.ITwitterListener;
import apt.tutorial.ITwitterMonitor;

public class Patchy extends Activity {
	private EditText status=null;
	private SharedPreferences prefs=null;
	private Twitter client=null;
	private List<TimelineEntry> timeline=new ArrayList<TimelineEntry>();
	private TimelineAdapter adapter=null;
	private ITwitterMonitor service=null;
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
		((ListView)findViewById(R.id.timeline)).setAdapter(adapter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			service.removeAccount(listener);
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
		
		return(super.onOptionsItemSelected(item));
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
																						status), 0);
				}
			});
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