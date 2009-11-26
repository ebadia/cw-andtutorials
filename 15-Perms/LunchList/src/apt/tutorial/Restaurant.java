package apt.tutorial;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Restaurant {
	private String name="";
	private String address="";
	private String type="";
	private String notes="";
	private String phoneNumber="";
	
	static Cursor getAll(SQLiteDatabase db, String orderBy) {
		return(db.rawQuery("SELECT * FROM restaurants "+orderBy,
												null));
	}
	
	static Restaurant getById(String id, SQLiteDatabase db) {
		String[] args={id};
		Cursor c=db.rawQuery("SELECT * FROM restaurants WHERE _id=?",
													args);
		
		c.moveToFirst();
		
		Restaurant result=new Restaurant().loadFrom(c);
		
		c.close();
		
		return(result);
	}
	
	Restaurant loadFrom(Cursor c) {
		name=c.getString(c.getColumnIndex("name"));
		address=c.getString(c.getColumnIndex("address"));
		type=c.getString(c.getColumnIndex("type"));
		notes=c.getString(c.getColumnIndex("notes"));
		phoneNumber=c.getString(c.getColumnIndex("phoneNumber"));
		
		return(this);
	}
	
	public String getName() {
		return(name);
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getAddress() {
		return(address);
	}
	
	public void setAddress(String address) {
		this.address=address;
	}
	
	public String getType() {
		return(type);
	}
	
	public void setType(String type) {
		this.type=type;
	}
	
	public String getNotes() {
		return(notes);
	}
	
	public void setNotes(String notes) {
		this.notes=notes;
	}
	
	public String getPhoneNumber() {
		return(phoneNumber);
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber=phoneNumber;
	}
	
	public String toString() {
		return(getName());
	}
	
	void save(SQLiteDatabase db) {
		ContentValues cv=new ContentValues();
					
		cv.put("name", name);
		cv.put("address", address);
		cv.put("type", type);
		cv.put("notes", notes);
		cv.put("phoneNumber", phoneNumber);
		db.insert("restaurants", "name", cv);
	}
	
	void update(String id, SQLiteDatabase db) {
		ContentValues cv=new ContentValues();
		String[] args={id};
					
		cv.put("name", name);
		cv.put("address", address);
		cv.put("type", type);
		cv.put("notes", notes);
		cv.put("phoneNumber", phoneNumber);
		db.update("restaurants", cv, "_id=?", args);
	}
}