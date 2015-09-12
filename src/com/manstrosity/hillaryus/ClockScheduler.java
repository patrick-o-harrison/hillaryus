package com.manstrosity.hillaryus;

import java.util.HashMap;

public class ClockScheduler {

    private final HashMap<String, ScheduleEntry> entries = new HashMap<>();

    public void schedule(String name, long interval, Schedulable callback) {
        entries.put(name, new ScheduleEntry(callback, interval));
    }

    public void remove(String name) {
        entries.remove(name);
    }

    public void suspend(String name) {
        ScheduleEntry entry = entries.get(name);
        if (entry != null) {
            entry.suspended = true;
        }
    }

    public void resume(String name) {
        ScheduleEntry entry = entries.get(name);
        if (entry != null) {
            entry.suspended = false;
        }
    }

    public void update(long currentTime) {
        entries.forEach((key, value) -> {
            value.update(currentTime);
        });
    }

    public void update() {
        update(System.nanoTime());
    }

    private class ScheduleEntry {

        public boolean suspended = false;
        private long lastTrigger = System.nanoTime();
        private final long interval;
        private final Schedulable callback;

        ScheduleEntry(Schedulable callback, long interval) {
            this.interval = interval;
            this.callback = callback;
        }

        void update(long currentTime) {
            if (!suspended && currentTime >= lastTrigger + interval) {
                callback.run(currentTime);
                lastTrigger = currentTime;
            }
        }
    }
}
