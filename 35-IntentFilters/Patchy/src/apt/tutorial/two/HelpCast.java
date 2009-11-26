package apt.tutorial.two;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;

public class HelpCast extends Activity {
	private VideoView video;
	private MediaController ctlr;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.helpcast);
	
		File clip=new File("/sdcard/.Patchy/helpcast.mp4");
		
		if (clip.exists()) {
			video=(VideoView)findViewById(R.id.video);
			video.setVideoPath(clip.getAbsolutePath());
			
			ctlr=new MediaController(this);
			ctlr.setMediaPlayer(video);
			video.setMediaController(ctlr);
			video.requestFocus();
		}
	}
}