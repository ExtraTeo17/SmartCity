package routing;

public abstract class ZoneMutator {
    final static class Mutation {
        private Mutation() {}
    }

    protected final Mutation mutation = new Mutation();
    private static int counter;

    protected ZoneMutator() {
        ++counter;
        if (counter > 1) {
            throw new IllegalCallerException("The can be only one ZoneMutator :)");
        }
    }
}
