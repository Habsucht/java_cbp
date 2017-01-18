package space.snowman.cbp.server.util;

import java.io.IOException;
import java.net.InetAddress;

public class localInfo {
    String IP;
    String hostName;

    public localInfo() {
        try {
            InetAddress hostAddress = InetAddress.getLocalHost();

            this.IP = hostAddress.getHostAddress();
            this.hostName = hostAddress.getHostName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
