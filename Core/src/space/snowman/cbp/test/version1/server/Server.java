package space.snowman.cbp.test.version1.server;

import java.io.*;
import java.net.*;

public class Server {
    static final int DEFAULT_PORT = 9999;

    static File downloadFolder = new File("C://_downloadFolder");

    public static void main(String args[]) {
        try {
            // Создаем ServerSocket для прослушивания порта.
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);

            // Теперь входим в бесконечный цикл ожидания и обработки подключений.
            while (true) {

                // Ожидаем подключения клиента. Метод переходит в состояние ожидания соединения;
                // он возвращает уже установленное соединение с клиентом
                Socket socket = serverSocket.accept();

                // Получаем потоки ввода и вывода для разговора с клиентом
                DataInputStream from_client = new DataInputStream(socket.getInputStream());
                DataOutputStream to_client = new DataOutputStream(socket.getOutputStream());

                // Параметр true передается для осуществления автоматического проталкивания в методе println()
                PrintWriter to_console = new PrintWriter(System.out, true);

                if (socket.isConnected()) {
                    to_console.println("Client is connected..." +
                                    "\n\tLocalPort = " + socket.getLocalPort() +
                                    "\n\tInetAddress.HostAddress = " + socket.getInetAddress().getHostAddress() +
                                    "\n\tReceiveBufferSize (SO_RCVBUF) = " + socket.getReceiveBufferSize());
                }

                String lineBuffer;
                int retryCount = 0;
                int errorCount = 0;

                while (true) {
                    to_console.println("*** Счётчик цикла - " + ++retryCount + " ***");

                    lineBuffer = from_client.readUTF();
                    to_console.println("<< " + lineBuffer + "\n--------------------------------");

                    switch (lineBuffer) {
                        case "POST: Hello!":                                                // message #1 < in
                            to_console.println("S: Получено приветствие от клиента: " + lineBuffer);
                            to_client.writeUTF("POST: Hello! What do you want?");       // message #2 > out
                            to_console.println(">> POST: Hello! What do you want?");
                            continue;
                        case "POST: I have changed the backup files.":                      // message #3 < in
                            to_console.println("S: Получено сообщение от клиента: " + lineBuffer);
                            to_client.writeUTF("GET: Give me a list of files.");        // message #4 > out
                            to_console.println(">> GET: Give me a list of files.");
                            continue;
                        case "POST: CLOSE CONNECT":
                            to_console.println("S: Получено сообщение на закрытие подключения: " + lineBuffer);
                            socket.close();
                            to_console.println(">> socket.close()");
                            break;
                        default:
                            to_console.println("S: ОШИБКА! Неизвестная команда: " + lineBuffer);
                            to_client.writeUTF("POST: Unknown command.");
                            to_console.println(">> POST: Unknown command.");
                            ++errorCount;

                            if (errorCount > 5) {
                                to_console.println("S: Закрытие подключения по превышению допустимого числа ошибок");
                                to_client.writeUTF("POST: CLOSE CONNECT");
                                to_console.println(">> POST: CLOSE CONNECT");
                                break;
                            } else {
                                to_console.println("*** Счётчик ошибок - " + errorCount + " ***");
                            }
                            continue;
                    }
                }
            } // И возвращаемся к началу цикла, где будем ожидать нового подключения
        } catch (Exception e) {     // Если что-то не в порядке, печатаем сообщение об ошибке
            System.err.println(e);
        }
    }
}
