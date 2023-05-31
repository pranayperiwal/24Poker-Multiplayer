package backend;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Serve extends Remote {
	String login(String username, String password) throws RemoteException;

	String register(String username, String password) throws RemoteException;

	boolean logout(String username) throws RemoteException;

	String getAllUsers() throws RemoteException;

	String getUser(String username) throws RemoteException;

	String getGameUserStats(String username) throws RemoteException;

	void userWon(String username, int gameID, String formula, double time) throws RemoteException;

	void updateUserGameCount(String username) throws RemoteException;
}
