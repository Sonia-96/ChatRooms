import java.net.Socket;

/**
 * This class is used by the server to handle the request of a single client and send responses back.
 * The actual work is performed by HTTPRequest class and HTTPResponse class.
 */

public class ConnectionHandler implements Runnable{
    private Socket clientSocket_;

    public ConnectionHandler(Socket c) {
        clientSocket_ = c;
    }

    @Override
    public void run() {
        try {
            // 2. read HTTP request
            HTTPRequest request = new HTTPRequest(clientSocket_.getInputStream());

            // 3. send response
            HTTPResponse response = new HTTPResponse(request, clientSocket_.getOutputStream(), clientSocket_.getInputStream());
            response.sendResponse();
            clientSocket_.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
