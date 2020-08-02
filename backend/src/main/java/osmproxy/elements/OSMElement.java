package osmproxy.elements;

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
        if (arg instanceof OSMElement){
            OSMElement obj = (OSMElement) arg;
            return this.getId().equals(obj.getId());
        }

        return false;
    }

    @Override
    public String toString() {
        return "id: " + id + "\n";
    }
}
