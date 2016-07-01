package com.bmc.truesight.saas.meter.client.command;

import com.bmc.truesight.saas.meter.client.model.ImmutableEvent;
import com.bmc.truesight.saas.meter.client.model.Event;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AddEventsTest {


    @Test
    public void testAddEvents() {

        Event e = defaultEvent()
                .build();

        AddEvents ae = AddEvents.of(e);

        assertThat(ae.getMethod(), is("event"));
        Map<String, Object> params = ae.getParams();

        String data = params.get("data").toString();

        validateEventString(e, data);

    }


    @Test
    public void testAddEventList() {

        Event e0 = defaultEvent()
                .build();

        Event e1 = defaultEvent()
                .title("event 2")
                .build();

        AddEvents ae = AddEvents.of(ImmutableList.of(e0, e1));

        assertThat(ae.getMethod(), is("event"));
        Map<String, Object> params = ae.getParams();

        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) params.get("data");

        validateEventString(e0, (String) data.get(0));
        validateEventString(e1, (String) data.get(1));

    }

    private void validateEventString(Event e, String eventString) {

        assertTrue(eventString.startsWith("_bevent:" + e.title().replaceAll("\\|", "\\\\|") + "|"));
        assertTrue(eventString.contains("t:" + e.severity().name()));
        assertTrue(eventString.contains("d:" + e.timestamp().getEpochSecond()));
        assertTrue(eventString.contains("properties:baseball=The Cubs,football=The Bears,basketball=The Bulls"));

        validateOptionalEventString(e.message(), "m", eventString);
        validateOptionalEventString(e.source(), "h", eventString);
        validateOptionalEventString(e.sender(), "s", eventString);
        validateOptionalEventString(e.appDataType(), "at", eventString);

        validateOptionalEventString(e.appData(), "ad", eventString);

        if (e.tags().isEmpty()) {
            assertFalse(eventString.contains("|tags:" + e.tags().stream().collect(joining(","))));
        } else {
            assertTrue(eventString.contains("|tags:" + e.tags().stream().collect(joining(","))));
        }
    }

    private void validateOptionalEventString(Optional<String> optional, String id, String eventString) {
        if (optional.isPresent()) {
            assertTrue(eventStringContains(eventString, id, optional.get()));
        } else {
            assertFalse(eventStringContains(eventString, id, optional.get()));
        }
    }

    private boolean eventStringContains(String eventString, String eventId, String data) {
        return eventString.contains("|" + eventId + ":" + data.replaceAll("\\|", "\\\\|"));
    }


     ImmutableEvent.Builder defaultEvent() {
         ImmutableMap immutableMap = ImmutableMap.of("baseball", "The Cubs", "football", "The Bears", "basketball", "The Bulls");
         return ImmutableEvent.builder()
                 .title("awesome event")
                 .message("hey now some stuff (and a pipe |)")
                 .severity(Event.Severity.error)
                 .source("saucy.fire")
                 .appData("ad stuff")
                 .appDataType("at stuff")
                 .sender("the sender")
                 .properties(immutableMap)
                 .tags(ImmutableSet.of("tag1", "tag2"));

    }

}