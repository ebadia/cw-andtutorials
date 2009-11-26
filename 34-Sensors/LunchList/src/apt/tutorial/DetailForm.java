package apt.tutorial;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class DetailForm extends Activity {
	EditText name=null;
	EditText address=null;
	EditText notes=null;
	EditText phone=null;
	RadioGroup types=null;
	Restaurant current=null;
	SQLiteDatabase db=null;
	String restaurantId=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_form);
		db=(new RestaurantSQLiteHelper(this))
																		.getWritableDatabase();
		
		name=(EditText)findViewById(R.id.name);
		address=(EditText)findViewById(R.id.addr);
		notes=(EditText)findViewById(R.id.notes);
		phone=(EditText)findViewById(R.id.phone);
		types=(RadioGroup)findViewById(R.id.types);
		
		Button save=(Button)findViewById(R.id.save);
		
		save.setOnClickListener(onSave);
		
		restaurantId=getIntent().getStringExtra(LunchList.ID_EXTRA);
		
		if (restaurantId==null) {
			current=new Restaurant();
		}
		else {
			load();
		}
		
		if (savedInstanceState!=null) {
			name.setText(savedInstanceState.getString("name"));
			address.setText(savedInstanceState.getString("address"));
			notes.setText(savedInstanceState.getString("notes"));
			phone.setText(savedInstanceState.getString("phone"));
			types.check(savedInstanceState.getInt("type"));
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	
		db.close();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("name",
																	name.getText().toString());
		savedInstanceState.putString("address",
																	address.getText().toString());
		savedInstanceState.putString("notes",
																	notes.getText().toString());
		savedInstanceState.putString("phone",
																	phone.getText().toString());
		savedInstanceState.putInt("type",
															types.getCheckedRadioButtonId());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication())
																	.inflate(R.menu.option_detail, menu);

		return(super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.call) {
			if (current.getPhoneNumber()!=null &&
					current.getPhoneNumber().length()>0) {
				String toDial="tel:"+current.getPhoneNumber();
				
				startActivity(new Intent(Intent.ACTION_CALL,
																	Uri.parse(toDial)));
			}
			
			return(true);
		}
		else if (item.getItemId()==R.id.photo) {
			startActivity(new Intent(this, Photographer.class));
			
			return(true);
		}

		return(super.onOptionsItemSelected(item));
	}
	
	private void load() {
		current=Restaurant.getById(restaurantId, db);
		
		name.setText(current.getName());
		address.setText(current.getAddress());
		notes.setText(current.getNotes());
		phone.setText(current.getPhoneNumber());
		
		if (current.getType().equals("sit_down")) {
			types.check(R.id.sit_down);
		}
		else if (current.getType().equals("take_out")) {
			types.check(R.id.take_out);
		}
		else {
			types.check(R.id.delivery);
		}
	}
	
	private View.OnClickListener onSave=new View.OnClickListener() {
		public void onClick(View v) {
			current.setName(name.getText().toString());
			current.setAddress(address.getText().toString());
			current.setNotes(notes.getText().toString());
			current.setPhoneNumber(phone.getText().toString());
			
			switch (types.getCheckedRadioButtonId()) {
				case R.id.sit_down:
					current.setType("sit_down");
					break;
					
				case R.id.take_out:
					current.setType("take_out");
					break;
					
				case R.id.delivery:
					current.setType("delivery");
					break;
			}

			if (restaurantId==null) {
				current.save(db);
			}
			else {
				current.update(restaurantId, db);
			}
			
			finish();
		}
	};
}