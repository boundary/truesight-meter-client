package com.bmc.truesight.saas.meter.client.command;

import com.bmc.truesight.saas.meter.client.model.ImmutableMeasure;
import com.bmc.truesight.saas.meter.client.model.Measure;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AddMeasuresTest {

    @Test
    public void testAddMeasures() {

        Measure m = ImmutableMeasure.builder()
                .name("the-measure")
                .value(44.2)
                .build();
        AddMeasures am = AddMeasures.of(m);

        String data = (String) am.getParams().get("data");
        System.out.println(data);

        validateMeasureString(m, data);
        assertFalse(data.contains("s:Vulcan"));
    }

    @Test
    public void testAddMeasureList() {

        Measure m0 = ImmutableMeasure.builder()
                .name("the-measure")
                .value(44.2)
                .build();
        Measure m1 = ImmutableMeasure.builder()
                .name("the-other-measure")
                .value(34.2)
                .source("another_source")
                .build();
        AddMeasures am = AddMeasures.of(ImmutableList.of(m0, m1));

        @SuppressWarnings("unchecked")
        List<String> data = (List<String>) am.getParams().get("data");

        validateMeasureString(m0, data.get(0));
        validateMeasureString(m1, data.get(1));
        assertFalse(data.contains("s:Vulcan"));
    }

    @Test
    public void testAddMeasuresWithSource() {

        Measure m = ImmutableMeasure.builder()
                                    .name("the-measure")
                                    .value(44.2)
                                    .source(Optional.of("Vulcan"))
                                    .build();
        AddMeasures am = AddMeasures.of(m);

        String data = (String) am.getParams().get("data");
        System.out.println(data);

        validateMeasureString(m, data);
        assertTrue(data.contains("s:Vulcan"));
    }

    private void validateOptionalMeasureString(Optional<String> optional, String id, String measureString) {
        if (optional.isPresent()) {
            assertTrue(measureStringContains(measureString, id, optional.get()));
        } else {
            assertFalse(measureStringContains(measureString, id, ""));
        }
    }

    private boolean measureStringContains(String measureString, String id, String data) {
        return measureString.contains("|" + id + ":" + data.replaceAll("\\|", "\\\\|"));
    }

    private void validateMeasureString(Measure m, String data) {
        assertTrue(data.startsWith("_bmetric:" + m.name().toUpperCase()));
        assertTrue(data.contains("v:" + m.value()));
        assertTrue(data.contains("t:" + m.timestamp().getEpochSecond()));

        System.out.println(m.toString());
        validateOptionalMeasureString(m.source(), "s", data);
    }
}
