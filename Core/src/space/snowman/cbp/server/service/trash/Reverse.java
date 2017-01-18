package space.snowman.cbp.server.service.trash;

import space.snowman.cbp.server.core.*;

import java.io.*;

/**
 * Это еще один пример службы. Она считывает строки, введенные клиентом, и возвращает их перевернутыми. Она
 * также выводит приветствие и инструкции и разрывает подключение, когда пользователь вводит строку, состоящую
 * из точки «.».
 **/
public class Reverse implements Service {
    public void serve(InputStream i, OutputStream o) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(i));
        PrintWriter out =
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(o)));
        out.print("Welcome to the line reversal server.\n");
        out.print("Enter lines.  End with a '.' on a line by itself.\n");
        for(;;) {
            out.print("> ");
            out.flush();
            String line = in.readLine();
            if ((line == null) || line.equals(".")) break;
            for(int j = line.length()-1; j >= 0; j--)
                out.print(line.charAt(j));
            out.print("\n");
        }
        out.close();
        in.close();
    }
}