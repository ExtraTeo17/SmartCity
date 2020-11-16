package agents;

import agents.abstractions.IRegistrable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator implements IRegistrable {
    public static final int resetValue = 1;
    private final ConcurrentMap<Class<?>, AtomicInteger> idMap;

    IdGenerator() {
        this.idMap = new ConcurrentHashMap<>();
    }

    private void register(Class<?> type) {
        idMap.putIfAbsent(type, new AtomicInteger());
    }

    @Override
    public final void register(Class<?>... types) {
        for (var type : types) {
            register(type);
        }
    }

    @Override
    public void registerAll(Class<?>[] types) {
        for (var type : types) {
            register(type);
        }
    }

    public int get(Class<?> type) {
        return idMap.get(type).getAndIncrement();
    }

    public void reset(Class<?> type) {
        idMap.get(type).set(resetValue);
    }
}
