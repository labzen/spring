package cn.labzen.spring.env;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Crypto {

  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final byte[] IV = "yR2EArZXF0aTkQBr".getBytes();
  private static final IvParameterSpec IV_PARAMETER_SPEC = new IvParameterSpec(IV);

  private final SecretKeySpec key;

  public Crypto(String password) {
    this.key = new SecretKeySpec(password.getBytes(), "AES");
  }

  public byte[] encrypt(String plaintext) throws InvalidAlgorithmParameterException, InvalidKeyException {
    return encrypt(plaintext.getBytes());
  }

  public byte[] encrypt(byte[] plaintextBytes) throws InvalidAlgorithmParameterException, InvalidKeyException {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, IV_PARAMETER_SPEC);
      return cipher.doFinal(plaintextBytes);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] decrypt(String ciphertext) {
    return decrypt(ciphertext.getBytes());
  }

  public byte[] decrypt(byte[] ciphertextBytes) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, IV_PARAMETER_SPEC);
      return cipher.doFinal(ciphertextBytes);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
             InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }
}
