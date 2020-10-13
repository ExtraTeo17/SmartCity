package smartcity.task.runnable.abstractions;

import java.util.concurrent.ScheduledFuture;

public interface ICompletableRunnable {
    ScheduledFuture<?> getSelf();

    default void onComplete() {
        onComplete(false);
    }

    default void onComplete(boolean interruptRunning) {
        boolean interrupted = false;
        try {
            ScheduledFuture<?> self;
            while ((self = getSelf()) == null) {
                //noinspection NestedTryStatement
                try {
                    //noinspection BusyWait
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            self.cancel(interruptRunning);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
