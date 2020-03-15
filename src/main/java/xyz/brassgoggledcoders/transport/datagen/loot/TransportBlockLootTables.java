package xyz.brassgoggledcoders.transport.datagen.loot;

import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.Direction;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.functions.CopyBlockState;
import net.minecraft.world.storage.loot.functions.CopyNbt;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.transport.Transport;
import xyz.brassgoggledcoders.transport.block.loader.LoaderBlock;
import xyz.brassgoggledcoders.transport.content.TransportBlocks;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransportBlockLootTables extends BlockLootTables {

    @Override
    @Nonnull
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS
                .getValues()
                .stream()
                .filter(block -> Optional.ofNullable(block.getRegistryName())
                        .filter(registryName -> registryName.getNamespace().equals(Transport.ID))
                        .isPresent()
                ).collect(Collectors.toList());
    }

    @Override
    protected void addTables() {
        this.registerLoader(TransportBlocks.ITEM_LOADER.getBlock());
        this.registerLoader(TransportBlocks.ENERGY_LOADER.getBlock());
        this.registerLoader(TransportBlocks.FLUID_LOADER.getBlock());
        this.registerDropSelfLootTable(TransportBlocks.DIAMOND_CROSSING_RAIL.getBlock());
        this.registerDropSelfLootTable(TransportBlocks.HOLDING_RAIL.getBlock());
        this.registerDropSelfLootTable(TransportBlocks.ELEVATOR_SWITCH_RAIL.getBlock());
        this.registerLootTable(TransportBlocks.ELEVATOR_SWITCH_SUPPORT.get(), LootTable.builder());
        this.registerDropSelfLootTable(TransportBlocks.SCAFFOLDING_RAIL.getBlock());
    }

    private void registerLoader(Block loader) {
        CopyBlockState.Builder copyBlockStateBuilder = CopyBlockState.func_227545_a_(loader);

        IntStream.range(0, Direction.values().length)
                .mapToObj(Direction::byIndex)
                .map(LoaderBlock.PROPERTIES::get)
                .forEach(copyBlockStateBuilder::func_227552_a_);

        this.registerLootTable(loader, new LootTable.Builder()
                .addLootPool(LootPool.builder()
                        .acceptFunction(copyBlockStateBuilder)
                        .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                                .replaceOperation("capability", "BlockEntityTag.capability"))
                        .rolls(RandomValueRange.of(1, 1))
                        .addEntry(ItemLootEntry.builder(loader))
                )
        );
    }
}
