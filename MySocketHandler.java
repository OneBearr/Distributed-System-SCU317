import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import javax.naming.InvalidNameException;
import javax.naming.NoPermissionException;

public class MySocketHandler implements Runnable {
    Socket socket;

    public MySocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest() throws Exception {
        // Prepare IO readers, and get incoming http request from client
        InputStreamReader inputStreamReader = new InputStreamReader(this.socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        // Prepare IO writers, and return the file content to client
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(this.socket.getOutputStream());
        PrintStream printStream = new PrintStream(bufferedOutputStream);

        // Get http request string and print in log
        String request = bufferedReader.readLine();
        System.out.println("The Http Request is: " + request);

        // Get requested file name
        String fileName = getFileName(request);

        // Get file contentType
        String contentType = contentTypeOf(fileName);

        // Amend the filepath
        fileName = "files/" +  fileName;

        // Get time info for logging
        Date date = new Date();

        try {
            // Throw 400 exception, Invalid file type suffix
            if (contentType == null) {
                throw new InvalidNameException();
            }

            // Throw 404 exception, valid file type, but invalid file name
            if (!(new File(fileName)).exists()) {
                throw new FileNotFoundException();
            }

            // Throw 403 exception, Invalid file permission
            if (!(new File(fileName)).canRead()) {
                throw new NoPermissionException();
            }

            // Return code 200 ok
            File fl = new File (fileName);
            long len = fl.length();
            // Put content-type, content-length and time into response header
            printStream.print("HTTP/1.1 200 OK\r\nContent-type: " + contentType 
                                + "\r\nContent-length: " + Long.toString(len) 
                                + "\r\nDate: " + date.toString() 
                                + "\r\n\r\n");
            
            System.out.println("Status code: 200 success!");

            // create a fileInputStream for writing the file content
            FileInputStream fileInputStream = new FileInputStream(fileName);
            byte[] buf = new byte[1024 * 1024];
            int length;
            while((length = fileInputStream.read(buf)) != -1) {
                printStream.write(buf, 0, length);
            }
            fileInputStream.close();

            printStream.close();
            socket.close();

        // Catch 400, 403, 404 error
        } catch (InvalidNameException invalidNameException) {
            // 400 exception, Invalid file type suffix
            printStream.print("HTTP/1.1 400 Bad Request\r\nContent-type: " + contentType 
                                    + "\r\nContent-length: 0" 
                                    + "\r\nDate: " + date.toString() 
                                    + "\r\n\r\n");
            System.out.println("Status code: 400 bad request");
            printStream.close();
            bufferedReader.close();
            socket.close();

        } catch (FileNotFoundException fileNotFoundException) {
            // 404 exception, valid file type, but invalid file name
            printStream.print("HTTP/1.1 404 Not Found\r\nContent-type: " + contentType 
                                    + "\r\nContent-length: 0" 
                                    + "\r\nDate: " + date.toString() 
                                    + "\r\n\r\n");
            System.out.println("Status code: 404 file not found");
            printStream.close();
            bufferedReader.close();
            socket.close();

        } catch (NoPermissionException noPermissionException) {
            // 403 exception, Invalid file permission
            printStream.print("HTTP/1.1 403 Forbidden\r\nContent-type: " + contentType 
                                    + "\r\nContent-length: 0" 
                                    + "\r\nDate: " + date.toString() 
                                    + "\r\n\r\n");
            System.out.println("Status code: 403 no read permission");
            printStream.close();
            bufferedReader.close();
            socket.close();
        } 
    
        System.out.println("Socket closed. \n");
    }

    // Find the type of file
    public String contentTypeOf(String fileName){
        String type = "";
        if (fileName.endsWith(".html")) {
            type = "text/html";
        } else if (fileName.endsWith(".jpg")) {
            type = "image/jpg";
        } else if (fileName.endsWith(".gif")) {
            type = "image/gif";
        } else if (fileName.endsWith(".txt")) {
            type = "text/txt";
        } else {
            type = null;
        }
        return type;
    }

    // Get the requested file name
    public String getFileName(String request) {
        StringTokenizer tokenizedRequests = new StringTokenizer(request);
        tokenizedRequests.nextToken();
        String filename = tokenizedRequests.nextToken();
        // Convert path of '/' to '/index.html'
        if (filename.length() == 1 && filename.endsWith("/")) {
            filename = filename + "index.html";
        }
        // Removes all the '/' before the filename
        while(filename.indexOf("/") == 0) {
            filename = filename.substring(1);
        }
        return filename;
    }
}