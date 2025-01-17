package io.github.lukebemish.excavated_variants.client;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.IInputStreamSource;
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.DynamicTextureSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TextureMetaGenerator;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.*;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

public class TextureRegistrar implements Supplier<IPathAwareInputStreamSource> {

    private final Collection<Pair<BaseOre, BaseStone>> originalPairs;
    private final List<Pair<BaseOre, BaseStone>> toMake;

    public TextureRegistrar(Collection<Pair<BaseOre, BaseStone>> originalPairs, List<Pair<BaseOre, BaseStone>> toMake) {
        this.originalPairs = originalPairs;
        this.toMake = toMake;
    }

    public Pair<IInputStreamSource, IInputStreamSource> setupExtractor(List<ResourceLocation> stoneOrigs, List<ResourceLocation> ores, ResourceLocation stoneNew, ResourceLocation out) {
        HashMap<String, AnimationSplittingSource.TimeAwareSource> sourceMap = new HashMap<>();
        int c1 = 0;
        for (ResourceLocation stoneOrig : stoneOrigs) {
            sourceMap.put("stoneOrig" + c1, new AnimationSplittingSource.TimeAwareSource(new TextureReader(stoneOrig), 1));
            c1++;
        }
        int c3 = 0;
        for (ResourceLocation ore : ores) {
            sourceMap.put("ore" + c3, new AnimationSplittingSource.TimeAwareSource(new TextureReader(ore), 1));
            c3++;
        }
        sourceMap.put("stoneNew", new AnimationSplittingSource.TimeAwareSource(new TextureReader(stoneNew), 1));

        List<ITexSource> stoneOrigSources = new ArrayList<>();
        for (int c2 = 0; c2 < c1; c2++) {
            stoneOrigSources.add(new AnimationFrameCapture("stoneOrig" + c2));
        }
        List<ITexSource> oreSources = new ArrayList<>();
        for (int c2 = 0; c2 < c3; c2++) {
            oreSources.add(new AnimationFrameCapture("ore" + c2));
        }

        var p1 = new DynamicTextureSource(out, new AnimationSplittingSource(sourceMap,
                new ForegroundTransfer(
                        new Overlay(stoneOrigSources),
                        new Overlay(oreSources),
                        new AnimationFrameCapture("stoneNew"),
                        6,
                        true,
                        true,
                        true,
                        0.2)));

        List<ResourceLocation> sources = new ArrayList<>(stoneOrigs);
        sources.addAll(ores);
        sources.add(stoneNew);

        var p2 = new TextureMetaGenerator(
                sources,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                out);
        return new Pair<>(p1, p2);
    }

    @Override
    public IPathAwareInputStreamSource get() {
        Map<ResourceLocation, Supplier<InputStream>> resources = BlockStateAssembler.getMap(this, this.originalPairs, this.toMake);
        return new IPathAwareInputStreamSource() {
            @Override
            public @NotNull Set<ResourceLocation> getLocations() {
                return resources.keySet();
            }

            @Override
            public @NotNull Supplier<InputStream> get(ResourceLocation outRl) {
                return resources.getOrDefault(outRl, () -> null);
            }
        };
    }
}
