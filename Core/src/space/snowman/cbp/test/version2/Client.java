package space.snowman.cbp.test.version2;

import java.net.*;
import java.io.*;

public class Client {
    private static final int serverPort = 6666;
    private static final String localhost = "127.0.0.1";

    public static void main(String[] ar) {
        Socket socket = null;
        try {
            try {
                System.out.println("Welcome to Client side\n" +
                        "Connecting to the server\n\t" +
                        "(IP address " + localhost + ", port " + serverPort + ")");
                InetAddress ipAddress = InetAddress.getByName(localhost);
                socket = new Socket(ipAddress, serverPort);
                System.out.println("The connection is established.");

                System.out.println(
                        "\tLocalPort = " + socket.getLocalPort() +
                        "\n\tInetAddress.HostAddress = " + socket.getInetAddress().getHostAddress() +
                        "\n\tReceiveBufferSize (SO_RCVBUF) = " + socket.getReceiveBufferSize());

                // Получаем входной и выходной потоки сокета для обмена сообщениями с сервером
                InputStream socketInputStream  = socket.getInputStream();
                OutputStream socketOutputStream = socket.getOutputStream();

                DataInputStream dataInputStream  = new DataInputStream(socketInputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(socketOutputStream);

                // Создаем поток для чтения с клавиатуры.
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader keyboard = new BufferedReader(inputStreamReader);
                String line = null;
                System.out.println("Type in something and press enter");
                System.out.println();

                while (true) {
                    // Пользователь должен ввести строку и нажать Enter
                    line = keyboard.readLine();
                    dataOutputStream.writeUTF(line);     // Отсылаем строку серверу
                    dataOutputStream.flush();            // Завершаем поток
                    line = dataInputStream.readUTF();    // Ждем ответа от сервера
                    if (line.endsWith("quit"))
                        break;
                    else {
                        System.out.println("The server sent me this line :\n\t" + line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}