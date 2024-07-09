package academy.mindswap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpHeaderBuilder {

    public static String notAllowed() {
        return """
                HTTP/1.0 405 Method Not Allowed\r
                Allow: GET\r
                """;
    }

    public static String notFound(String fileName, long length) {
        return "HTTP/1.0 404 Not Found\r\n" +
                contentType(fileName) +
                "Content-Length: " + length + "\r\n\r\n";
    }

    public static String ok(String fileName, long length) {
        return "HTTP/1.0 200 Document Follows\r\n" +
                contentType(fileName) +
                "Content-Length: " + length + "\r\n\r\n";
    }

    private static String contentType(String fileName) {

        String contentType = "";
        try {
            contentType = Files.probeContentType(Path.of("www" + fileName));
        } catch (IOException e) {

        }

        return "Content-Type: " + contentType + "; charset=UTF-8\r\n";

       /* if (HttpMediaType.isType(fileName, MediaType.IMG)) {
            return "Content-Type: image/" + HttpMediaType.getExtension(fileName) + "\r\n";
        }

        if (HttpMediaType.isType(fileName, MediaType.AUDIO)) {
            return "Content-Type: audio/" + HttpMediaType.getExtension(fileName) + "\r\n";
        }

        return "Content-Type: text/html; charset=UTF-8\r\n";*/
    }
}