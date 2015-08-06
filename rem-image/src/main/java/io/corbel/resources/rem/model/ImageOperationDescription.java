package io.corbel.resources.rem.model;

public class ImageOperationDescription {

    private final String name;
    private final String parameters;

    public ImageOperationDescription(String name, String parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImageOperationDescription))
            return false;

        ImageOperationDescription that = (ImageOperationDescription) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return !(parameters != null ? !parameters.equals(that.parameters) : that.parameters != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

}
