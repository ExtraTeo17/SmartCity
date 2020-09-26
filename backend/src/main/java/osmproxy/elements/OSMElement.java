package osmproxy.elements;

import java.util.Objects;

public class OSMElement {
    protected final long id;

    OSMElement(final String id) {
        this.id = Long.parseLong(id);
    }

    OSMElement(final long id) {
        this.id = id;
    }

    public final long getId() {
        return id;
    }

    @Override
    public boolean equals(Object arg) {
        if (arg instanceof OSMElement) {
            OSMElement obj = (OSMElement) arg;
            return this.id == obj.id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "id: " + id + "\n";
    }

    public static OSMElement of(long id) {
        return new OSMElement(id);
    }
}
