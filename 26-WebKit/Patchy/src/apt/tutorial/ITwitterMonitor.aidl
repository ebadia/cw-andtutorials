package apt.tutorial;

import apt.tutorial.ITwitterListener;

interface ITwitterMonitor {
	void registerAccount(String user, String password,
												ITwitterListener callback);
	void removeAccount(ITwitterListener callback);
	void setBestFriends(ITwitterListener callback,
												in List<String> bff);
}