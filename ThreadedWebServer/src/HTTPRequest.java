import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class implements an HTTP Request. It gets the filename and stores other request headers in a map.
 */

public class HTTPRequest {
    private static final String SOURCE_FOLDER = "resources";
    private static final String FILE_NOT_FOUND = "/404.html";
    private File file_;
    private final Map<String, String> headers_;

    HTTPRequest(InputStream in) {
        Scanner scanner = new Scanner(in);

        // get the file
        String line = scanner.nextLine();
        String filename = line.split(" ")[1];
        if (filename.equals("/")) {
            filename = "/index.html";
        }
        file_ = new File(SOURCE_FOLDER + filename);
        if (!file_.exists()) {
            file_ = new File(SOURCE_FOLDER + FILE_NOT_FOUND);
        }
        System.out.println("Handle request for: " + Thread.currentThread().getName() + " " + file_.getPath());

        // get the requests
        headers_ = new HashMap<>();
        line = scanner.nextLine();
        while (!line.equals("")) {
            String[] pair = line.split(": ");
            headers_.put(pair[0], pair[1]);
            line = scanner.nextLine();
        }
    }

    public boolean isWebSocketRequest() {
        return headers_.containsKey("Sec-WebSocket-Key");
    }

    protected File getFile() {
        return file_;
    }

    protected Map<String, String> getHeaders() {
        return headers_;
    }
}
