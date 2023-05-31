package frontend;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import backend.Serve;

public class UserProfilePanel extends JPanel {

	private String username, rank, avgtimetowin, winCount, gameCount;
	private JLabel name, wins, games, time, rankLabel;
	private Serve server;

	public UserProfilePanel(String user, Serve server) {

		this.server = server;

		String[] userDetails = user.split(" ");

		username = userDetails[0];
		winCount = userDetails[2];
		gameCount = userDetails[3];
		avgtimetowin = userDetails[4];
		rank = userDetails[5];

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		name = new JLabel(username);
		name.setBorder(new EmptyBorder(20, 20, 0, 0));
		name.setFont(new Font(name.getFont().getName(), Font.BOLD, 24));

		wins = new JLabel("No. of wins: " + winCount);
		wins.setBorder(new EmptyBorder(10, 20, 0, 0));

		games = new JLabel("No. of games: " + gameCount);
		games.setBorder(new EmptyBorder(10, 20, 0, 0));

		time = new JLabel("Average time to wins: " + avgtimetowin + "s");
		time.setBorder(new EmptyBorder(10, 20, 0, 0));

		rankLabel = new JLabel("Rank: #" + rank);
		rankLabel.setBorder(new EmptyBorder(20, 20, 0, 0));
		rankLabel.setFont(new Font(rankLabel.getFont().getName(), Font.BOLD, 18));

		add(name);
		add(wins);
		add(games);
		add(time);
		add(rankLabel);

		TitledBorder titledBorder = new TitledBorder("");
		setBorder(titledBorder);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent evt) {
				updateUser();
			}
		});

	}

	private void updateUser() {
		try {

			String user = server.getUser(username);

			String[] userDetails = user.split(" ");

			winCount = userDetails[2];
			wins.setText("No. of wins: " + winCount);

			gameCount = userDetails[3];
			games.setText("No. of games: " + gameCount);

			avgtimetowin = userDetails[4];
			time.setText("Average time to wins: " + avgtimetowin + "s");

			rank = userDetails[5];
			rankLabel.setText("Rank: #" + rank);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
