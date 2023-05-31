package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import backend.Serve;

public class Client extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private Serve server;

	public Client(String host) {
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			server = (Serve) registry.lookup("AssignmentServer");
		} catch (Exception e) {
			System.err.println("Failed accessing RMI: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Client(args[0]));
	}

	// creates login ui
	@Override
	public void run() {
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 250);

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel centralGrid = new JPanel(new GridLayout(3, 2, 5, 5));

		JLabel usernameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");

		usernameField = new JTextField();

		passwordField = new JPasswordField();

		JButton loginButton = new JButton("Login");
		loginButton.setPreferredSize(new Dimension(10, 40));
		loginButton.addActionListener(e -> login());

		JButton registerButton = new JButton("Register");
		registerButton.addActionListener(e -> register());

		centralGrid.add(usernameLabel);
		centralGrid.add(usernameField);
		centralGrid.add(passwordLabel);
		centralGrid.add(passwordField);

		centralGrid.add(registerButton);
		centralGrid.add(loginButton);

		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		mainPanel.add(centralGrid, BorderLayout.CENTER);

		getContentPane().add(mainPanel);
		pack();

		setVisible(true);
	}

	// login function
	private void login() {
		String username = usernameField.getText();
		char[] password = passwordField.getPassword();

		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Error: Username not entered.", "Login Error",
					JOptionPane.ERROR_MESSAGE);
		} else {

			try {
				String returnedValue = server.login(username, (new String(password)));

				if (returnedValue.equals("user already online")) {
					JOptionPane.showMessageDialog(this, "Error: User is already online.", "Login Error",
							JOptionPane.ERROR_MESSAGE);
				} else if (returnedValue.equals("password incorrect")) {
					JOptionPane.showMessageDialog(this, "Error: Password not correct.", "Login Error",
							JOptionPane.ERROR_MESSAGE);
				} else if (returnedValue.equals("user not found")) {
					JOptionPane.showMessageDialog(this, "Error: User does not exist.", "Login Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					JFrame mainGameUI = new MainGameUI(returnedValue, server);
					this.setVisible(false);
					mainGameUI.setVisible(true);
				}

			} catch (RemoteException e) {
				System.err.println("Failed invoking RMI for register: " + e.getMessage());
			}

		}
	}

	// opens register frame
	private void register() {

		JFrame registerPanel = new RegisterPanel(server, this);
		registerPanel.setVisible(true);
	}
}
