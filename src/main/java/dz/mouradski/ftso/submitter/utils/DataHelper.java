package dz.mouradski.ftso.submitter.utils;


import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.Random;

import static org.web3j.crypto.Hash.sha3;

public class DataHelper {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static Pair<String, BigInteger> getRandomBigIntegerAsString() {
        BigInteger bn = toBN(getRandomHexString(64));

        return Pair.of(bn.toString(), bn);
    }


    public static BigInteger toBN(String input) {
        return new BigInteger(input.replace("0x", ""), 16);
    }

    private static String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

    public static Pair<String, Byte[]> priceHash(BigInteger price, String random, String address) {

        String priceString = org.web3j.utils.Numeric.toHexStringNoPrefixZeroPadded(price, 64);
        String randomString = org.web3j.utils.Numeric.toHexStringNoPrefixZeroPadded(new BigInteger(random), 64);
        String addressString = "000000000000000000000000" + address.replace("0x", "");

        String toHash = priceString + randomString + addressString;

        byte[] bytes = sha3(toBytes(toHash.toLowerCase()));

        String hash = bytesToHex(bytes).toLowerCase();
        Byte[] byteObjects = new Byte[bytes.length];

        int i = 0;

        for (byte b : bytes) {
            byteObjects[i++] = b;
        }

        return Pair.of(hash, byteObjects);
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] toBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {

            try {
                String hexValue = "0x" + s.substring(i, i + 2);
                int iValue = Integer.decode(hexValue);
                data[i / 2] = (byte) iValue;
            } catch (Exception e) {

            }
        }
        return data;
    }

}
