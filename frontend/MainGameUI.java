package frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import backend.Serve;

public class MainGameUI extends JFrame {
	private JPanel buttonPanel, userProfilePanel, playGameMainPanel, leaderBoardPanel, cardPanel;

	private JPanel gameInitialPanel, gameJoiningPanel, gamePlayingPanel, gameOverPanel;
	private JButton newGameButton;

	private CardLayout cardLayout;

	private Serve server;

	private String cards;
	private String[] users;
	private Integer gameID;

	private String winner;
	private String winningFormula;

	public MainGameUI(String user, Serve server) {
		this.server = server;
		String username = user.split(" ")[0];

		// Create the button panel and add some buttons to it
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		// user profile button
		JButton userProfileButton = new JButton("User Profile");
		userProfileButton.addActionListener(e -> {
			cardLayout.show(cardPanel, "user profile");
		});

		// play game button
		JButton plaGameButton = new JButton("Play Game");
		plaGameButton.addActionListener(e -> {
			cardLayout.show(cardPanel, "game initial");
		});

		// leaderboard button
		JButton leaderboardButton = new JButton("Leader Board");
		leaderboardButton.addActionListener(e -> {
			cardLayout.show(cardPanel, "leaderboard");
		});

		// logout button
		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> {
			try {
				server.logout(username);
				dispose(); // Close the JFrame
				System.exit(0);
			} catch (RemoteException ex) {

				System.err.println("Failed invoking RMI for register: " + ex.getMessage());
			}

		});

		buttonPanel.add(userProfileButton);
		buttonPanel.add(plaGameButton);
		buttonPanel.add(leaderboardButton);
		buttonPanel.add(logoutButton);

		// create card panel which will contain the main content
		cardPanel = new JPanel();
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		// create the user profile panel
		userProfilePanel = new UserProfilePanel(user, server);

		// create the initial game panel
		gameInitialPanel = new JPanel();
		newGameButton = new JButton("New Game");
		newGameButton.addActionListener(e -> {
			cardLayout.show(cardPanel, "game joining");

		});
		gameInitialPanel.setLayout(new BorderLayout());
		gameInitialPanel.add(newGameButton, BorderLayout.CENTER);

		// create the game joining panel
		gameJoiningPanel = new GameJoiningPanel(cardLayout, cardPanel, username, gameID, cards, users, this);

		// create the game playing panel
		gamePlayingPanel = new GamePlayingPanel(cardLayout, cardPanel, gameJoiningPanel, server, this);

		// create the game over panel
		gameOverPanel = new GameOverPanel(cardLayout, cardPanel, username, this, server);

		// create the leaderboard panel
		leaderBoardPanel = new LeaderboardPanel(server);

		// add panels to card layout panel
		cardPanel.add("user profile", userProfilePanel);
		cardPanel.add("game initial", gameInitialPanel);
		cardPanel.add("game joining", gameJoiningPanel);
		cardPanel.add("game playing", gamePlayingPanel);
		cardPanel.add("game over", gameOverPanel);
		cardPanel.add("leaderboard", leaderBoardPanel);

		// add button panel and card panel
		add(buttonPanel, BorderLayout.NORTH);
		add(cardPanel, BorderLayout.CENTER);

		// Set the frame properties
		setTitle("JPoker 24-Game");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(new Dimension(600, 400));
		setLocationRelativeTo(null);
		setVisible(true);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				try {
					server.logout(username);
					dispose(); // Close the JFrame
					System.exit(0);
				} catch (RemoteException e) {

					System.err.println("Failed invoking RMI for register: " + e.getMessage());
				}

			}
		});
	}

	public CardLayout getCardLayout() {
		return cardLayout;
	}

	public JPanel getCardPanel() {
		return cardPanel;
	}

	public void setGameID(Integer gameID) {
		this.gameID = gameID;
	}

	public Integer getGameID() {
		return gameID;
	}

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public String getWinningFormula() {
		return winningFormula;
	}

	public void setWinningFormula(String winningFormula) {
		this.winningFormula = winningFormula;
	}

}
