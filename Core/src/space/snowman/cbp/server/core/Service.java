package space.snowman.cbp.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Здесь описан интерфейс Service, с которым мы так часто встречались. Он определяет только один метод, который
 * вызывается для предоставления услуги. Методу serve() передаются поток ввода и поток вывода, связанные с
 * клиентом. Он может делать с ними все, что угодно, только перед выходом должен закрыть их.
 *
 * Все соединения с этой службой по одному порту совместно используют один объект Service. Таким образом, любое
 * локальное состояние индивидуального подключения должно храниться в локальных переменных метода serve().
 * Состояние, характеризующее все подключения по данному порту, должно храниться в переменных экземпляра класса
 * Service. Если одна служба Service запущена на нескольких портах, обычно будут иметься несколько экземпляров
 * Service, по одному на каждый порт. Данные, относящиеся ко всем подключениям на всех портах, должны храниться
 * в статических переменных.
 *
 * Обратите внимание на то, что если экземпляры этого интерфейса будут динамически создаваться методом main()
 * класса Server, в их реализации должен быть включен конструктор без аргументов.
 **/
public interface Service {

    public void serve(InputStream inputStream, OutputStream outputStream) throws IOException;

}