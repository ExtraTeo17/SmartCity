package genesis;

import com.google.inject.Binder;
import com.google.inject.Module;

abstract class AbstractModule implements Module {
    @Override
    public void configure(Binder binder) {
    }
}
