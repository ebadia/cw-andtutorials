package apt.tutorial;

interface ITwitterListener {
	void newFriendStatus(String friend, String status,
												String createdAt);
}