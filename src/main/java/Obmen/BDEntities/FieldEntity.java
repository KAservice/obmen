package Obmen.BDEntities;

public class FieldEntity {

    private String name;
    private int type;
    private String value;
    private String domain;

    public FieldEntity(String name, int type, String value, String domain) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "FieldEntity{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
