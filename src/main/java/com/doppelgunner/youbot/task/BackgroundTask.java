package com.doppelgunner.youbot.task;

import javafx.concurrent.Task;

/**
 * Created by robertoguazon on 17/07/2017.
 */
public class BackgroundTask extends Task<Void>  {

    private Runnable runnable;

    public BackgroundTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected Void call() throws Exception {
        runnable.run();
        return null;
    }
}
