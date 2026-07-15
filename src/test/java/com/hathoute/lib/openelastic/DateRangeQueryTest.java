package com.hathoute.lib.openelastic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeQueryTest {

    @Test
    void dateGtSetsFieldAndBound() {
        DateRangeQuery q = Query.dateGt("created", "2024-01-01");
        assertThat(q.field()).isEqualTo("created");
        assertThat(q.gt()).isEqualTo("2024-01-01");
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isNull();
    }

    @Test
    void dateGteSetsFieldAndBound() {
        DateRangeQuery q = Query.dateGte("created", "2024-01-01");
        assertThat(q.field()).isEqualTo("created");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isEqualTo("2024-01-01");
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isNull();
    }

    @Test
    void dateLtSetsFieldAndBound() {
        DateRangeQuery q = Query.dateLt("updated", "2024-12-31");
        assertThat(q.field()).isEqualTo("updated");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isEqualTo("2024-12-31");
        assertThat(q.lte()).isNull();
    }

    @Test
    void dateLteSetsFieldAndBound() {
        DateRangeQuery q = Query.dateLte("updated", "2024-12-31");
        assertThat(q.field()).isEqualTo("updated");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isEqualTo("2024-12-31");
    }

    @Test
    void builderCombinesBoundsWithFormatAndTimeZone() {
        DateRangeQuery q = Query.date("created")
                .gte("2024-01-01")
                .lt("2024-12-31")
                .format("yyyy-MM-dd")
                .timeZone("UTC")
                .build();
        assertThat(q.field()).isEqualTo("created");
        assertThat(q.gte()).isEqualTo("2024-01-01");
        assertThat(q.lt()).isEqualTo("2024-12-31");
        assertThat(q.format()).isEqualTo("yyyy-MM-dd");
        assertThat(q.timeZone()).isEqualTo("UTC");
    }

    @Test
    void defaultsToNullFormatAndTimeZone() {
        DateRangeQuery q = Query.dateGt("created", "2024-06-15");
        assertThat(q.format()).isNull();
        assertThat(q.timeZone()).isNull();
    }

    @Test
    void failsWhenFieldIsNull() {
        assertThatThrownBy(() -> Query.dateGt(null, "2024-01-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenFieldIsBlank() {
        assertThatThrownBy(() -> Query.dateGt("  ", "2024-01-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenNoBoundsSet() {
        assertThatThrownBy(() -> Query.date("created").build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
