package frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.JPanel;

import backend.Serve;
import frontend.jms.GameWinnerTopicSubscriber;

public class GamePlayingPanel extends JPanel {

	private CardLayout cardLayout;
	private JPanel cardPanel;

	JPanel gameCardsPanel, playerPanel, inputPanel;

	GameJoiningPanel gameJoiningPanel;

	Serve server;

	private String mainUser;
//	private Integer gameID;
	private MainGameUI mainGame;

	private long gameStartTime = 0;
	private long gameEndTime = 0;

	public long getGameEndTime() {
		return gameEndTime;
	}

	public void setGameEndTime(long gameEndTime) {
		this.gameEndTime = gameEndTime;
	}

	public long getGameStartTime() {
		return gameStartTime;
	}

	public void setGameStartTime(long gameStartTime) {
		this.gameStartTime = gameStartTime;
	}

	public GamePlayingPanel(CardLayout cardLayout, JPanel cardPanel, JPanel gameJoiningPanel, Serve server,
			MainGameUI mainGame) {

		this.server = server;

		this.cardLayout = cardLayout;
		this.cardPanel = cardPanel;

		this.gameJoiningPanel = (GameJoiningPanel) gameJoiningPanel;

		this.mainUser = this.gameJoiningPanel.getUsername();
		this.mainGame = mainGame;

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent evt) {
				startGame();
				System.out.println("start game");
			}
		});

		setLayout(new BorderLayout());

	}

	public void startGame() {

		System.out.println("Game starting stage");

		String[] gameCards = gameJoiningPanel.getCards().split(" ");
		String[] users = gameJoiningPanel.getUsers();
		String host = "localhost";
		GameWinnerTopicSubscriber winnerSubscriber = null;
		// create subscriber
		try {
			winnerSubscriber = new GameWinnerTopicSubscriber(host);
			winnerSubscriber.receiveWinnerDetails(cardLayout, cardPanel, mainGame);
//			System.out.println("winner:" + winnerDetails);
		} catch (NamingException | JMSException e1) {
			e1.printStackTrace();
		}

		// RMI call to get the player details
		GameUserDetails[] usersDetails = new GameUserDetails[users.length];

		for (int i = 0; i < users.length; i++) {
			try {
				String userDetails = server.getGameUserStats(users[i]);
				usersDetails[i] = new GameUserDetails(users[i], Integer.parseInt(userDetails.split(" ")[0]),
						Double.parseDouble(userDetails.split(" ")[1]));

			} catch (RemoteException e) {
				System.err.println("Failed invoking RMI for register: " + e.getMessage());
			}

		}

		gameCardsPanel = new GameCardsPanel(gameCards);

		playerPanel = new GamePlayerPanel(usersDetails);

//		gameStartTime = System.currentTimeMillis();
//		System.out.println("start time reset?: " + gameStartTime);
		inputPanel = new GameInputPanel(server, mainUser, mainGame, this);

		this.add(gameCardsPanel, BorderLayout.CENTER);
		this.add(playerPanel, BorderLayout.EAST);
		this.add(inputPanel, BorderLayout.SOUTH);
		this.validate();

	}

	// stores the details for the users in the game
	public class GameUserDetails {
		String username;
		int winCount;
		double avgTime;

		public GameUserDetails(String username, int winCount, double avgTime) {

			this.username = username;
			this.winCount = winCount;
			this.avgTime = avgTime;
		}

		public int getWinCount() {
			return winCount;
		}

		public double getAvgTime() {
			return avgTime;
		}

		public String getUsername() {
			return username;
		}

	}
}
