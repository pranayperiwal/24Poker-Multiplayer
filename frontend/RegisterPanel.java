package frontend;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.rmi.RemoteException;

import javax.swing.*;

import backend.Serve;

public class RegisterPanel extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    
    private Serve server;
    
    private JFrame loginui;
    
    public RegisterPanel(Serve server, JFrame loginui) {
    	this.server = server;
    	this.loginui = loginui;
    	
    	setTitle("Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel centralGrid = new JPanel(new GridLayout(4, 2, 5, 5));
        
        
                
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");

        
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(10, 40));
        registerButton.addActionListener(e -> register());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
        	dispose();
        });
        
        centralGrid.add(usernameLabel);
        centralGrid.add(usernameField);
        centralGrid.add(passwordLabel);
        centralGrid.add(passwordField);
        centralGrid.add(confirmPasswordLabel);
        centralGrid.add(confirmPasswordField);
        
        centralGrid.add(registerButton);
        centralGrid.add(cancelButton);
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(centralGrid, BorderLayout.CENTER);
        
        getContentPane().add(mainPanel);
        pack();
    }
    
	
	private void register() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();
        
        if(username.isEmpty()) {
        	JOptionPane.showMessageDialog(this, "Error: Username not entered", "Register Error", JOptionPane.ERROR_MESSAGE);
        }
        else if(password.length == 0) {
        	JOptionPane.showMessageDialog(this, "Error: Passwords not entered.", "Register Error", JOptionPane.ERROR_MESSAGE);
        }
        else if(!(new String(confirmPassword)).equals((new String(password)))){
        	JOptionPane.showMessageDialog(this, "Error: Passwords do not match.", "Register Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
        	if(server != null) {
        		
    			try {
    				String returnedValue = server.register(username, new String(password));
    				
    				if(returnedValue.equals("error")) {
    					
    					JOptionPane.showMessageDialog(this, "Error: There was an error in registering the user.", "Register Error", JOptionPane.ERROR_MESSAGE);
    					    	            	
    				}
    				else if(returnedValue.equals("username used")){
    					
    					JOptionPane.showMessageDialog(this, "Error: Username already taken.", "Register Error", JOptionPane.ERROR_MESSAGE);
    				}
    				else {
						JFrame mainGameUI = new MainGameUI(returnedValue, server);
	        			
	        			//hide register and login frames
	        			this.setVisible(false);
	        			loginui.setVisible(false);
	        			
	        			//show game frame
	        			mainGameUI.setVisible(true);
    				}
    				
    			} catch (RemoteException e) {
    				System.err.println("Failed invoking RMI for register: " + e.getMessage());
    			}
    		}
        	
        }
        
        
    }
    
}
