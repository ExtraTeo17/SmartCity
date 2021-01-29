package osmproxy.elements;

import utilities.ForSerialization;

import java.io.Serializable;
import java.util.Objects;

public class OSMElement implements Serializable {
    final long id;

    /*@ForSerialization
    OSMElement() {
        id = 0;
    }*/

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
        return "id: " + id;
    }

    public static OSMElement of(long id) {
        return new OSMElement(id);
    }
}
