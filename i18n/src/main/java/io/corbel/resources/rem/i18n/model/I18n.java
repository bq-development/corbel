package io.corbel.resources.rem.i18n.model;

import javax.validation.constraints.NotNull;

public class I18n {
    private String id;
    @NotNull private String key;
    @NotNull private String value;
    @NotNull private String lang;

    public I18n() {}

    public String getId() {
        return id;
    }

    public I18n setId(String id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return key;
    }

    public I18n setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public I18n setValue(String value) {
        this.value = value;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public I18n setLang(String lang) {
        this.lang = lang;
        return this;
    }

}
