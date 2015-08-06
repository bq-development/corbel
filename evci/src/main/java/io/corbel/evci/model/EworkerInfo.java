package io.corbel.evci.model;

/**
 * @author Cristian del Cerro
 */
public class EworkerInfo {

    String name;
    String version;

    public EworkerInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
