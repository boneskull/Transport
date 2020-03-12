package xyz.brassgoggledcoders.transport.datagen;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.CopyBlockState;
import net.minecraft.world.storage.loot.functions.CopyName;
import net.minecraft.world.storage.loot.functions.CopyNbt;
import xyz.brassgoggledcoders.transport.content.TransportBlocks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TransportLootTableProvider extends LootTableProvider {
    public TransportLootTableProvider(DataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    @Nonnull
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return Lists.newArrayList(
                Pair.of(this::getBlockTables, LootParameterSets.BLOCK)
        );
    }

    private Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> getBlockTables() {
        return acceptor -> {
            this.registerLoaderLootTable(TransportBlocks.ITEM_LOADER.getBlock(), acceptor);
            this.registerLoaderLootTable(TransportBlocks.ENERGY_LOADER.getBlock(), acceptor);
            this.registerLoaderLootTable(TransportBlocks.FLUID_LOADER.getBlock(), acceptor);
        };
    }

    private void registerLoaderLootTable(Block loader, BiConsumer<ResourceLocation, LootTable.Builder> acceptor) {
        acceptor.accept(loader.getRegistryName(), new LootTable.Builder()
                .addLootPool(LootPool.builder()
                        .acceptFunction(CopyBlockState.func_227545_a_(loader))
                        .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY))
                        .rolls(RandomValueRange.of(1, 1))
                        .addEntry(ItemLootEntry.builder(loader))
                )
        );
    }
}
