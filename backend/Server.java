package backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class Server extends UnicastRemoteObject implements Serve, Serializable {

	private static final long serialVersionUID = 1L;
	private String userInfoFile = "UserInfo.txt";
	private String onlineUserFile = "OnlineUser.txt";

	private String host = "localhost";

	private GameWinnerTopicPublisher gameWinnerPublisher;

	public static void main(String[] args) {
		try {
			Server app = new Server();

			String network = args[0];

			System.setProperty("java.rmi.server.hostname", network);

			System.setSecurityManager(new SecurityManager());

			Naming.rebind("AssignmentServer", app);
			System.out.println("Service Registered");

			// setup the queue to check for number of users online who ready to play
			// and start game when ready
			app.receiveActiveGameUserQueue();

			// setup the publisher to send winner of each game to all users
			app.gameWinnerPublisher = new GameWinnerTopicPublisher(app.host);

		} catch (Exception e) {
			System.err.println("Exception thrown: " + e);
		}
	}

	public Server() throws RemoteException {
		try {
			// resets the OnlineUser txt file
			BufferedWriter writerOnlineUser = new BufferedWriter(new FileWriter(onlineUserFile));
			writerOnlineUser.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

	}

	// handle AcitveGameUserQueueReceiver
	public void receiveActiveGameUserQueue() {

		ActiveGameUsersQueueReceiver usersReceiver = null;

		try {
			usersReceiver = new ActiveGameUsersQueueReceiver(host, new GameDetailsTopicPublisher(host));
			usersReceiver.receiveMessages();// sets up the listener to receive users

		} catch (NamingException | JMSException e) {
			System.err.println("Program aborted");
			if (usersReceiver != null) {
				try {
					usersReceiver.close();
				} catch (Exception ex) {
				}
			}
		}

	}

	@Override
	// login function for user
	public String login(String username, String password) throws RemoteException {
		System.out.println("Login attempt: " + username);
		try {

			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));
			String line = readerUserInfo.readLine();
			while (line != null) {
				String user = line.split(" ")[0];
				if (username.equals(user)) {
					String pass = line.split(" ")[1];
					if (password.equals(pass)) {

						// must check if already online
						BufferedWriter writerOnlineUser = new BufferedWriter(new FileWriter(onlineUserFile, true));
						BufferedReader readerOnlineUser = new BufferedReader(new FileReader(onlineUserFile));

						// check if user is online already
						String onlineUser = readerOnlineUser.readLine();
						while (onlineUser != null) {

							if (onlineUser.equals(username)) {
								readerOnlineUser.close();
								writerOnlineUser.close();
								readerUserInfo.close();
								return "user already online";
							}

							onlineUser = readerOnlineUser.readLine();
						}

						// if user not online, add to onlineUser list
						writerOnlineUser.write(username);
						writerOnlineUser.newLine();

						readerOnlineUser.close();
						writerOnlineUser.close();
						readerUserInfo.close();

						System.out.println("Login successful: " + username);
						return line;
					} else {
						readerUserInfo.close();
						return "password incorrect";
					}
				}
				line = readerUserInfo.readLine();

			}
			readerUserInfo.close();

		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
		return "user not found";

	}

	@Override
	// register function for user
	public synchronized String register(String username, String password) throws RemoteException {
		System.out.println("Registering new user: " + username);
		try {
			BufferedWriter writerUserInfo = new BufferedWriter(new FileWriter(userInfoFile, true));
			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));
			BufferedWriter writerOnlineUser = new BufferedWriter(new FileWriter(onlineUserFile, true));

			// check the username is not used
			int userCount = 0;
			String line = readerUserInfo.readLine();

			while (line != null) {
				userCount++;
				String user = line.split(" ")[0];
				if (user.equals(username)) {
					writerUserInfo.close();
					readerUserInfo.close();
					writerOnlineUser.close();
					return "username used";
				}

				line = readerUserInfo.readLine();
			}

			// if username has not been used

			// register user
			int winCount = 0, gameCount = 0, avgtimetowin = 0, rank = userCount + 1;
			String userLine = username + " " + password + " " + winCount + " " + gameCount + " " + avgtimetowin + " "
					+ rank;
			writerUserInfo.write(userLine);
			writerUserInfo.newLine();

			// write user to onlineUser file
			writerOnlineUser.write(username);
			writerOnlineUser.newLine();

			writerOnlineUser.close();
			writerUserInfo.close();
			readerUserInfo.close();
			return userLine;

		} catch (IOException e) {

			System.err.println("Error reading file: " + e.getMessage());
			e.printStackTrace();
		}

		return "error";
	}

	@Override
	public synchronized boolean logout(String username) throws RemoteException {
		try {
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader readerOnlineUser = new BufferedReader(new FileReader(onlineUserFile));

			// read the lines and find the user
			int lineToBeDeleted = 0;
			String onlineUser = readerOnlineUser.readLine();
			while (onlineUser != null) {
				lines.add(onlineUser);
				if (onlineUser.equals(username)) {
					lineToBeDeleted = lines.size() - 1;
				}
				onlineUser = readerOnlineUser.readLine();
			}

			// remove the online user from lines
			lines.remove(lineToBeDeleted);

			BufferedWriter writerOnlineUser = new BufferedWriter(new FileWriter(onlineUserFile));
			// write the lines to the file again
			for (String line : lines) {
				writerOnlineUser.write(line);
				writerOnlineUser.newLine();
			}

			readerOnlineUser.close();
			writerOnlineUser.close();

		} catch (IOException e) {

			System.err.println("Error reading file: " + e.getMessage());
		}

		return false;
	}

	@Override
	public String getAllUsers() throws RemoteException {
		try {
			String allContent = "";
			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			String line = readerUserInfo.readLine();
			while (line != null) {
				allContent += line + "\n";
				line = readerUserInfo.readLine();
			}

			readerUserInfo.close();

			return allContent;
		} catch (IOException e) {

			System.err.println("Error reading file: " + e.getMessage());
		}
		return "";
	}

	@Override
	public String getUser(String username) {
		try {
			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			String line = readerUserInfo.readLine();
			while (line != null) {
				if (line.split(" ")[0].equals(username)) {
					readerUserInfo.close();
					return line;
				}
				line = readerUserInfo.readLine();
			}

			readerUserInfo.close();

		} catch (IOException e) {

			System.err.println("Error reading file: " + e.getMessage());
		}
		return "";
	}

	// function to get the stats of the users in the current game
	@Override
	public String getGameUserStats(String username) throws RemoteException {
		try {
			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			String line = readerUserInfo.readLine();
			while (line != null) {
				if (line.split(" ")[0].equals(username)) {
					readerUserInfo.close();
					return line.split(" ")[2] + " " + line.split(" ")[4];
				}
				line = readerUserInfo.readLine();
			}

			readerUserInfo.close();

		} catch (IOException e) {

			System.err.println("Error reading file: " + e.getMessage());
		}
		return "";
	}

	public synchronized void userWon(String username, int gameID, String formula, double time) throws RemoteException {
		// needs to publish a message to a WinnersTopic including the game ID
		// the clients will filter the message based on game ID

		try {
			gameWinnerPublisher.publishMessage(username, gameID, formula);

			// update the winners stats
			updateWinnerStats(username, time);

			// update the ranks
			updateRanks();

		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	private synchronized void updateWinnerStats(String username, double time) {

		// find the user, update the winCount and avgTime

		try {
			File tempUserInfoFile = new File("NewUserInfo.txt");
			File userInfoFile = new File("UserInfo.txt");

			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			BufferedWriter writerUserInfoNewFile = new BufferedWriter(new FileWriter(tempUserInfoFile));

			String line = readerUserInfo.readLine();

			while (line != null) {
				String user = line.split(" ")[0];
				if (user.equals(username)) {

					String[] userDetails = line.split(" ");

					// update win count
					userDetails[2] = String.valueOf(Integer.parseInt(userDetails[2]) + 1);

					// update avg time to win

					// if game count == 0
					if (userDetails[3].equals("0")) {
						userDetails[4] = String.valueOf(time);
					} else {
						double avg = ((Double.parseDouble(userDetails[4]) * (Double.parseDouble(userDetails[3])))
								+ time) / (Double.parseDouble(userDetails[3]) + 1);
						userDetails[4] = String.valueOf(Math.round(avg * 100) / 100.0);
					}

					// join the userDetails again
					String userLine = String.join(" ", userDetails);

					System.out.println("Winning user: " + userLine.split(" ")[0]);

					// update the new text file
					writerUserInfoNewFile.write(userLine);
					writerUserInfoNewFile.newLine();

				} else {
					// if not winning user, rewrite old file content
					writerUserInfoNewFile.write(line);
					writerUserInfoNewFile.newLine();
				}

				line = readerUserInfo.readLine();
			}

			tempUserInfoFile.renameTo(userInfoFile);

			readerUserInfo.close();
			writerUserInfoNewFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private synchronized void updateRanks() {
		// username, winCount
		ArrayList<String[]> usersWinCount = new ArrayList<>();

		// loop over all lines and add to arraylist
		try {
			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			String line = readerUserInfo.readLine();
			while (line != null) {
				String[] values = { line.split(" ")[0], line.split(" ")[2] };
				usersWinCount.add(values);
				line = readerUserInfo.readLine();
			}
			readerUserInfo.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		// sort the array
		String[][] usersWinCountArray = usersWinCount.toArray(new String[0][]);
		Arrays.sort(usersWinCountArray, new Comparator<String[]>() {
			@Override
			public int compare(final String[] entry1, final String[] entry2) {
				return Integer.compare(Integer.parseInt(entry2[1]), Integer.parseInt(entry1[1]));
			}
		});

		// arraylist of usernames in sorted order of winCount
		ArrayList<String> sortedUsernames = new ArrayList<>();

		for (String[] user : usersWinCountArray) {
			sortedUsernames.add(user[0]);
		}

		// update the ranks
		try {
			File tempUserInfoFile = new File("NewUserInfo.txt");
			File userInfoFile = new File("UserInfo.txt");

			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			BufferedWriter writerUserInfoNewFile = new BufferedWriter(new FileWriter(tempUserInfoFile));

			String line = readerUserInfo.readLine();

			while (line != null) {
				String[] userDetails = line.split(" ");

				int index = sortedUsernames.indexOf(userDetails[0]);

				userDetails[5] = String.valueOf(index + 1);

				String updatedRankUserLine = String.join(" ", userDetails);

				// update the new text file
				writerUserInfoNewFile.write(updatedRankUserLine);
				writerUserInfoNewFile.newLine();

				line = readerUserInfo.readLine();

			}

			tempUserInfoFile.renameTo(userInfoFile);

			readerUserInfo.close();
			writerUserInfoNewFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// update the game count of the users who just finished playing the game
	@Override
	public synchronized void updateUserGameCount(String username) throws RemoteException {
		try {
			File tempUserInfoFile = new File("NewUserInfo.txt");
			File userInfoFile = new File("UserInfo.txt");

			BufferedReader readerUserInfo = new BufferedReader(new FileReader(userInfoFile));

			BufferedWriter writerUserInfoNewFile = new BufferedWriter(new FileWriter(tempUserInfoFile));

			String line = readerUserInfo.readLine();

			while (line != null) {
				String user = line.split(" ")[0];
				if (user.equals(username)) {

					String[] userDetails = line.split(" ");

					// update game count
					userDetails[3] = String.valueOf(Integer.parseInt(userDetails[3]) + 1);

					// join the userDetails again
					String userLine = String.join(" ", userDetails);

					// update the new text file
					writerUserInfoNewFile.write(userLine);
					writerUserInfoNewFile.newLine();

				} else {
					// if not winning user, rewrite old file content
					writerUserInfoNewFile.write(line);
					writerUserInfoNewFile.newLine();
				}

				line = readerUserInfo.readLine();
			}

			tempUserInfoFile.renameTo(userInfoFile);

			readerUserInfo.close();
			writerUserInfoNewFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
