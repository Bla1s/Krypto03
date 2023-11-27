import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

class ecb {
    public static void ECBEncryption() throws IOException {
        BufferedImage originalImage = ImageIO.read(new File("example.bmp"));
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int block_size = 8;

        List<byte[]> encryptedBlocks = new ArrayList<>();
        byte[] key = readKeyFromFile("key.txt");
        if (key==null) {
            key = generateRandomKey(32);
        }

        for (int y = 0; y < height; y += block_size) {
            for (int x = 0; x < width; x += block_size) {
                int blockWidth = Math.min(block_size, width - x);
                int blockHeight = Math.min(block_size, height - y);

                byte[] block = getBlockBytes(originalImage, x, y, blockWidth, blockHeight);
                byte[] encryptedBlock = encryptBlockXOR(block, key);

                encryptedBlocks.add(encryptedBlock);
            }
        }

        BufferedImage encryptedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int index = 0;
        for (int y = 0; y < height; y += block_size) {
            for (int x = 0; x < width; x += block_size) {
                int blockWidth = Math.min(block_size, width - x);
                int blockHeight = Math.min(block_size, height - y);

                for (int blockY = 0; blockY < blockHeight; blockY++) {
                    for (int blockX = 0; blockX < blockWidth; blockX++) {
                        int pixel = (encryptedBlocks.get(index)[blockY * blockWidth + blockX] & 0xFF) << 16 |
                                (encryptedBlocks.get(index)[blockY * blockWidth + blockX] & 0xFF) << 8 |
                                (encryptedBlocks.get(index)[blockY * blockWidth + blockX] & 0xFF);

                        encryptedImage.setRGB(x + blockX, y + blockY, pixel);
                    }
                }
                index++;
            }
        }

        ImageIO.write(encryptedImage, "bmp", new File("ecb_crypto.bmp"));
    }

    private static byte[] getBlockBytes(BufferedImage image, int x, int y, int width, int height) {
        byte[] block = new byte[width * height];
        int index = 0;
        for (int blockY = 0; blockY < height; blockY++) {
            for (int blockX = 0; blockX < width; blockX++) {
                int pixel = image.getRGB(x + blockX, y + blockY);
                block[index++] = (byte) ((pixel >> 16) & 0xFF);
            }
        }
        return block;
    }

    private static byte[] encryptBlockXOR(byte[] block, byte[] key) {
        byte[] encryptedBlock = new byte[block.length];
        for (int i = 0; i < block.length; i++) {
            encryptedBlock[i] = (byte) (block[i] ^ key[i % key.length]);
        }
        return encryptedBlock;
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
