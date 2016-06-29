package com.bmc.truesight.saas.meter.client.model;

import com.google.common.collect.ImmutableSet;
import org.immutables.value.Value;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Immutable representation of a single event
 */
@Value.Immutable
public abstract class Event {

    /**
     * Used to describe the type of event.  Possible values:<UL>
     * <LI><CODE>Severity.info</CODE></LI>
     * <LI><CODE>Severity.warn</CODE></LI>
     * <LI><CODE>Severity.error</CODE></LI>
     * <LI><CODE>Severity.critical</CODE></LI>
     * </UL>
     */
    public enum Severity {
        info, warn, error, critical
    }

    public abstract String title();
    @Value.Default
    public Severity severity() {
        return Severity.info;
    }
    @Value.Default
    public Instant timestamp() {
        return Instant.now();
    }
    public abstract Optional<String> message();
    public abstract Optional<String> source();
    public abstract ImmutableSet<String> tags();
    public abstract Optional<String> appDataType();
    public abstract Optional<String> appData();
    public abstract Optional<String> sender();
    public abstract Map<String, String> properties();

    /**
     * Creates an Event with the given required parameter: title.
     *
     * @param title the Title of this event (must not be greater than 255 characters long).
     * @return a new Immutable Event built with the title provided.
     */
    public static Event of(String title) {
        return ImmutableEvent.builder()
                .title(title)
                .build();
    }

    /**
     * Creates an Event with the given required parameters: title and severity.
     *
     * @param title    the Title of this event (must not be greater than 255 characters long).
     * @param severity the Severity of the event (info, warn, error or critical)
     * @return a new Immutable Event built with the title and severity provided.
     */
    public static Event of(String title, Severity severity) {
        return ImmutableEvent.builder()
                .title(title)
                .severity(severity)
                .build();
    }

    /**
     * Creates an Event with the given required parameters: title, severity and message.
     *
     * @param title    the Title of this event (must not be greater than 255 characters long).
     * @param severity the Severity of the event (info, warn, error or critical)
     * @param message  a message for this event (must not be greater than 255 characters long).
     * @return a new Immutable Event built with the title, severity and message provided.
     */
    public static Event of(String title, Severity severity, String message) {
        return ImmutableEvent.builder()
                .title(title)
                .severity(severity)
                .message(message)
                .build();
    }

    /**
     * Creates an Event with the given required parameters: title, severity, message and properties.
     *
     * @param title      the Title of this event (must not be greater than 255 characters long).
     * @param severity   the Severity of the event (info, warn, error or critical)
     * @param message    a message for this event (must not be greater than 255 characters long).
     * @param properties a Map of properties Key and Values, must not be null
     * @return a new Immutable Event built with the title, severity, message and properties provided.
     */
    public static Event of(String title, Severity severity, String message, Map<String, String> properties) {
        return ImmutableEvent.builder()
                .title(title)
                .severity(severity)
                .message(message)
                .properties(properties)
                .build();
    }

    @Value.Check
    protected void check() {
        checkArgument(title().getBytes().length <= 255, "Title length must not be > 255 bytes");
        message().ifPresent(m -> {
            checkArgument(m.getBytes().length <= 255, "Message length must not be > 255 bytes");
        });
    }
}
