package osmproxy.elements;

import utilities.NumericHelper;

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
    public String toString() {
        return "id: " + id + "\n";
    }
}
