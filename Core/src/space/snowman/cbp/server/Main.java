package space.snowman.cbp.server;

import space.snowman.cbp.server.core.Server;
import space.snowman.cbp.server.service.Control;
import space.snowman.cbp.server.service.SMTP;
import space.snowman.cbp.server.service.TransferFile;

import java.io.IOException;

public class Main {
    private static final String DEFAULT_PASSWORD = "123";
    //private static final int DEFAULT_PORT = 8888;

    /**
     * Метод main() для запуска сервера как самостоятельной программы.
     **/
    public static void main(String[] args) {
        try {
            Server server = new Server(System.out, 10);
            server.addService(new Control(server, DEFAULT_PASSWORD), 8888);
            server.addService(new TransferFile(), 9999);
            server.addService(new SMTP(), 25);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Сервер: " + e);
            System.exit(1);
        }
    }
}