package apt.tutorial.four;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IntentExplorer extends Activity {
	static final int PICK_REQUEST=1337;
	private EditText type=null;
	private TextView chosenContent=null;
	private Uri chosenUri=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		type=(EditText)findViewById(R.id.type);
		chosenContent=(TextView)findViewById(R.id.uri);
		
		Button btn=(Button)findViewById(R.id.pick);
		
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i=new Intent(Intent.ACTION_PICK,
										Uri.parse(type.getText().toString()));

				startActivityForResult(i, PICK_REQUEST);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode,
																	int resultCode,
																	Intent data) {
		if (requestCode==PICK_REQUEST) {
			if (resultCode==RESULT_OK) {
				chosenUri=data.getData();
				chosenContent.setText(chosenUri.toString());
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Intent i=new Intent(null, chosenUri);
						
		i.addCategory(Intent.CATEGORY_ALTERNATIVE);
		
		int c=menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
													null, null, i, 0, null);
		
		return(super.onPrepareOptionsMenu(menu));
	}
}