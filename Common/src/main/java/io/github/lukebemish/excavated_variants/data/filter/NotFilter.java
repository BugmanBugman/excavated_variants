package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

public record NotFilter(Filter filter) implements ObjectFilter {
    public static final Codec<NotFilter> CODEC = RecordCodecBuilder.create(i->i.group(
            Filter.CODEC.fieldOf("filter").forGetter(NotFilter::filter)
    ).apply(i,NotFilter::new));

    @Override
    public boolean matches(BaseOre ore, BaseStone stone) {
        return !filter().matches(ore, stone);
    }

    @Override
    public boolean matches(String ore, String stone) {
        return !filter().matches(ore, stone);
    }

    @Override
    public Codec<? extends ObjectFilter> codec() {
        return CODEC;
    }
}