package OSMProxy.Elements;

public class OSMElement {

    protected final long id;

    public OSMElement(final String id) {
        this.id = Long.parseLong(id);
    }

    public OSMElement(final long id) {
        this.id = id;
    }

    public final Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object arg) {
        OSMElement obj = (OSMElement) arg;
        return this.getId().equals(obj.getId());
    }

    @Override
    public String toString() {
        return "id: " + id + "\n";
    }
}
