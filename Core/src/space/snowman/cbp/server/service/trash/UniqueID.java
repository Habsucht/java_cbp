package space.snowman.cbp.server.service.trash;

import space.snowman.cbp.server.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Эта служба демонстрирует, как следует поддерживать состояние на протяжении нескольких подключений путем
 * сохранения его в переменных экземпляра и применения синхронизованного доступа к этим переменным. Эта служба
 * подсчитывает число подключившихся к ней клиентов и сообщает каждому клиенту его номер.
 **/
public class UniqueID implements Service {
    public int id = 0;
    public synchronized int nextId() { return id++; }
    public void serve(InputStream i, OutputStream o) throws IOException {
        PrintWriter out = new PrintWriter(o);
        out.print("Ваш номер #: " + nextId() + "\n");
        out.close();
        i.close();
    }
}