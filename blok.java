import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class blok {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        try {
            ecb.ECBEncryption();
            cbc.CBCEncryption();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
