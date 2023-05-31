package frontend;

import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.jms.ActiveGameUsersQueueSender;
import frontend.jms.GameDetailsTopicSubscriber;
import frontend.jms.StartGameDetails;

public class GameJoiningPanel extends JPanel {
	private CardLayout cardLayout;
	private JPanel cardPanel;
	private String username;
	private String cards;
	private String[] users;
	private Integer gameID;
	private MainGameUI mainGame;
	private ActiveGameUsersQueueSender joiningGameSender = null;
	private GameDetailsTopicSubscriber gameDetailsSubscriber = null;

	public GameJoiningPanel(CardLayout cardLayout, JPanel cardPanel, String username, Integer gameID, String cards,
			String[] users, MainGameUI mainGame) {

		this.cardLayout = cardLayout;
		this.cardPanel = cardPanel;
		this.username = username;

		this.cards = cards;
		this.users = users;
		this.mainGame = mainGame;

		String host = "localhost";

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent evt) {
				startWaiting();
			}
		});

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel label = new JLabel("Waiting for players...");
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		add(Box.createVerticalGlue());

		add(Box.createHorizontalGlue());
		add(label);

		add(Box.createHorizontalGlue());
		add(Box.createVerticalGlue());

	}

	public void startWaiting() {
		System.out.println("Game joining stage");
		// send message to ActiveGameUsers queue with username

		String host = "localhost";

		try {
			joiningGameSender = new ActiveGameUsersQueueSender(host, username);
			gameDetailsSubscriber = new GameDetailsTopicSubscriber(host);

			joiningGameSender.sendMessages();

			StartGameDetails gameDetails = gameDetailsSubscriber.receiveGameDetails();

			cards = gameDetails.getCards();
			users = gameDetails.getUsers();
			gameID = gameDetails.getGameID();
			mainGame.setGameID(gameID);
			System.out.println("Cards:" + cards);
			System.out.println("Users:" + Arrays.toString(users));
			System.out.println("GameID:" + mainGame.getGameID());

		} catch (NamingException | JMSException e) {
			System.err.println("Program aborted");
		} finally {
			if (joiningGameSender != null) {
				try {
					joiningGameSender.close();
				} catch (Exception e) {
				}
			}
		}

		cardLayout.show(cardPanel, "game playing");
	}

	public String getCards() {
		return cards;
	}

	public String[] getUsers() {
		return users;
	}

	public Integer getGameID() {
		return gameID;
	}

	public String getUsername() {
		return username;
	}

}
