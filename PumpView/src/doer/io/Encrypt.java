package doer.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class Encrypt {
  private static final String KEY = "doersecretcryptoautomation";
  public static String encrypt(final String text) {
    return Base64.encodeBase64String(Encrypt.xor(text.getBytes()));
  }
  
  public static String decrypt(final String hash) {
    try {
      return new String(Encrypt.xor(Base64.decodeBase64(hash.getBytes())), "UTF-8");
    } catch (java.io.UnsupportedEncodingException ex) {
      throw new IllegalStateException(ex);
    }
  }
  
  private static byte[] xor(final byte[] input) {
    final byte[] output = new byte[input.length];
    final byte[] secret = Encrypt.KEY.getBytes();
    int spos = 0;
    for (int pos = 0; pos < input.length; ++pos) {
      output[pos] = (byte) (input[pos] ^ secret[spos]);
      spos += 1;
      if (spos >= secret.length) {
        spos = 0;
      }
    }
    return output;
  }
  
  public static String encryptMD5(final String hash) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		md.update(hash.getBytes("UTF-8"));
		return new String(Hex.encodeHex(md.digest()));
  }
}
