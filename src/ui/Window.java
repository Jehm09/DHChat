package ui;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Window extends JFrame {

    private static final long serialVersionUID = 789436913970326654L;
	private JPanel contentPane;
	private JTextField txtSendBox;
	private JTextArea textAreaMessages;
	
	public Window(Main m) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtSendBox = new JTextField();
		txtSendBox.setBounds(10, 331, 514, 20);
		txtSendBox.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					try {
						if (m.client == null) {
							m.server.send(txtSendBox.getText() + "\n");
						}
						else {
							m.client.send(txtSendBox.getText() + "\n");
						}

						textAreaMessages.setText(textAreaMessages.getText() + txtSendBox.getText() + "\n");
						
						txtSendBox.setText("");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
			}
		});
		txtSendBox.setColumns(10);
		contentPane.add(txtSendBox);
		
		textAreaMessages = new JTextArea();
		textAreaMessages.setBounds(10, 11, 514, 309);
		textAreaMessages.setFocusable(false);
		contentPane.add(textAreaMessages);
	}
	
	public void println(String str) {
		textAreaMessages.setText(textAreaMessages.getText() + str);
	}

}