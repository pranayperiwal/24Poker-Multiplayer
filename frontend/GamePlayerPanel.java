package frontend;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import frontend.GamePlayingPanel.GameUserDetails;

public class GamePlayerPanel extends JPanel {

	JPanel[] users = new JPanel[4];

	public GamePlayerPanel(GameUserDetails[] userDetails) {

//		this.userNames = userNames;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addUsers(userDetails);

	}

	private void addUsers(GameUserDetails[] userDetails) {

		for (int i = 0; i < userDetails.length; i++) {
			JPanel user = new JPanel();
			user.setLayout(new BoxLayout(user, BoxLayout.Y_AXIS));
			user.setPreferredSize(new Dimension(150, 140));
			user.setBorder(new TitledBorder(""));

			JLabel username = new JLabel(userDetails[i].getUsername());
			username.setBorder(new EmptyBorder(5, 20, 0, 0));
			username.setFont(new Font(username.getFont().getName(), Font.BOLD, 20));

			JLabel winsAndAvg = new JLabel(
					"Win: " + userDetails[i].getWinCount() + " Avg " + userDetails[i].getAvgTime());
			winsAndAvg.setBorder(new EmptyBorder(10, 20, 20, 30));

			user.add(username);
			user.add(winsAndAvg);

			this.add(user);

		}

	}

}
