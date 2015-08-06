package io.corbel.resources.model;

/**
 * @author Cristian del Cerro
 */
public class PluginInfo {

    String name;
    String version;

    public PluginInfo(String name, String version) {
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
