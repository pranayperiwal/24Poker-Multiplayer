package frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import backend.Serve;

public class GameOverPanel extends JPanel {

//	private JPanel mainGame;
	private MainGameUI mainGame;
	private Serve server;
	private String username;
	private JPanel mainPanel;
	private JButton nextGame;

	public GameOverPanel(CardLayout cardLayout, JPanel cardPanel, String username, MainGameUI mainGame, Serve server) {

		this.username = username;
		this.mainGame = mainGame;
		this.server = server;

		mainPanel = new JPanel();
		nextGame = new JButton("Next Game");
		nextGame.addActionListener(e -> {
			cardLayout.show(cardPanel, "game joining");
		});

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel winnerLabel = new JLabel("Winner: ");
		winnerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		JLabel formulaLabel = new JLabel("");
		formulaLabel.setFont(new Font(formulaLabel.getFont().getName(), Font.BOLD, 24));
		formulaLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(Box.createHorizontalGlue());

		mainPanel.add(winnerLabel);
		mainPanel.add(formulaLabel);

		mainPanel.add(Box.createHorizontalGlue());
		mainPanel.add(Box.createVerticalGlue());

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		add(nextGame, BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent evt) {
				winnerLabel.setText("Winner: " + mainGame.getWinner());
				formulaLabel.setText(mainGame.getWinningFormula());
				updateGameCount();

			}
		});

	}

	public void updateGameCount() {
		// RMI call to update the game count
		try {
			server.updateUserGameCount(username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
