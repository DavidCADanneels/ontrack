package net.nemerosa.ontrack.model.job;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RunnableJobTask implements JobTask {

    private final AtomicReference<String> info = new AtomicReference<>();
    private final Consumer<Consumer<String>> runner;

    public RunnableJobTask(Consumer<Consumer<String>> runner) {
        this.runner = runner;
    }

    @Override
    public String getInfo() {
        return info.get();
    }

    @Override
    public void run() {
        runner.accept(info::set);
    }
}
