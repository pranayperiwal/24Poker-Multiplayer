package frontend.jms;

public class StartGameDetails {
	private String cards;
	private String[] users;
	private int gameID;

	public StartGameDetails(String cards, String[] users, int gameID) {
		this.cards = cards;
		this.users = users;
		this.gameID = gameID;
	}

	public String getCards() {
		return cards;
	}

	public String[] getUsers() {
		return users;
	}

	public int getGameID() {
		return gameID;
	}
}