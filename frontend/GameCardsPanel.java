package frontend;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameCardsPanel extends JPanel {

	JPanel gameCardsPanel, playerPanel, inputPanel;
	JLabel[] cards = new JLabel[4];

	public GameCardsPanel(String[] gameCards) {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createVerticalGlue());

		add(Box.createHorizontalGlue());

		loadImages(gameCards);

		add(Box.createHorizontalGlue());
		add(Box.createVerticalGlue());

	}

	private void loadImages(String[] gameCards) {
		for (int i = 0; i < 4; i++) {

			try {
//				System.out.println("../cards/" + gameCards[i] + ".gif");
				BufferedImage myPicture = ImageIO
						.read(this.getClass().getResource("../cards/" + gameCards[i] + ".gif"));
				cards[i] = new JLabel(new ImageIcon(myPicture));

				cards[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
				this.add(cards[i]);
				this.add(Box.createRigidArea(new Dimension(10, 0)));

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
