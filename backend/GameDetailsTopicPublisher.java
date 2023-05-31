package backend;

import java.util.ArrayList;
import java.util.Arrays;

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

public class GameDetailsTopicPublisher {

	private String host;

	private TopicConnectionFactory connectionFactory;
	private TopicConnection connection;
	private TopicPublisher publisher;
	private TopicSession session;
	private Topic onlineUsersTopic;

	public GameDetailsTopicPublisher(String host) throws NamingException, JMSException {
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
//		Properties properties = new Properties();
//		properties.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
//		properties.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
//		properties.setProperty("java.naming.provider.url", "iiop://localhost:3700");
//		properties.put("java.naming.provider.url", "jnp://1.2.3.4:3700");
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
//			jndiContext = new InitialContext(properties);
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("[GameDetailsTopicPublisher] Could not create JNDI API context: " + e);
			e.printStackTrace();
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
			publisher = session.createPublisher(onlineUsersTopic);

		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void publishMessage(String cards, ArrayList<String> users, int gameID) throws JMSException {

		String[] usersArray = users.toArray(new String[0]);
		String msg = cards + "\t" + Arrays.toString(usersArray) + "\t" + gameID;

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
