package space.snowman.cbp.server.service.trash;

import space.snowman.cbp.server.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

/** Очень простая служба. Она сообщает клиенту текущее время на сервере и закрывает подключение. */

public class Time implements Service {
    public void serve(InputStream i, OutputStream o) throws IOException {
        PrintWriter out = new PrintWriter(o);
        out.print(new Date() + "\n");
        out.close();
        i.close();
    }
}