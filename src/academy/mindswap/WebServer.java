package academy.mindswap;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {


    public static void main(String[] args) {
        int port = 8085;
        if (System.getenv("PORT") != null) {
            port = Integer.parseInt(System.getenv("PORT"));
        }
        try {
            new WebServer().start(port);
        } catch (IOException e) {
            // System.err.println(Messages.SERVER_ERROR);
        }
    }

    private void start(int port) throws IOException {
        ServerSocket socket = new ServerSocket(port);
        ExecutorService service = Executors.newCachedThreadPool();
        System.out.printf("Server running at %s:%S  \n", socket.getInetAddress().getHostAddress(), port);
        serveRequests(socket, service);
    }

    private void serveRequests(ServerSocket socket, ExecutorService service) {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                service.submit(new RequestHandler(clientSocket));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private static class RequestHandler implements Runnable {

        private Socket clientSocket;

        public RequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            dealWithRequest(clientSocket);
        }

        private void dealWithRequest(Socket clientSocket) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());


                String line = in.readLine();

                if (line != null) {
                    System.out.println(line);
                    String[] slipHeader = splitHeader(in, line);
                    reply(out, slipHeader[1], slipHeader[2]);
                }
                close(clientSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String[] splitHeader(BufferedReader in, String line) throws IOException {

            String[] splitHeader = line.split(" ");
            String[] headerParts = new String[3];
            headerParts[0] = line;
            headerParts[1] = splitHeader[0];
            headerParts[2] = splitHeader[1];
            return headerParts;

        }

        private void reply(DataOutputStream out, String httpVerb, String resource) throws IOException {
            if (!"GET".equals(httpVerb)) {
                resource = "/404.html";
            }

            File file;
            switch (resource) {
                case "/":
                case "/index.html":
                    file = new File("/Users/mindera/Desktop/Mindera_Mindswap/web_server/src/academy/mindswap/index.html");
                    out.writeBytes("HTTP/1.1 200 OK\r\n\r\n");
                    replyWithFile(out, file);
                    break;

                case "/nextpage.html":
                    file = new File("/Users/mindera/Desktop/Mindera_Mindswap/web_server/src/academy/mindswap/nextpage.html");
                    out.writeBytes("HTTP/1.1 200 OK\r\n\r\n");
                    replyWithFile(out, file);
                    break;

                default:
                    file = new File("/Users/mindera/Desktop/Mindera_Mindswap/web_server/src/academy/mindswap" + resource);
                    if (!file.exists()) {
                        file = new File("/Users/mindera/Desktop/Mindera_Mindswap/web_server/src/academy/mindswap/404.html");
                        out.writeBytes("HTTP/1.1 404 Not Found\r\n\r\n");
                    } else {
                        String contentType = Files.probeContentType(file.toPath());
                        out.writeBytes("HTTP/1.1 200 OK\r\n");
                        out.writeBytes("Content-Type: " + contentType + "\r\n\r\n");
                    }
                    break;
            }
            replyWithFile(out, file);
        }

        private void replyWithHeader(DataOutputStream out, String header) throws IOException {
            out.writeBytes(header);
        }

        private void replyWithHeader(DataOutputStream out, String header, String message) throws IOException {
            out.writeBytes(header + message);

        }

        private void replyWithFile(DataOutputStream out, File file) throws IOException {
            byte[] bytes = Files.readAllBytes(Path.of(file.getPath()));
            out.write(bytes, 0, bytes.length);
            close(clientSocket);

        }


        private void close(Socket clientSocket) throws IOException {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}