package space.snowman.cbp.server.service;

import space.snowman.cbp.server.core.*;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Это нетривиальная служба. Она реализует командный протокол, дающий защищенные паролем средства управления
 * операциями сервера во время его исполнения.
 * См. метод main() класса Server, чтобы увидеть, как эта служба запускается.
 *
 * Распознаются следующие команды:
 * password:    сообщает пароль; авторизация обязательна для большинства команд
 * add:         динамически добавляет названную службу на заданном порте
 * remove:      динамически удаляет службу, работающую на заданном порте
 * max:         изменяет лимит числа подключений.
 * status:      отображает действующие службы, текущие соединения и лимит числа подключений
 * help:        отображает страницу помощи
 * quit:        отключение
 *
 * Эта служба выводит “подсказку” и посылает весь свой вывод в адрес клиента заглавными буквами. К этой службе
 * в каждый момент времени может подключиться только один клиент.
 **/
public class Control implements Service {
    Server server;             // Сервер, которым мы управляем.
    String password;           // Пароль, который мы требуем.
    boolean connected = false; // Подключен ли уже кто-то к этой службе?

    /**
     * Создаем новую службу Control. Она будет управлять заданным объектом Server и будет требовать заданный
     * пароль для авторизации. Обратите внимание на то, что у этой службы нет конструктора без аргументов.
     * Это значит, что, в отличие от вышеприведенных служб, нет возможности динамически создать ее экземпляр
     * и добавить ее к списку служб сервера.
     **/
    public Control(Server server, String password) {
        this.server = server;
        this.password = password;
    }

    /**
     * Это метод serve(), осуществляющий обслуживание. Он читает строку, отправленную клиентом, и применяет
     * java.util.StringTokenizer, чтобы разобрать ее на команды и аргументы. В зависимости от этой команды
     * он делает множество разных вещей.
     **/
    public void serve(InputStream inputStream, OutputStream outputStream) throws IOException {
        // Настраиваем потоки
        BufferedReader from_client = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter to_client = new PrintWriter(outputStream, true);
        String lineBuffer;                  // Для чтения строк из клиентского ввода
        boolean authorized = false;         // Пользователь уже сказал пароль?

        // Если к этой службе уже подключен клиент, отображаем сообщение для этого клиента и закрываем
        // подключение. Мы используем синхронизированный блок для предотвращения “состояния гонки”.
        synchronized(this) {
            if (connected) {
                to_client.print("РАЗРЕШАЕТСЯ ТОЛЬКО ОДНО ПОДКЛЮЧЕНИЕ.\n");
                to_client.close();
                return;
            }
            else connected = true;
        }

        // Это главный цикл: в нем команда считывается, анализируется и обрабатывается.
        for(;;) {
            to_client.print(">> ");                                 // Отображаем “подсказку”
            to_client.flush();                                      // Выводим ее немедленно
            lineBuffer = from_client.readLine();                    // Получаем пользовательский ввод
            if (lineBuffer == null) break;                          // Выходим из цикла при получении EOF.
            try {
                // Применяем StringTokenizer для анализа команды пользователя.
                StringTokenizer t = new StringTokenizer(lineBuffer);
                if (!t.hasMoreTokens()) {
                    continue;   // если ввод пустой
                }

                // Выделяем из ввода первое слово и приводим его к нижнему регистру
                String command = t.nextToken().toLowerCase();

                // Теперь сравниваем его со всеми допустимыми командами, выполняя для каждой команды
                // соответствующие действия
                if (command.equals("password")) {                   // Команда Password
                    String p = t.nextToken();                       // Получаем следующее слово
                    if (p.equals(this.password)) {                  // Пароль правильный?
                        to_client.print("OK\n");                    // Допустим, да
                        authorized = true;                          // Подтверждаем авторизацию
                    } else to_client.print("НЕВЕРНЫЙ ПАРОЛЬ\n");    // В противном случае – нет
                } else if (command.equals("add")) {                 // Команда Add Service
                    // Проверяем, был ли указан пароль
                    if (!authorized) {
                        to_client.print("НЕОБХОДИМ ПАРОЛЬ\n");
                    } else {
                        // Получаем название службы и пытаемся динамически загрузить ее и создать ее экземпляр.
                        // Исключения обрабатываются ниже
                        String serviceName = t.nextToken();
                        Class serviceClass = Class.forName(serviceName);
                        Service service;
                        try {
                            service = (Service)serviceClass.newInstance();
                        } catch (NoSuchMethodError e) {
                            throw new IllegalArgumentException("У службы должен быть конструктор без аргументов");
                        }
                        int port = Integer.parseInt(t.nextToken());
                        // Если никаких исключений не произошло, добавляем службу
                        server.addService(service, port);
                        to_client.print("СЛУЖБА ДОБАВЛЕНА\n");      // сообщаем об этом клиенту
                    }
                } else if (command.equals("remove")) {              // Команда удаления службы
                    if (!authorized) {
                        to_client.print("НЕОБХОДИМ ПАРОЛЬ\n");
                    } else {
                        int port = Integer.parseInt(t.nextToken());
                        server.removeService(port);                 // удаляем службу
                        to_client.print("СЛУЖБА УДАЛЕНА\n");        // сообщаем об этом клиенту
                    }
                } else if (command.equals("max")) {                 // Set connection limit
                    if (!authorized) {
                        to_client.print("НЕОБХОДИМ ПАРОЛЬ\n");
                    } else {
                        int max = Integer.parseInt(t.nextToken());
                        server.setMaxConnections(max);
                        to_client.print("ЛИМИТ ПОДКЛЮЧЕНИЙ ИЗМЕНЕН\n");
                    }
                } else if (command.equals("status")) {              // Отображение состояния
                    if (!authorized) {
                        to_client.print("НЕОБХОДИМ ПАРОЛЬ\n");
                    } else server.displayStatus(to_client);
                } else if (command.equals("help")) {                // Команда Help
                    // Отображаем синтаксис команд. Пароль необязателен
                    to_client.print("КОМАНДЫ:\n" +
                            "\tpassword <password>\n" +
                            "\tadd <service> <port>\n" +
                            "\tremove <port>\n" +
                            "\tmax <max-connections>\n" +
                            "\tstatus\n" +
                            "\thelp\n" +
                            "\tquit\n");
                } else if (command.equals("quit")) break;           // Команда Quit.
                else to_client.print("ОШИБКА! НЕИЗВЕСТНАЯ КОМАНДА\n");     // Ошибка
            } catch (Exception e) {
                // Если в процессе анализа или выполнения команды возникло исключение, печатаем сообщение об
                // ошибке, затем выводим подробные сведения об исключении.
                to_client.print("ОШИБКА ВО ВРЕМЯ АНАЛИЗА ИЛИ ВЫПОЛНЕНИЯ КОМАНДЫ:\n" + e + "\n");
            }
        }
        // Окончательно, когда происходит выход из цикла обработки команд, закрываем потоки (streams) и
        // присваиваем флагу connected значение false, так что теперь могут подключаться новые клиенты.
        connected = false;
        to_client.close();
        from_client.close();
    }
}