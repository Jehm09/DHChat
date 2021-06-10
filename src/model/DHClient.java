package model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class DHClient {
	private Socket socket;
	
	private InputStream is;
	private OutputStream os;
	
	private byte aesKey[];
	
	public DHClient(String IP, int port) throws Exception {
		socket = new Socket(IP, port);
		
		is = socket.getInputStream();
		os = socket.getOutputStream();
		
		//Se recibe la llave publica del server
		byte buffer[] = new byte[4096];
		int numberOfBytesRead = is.read(buffer);

		byte serversPublicKey[] = Arrays.copyOfRange(buffer, 0, numberOfBytesRead);

		KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serversPublicKey);
		PublicKey clientPubKey = serverKeyFac.generatePublic(x509KeySpec);

		DHParameterSpec dhParamFromclientPubKey = ((DHPublicKey)clientPubKey).getParams();

		KeyPairGenerator serverKpairGen = KeyPairGenerator.getInstance("DH");
		serverKpairGen.initialize(dhParamFromclientPubKey);
		KeyPair serverKpair = serverKpairGen.generateKeyPair();

		KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
		serverKeyAgree.init(serverKpair.getPrivate());

		byte[] ourPublicKey = serverKpair.getPublic().getEncoded();

		//Envia la llave publica al servidor
		os.write(ourPublicKey);
		os.flush();

		serverKeyAgree.doPhase(clientPubKey, true);

		byte[] serverSharedSecret = new byte[4096];

		try {
			serverSharedSecret = serverKeyAgree.generateSecret();
		} catch (Exception e) {
			e.printStackTrace();
		}

		aesKey = Arrays.copyOf(serverSharedSecret, 16);
	}
	
	public void send(String toSend) throws Exception {
		os.write(encrypt(toSend.getBytes("UTF-8"), aesKey));
		os.flush();
	}
	
	public String receive() throws Exception {
		byte formServer[] = new byte[4096];
		int bytesRead = is.read(formServer);
		
		byte exactFromServer[] = Arrays.copyOf(formServer, bytesRead);
		return new String(decrypt(exactFromServer, aesKey));
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