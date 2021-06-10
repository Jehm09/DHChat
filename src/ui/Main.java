package ui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import model.DHClient;
import model.DHServer;

public class Main{

    private int port = 8080;
	private String IP = "localhost";
	private Window frame;

    protected DHClient client = null;
	protected DHServer server = null;

    public static void main(String[] args) {
        Main m = new Main();
		
		m.frame = new Window(m);

		if (m.client == null)
			m.frame.setTitle("DH Secure Chat - servidor" );
		else
			m.frame.setTitle("DH Secure Chat - cliente" );

		m.frame.setVisible(true);
    }

    public Main() {
        		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		String serverPrompt = JOptionPane.showInputDialog("Host? (Y/N)");

		boolean serverOn = serverPrompt.toUpperCase().equals("Y");

		if (serverOn) {
			String portStr = JOptionPane.showInputDialog("Ingrese el puerto");

			this.port = Integer.parseInt(portStr);

			boolean started = false;
			try {
				this.server = new DHServer(port);
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Se inicia el hilo para recibir los mensajes
			if (started) {
				Thread receiveThread = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								String received = server.receive();
								
								frame.println("Cliente: " + received);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				receiveThread.start();
			}
		}
		else {
			String IPStr = JOptionPane.showInputDialog("Ingrese el servidor.");

			this.IP = IPStr;

			String portStr = JOptionPane.showInputDialog("Ingrese el puerto.");

			this.port = Integer.parseInt(portStr);
			
			boolean started = false;
			try {
				client = new DHClient(IP, port);
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (started) {
				Thread receiveThread = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								String received = client.receive();
								
								frame.println("Servidor: " + received);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				receiveThread.start();
			}
		}
    }

}