package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ActiveGameUsersQueueReceiver {

	private String host;
	private ArrayList<String> users = new ArrayList<String>();
	private long startTime;
	private Timer timer;
	private int gameCount = 0;

	private GameDetailsTopicPublisher gameStartPublisher;
//	private ArrayList<GameDetailsTopicPublisher> gameStartPublisherList;

	public ActiveGameUsersQueueReceiver(String host, GameDetailsTopicPublisher gameStartPublisher)
			throws NamingException, JMSException {
		this.host = host;
		this.gameStartPublisher = gameStartPublisher;
//		this.gameStartPublisherList = gameStartPublisherList;

		// Access JNDI
		createJNDIContext();

		// Lookup JMS resources
		lookupConnectionFactory();
		lookupQueue();

		// Create connection->session->sender
		createConnection();

	}

	private InitialContext jndiContext;

	private void createJNDIContext() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("[ActiveGameUsersQueueReceiver] Could not create JNDI API context: " + e);
			throw e;
		}
	}

	private ConnectionFactory connectionFactory;

	private void lookupConnectionFactory() throws NamingException {

		try {
			connectionFactory = (ConnectionFactory) jndiContext.lookup("jms/ActiveGameUsersConnectionFactory");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}

	private Queue queue;

	private void lookupQueue() throws NamingException {

		try {
			queue = (Queue) jndiContext.lookup("jms/ActiveGameUsersQueue");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}

	private Connection connection;

	private void createConnection() throws JMSException {
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}

	public void receiveMessages() throws JMSException {
		createSession();
		createReceiver();

	}

	private Session session;

	private void createSession() throws JMSException {
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	private MessageConsumer queueReceiver;

	private void createReceiver() throws JMSException {
		try {
			queueReceiver = session.createConsumer(queue);

			queueReceiver.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message m) {

					if (m != null && m instanceof TextMessage) {
						// start the timer if this is the first user
						if (users.size() == 0) {
							startTime = System.currentTimeMillis();

							// schedule a task for after 10s to start game
							timer = new Timer();
							timer.schedule(new StartGameTimer(), 10000);
						}

						TextMessage textMessage = (TextMessage) m;

						String user;

						try {
							user = textMessage.getText();
							long duration = System.currentTimeMillis() - startTime;

							users.add(user);

							// if new user joins (after the first) and timer is more than 10s
							if (duration >= 10000) {
								// game should start

								System.out.println(
										"Starting game as its more than 10s since 1st user joined and 2nd user just joined");

								startGame();

							} else {

								// if 4 users have joined
								if (users.size() == 4) {
									// game should start
									System.out.println("Starting game as 4th user joined");
									startGame();
									timer.cancel();
									timer.purge();

								} else {
									System.out.println("user joined but stil waiting");
								}

							}

						} catch (JMSException e) {
							e.printStackTrace();
						}

					}

				}
			});

		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
			}
		}
	}

	private void startGame() {
		try {
			String cards = selectCards();

			// publish to the topic
			gameStartPublisher.publishMessage(cards, users, gameCount++);
			System.out.println("Game count in publisher: " + (gameCount - 1));

			// remove users from the array list
			users.clear();

		} catch (JMSException e1) {
			System.err.println("Error in creating gameStartPublisher:");
			e1.printStackTrace();
		}

	}

	// selects 4 random cards
	private String selectCards() {
		String cards = "";

		HashMap<Integer, String> suits = new HashMap<Integer, String>();
		suits.put(0, "c");
		suits.put(1, "d");
		suits.put(2, "h");
		suits.put(3, "s");

		HashMap<Integer, String> faceCards = new HashMap<Integer, String>();
		faceCards.put(1, "a");
		faceCards.put(11, "j");
		faceCards.put(12, "q");
		faceCards.put(13, "k");

		Random rand = new Random();

		for (int i = 0; i < 4; i++) {
			// chose a random number between 1 and 13
			String cardName = "";
			int cardNumber = rand.nextInt(13) + 1;
			if (cardNumber == 1 || cardNumber >= 11) {
				cardName += faceCards.get(cardNumber);
			} else {
				cardName += cardNumber;
			}

			// chose a random char between between 0 and 3
			int cardSuit = rand.nextInt(4);
			cardName += suits.get(cardSuit);

			cards += cardName + " ";

		}

		return cards;

	}

	private class StartGameTimer extends TimerTask {

		@Override
		public void run() {
			System.out.println("Its been more than 10s");
			if (users.size() > 1) {
				// start game
				System.out.println("Start game because more than 1 user and more than 10s");
				startGame();
			}

		}

	}
}
