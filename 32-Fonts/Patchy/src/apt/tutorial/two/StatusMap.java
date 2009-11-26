package apt.tutorial.two;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class StatusMap extends MapActivity {
	private MapView map=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_map);
		
		map=(MapView)findViewById(R.id.map);
		
		map.getController().setZoom(17);
		
		double lat=getIntent().getDoubleExtra(Patchy.LATITUDE, 0);
		double lon=getIntent().getDoubleExtra(Patchy.LONGITUDE, 0);
		
		GeoPoint status=new GeoPoint((int)(lat*1000000.0),
																	(int)(lon*1000000.0));
		
		map.getController().setCenter(status);
		map.setBuiltInZoomControls(true);
		
		String statusText=getIntent().getStringExtra(Patchy.STATUS_TEXT);
		Drawable marker=getResources().getDrawable(R.drawable.marker);
		
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
														marker.getIntrinsicHeight());
		
		map.getOverlays().add(new StatusOverlay(marker, status,
																							statusText));
	}
	
 	@Override
	protected boolean isRouteDisplayed() {
		return(false);
	}
		
	private class StatusOverlay extends ItemizedOverlay<OverlayItem> {
		private OverlayItem item=null;
		private Drawable marker=null;
		
		public StatusOverlay(Drawable marker, GeoPoint status,
													String statusText) {
			super(marker);
			this.marker=marker;
			
			item=new OverlayItem(status, "Tweet!", statusText);

			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
			return(item);
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView,
											boolean shadow) {
			super.draw(canvas, mapView, shadow);
			
			boundCenterBottom(marker);
		}
 		
		@Override
		protected boolean onTap(int i) {
			Toast.makeText(StatusMap.this,
											item.getSnippet(),
											Toast.LENGTH_SHORT).show();
			
			return(true);
		}
		
		@Override
		public int size() {
			return(1);
		}
	}
}