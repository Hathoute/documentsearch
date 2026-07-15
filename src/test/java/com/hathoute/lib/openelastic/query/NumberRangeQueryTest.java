package com.hathoute.lib.openelastic.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberRangeQueryTest {

    @Test
    void gtSetsFieldAndBound() {
        NumberRangeQuery q = Query.gt("price", 100);
        assertThat(q.field()).isEqualTo("price");
        assertThat(q.gt()).isEqualTo(100.0);
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isNull();
    }

    @Test
    void gteSetsFieldAndBound() {
        NumberRangeQuery q = Query.gte("price", 100);
        assertThat(q.field()).isEqualTo("price");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isEqualTo(100.0);
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isNull();
    }

    @Test
    void ltSetsFieldAndBound() {
        NumberRangeQuery q = Query.lt("age", 65);
        assertThat(q.field()).isEqualTo("age");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isEqualTo(65.0);
        assertThat(q.lte()).isNull();
    }

    @Test
    void lteSetsFieldAndBound() {
        NumberRangeQuery q = Query.lte("quantity", 10);
        assertThat(q.field()).isEqualTo("quantity");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isEqualTo(10.0);
    }

    @Test
    void builderCombinesExclusiveBounds() {
        NumberRangeQuery q = Query.num("price").gt(10).lt(100).build();
        assertThat(q.field()).isEqualTo("price");
        assertThat(q.gt()).isEqualTo(10.0);
        assertThat(q.gte()).isNull();
        assertThat(q.lt()).isEqualTo(100.0);
        assertThat(q.lte()).isNull();
    }

    @Test
    void builderCombinesInclusiveBounds() {
        NumberRangeQuery q = Query.num("price").gte(10).lte(100).build();
        assertThat(q.field()).isEqualTo("price");
        assertThat(q.gt()).isNull();
        assertThat(q.gte()).isEqualTo(10.0);
        assertThat(q.lt()).isNull();
        assertThat(q.lte()).isEqualTo(100.0);
    }

    @Test
    void builderCombinesAllBounds() {
        NumberRangeQuery q = Query.num("price").gt(5).gte(10).lt(50).lte(100).build();
        assertThat(q.gt()).isEqualTo(5.0);
        assertThat(q.gte()).isEqualTo(10.0);
        assertThat(q.lt()).isEqualTo(50.0);
        assertThat(q.lte()).isEqualTo(100.0);
    }

    @Test
    void supportsDecimalValue() {
        NumberRangeQuery q = Query.lte("score", 99.9);
        assertThat(q.lte()).isEqualTo(99.9);
    }

    @Test
    void failsWhenFieldIsNull() {
        assertThatThrownBy(() -> Query.gt(null, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenFieldIsBlank() {
        assertThatThrownBy(() -> Query.gt("  ", 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenNoBoundsSet() {
        assertThatThrownBy(() -> Query.num("price").build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
