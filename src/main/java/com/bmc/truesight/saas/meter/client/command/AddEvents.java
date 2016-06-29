package com.bmc.truesight.saas.meter.client.command;

import com.bmc.truesight.saas.meter.client.model.Event;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class AddEvents extends VoidCommand {

    private static final String EVENT_FIELD_FMT = "%s:%s";
    private static final String FIELD_DELIMITER = "|";

    private static final String TITLE_ID = "_bevent";
    private static final String MESSAGE_ID = "m";
    private static final String TYPE_ID = "t";
    private static final String SOURCE_ID = "h";
    private static final String SENDER_ID = "s";
    private static final String TIMESTAMP_ID = "d";
    private static final String TAGS_ID = "tags";
    private static final String AT_ID = "at";
    private static final String AD_ID = "ad";
    private static final String PROPERTIES_ID = "properties";


    private static final Predicate<String> hasPipes = Pattern.compile("\\" + FIELD_DELIMITER).asPredicate();

    private static final  BiFunction<String, String, String> format = (id, data) -> {
        if (hasPipes.test(data)) {
            return String.format(EVENT_FIELD_FMT, id, data.replaceAll("\\" + FIELD_DELIMITER, "\\\\" + FIELD_DELIMITER));
        } else {
            return String.format(EVENT_FIELD_FMT, id, data);
        }
    };

    private final ImmutableMap<String, Object> params;

    public AddEvents(ImmutableMap<String, Object> params) {
        this.params = params;
    }

    public static AddEvents of(Event event) {
        return new AddEvents(ImmutableMap.of("data", toEventString(event)));
    }

    public static AddEvents of(List<Event> events) {
        return new AddEvents(ImmutableMap.of("data", toEventArrayString(events)));
    }

    private static List<String> toEventArrayString(List<Event> events) {

        return events
                .stream()
                .map(AddEvents::toEventString)
                .collect(toList());

    }

    private static String toEventString(Event event) {

        final ImmutableList.Builder<String> fields = ImmutableList.builder();

        fields.add(format(TITLE_ID, event.title()));
        fields.add(format(TYPE_ID, event.severity().name()));

        // TODO: hacking the ms time to seconds for the moment as the meter doesn't
        // yet handle millisecond resolution. Need to come back and fix this once the meter
        // is updated
        fields.add(format(TIMESTAMP_ID, String.valueOf(event.timestamp().getEpochSecond())));


        event.message().ifPresent(message -> fields.add(format(MESSAGE_ID, message)));
        event.source().ifPresent(source -> fields.add(format(SOURCE_ID, source)));
        event.sender().ifPresent(sender -> fields.add(format(SENDER_ID, sender)));
        if (!event.tags().isEmpty()) {
            String tagStr = event.tags()
                    .stream()
                    .collect(joining(","));
            fields.add(format(TAGS_ID, tagStr));
        }

        event.appDataType().ifPresent(at -> fields.add(format(AT_ID, at)));
        event.appData().ifPresent(ad -> fields.add(format(AD_ID, ad)));
        String propString = event.properties()
                .entrySet()
                .stream()
                .map(pair -> pair.getKey() + "=" + pair.getValue())
                .collect(joining(","));
        fields.add(format(PROPERTIES_ID, propString));

        return fields.build().stream().collect(joining(FIELD_DELIMITER));
    }

    private static String format(String fieldId, String data) {
        return format.apply(fieldId, data);
    }

    @Override
    public String getMethod() {
        return "event";
    }

    @Override
    public ImmutableMap<String, Object> getParams() {
        return this.params;
    }

}
