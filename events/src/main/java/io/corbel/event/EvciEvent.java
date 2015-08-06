package io.corbel.event;

import io.corbel.eventbus.Event;

/**
 * @author Cristian del Cerro
 */
public class EvciEvent implements Event {
    private String type;
    private String data;

    public EvciEvent() {}

    public EvciEvent(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EvciEvent))
            return false;

        EvciEvent evciEvent = (EvciEvent) o;

        if (data != null ? !data.equals(evciEvent.data) : evciEvent.data != null)
            return false;
        if (type != null ? !type.equals(evciEvent.type) : evciEvent.type != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

}
