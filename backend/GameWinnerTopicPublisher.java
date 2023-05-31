package backend;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GameWinnerTopicPublisher {

	private String host;

	private TopicConnectionFactory connectionFactory;
	private TopicConnection connection;
	private TopicPublisher publisher;
	private TopicSession session;
	private Topic onlineUsersTopic;

	public GameWinnerTopicPublisher(String host) throws NamingException, JMSException {
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
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("[GameWinnerTopicPublisher] Could not create JNDI API context: " + e);
			e.printStackTrace();
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
			onlineUsersTopic = (Topic) jndiContext.lookup("jms/GameWinnerTopic");
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
			publisher = session.createPublisher(onlineUsersTopic);

		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void publishMessage(String username, int gameID, String formula) throws JMSException {

		String msg = gameID + "\t" + username + "\t" + formula;

		TextMessage message = session.createTextMessage(msg);
		publisher.send(message);

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
