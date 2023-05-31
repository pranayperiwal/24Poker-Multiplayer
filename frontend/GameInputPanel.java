package frontend;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.rmi.RemoteException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import backend.Serve;

public class GameInputPanel extends JPanel {

	private JTextField input;
	private JButton submit;
	private JLabel valueLabel;
	private int value;
	private Serve server;
	private String user;

	private int gameID;

	public GameInputPanel(Serve server, String mainUser, MainGameUI mainGame, GamePlayingPanel gamePlayingPanel) {

		this.gameID = mainGame.getGameID();
		this.user = mainUser;
		this.server = server;

		gamePlayingPanel.setGameStartTime(System.currentTimeMillis());
//		System.out.println("starat time from input panel parameter: " + gamePlayingPanel.getGameStartTime());

		setLayout(new FlowLayout());
		input = new JTextField();
		input.setPreferredSize(new Dimension(400, 30));

		value = 0;
		valueLabel = new JLabel("= " + value);

		submit = new JButton("Submit");

		submit.addActionListener(e -> {
			String formula = input.getText();
			if (!input.getText().isEmpty()) {

				formula = formula.replace("K", "13");
				formula = formula.replace("Q", "12");
				formula = formula.replace("J", "11");
				formula = formula.replace("A", "1");
//				System.out.println("Game id from input panel:" + mainGame.getGameID());
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByName("JavaScript");
				try {
					int computedValue = (Integer) engine.eval(formula);
					valueLabel.setText("= " + computedValue);
					if (computedValue == 24) {
//						System.out
//								.println("starat time from input panel action: " + gamePlayingPanel.getGameStartTime());

						long endTime = System.currentTimeMillis();
						gamePlayingPanel.setGameEndTime(endTime);
						System.out.println("endtime: " + endTime);
						double timeTaken = (double) Math.round((endTime - gamePlayingPanel.getGameStartTime()) / 10.0)
								/ 100.0;
//						System.out.println("Time from frontend: " + timeTaken);

						// RMI call to let server know the user has won
						try {
							server.userWon(user, mainGame.getGameID(), formula, timeTaken);
						} catch (RemoteException ex) {
							ex.printStackTrace();
						}
						input.setText("");

					}
				} catch (ScriptException ex) {
					ex.printStackTrace();
				}
			}
		});

		add(input);
		add(valueLabel);
		add(submit);

	}

}
