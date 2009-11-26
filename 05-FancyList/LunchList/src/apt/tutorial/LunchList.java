package apt.tutorial;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class LunchList extends Activity {
	List<Restaurant> model=new ArrayList<Restaurant>();
	RestaurantAdapter adapter=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button save=(Button)findViewById(R.id.save);
		
		save.setOnClickListener(onSave);
		
		ListView list=(ListView)findViewById(R.id.restaurants);
		
		adapter=new RestaurantAdapter();
		list.setAdapter(adapter);
	}
	
	private View.OnClickListener onSave=new View.OnClickListener() {
		public void onClick(View v) {
			Restaurant r=new Restaurant();
			EditText name=(EditText)findViewById(R.id.name);
			EditText address=(EditText)findViewById(R.id.addr);
			
			r.setName(name.getText().toString());
			r.setAddress(address.getText().toString());
			
			RadioGroup types=(RadioGroup)findViewById(R.id.types);
			
			switch (types.getCheckedRadioButtonId()) {
				case R.id.sit_down:
					r.setType("sit_down");
					break;
					
				case R.id.take_out:
					r.setType("take_out");
					break;
					
				case R.id.delivery:
					r.setType("delivery");
					break;
			}
			
			adapter.add(r);
		}
	};
	
	class RestaurantAdapter extends ArrayAdapter<Restaurant> {
		RestaurantAdapter() {
			super(LunchList.this,
						android.R.layout.simple_list_item_1,
						model);
		}
		
		public View getView(int position, View convertView,
												ViewGroup parent) {
			View row=convertView;
			RestaurantWrapper wrapper=null;
			
			if (row==null) {													
				LayoutInflater inflater=getLayoutInflater();
				
				row=inflater.inflate(R.layout.row, null);
				wrapper=new RestaurantWrapper(row);
				row.setTag(wrapper);
			}
			else {
				wrapper=(RestaurantWrapper)row.getTag();
			}
			
			wrapper.populateFrom(model.get(position));
			
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
		
		void populateFrom(Restaurant r) {
			getName().setText(r.getName());
			getAddress().setText(r.getAddress());
	
			if (r.getType().equals("sit_down")) {
				getIcon().setImageResource(R.drawable.ball_red);
			}
			else if (r.getType().equals("take_out")) {
				getIcon().setImageResource(R.drawable.ball_yellow);
			}
			else {
				getIcon().setImageResource(R.drawable.ball_green);
			}
		}
		
		TextView getName() {
			if (name==null) {
				name=(TextView)row.findViewById(R.id.name);
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
