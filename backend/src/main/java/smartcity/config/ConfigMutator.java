package smartcity.config;

// contribution: https://stackoverflow.com/a/18634125/6841224
public abstract class ConfigMutator {
    public static final class Mutation {
        private Mutation() {}
    }

    protected final Mutation mutation = new Mutation();
    private static int counter;

    protected ConfigMutator() {
        ++counter;
        if (counter > 1) {
            throw new IllegalCallerException("The can be only one ConfigMutator :)");
        }
    }
}
