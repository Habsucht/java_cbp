package space.snowman.cbp.server.service;

import space.snowman.cbp.server.core.*;

import java.io.*;

public class TransferFile implements Service {

    public void serve(InputStream inputStream, OutputStream outputStream) throws IOException {
        DataInputStream from_client = new DataInputStream(inputStream);
        DataOutputStream to_client = new DataOutputStream(outputStream);

        PrintWriter to_console = new PrintWriter(System.out, true);

        String lineBuffer;
        int retryCount = 0;
        int errorCount = 0;

        while (true) {
            to_console.println("*** Счётчик цикла - " + ++retryCount + " ***");

            lineBuffer = from_client.readUTF();
            to_console.println("<< " + lineBuffer + "\n--------------------------------");

            switch (lineBuffer) {
                case "POST: Hello!":                                                // message #1 < in
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    to_client.writeUTF("POST: Hello! What do you want?");       // message #2 > out
                    to_console.println(">> POST: Hello! What do you want?");
                    continue;
                case "POST: I have changed the backup files.":                      // message #3 < in
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    to_client.writeUTF("GET: Give me a list of files.");        // message #4 > out
                    to_console.println(">> GET: Give me a list of files.");
                    continue;
                case "POST: CLOSE CONNECT":
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    inputStream.close();
                    to_console.println(">> inputStream.close();");
                    outputStream.close();
                    to_console.println(">> outputStream.close();");
                    break;
                default:
                    to_console.println("S: ERROR! Unknown command: " + lineBuffer);
                    to_client.writeUTF("POST: Unknown command.");
                    to_console.println(">> POST: Unknown command.");
                    ++errorCount;

                    if (errorCount > 5) {
                        to_console.println("S: Closing connection for exceeding the maximum permissible number of errors");
                        to_client.writeUTF("POST: CLOSE CONNECT");
                        to_console.println(">> POST: CLOSE CONNECT");
                        break;
                    } else {
                        to_console.println("*** Counter error - " + errorCount + " ***");
                    }
                    continue;
            }
        }
        /**
        int filesCount;

        // Поочерёдная загрузка файлов
        for (int i = 0; i < filesCount; i++){

            long fileSize = from_client.readLong();

            String fileName = from_client.readUTF();

            byte[] buffer = new byte[64*1024];

            FileOutputStream fileOutputStream = new FileOutputStream(downloadFolder + fileName);

            int count = 0;
            int total = 0;

            while ((count = from_client.read(buffer, 0, (4096))) != -1) {
                total += count;
                fileOutputStream.write(buffer, 0, count);

                if(total == fileSize)
                    break;
            }

            fileOutputStream.flush();
            fileOutputStream.close();
         **/
    }
 }

