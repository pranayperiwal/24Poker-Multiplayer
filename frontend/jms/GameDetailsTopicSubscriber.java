package frontend.jms;

import java.time.Instant;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GameDetailsTopicSubscriber {

	private String host;

	private Instant start;

	private TopicConnectionFactory connectionFactory;
	private TopicConnection connection;
	private TopicSubscriber subscriber;
	private TopicSession session;
	private Topic onlineUsersTopic;

	public GameDetailsTopicSubscriber(String host) throws NamingException, JMSException {
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

			connectionFactory = (TopicConnectionFactory) jndiContext.lookup("jms/AllOnlineUsersConnectionFactory");

		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}

	private void lookupTopic() throws NamingException {

		try {
			onlineUsersTopic = (Topic) jndiContext.lookup("jms/AllOnlineUsersTopic");
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
			subscriber = session.createSubscriber(onlineUsersTopic, null, true);

		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public StartGameDetails receiveGameDetails() throws JMSException {
		Message message = subscriber.receive(); // blocking function
		try {
			TextMessage payload = (TextMessage) message;
			String data = payload.getText();
			String cards = data.split("\t")[0];
			String usersString = data.split("\t")[1];
			int gameID = Integer.parseInt(data.split("\t")[2]);
			String[] users = usersString.substring(1, usersString.length() - 1).split(", ");

			return (new StartGameDetails(cards, users, gameID));

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			System.err.println("Exception in receiving game details object in subcriber");
			e.printStackTrace();
		}

		return null;
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
