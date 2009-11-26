package apt.tutorial.two;

import android.app.Activity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.Contacts;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import winterwell.jtwitter.Twitter;

public class FriendsCursor extends MatrixCursor {
	private static final String[] COLUMNS={"screenName",
																					"isContact",
																					"_id"};
	private static final String[] PROJECTION=new String[] {
																				Contacts.People._ID,
																												};
	
	FriendsCursor() {
		super(COLUMNS);
	}
	
	void populate(Twitter client, final Activity ctxt) {
		final ArrayList<String> screenNames=new ArrayList<String>();
		
		try {
			for (Twitter.User u : client.getFriends()) {
				screenNames.add(u.screenName);
			}
		}
		catch (Throwable t) {
			Log.e("FriendsCursor",
						"Exception in JTwitter#getFriends()", t);
		}
		
		Collections.sort(screenNames);
		
		ctxt.runOnUiThread(new Runnable() {
			public void run() {
				int i=0;
				
				for (String u : screenNames) {
					int isContact=0;
					String[] args={u};
					StringBuffer query=new StringBuffer(Contacts.OrganizationColumns.LABEL);
					
					query.append("='Twitter' AND ");
					query.append(Contacts.OrganizationColumns.COMPANY);
					query.append("=?");
					
					Cursor c=ctxt.managedQuery(Contacts.Organizations.CONTENT_URI,
																			PROJECTION,
																			query.toString(),
																			args,
																			Contacts.People.DEFAULT_SORT_ORDER);
					
					if (c.getCount()>0) {
						isContact=1;
					}
					
					c.close();
					
					newRow().add(u).add(isContact).add(i++);
				}
			}
		});
	};
}