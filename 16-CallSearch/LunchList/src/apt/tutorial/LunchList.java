package apt.tutorial;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LunchList extends Activity {
	public final static String ID_EXTRA="apt.tutorial._ID";
	RestaurantAdapter adapter=null;
	ProgressBar progress=null;
	AtomicBoolean isActive=new AtomicBoolean(true);
	SQLiteDatabase db=null;
	Cursor model=null;
	SharedPreferences prefs=null;
	ListView list=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		progress=(ProgressBar)findViewById(R.id.progress);
		
		db=(new RestaurantSQLiteHelper(this))
																		.getWritableDatabase();
		
		list=(ListView)findViewById(R.id.restaurants);
		prefs=PreferenceManager.getDefaultSharedPreferences(this);
		list.setOnItemClickListener(onListClick);
		initList();

		prefs.registerOnSharedPreferenceChangeListener(prefListener);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		isActive.set(false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		isActive.set(true);
		
		if (progress.getProgress()>0) {
			startWork();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	
		model.close();	
		db.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication())
																	.inflate(R.menu.option, menu);

		return(super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.add) {
			startActivity(new Intent(this, DetailForm.class));
			
			return(true);
		}
		else if (item.getItemId()==R.id.prefs) {
			startActivity(new Intent(this, EditPreferences.class));
			
			return(true);
		}
		else if (item.getItemId()==R.id.search) {
			onSearchRequested();
			return(true);
		}
		else if (item.getItemId()==R.id.run) {
			startWork();
			
			return(true);
		}
		
		return(super.onOptionsItemSelected(item));
	}
	
	private void initList() {
		if (model!=null) {
			stopManagingCursor(model);
			model.close();
		}
		
		String where=null;
		
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			where="name LIKE \"%"+getIntent().getStringExtra(SearchManager.QUERY)+"%\"";
		}
		
		model=Restaurant.getAll(db, where, prefs.getString("sort_order", ""));
		startManagingCursor(model);
		
		adapter=new RestaurantAdapter(model);
		list.setAdapter(adapter);
	}
	
	private void startWork() {
		progress.setVisibility(View.VISIBLE);
		new Thread(longTask).start();			
	}
	
	private void doSomeLongWork(final int incr) {
		runOnUiThread(new Runnable() {
			public void run() {
				progress.incrementProgressBy(incr);
			}
		});
		
		SystemClock.sleep(250);	// should be something more useful!
	}
	
	private AdapterView.OnItemClickListener onListClick=new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent,
														 View view, int position,
														 long id) {
			Intent i=new Intent(LunchList.this, DetailForm.class);
			
			i.putExtra(ID_EXTRA, String.valueOf(id));
			startActivity(i);
		}
	};
	
	private Runnable longTask=new Runnable() {
		public void run() {
			for (int i=progress.getProgress();
					 i<100 && isActive.get();
					 i+=2) {
				doSomeLongWork(2);
			}
			
			if (isActive.get()) {
				runOnUiThread(new Runnable() {
					public void run() {
						progress.setVisibility(View.GONE);
						progress.setProgress(0);
					}
				});
			}
		}
	};
	
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener=
		new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			if (key.equals("sort_order")) {
				runOnUiThread(new Runnable() {
					public void run() {
						initList();
					}
				});
			}
		}
	};
	
	class RestaurantAdapter extends CursorAdapter {
		RestaurantAdapter(Cursor c) {
			super(LunchList.this, c);
		}
		
		@Override
		public void bindView(View row, Context ctxt,
													Cursor c) {
			RestaurantWrapper wrapper=(RestaurantWrapper)row.getTag();
			
			wrapper.populateFrom(c);
		}
		
		@Override
		public View newView(Context ctxt, Cursor c,
												ViewGroup parent) {
			LayoutInflater inflater=getLayoutInflater();
			
			View row=inflater.inflate(R.layout.row, null);
			RestaurantWrapper wrapper=new RestaurantWrapper(row);
			row.setTag(wrapper);
			
			wrapper.populateFrom(c);
			
			return(row);
		}
	}
	
	class RestaurantWrapper {
		private TextView name=null;
		private TextView address=null;
		private ImageView icon=null;
		private View row=null;
		
		RestaurantWrapper(View row) {
			this.row=row;
		}
		
		void populateFrom(Cursor c) {
			getName().setText(c.getString(c.getColumnIndex("name")));
			getAddress().setText(c.getString(c.getColumnIndex("address")));
	
			String type=c.getString(c.getColumnIndex("type"));
	
			if (type.equals("sit_down")) {
				getIcon().setImageResource(R.drawable.ball_red);
			}
			else if (type.equals("take_out")) {
				getIcon().setImageResource(R.drawable.ball_yellow);
			}
			else {
				getIcon().setImageResource(R.drawable.ball_green);
			}
		}
		
		TextView getName() {
			if (name==null) {
				name=(TextView)row.findViewById(R.id.title);
			}
			
			return(name);
		}
		
		TextView getAddress() {
			if (address==null) {
				address=(TextView)row.findViewById(R.id.address);
			}
			
			return(address);
		}
		
		ImageView getIcon() {
			if (icon==null) {
				icon=(ImageView)row.findViewById(R.id.icon);
			}
			
			return(icon);
		}
	}
}
