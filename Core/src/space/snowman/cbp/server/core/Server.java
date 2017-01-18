package space.snowman.cbp.server.core;

/*
 * Copyright (c) 2000 David Flanagan.  All rights reserved.
 * This code is from the book Java Examples in a Nutshell, 2nd Edition.
 * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
 * You may study, use, and modify it for any non-commercial purpose.
 * You may distribute it non-commercially as long as you retain this notice.
 * For a commercial use license, or to purchase the book (recommended),
 * visit http://www.davidflanagan.com/javaexamples2.
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Этот класс представляет собой универсальный шаблон настраиваемого многопоточного сервера. Он ожидает
 * подключений по любому числу заданных портов и, получив подключение к порту, передает потоки ввода
 * и вывода заданному объекту Service, осуществляющему реальное обслуживание. Он может ограничивать число
 * одновременных подключений и регистрировать свои действия в заданном потоке.
 **/
public class Server {
    // Параметры состояния сервера
    Map services;                   // Хеш-таблица, связывающая порты с объектами Listener
    Set connections;                // Набор текущих подключений
    int maxConnections;             // Лимит одновременных подключений
    ThreadGroup threadGroup;        // Группа всех наших потоков исполнения
    PrintWriter logStream;          // Сюда мы направляем наш регистрационный вывод

    /**
     * Это конструктор сервера Server(). Ему должны передаваться поток (stream), в который направляется
     * регистрационный вывод (возможно, null), и максимальное число одновременных подключений.
     **/
    public Server(OutputStream logStream, int maxConnections) {
        setLogStream(logStream);
        log("Сервер запущен");
        threadGroup = new ThreadGroup(Server.class.getName());
        this.maxConnections = maxConnections;
        services = new HashMap();
        connections = new HashSet(maxConnections);
    }

    /**
     * Открытый (public) метод, устанавливающий текущий регистрационный поток. Аргументу null соответствует
     * отключение регистрации.
     **/
    public synchronized void setLogStream(OutputStream out) {
        if (out != null) {
            logStream = new PrintWriter(out);
        } else {
            logStream = null;
        }
    }

    /** Записываем заданную строку в регистрационный журнал */
    protected synchronized void log(String s) {
        if (logStream != null) {
            logStream.println("[" + new Date() + "] " + s);
            logStream.flush();
        }
    }

    /** Записываем заданный объект в регистрационный журнал */
    protected void log(Object o) { log(o.toString()); }

    /** Этот метод заставляет сервер открыть новую службу. Он запускает заданный объект Service на заданном порте. */
    public synchronized void addService(Service service, int port) throws IOException {
        Integer key = new Integer(port);  // ключ хеш-таблицы
        // Проверяем, не занят ли этот порт какой-либо службой
        if (services.get(key) != null) throw new IllegalArgumentException("Порт " + port + " уже используется.");
        // Создаем объект Listener, который будет ожидать подключений к этому порту
        Listener listener = new Listener(threadGroup, port, service);
        // Сохраняем его в хеш-таблице
        services.put(key, listener);
        // Регистрируем событие
        log("Запуск службы " + service.getClass().getName() + " по порту " + port);
        // Запускаем listener.
        listener.start();
    }

    /**
     * Этот метод заставляет сервер закрыть службу по заданному порту. Он не закрывает существующие подключения
     * к этой службе, а просто приказывает серверу прекратить принимать новые подключения.
     **/
    public synchronized void removeService(int port) {
        Integer key = new Integer(port);  // Ключ хеш-таблицы
        // Ищем в хеш-таблице объект Listener, соответствующий заданному порту
        final Listener listener = (Listener) services.get(key);
        if (listener == null) return;
        // Просим listener остановиться
        listener.pleaseStop();
        // Удаляем его из хеш-таблицы
        services.remove(key);
        // И регистрируем событие.
        log("Остановка службы " + listener.service.getClass().getName() + " по порту " + port);
    }

    /**
     * Этот вложенный подкласс класса Thread «слушает сеть». Он ожидает попыток подключиться к заданному порту
     * (с помощью ServerSocket), и когда получает запрос на подключение, вызывает метод сервера addConnection(),
     * чтобы принять (или отклонить) подключение. Для каждой службы Service, предоставляемой сервером Server,
     * есть один объект Listener.
     **/
    public class Listener extends Thread {
        ServerSocket listen_socket;    // Объект ServerSocket, ожидающий подключений
        int port;                      // Прослушиваемый порт
        Service service;               // Служба по этому порту
        volatile boolean stop = false; // Признак команды остановки

        /**
         * Конструктор Listener создает для себя поток исполнения в составе заданной группы. Он создает объект
         * ServerSocket, ожидающий подключений по заданному порту. Он настраивает ServerSocket так, чтобы его
         * можно было прервать, за счет чего служба может быть удалена с сервера.
         **/
        public Listener(ThreadGroup group, int port, Service service) throws IOException {
            super(group, "Listener:" + port);
            listen_socket = new ServerSocket(port);
            // Задаем ненулевую паузу, чтобы accept() можно было прервать
            listen_socket.setSoTimeout(600000);
            this.port = port;
            this.service = service;
        }

        /** Это вежливый способ сообщить Listener, что нужно прекратить прием новых подключений */
        public void pleaseStop() {
            this.stop = true;              // Установка флага остановки
            this.interrupt();              // Прекращение блокировки в accept().
            try { listen_socket.close(); } // Прекращение ожидания новых подключений.
            catch(IOException e) {}
        }

        /**
         * Класс Listener является подклассом класса Thread, его тело приведено ниже. Ожидаем запросов на
         * подключение, принимаем их и передаем Socket методу сервера addConnection.
         **/
        public void run() {
            while(!stop) {      // Цикл продолжается, пока нас не попросят остановиться.
                try {
                    Socket client = listen_socket.accept();
                    addConnection(client, service);
                } catch (InterruptedIOException e) {

                } catch (IOException e) {
                    log(e);
                }
            }
        }
    }

    /**
     * Это метод, вызываемый объектами Listener, когда они принимают соединение с клиентом. Он либо создает
     * объект Connection для этого подключения и добавляет его в список имеющихся подключений, либо, если
     * лимит подключений исчерпан, закрывает подключение.
     **/
    protected synchronized void addConnection(Socket s, Service service) {
        // Если лимит числа подключений исчерпан,
        if (connections.size() >= maxConnections) {
            try {
                // сообщаем клиенту, что его запрос отклонен.
                PrintWriter out = new PrintWriter(s.getOutputStream());
                out.print("В подключении отказано: сервер перегружен, попытайтесь подключиться позже.\n");
                out.flush();
                // И закрываем подключение клиента, которому отказано.
                s.close();
                // И, конечно, делаем об этом регистрационную запись.
                log("Подключение отклонено для " + s.getInetAddress().getHostAddress() +
                        ":" + s.getPort() + ": исчерпан лимит числа подключений.");
            } catch (IOException e) {log(e);}
        } else {    // В противном случае, если лимит не исчерпан,
            // создаем процесс Connection для обработки этого подключения.
            Connection c = new Connection(s, service);
            // Добавляем его в список текущих подключений.
            connections.add(c);
            // Регистрируем новое соединение.
            log("Установлено подключение к " + s.getInetAddress().getHostAddress() +
                    ":" + s.getPort() + " по порту " + s.getLocalPort() +
                    " для службы " + service.getClass().getName());
            // И запускаем процесс Connection, предоставляющий услугу.
            c.start();
        }
    }

    /**
     * Процесс Connection вызывает этот метод непосредственно перед выходом. Он удаляет заданный объект
     * Connection из набора подключений.
     **/
    protected synchronized void endConnection(Connection c) {
        connections.remove(c);
        log("Подключение к " + c.client.getInetAddress().getHostAddress() +
                ":" + c.client.getPort() + " закрыто.");
    }

    /** Этот метод изменяет максимально допустимое число подключений */
    public synchronized void setMaxConnections(int max) {
        maxConnections = max;
    }

    /**
     * Этот метод выводит в заданный поток информацию о статусе сервера.Он может применяться для отладки и
     * ниже в этом примере используется службой Control.
     **/
    public synchronized void displayStatus(PrintWriter out) {
        // Отображаем список всех предоставляемых служб.
        Iterator keys = services.keySet().iterator();
        while(keys.hasNext()) {
            Integer port = (Integer) keys.next();
            Listener listener =	(Listener) services.get(port);
            out.print("СЛУЖБА " + listener.service.getClass().getName() +
                    " ПО ПОРТУ " + port + "\n");
        }

        // Отображаем текущее ограничение на число подключений.
        out.print("ЛИМИТ ПОДКЛЮЧЕНИЙ: " + maxConnections + "\n");

        // Отображаем список всех текущих подключений.
        Iterator conns = connections.iterator();
        while(conns.hasNext()) {
            Connection c = (Connection)conns.next();
            out.print("ПОДКЛЮЧЕНИЕ К " +
                    c.client.getInetAddress().getHostAddress() +
                    ":" + c.client.getPort() +
                    " ПО ПОРТУ " + c.client.getLocalPort() +
                    " ДЛЯ СЛУЖБЫ " + c.service.getClass().getName() + "\n");
        }
    }

    /**
     * Этот подкласс класса Thread обрабатывает индивидуальные подключения между клиентом и службой Service,
     * предоставляемой настоящим сервером. Поскольку каждое такое подключение обладает собственным потоком
     * исполнения, у каждой службы может иметься несколько подключений одновременно. Вне зависимости от всех
     * других используемых потоков исполнения, именно это делает наш сервер многопоточным.
     **/
    public class Connection extends Thread {
        Socket client;     // Объект Socket для общения с клиентом.
        Service service;   // Служба, предоставляемая клиенту.

        /**
         * Этот конструктор просто сохраняет некоторые параметры состояния и вызывает конструктор родительского
         * класса для создания потока исполнения, обрабатывающего подключение. Объекты Connection создаются
         * потоками исполнения Listener. Эти потоки являются частью группы потоков сервера, поэтому процессы
         * Connection также входят в эту группу
         **/
        public Connection(Socket client, Service service) {
            super("Server.Connection:" +
                    client.getInetAddress().getHostAddress() +
                    ":" + client.getPort());
            this.client = client;
            this.service = service;
        }

        /**
         * Это тело любого и каждого потока исполнения Connection. Все, что оно делает, – это передает потоки ввода
         * и вывода клиента методу serve() заданного объекта Service, который несет ответственность за чтение и
         * запись в эти потоки для осуществления действительного обслуживания. Вспомним, что объект Service был
         * передан методом Server.addService() объекту Listener, а затем через метод addConnection() этому объекту
         * Connection, и теперь наконец используется для предоставления услуги. Обратите внимание на то, что
         * непосредственно перед выходом этот поток исполнения всегда вызывает метод endConnection(), чтобы удалить
         * себя из набора подключений.
         **/
        public void run() {
            try {
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();
                service.serve(in, out);
            } catch (IOException e) {
                log(e);
            }
            finally { endConnection(this); }
        }
    }
}