package space.snowman.cbp.server.service;

import space.snowman.cbp.server.core.Service;
import space.snowman.cbp.server.model.EMail;

import java.io.*;
import java.util.Base64;

public class SMTP implements Service {

    public void serve(InputStream inputStream, OutputStream outputStream) throws IOException {
        BufferedReader from_client = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter to_client = new PrintWriter(outputStream, true);

        PrintWriter to_console = new PrintWriter(System.out, true);

        String lineBuffer;
        int retryCount = 0;
        int errorCount = 0;
        boolean data = false;

        EMail eMail = new EMail();

        to_client.println("220 localhost ESMTP SubEthaSMTP null");
        to_console.println(">> 220 localhost ESMTP SubEthaSMTP null");

        while (true) {
            to_console.println("--------------------------------\n *** Counter - " + ++retryCount + " ***");

            lineBuffer = from_client.readLine();
            to_console.println("<< " + lineBuffer + "\n--------------------------------");

            if (errorCount > 5) break;

            if (!data) {
                if (lineBuffer.substring(0, 4).toUpperCase().equals("EHLO") || lineBuffer.substring(0, 4).toUpperCase().equals("HELO")) {
                    to_console.println("S: Message received from the client: " + lineBuffer);

                    to_client.println("250-localhost Hello client.server.net");
                    to_client.println("250 DSN");

                    to_console.println(">> 250-localhost Hello client.server.net\n" +
                            ">> 250 DSN\n");

                } else if (lineBuffer.substring(0, 4).toUpperCase().equals("MAIL")) {
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    to_client.println("250 2.1.0 Ok");
                    to_console.println(">> 250 2.1.0 Ok");

                } else if (lineBuffer.substring(0, 4).toUpperCase().equals("RCPT")) {
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    to_client.println("250 2.1.5 Ok");
                    to_console.println(">> 250 2.1.5 Ok");

                } else if (lineBuffer.substring(0, 4).toUpperCase().equals("DATA")) {
                    to_console.println("S: Message received from the client: " + lineBuffer);
                    to_client.println("354 End data with <CR><LF>.<CR><LF>");
                    to_console.println(">> 354 End data with <CR><LF>.<CR><LF>");
                    data = true;

                } else {
                    ++errorCount;
                    to_console.println("S: ERROR! Unknown command: " + lineBuffer);
                    to_client.println("Unknown command.");
                    to_console.println(">> POST: Unknown command. " + errorCount);
                }
            } else {
                if (lineBuffer != null) {
                    to_console.println(lineBuffer);
                    if (lineBuffer.contains("MIME-Version: ") & lineBuffer.indexOf("MIME-Version: ", 0) == 0) {
                        eMail.setMime(lineBuffer.substring(14));
                    } else if (lineBuffer.contains("From: ") & lineBuffer.indexOf("From: ", 0) == 0) {
                        eMail.setFrom(lineBuffer.substring(6));
                    } else if (lineBuffer.contains("To: ") & lineBuffer.indexOf("To: ", 0) == 0) {
                        eMail.setTo(lineBuffer.substring(4));
                    } else if (lineBuffer.contains("Date: ") & lineBuffer.indexOf("Date: ", 0) == 0) {
                        eMail.setDate(lineBuffer.substring(6));
                    } else if (lineBuffer.contains("Subject: ") & lineBuffer.indexOf("Subject: ", 0) == 0) {
                        eMail.setSubject(lineBuffer.substring(9));
                    } else if (lineBuffer.contains("Content-Type: ") & lineBuffer.indexOf("Content-Type: ", 0) == 0) {
                        eMail.setContentType(lineBuffer.substring(14));
                    } else if (lineBuffer.contains("Content-Transfer-Encoding: ") & lineBuffer.indexOf("Content-Transfer-Encoding: ", 0) == 0) {
                        eMail.setContentTransferEncoding(lineBuffer.substring(27));
                    } else if (lineBuffer.equals("")) {
                        while (true) {
                            lineBuffer = from_client.readLine();
                            if (!lineBuffer.equals(".")) {
                                if (eMail.getContentTransferEncoding().contains("base64")) {
                                    eMail.setDataBase64(eMail.getDataBase64() + lineBuffer);

                                    byte[] decodedStr = Base64.getMimeDecoder().decode(lineBuffer);
                                    eMail.setDataText(eMail.getDataText() + new String(decodedStr, "utf-8"));
                                } else {
                                    eMail.setDataText(eMail.getDataText() + lineBuffer);
                                }
                            } else {
                                to_client.println("250 2.0.0 Ok");
                                to_console.println(">> 250 2.0.0 Ok");
                                to_client.println("221 localhost CommuniGate Pro SMTP closing connection");
                                to_console.println(">> 221 localhost CommuniGate Pro SMTP closing connection");
                                to_console.println(eMail.toString());
                                break;
                            }
                        }
                        break;
                    }
                } else {
                    ++errorCount;
                }
            }
        }
    }
}

