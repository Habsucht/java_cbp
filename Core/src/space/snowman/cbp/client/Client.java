package space.snowman.cbp.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9999;

    static File loadFolder = new File("C://_loadFolder");
    static ArrayList<File> fileList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        try {
            // Соединяемся с заданными узлом и портом
            Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);

            // Создаем потоки для связи с сервером.
            DataInputStream from_server = new DataInputStream(socket.getInputStream());
            DataOutputStream to_server = new DataOutputStream(socket.getOutputStream());

            // Параметр true передается для осуществления автоматического проталкивания в методе println()
	        PrintWriter to_console = new PrintWriter(System.out, true);

            // Сообщаем пользователю, что подключение установлено
            to_console.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort() +
                            "\n\tLocalPort = " + socket.getLocalPort() +
                            "\n\tInetAddress.HostAddress = " + socket.getInetAddress().getHostAddress() +
                            "\n\tReceiveBufferSize (SO_RCVBUF) = " + socket.getReceiveBufferSize());
            to_console.println("\nStart communication...");

            to_server.writeUTF("POST: Hello!");                                         // message #1 > out
            to_server.flush();

            String lineBuffer;
            int retryCount = 0;
            int errorCount = 0;

            while (true) {
                to_console.println("*** Счётчик цикла - " + ++retryCount + " ***");

                lineBuffer = from_server.readUTF();
                to_console.println("<< " + lineBuffer + "\n--------------------------------");

                switch (lineBuffer) {
                    case "POST: Hello! What do you want?":                                  // message #2 < in
                        to_console.println("C: Получено приветствие от сервера: " + lineBuffer);
                        to_server.writeUTF("POST: I have changed the backup files.");   // message #3 > out
                        to_console.println(">> POST: I have changed the backup files.");
                        continue;
                    case "GET: Give me a list of files.":                                   // message #4 < in
                        to_console.println("C: Получен запрос на список файлов: " + lineBuffer);
                        to_server.writeUTF("POST: File's list.");                       // message #5 > out
                        to_console.println(">> POST: File's list.");
                        continue;
                    case "POST: CLOSE CONNECT":
                        to_console.println("C: Получено сообщение на закрытие подключения: " + lineBuffer);
                        socket.close();
                        to_console.println(">> socket.close()");
                        break;
                    default:
                        to_console.println("C: ОШИБКА! Неизвестная команда: " + lineBuffer);
                        to_server.writeUTF("POST: Unknown command.");
                        to_console.println(">> POST: Unknown command.");
                        ++errorCount;

                        if (errorCount > 5) {
                            to_console.println("C: Закрытие подключения по превышению допустимого числа ошибок");
                            to_server.writeUTF("POST: CLOSE CONNECT");
                            to_console.println(">> POST: CLOSE CONNECT");
                            break;
                        } else {
                            to_console.println("*** Счётчик ошибок - " + errorCount + " ***");
                        }
                        continue;
                }
            }
        } catch (Exception e) {     // Если что-то не в порядке, печатаем сообщение об ошибке.
            System.err.println(e);
        }
    }
}
