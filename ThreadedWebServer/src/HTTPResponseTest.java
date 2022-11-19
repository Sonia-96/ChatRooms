import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class HTTPResponseTest {
    @Test
    public void testGenerateAcceptString() throws NoSuchAlgorithmException {
        String test = "dGhlIHNhbXBsZSBub25jZQ==";
        String result = "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        Assertions.assertEquals(result, HTTPResponse.generateAcceptString(test));
    }

    @Test
    public void testUnmaskPayload() throws IOException {
        byte[] msg1 = {(byte) 0x81, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f}; // unmasked
        InputStream stream1 = new ByteArrayInputStream(msg1);
        byte[] decoded1 = HTTPResponse.decodeMessage(stream1);
        Assertions.assertEquals("Hello", new String(decoded1, StandardCharsets.UTF_8));

        byte[] msg2 = {(byte) 0x81, (byte) 0x85, 0x37, (byte) 0xfa, 0x21, 0x3d, 0x7f, (byte) 0x9f, 0x4d, 0x51, 0x58};
        InputStream stream2 = new ByteArrayInputStream(msg2);
        byte[] decoded2 = HTTPResponse.decodeMessage(stream2);
        Assertions.assertEquals("Hello", new String(decoded2, StandardCharsets.UTF_8));

        byte[] msg3 = {(byte) 0x82, 0x7E, (byte) 0x01, (byte) 0x00};
        InputStream stream3 = new ByteArrayInputStream(msg3);

        byte[] msg4 = {(byte) 0x82, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00};
        InputStream stream4 = new ByteArrayInputStream(msg4);
    }
}
