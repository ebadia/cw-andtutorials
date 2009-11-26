package apt.tutorial;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class DetailForm extends Activity {
	EditText name=null;
	EditText address=null;
	EditText notes=null;
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
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putString("name",
																	name.getText().toString());
		savedInstanceState.putString("address",
																	address.getText().toString());
		savedInstanceState.putString("notes",
																	notes.getText().toString());
		savedInstanceState.putInt("type",
															types.getCheckedRadioButtonId());
	}
	
	private void load() {
		current=Restaurant.getById(restaurantId, db);
		
		name.setText(current.getName());
		address.setText(current.getAddress());
		notes.setText(current.getNotes());
		
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