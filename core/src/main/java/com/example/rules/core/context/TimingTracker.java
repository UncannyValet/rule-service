package com.example.rules.core.context;

import java.io.Serializable;
import java.util.*;

/**
 * Track timings and print out a summary.
 */
public class TimingTracker implements Serializable {
    private final List<Timing> timings = new ArrayList<>();
    private final Map<String, Timing> timingsMap = new HashMap<>();
    private long activeStartTime;
    private long activeTime;
    private int activeCount;

    private Timing currentTiming;

    /**
     * Start timing a new operation with the given name.
     *
     * @param name operation name
     * @return this
     */
    public synchronized TimingTracker start(String name) {
        currentTiming = new Timing(name);
        timingsMap.put(name, currentTiming);
        checkActiveStart();
        return this;
    }

    /**
     * Restarts a timing operation with the given name,
     * starting a new one if none are found
     *
     * @param name operation name
     * @return this
     */
    public synchronized TimingTracker restart(String name) {
        Timing timing = timingsMap.get(name);
        if (timing == null) {
            return start(name);
        } else {
            timing.restart();
            currentTiming = timing;
            checkActiveStart();
            return this;
        }
    }

    private void checkActiveStart() {
        if (activeCount++ == 0) {
            // First start of an active span, record time
            activeStartTime = System.currentTimeMillis();
        }
    }

    /**
     * End timing the most recently started operation.
     *
     * @return this
     */
    public TimingTracker end() {
        end(currentTiming);
        return this;
    }

    /**
     * End timing the operation with the given name.
     *
     * @param name operation name
     * @return this
     */
    public TimingTracker end(String name) {
        end(timingsMap.get(name));
        return this;
    }

    private synchronized void end(Timing timing) {
        if (timing != null) {
            timing.end();
            timings.add(new Timing(timing));
            if (timing.equals(currentTiming)) {
                // Handle case where the currentTiming is ended by name
                currentTiming = null;
            }
            if (activeCount <= 1) {
                activeTime += System.currentTimeMillis() - activeStartTime;
                activeCount = 0;
            } else {
                --activeCount;
            }
        }
    }

    /**
     * Get the elapsed time of the operation with the given name.
     *
     * @param name operation name
     * @return elapsed time in milliseconds
     */
    public long getElapsed(String name) {
        Timing timing = timingsMap.get(name);
        if (timing != null) {
            return timing.getElapsed();
        }
        return 0;
    }

    /**
     * Get the total elapsed time from the beginning of the first operation to the end of the last operation.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsed() {
        long start = timings.stream()
                .mapToLong(t -> t.start)
                .min()
                .orElse(0L);
        long end = timings.stream()
                .mapToLong(t -> t.end)
                .max()
                .orElse(0);
        return end - start;
    }

    /**
     * Get the total elapsed time of all operations, treated independently
     *
     * @return total time in milliseconds
     */
    public long getTotal() {
        long retval = 0;
        for (Timing timing : timingsMap.values()) {
            retval += timing.getElapsed();
        }
        return retval;
    }

    public long getActive() {
        return activeTime;
    }

    /**
     * Determine if any timings have been recorded.
     *
     * @return true if no timings have been recorded.
     */
    public boolean isEmpty() {
        return timings.isEmpty();
    }

    @Override
    public String toString() {
        if (timings.isEmpty()) {
            return "";
        }
        return timings + " (elapsed: " + getElapsed() + " ms)" + (currentTiming != null ? " *" : "");
    }

    private static class Timing implements Serializable {
        private final String name;
        private long start;
        private long end;
        private boolean running;

        Timing(String name) {
            this.name = name;
            this.start = System.currentTimeMillis();
            running = true;
        }

        Timing(Timing timing) {
            name = timing.name;
            start = timing.start;
            end = timing.end;
            running = timing.running;
        }

        void end() {
            if (running) {
                end = System.currentTimeMillis();
                running = false;
            }
        }

        void restart() {
            if (!running) {
                start = System.currentTimeMillis() - getElapsed();
                running = true;
            }
        }

        long getElapsed() {
            if (running) {
                return System.currentTimeMillis() - start;
            } else {
                return end - start;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || !obj.getClass().equals(getClass())) return false;

            return Objects.equals(this.name, ((Timing)obj).name);
        }

        @Override
        public String toString() {
            return name + ": " + getElapsed() + " ms";
        }
    }
}
