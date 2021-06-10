package model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class DHServer {
	private ServerSocket server;
	
	private InputStream is;
	private OutputStream os;
	
	private byte aesKey[];
	
	public DHServer(int port) throws Exception {
		server = new ServerSocket(port);
		
		Socket socketFromServer = server.accept();
		
		is = socketFromServer.getInputStream();
		os = socketFromServer.getOutputStream();
		
		KeyPairGenerator clientKpairGen = KeyPairGenerator.getInstance("DH");
		clientKpairGen.initialize(2048);
		KeyPair clientKpair = clientKpairGen.generateKeyPair();

		KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
		clientKeyAgree.init(clientKpair.getPrivate());

		byte[] clientPubKeyEnc = clientKpair.getPublic().getEncoded();

		//Evia la llave publica al cliente
		os.write(clientPubKeyEnc);
		os.flush();

		//Recibe la llave publica del cliente
		byte buffer[] = new byte[4096];
		int numberOfBytesRead = is.read(buffer);

		byte clientsPublicKey[] = Arrays.copyOfRange(buffer, 0, numberOfBytesRead);

		KeyFactory clientKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientsPublicKey);
		PublicKey serverPubKey = clientKeyFac.generatePublic(x509KeySpec);
		
		clientKeyAgree.doPhase(serverPubKey, true);

		byte[] clientSharedSecret = new byte[4096];

		try {
			clientSharedSecret = clientKeyAgree.generateSecret();
		} catch (Exception e) {
			e.printStackTrace();
		}

		aesKey = Arrays.copyOf(clientSharedSecret, 16); // 16 bytes = 128 bits
	}
	
	public void send(String toSend) throws Exception {
		os.write(encrypt(toSend.getBytes("UTF-8"), aesKey));
		os.flush();
	}
	
	public String receive() throws Exception {
		byte fromClient[] = new byte[4096];
		int bytesRead = is.read(fromClient);

		byte exactFromClient[] = Arrays.copyOf(fromClient, bytesRead);
		return new String(decrypt(exactFromClient, aesKey));
	}
	
	public static byte[] encrypt(byte bytesToEncrypt[], byte key[]) throws Exception {
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

		return cipher.doFinal(bytesToEncrypt);
	}

	public static byte[] decrypt(byte bytesToDecrypt[], byte key[]) throws Exception {
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

		return cipher.doFinal(bytesToDecrypt);
	}
}