package apt.tutorial.two;

interface ITwitterListener {
	void newFriendStatus(String friend, String status,
												String createdAt);
}