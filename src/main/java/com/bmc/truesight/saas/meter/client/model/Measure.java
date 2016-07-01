package com.bmc.truesight.saas.meter.client.model;

import org.immutables.value.Value;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Immutable representation of a single measurement
 */
@Value.Immutable
public abstract class Measure {


    public abstract String name();
    public abstract double value();
    @Value.Default
    public Instant timestamp() {
        return Instant.now();
    }
    public abstract Optional<String> source();

    /**
     * Creates a Measure using the required parameters: name and value.
     * The timestamp for this Measure defaults to <a href="https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html#now--">Instant.now()</a>.
     * The source of the event will be the meter name.
     * @param name  the name of the Metric displayed in Pulse (must not be greater than 100 characters in length).
     * @param value the value for this Metric.
     * @return an Immutable Measure with the name and value given.
     */
    public static Measure of(String name, double value) {
        return ImmutableMeasure.builder()
                .name(name)
                .value(value)
                .build();
    }

    /**
     * Creates a Measure using the required parameters: name, value and timestamp.  The source of the event will be the
     * meter name.
     *
     * @param name      the name of the Metric displayed in Pulse (must not be greater than 100 characters in length).
     * @param value     the value for this Metric.
     * @param timestamp specifies the time the Measure
     * @return an Immutable Measure with the name and value given.
     */
    public static Measure of(String name, double value, Instant timestamp) {
        return ImmutableMeasure.builder()
                .name(name)
                .value(value)
                .timestamp(timestamp)
                .build();
    }

    /**
     * Creates a Measure using the required parameters: name, value and source.
     * The timestamp for this Measure defaults to <a href="https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html#now--">Instant.now()</a>.
     *
     * @param name   the name of the Metric displayed in Pulse (must not be greater than 100 characters in length).
     * @param value  the value for this Metric.
     * @param source the event source (must not be greater than 100 characters in length).
     * @return an Immutable Measure with the name and value given.
     */
    public static Measure of(String name, double value, String source) {
        return ImmutableMeasure.builder()
                .name(name)
                .value(value)
                .source(source)
                .build();
    }

    /**
     * Creates a Measure using the required parameters: name, value, timestamp and source.
     *
     * @param name      the name of the Metric displayed in Pulse (must not be greater than 100 characters in length).
     * @param value     the value for this Metric.
     * @param timestamp specifies the time the Measure
     * @param source    the event source (must not be greater than 100 characters in length).
     * @return an Immutable Measure with the name and value given.
     */
    public static Measure of(String name, double value, Instant timestamp, String source) {
        return ImmutableMeasure.builder()
                .name(name)
                .value(value)
                .timestamp(timestamp)
                .source(source)
                .build();
    }

    @Value.Check
    protected void check() {
        checkArgument(name().getBytes().length <= 100, "Metric name length must not be > 100 bytes");
        source().ifPresent(m -> {
            checkArgument(m.getBytes().length <= 100, "Source name length must not be > 100 bytes");
        });
    }
}
