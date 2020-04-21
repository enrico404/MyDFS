package Server;

public class SlaveServerCache {
    private String name;
    private String ip;

    public SlaveServerCache(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }
}
