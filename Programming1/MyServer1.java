import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer1 {

    public static void main(String[] args) throws IOException {
        
        // check the terminal command
        if (args.length != 4) {
            System.out.println("Wrong command! Your command should be like : java <your java server file> -document_root \"<file path>\" -port <your port#>");
            System.exit(0);
        }

        // Create the server socket by using the input port number
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[3]));
        System.out.println("MyServer1 started, listening for incomming requests...");

        // Forever loop for listening incoming connections
        while(true) {
            // one thread for each socket request
            Socket socket = serverSocket.accept();
            
            // dynamic socket timeout mechanism
            int count = Thread.activeCount();
            if (count < 10) {
                socket.setSoTimeout(1000 * 60);
            } else if (count < 20) {
                socket.setSoTimeout(1000 * 30);
            } else {
                socket.setSoTimeout(1000 * 5);
            }
            
            // initial a new thread to handle this new socket
            MySocketHandler mySocketHandler = new MySocketHandler(socket);
            Thread thread = new Thread(mySocketHandler);
            thread.start();
        }
    }
}