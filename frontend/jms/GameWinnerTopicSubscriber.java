package frontend.jms;

import java.awt.CardLayout;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JPanel;

import frontend.MainGameUI;

public class GameWinnerTopicSubscriber {

	private String host;

	private TopicConnectionFactory connectionFactory;
	private TopicConnection connection;
	private TopicSubscriber subscriber;
	private TopicSession session;
	private Topic gameWinnerTopic;
	private String winnerDetails = "";

	private int gameID;

	public GameWinnerTopicSubscriber(String host) throws NamingException, JMSException {
		this.host = host;

		// Access JNDI
		createJNDIContext();

		// Lookup JMS resources
		lookupConnectionFactory();
		lookupTopic();

		// Create connection->session->sender
		createConnection();

		createSession();
		createPublisher();
	}

	private InitialContext jndiContext;

	private void createJNDIContext() throws NamingException {
//		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
//		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}

	private void lookupConnectionFactory() throws NamingException {

		try {

			connectionFactory = (TopicConnectionFactory) jndiContext.lookup("jms/GameWinnerConnectionFactory");

		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}

	private void lookupTopic() throws NamingException {

		try {
			gameWinnerTopic = (Topic) jndiContext.lookup("jms/GameWinnerTopic");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}

	private void createConnection() throws JMSException {
		try {
			connection = connectionFactory.createTopicConnection();
			connection.start();

		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}

	private void createSession() throws JMSException {
		try {
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	private void createPublisher() throws JMSException {
		try {
			subscriber = session.createSubscriber(gameWinnerTopic, null, true);

		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void receiveWinnerDetails(CardLayout cardLayout, JPanel cardPanel, MainGameUI mainGame) throws JMSException {
		this.gameID = mainGame.getGameID();
		subscriber.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message m) {
				if (m != null && m instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) m;
					String msg;
					try {
						msg = textMessage.getText();

						int gameIDMessage = Integer.parseInt(msg.split("\t")[0]);
						String winner = msg.split("\t")[1];
						String formula = msg.split("\t")[2];
						if (gameIDMessage == gameID) {
							mainGame.setWinner(winner);
							mainGame.setWinningFormula(formula);
							cardLayout.show(cardPanel, "game over");

						}
					} catch (JMSException e) {
						e.printStackTrace();
					}

				}

			}

		});

	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
			}
		}
	}

}
