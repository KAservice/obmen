package Obmen.BDEntities;

public class ConfigurationEntity {

    public String url;
    public String user;
    public String password;
    public int baseID;
    public String obmenURL;
    public long delay;
    public long period;

    public ConfigurationEntity(String url, String user, String password, int baseID, String obmenURL, long delay, long period) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.baseID = baseID;
        this.obmenURL = obmenURL;
        this.delay = delay;
        this.period = period;

    }

    public ConfigurationEntity() {
    }

    @Override
    public String toString() {
        return "ConfigurationEntity{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", baseID='" + baseID + '\'' +
                '}';
    }
}
