import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.DataBufferByte;

class cbc {
    public static void CBCEncryption() throws IOException, NoSuchAlgorithmException {
        BufferedImage image = ImageIO.read(new File("example.bmp"));
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int block_size = 8;

        byte[] key = readKeyFromFile("key.txt");
        if (key==null) {
            key = generateRandomKey(32);
        }

        byte[] vec = new byte[block_size];
        byte[] encryptedData = new byte[0];

        for (int i = 0; i < data.length; i += block_size) {
            byte[] block = new byte[block_size];
            System.arraycopy(data, i, block, 0, Math.min(block_size, data.length - i));

            key = sha256Key(key);
            byte[] xorResult = xorBytes(block, vec);
            byte[] encryptedBlock = encryptBlock(xorResult, key);
            vec = encryptedBlock;
            encryptedData = concatArrays(encryptedData, encryptedBlock);
        }

        BufferedImage encryptedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        encryptedImage.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), encryptedData);
        ImageIO.write(encryptedImage, "bmp", new File("cbc_crypto.bmp"));
    }
    private static byte[] xorBytes(byte[] b1, byte[] b2) {
        int length = Math.min(b1.length, b2.length);
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (b1[i] ^ b2[i]);
        }
        return result;
    }

    private static byte[] encryptBlock(byte[] block, byte[] key) throws NoSuchAlgorithmException {
        byte[] temp = sha256Key(key, block.length);
        byte[] encryptedBlock = xorBytes(block, temp);
        return encryptedBlock;
    }

    private static byte[] sha256Key(byte[] key, int bl_len) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key);
        byte[] hash = md.digest();

        List<Byte> prSeq = new ArrayList<>();
        for (byte b : hash) {
            prSeq.add(b);
        }

        while (prSeq.size() < bl_len) {
            md.reset();
            md.update(toPrimitiveArray(prSeq));
            byte[] temp = md.digest();

            for (byte b : temp) {
                prSeq.add(b);
            }
        }

        return toPrimitiveArray(prSeq);
    }

    private static byte[] sha256Key(byte[] key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(key);
    }

    private static byte[] toPrimitiveArray(List<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public static byte[] readKeyFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
    private static byte[] generateRandomKey(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomKey = new byte[length];
        secureRandom.nextBytes(randomKey);
        return randomKey;
    }
}
