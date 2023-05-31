package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import backend.Serve;

public class LeaderboardPanel extends JPanel {
	private Serve server;
	private Object[][] allUsers;
	private JTable table;
	private JScrollPane scrollPane;

	public LeaderboardPanel(Serve server) {
		this.server = server;

		String[] columnNames = { "Rank", "Player", "Games won", "Games played", "Avg winning time" };
		try {

			String[] users = server.getAllUsers().split("\n");
			allUsers = new Object[users.length][];

			String[][] allUsersYetToBeSorted = new String[users.length][];

			// all users are stored in a 2d String array
			for (int i = 0; i < users.length; i++) {
				String user = users[i];
				String[] userDetails = user.split(" ");
				allUsersYetToBeSorted[i] = userDetails;

			}

			// sort the string array based on rank
			sortArray(allUsersYetToBeSorted);

			// store the sorted array values into allUsers Object array
			int index = 0;
			for (String[] row : allUsersYetToBeSorted) {
				Object[] oneUser = { row[5], row[0], row[2], row[3], row[4] };
				allUsers[index] = oneUser;
				index++;
			}
		} catch (RemoteException ex) {

			System.err.println("Failed invoking RMI for register: " + ex.getMessage());
		}

		table = new JTable(allUsers, columnNames);
		table.setGridColor(Color.black);

		scrollPane = new JScrollPane(table);

		add(scrollPane, BorderLayout.CENTER);

		setBorder(new TitledBorder(""));

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent evt) {
				updateTable();
			}
		});

	}

	private void updateTable() {
		String[] columnNames = { "Rank", "Player", "Games won", "Games played", "Avg winning time" };
		try {

			String[] users = server.getAllUsers().split("\n");
			allUsers = new Object[users.length][];

			String[][] allUsersYetToBeSorted = new String[users.length][];

			// all users are stored in a 2d String array
			for (int i = 0; i < users.length; i++) {
				String user = users[i];
				String[] userDetails = user.split(" ");
				allUsersYetToBeSorted[i] = userDetails;

			}

			// sort the string array based on rank
			sortArray(allUsersYetToBeSorted);

			// store the sorted array values into allUsers Object array
			int index = 0;
			for (String[] row : allUsersYetToBeSorted) {
				Object[] oneUser = { row[5], row[0], row[2], row[3], row[4] };
				allUsers[index] = oneUser;
				index++;
			}
		} catch (RemoteException ex) {

			System.err.println("Failed invoking RMI for register: " + ex.getMessage());
		}

		TableModel newModel = new DefaultTableModel(allUsers, columnNames);

		table.setModel(newModel);

	}

	private String[][] sortArray(String[][] stringToBeSorted) {
		// sort the string array
		Arrays.sort(stringToBeSorted, new Comparator<String[]>() {
			public int compare(String[] a, String[] b) {
				return a[5].compareTo(b[5]);
			}
		});

		return stringToBeSorted;
	}
}
