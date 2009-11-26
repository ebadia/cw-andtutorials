package apt.tutorial.two;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusEntryView extends LinearLayout {
	private String labelCaption=null;
	private String buttonCaption=null;
	private Button send=null;
	private View.OnClickListener onSend=null;
	private EditText status=null;
	
	public StatusEntryView(final Context ctxt,
												 AttributeSet attrs) {
		super(ctxt, attrs);
		
		TypedArray a=ctxt.obtainStyledAttributes(attrs,
																							R.styleable.StatusEntryView,
																							0, 0);
		
		labelCaption=a.getString(R.styleable.StatusEntryView_labelCaption);
		buttonCaption=a.getString(R.styleable.StatusEntryView_buttonCaption);
		
		a.recycle();
	}
	
	public void setOnSendListener(View.OnClickListener onSend) {
		this.onSend=onSend;
	}
	
	public String getText() {
		return(status.getText().toString());
	}
	
	public void setText(String text) {
		status.setText(text);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		((Activity)getContext())
			.getLayoutInflater()
			.inflate(R.layout.status_entry, this);
		
		if (labelCaption!=null) {
			TextView label=(TextView)findViewById(R.id.label);
			
			label.setText(labelCaption);
		}
		
		send=(Button)findViewById(R.id.send);
		
		if (buttonCaption!=null) {
			send.setText(buttonCaption);
		}
		
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (onSend!=null) {
					onSend.onClick(StatusEntryView.this);
				}
			}
		});
		
		status=(EditText)findViewById(R.id.status);
	}
}