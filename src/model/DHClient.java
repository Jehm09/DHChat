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
	
 /**
     * Constructor del DHClient Se encarga de generar las llaves publicas y privadas
     * usando DH y asignar la llave a usar el algoritmo aes
     * @param IP direccion ip del server socket
     * @param port El puerto por el que se va a crear el socket
     * @throws Exception
     */
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
	
	/**
	 * Este metodo se encargarga de enviar el mensaje encriptado al servidor
	 * @param toSend string con el mensaje a enviar
	 * @throws Exception
	 */
	public void send(String toSend) throws Exception {
		os.write(encrypt(toSend.getBytes("UTF-8"), aesKey));
		os.flush();
	}
	
	/**
	 * Este metodo se encarga de recibir el mensaje enviado del servidor y desencriptarlo
	 * @return retorna un string con el mensaje encriptado
	 * @throws Exception
	 */
	public String receive() throws Exception {
		byte formServer[] = new byte[4096];
		int bytesRead = is.read(formServer);
		
		byte exactFromServer[] = Arrays.copyOf(formServer, bytesRead);
		return new String(decrypt(exactFromServer, aesKey));
	}
	
	/**
	 * Este metodo se encarga de encriptar un arreglo de bytes (mensaje)
	 * usando el algoritmo aes con la llave generada por DH
	 * @param bytesToEncrypt arreglo de bytes que representa el mensaje a encriptar
	 * @param key llave generada con DH
	 * @return retorna el mensaje encriptado en un arreglo de bytes
	 * @throws Exception
	 */
	public static byte[] encrypt(byte bytesToEncrypt[], byte key[]) throws Exception {
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

		return cipher.doFinal(bytesToEncrypt);
	}

	/**
	 * Este metodo se encarga de desencriptar un arreglo de bytes (mensaje)
	 * usando el algoritmo aes con la llave generada por DH
	 * @param bytesToDecrypt arreglo de bytes que representa el mensaje a desencriptar
	 * @param key llave generada con DH
	 * @return retorna el mensaje desencriptado en un arreglo de bytes
	 * @throws Exception
	 */
	public static byte[] decrypt(byte bytesToDecrypt[], byte key[]) throws Exception {
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

		return cipher.doFinal(bytesToDecrypt);
	}
}