package xyz.brassgoggledcoders.transport.content;

import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.network.locator.instance.EmptyLocatorInstance;
import com.tterrag.registrate.builders.ContainerBuilder;
import com.tterrag.registrate.util.entry.ContainerEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.transport.Transport;
import xyz.brassgoggledcoders.transport.api.TransportAPI;
import xyz.brassgoggledcoders.transport.api.module.ModuleInstance;
import xyz.brassgoggledcoders.transport.api.module.ModuleType;
import xyz.brassgoggledcoders.transport.container.EntityLocatorInstance;
import xyz.brassgoggledcoders.transport.container.locomotive.SteamLocomotiveContainer;
import xyz.brassgoggledcoders.transport.container.moduleconfigurator.ModuleConfiguratorContainer;
import xyz.brassgoggledcoders.transport.container.navigation.NavigationChartContainer;
import xyz.brassgoggledcoders.transport.screen.ModuleConfiguratorScreen;
import xyz.brassgoggledcoders.transport.screen.locomotive.SteamLocomotiveScreen;
import xyz.brassgoggledcoders.transport.screen.navigation.NavigationChartScreen;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class TransportContainers {
    private static final DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, Transport.ID);

    public static final RegistryObject<ContainerType<BasicAddonContainer>> MODULE = CONTAINERS.register("module",
            () -> IForgeContainerType.create((windowId, inv, data) -> {
                Entity entity = inv.player.getEntityWorld().getEntityByID(data.readInt());
                ModuleType moduleType = TransportAPI.getModuleType(data.readResourceLocation());

                if (entity != null && moduleType != null) {
                    return entity.getCapability(TransportAPI.MODULAR_ENTITY)
                            .map(modularEntity -> {
                                ModuleInstance<?> moduleInstance = modularEntity.getModuleInstance(moduleType);
                                if (moduleInstance != null) {
                                    return new BasicAddonContainer(moduleInstance, new EntityLocatorInstance(entity),
                                            IWorldPosCallable.DUMMY, inv, windowId);
                                } else {
                                    return new BasicAddonContainer(new Object(), new EmptyLocatorInstance(), IWorldPosCallable.DUMMY, inv,
                                            windowId);
                                }
                            }).orElseGet(() -> new BasicAddonContainer(new Object(), new EmptyLocatorInstance(),
                                    IWorldPosCallable.DUMMY, inv, windowId));
                }

                Transport.LOGGER.warn("Failed to find Module for Container");
                return new BasicAddonContainer(new Object(), new EmptyLocatorInstance(), IWorldPosCallable.DUMMY, inv,
                        windowId);
            }));

    public static final ContainerEntry<ModuleConfiguratorContainer> MODULE_CONFIGURATOR =
            Transport.getRegistrate()
                    .object("module_configurator")
                    .container(new ContainerBuilder.ContainerFactory<ModuleConfiguratorContainer>() {
                        @Override
                        @Nonnull
                        @ParametersAreNonnullByDefault
                        public ModuleConfiguratorContainer create(ContainerType<ModuleConfiguratorContainer> containerType,
                                                                  int i, PlayerInventory playerInventory) {
                            return new ModuleConfiguratorContainer(containerType, i, playerInventory);
                        }
                    }, () -> ModuleConfiguratorScreen::new)
                    .register();

    public static final RegistryEntry<ContainerType<NavigationChartContainer>> NAVIGATION_CHART =
            Transport.getRegistrate()
                    .object("navigation_chart")
                    .container(new ContainerBuilder.ContainerFactory<NavigationChartContainer>() {
                        @Override
                        @Nonnull
                        @ParametersAreNonnullByDefault
                        public NavigationChartContainer create(ContainerType<NavigationChartContainer> containerType,
                                                               int i, PlayerInventory playerInventory) {
                            return new NavigationChartContainer(containerType, i);
                        }
                    }, () -> NavigationChartScreen::new)
                    .register();

    public static final RegistryEntry<ContainerType<SteamLocomotiveContainer>> STEAM_LOCOMOTIVE =
            Transport.getRegistrate()
                    .object("steam_locomotive")
                    .container(SteamLocomotiveContainer::new, () -> SteamLocomotiveScreen::new)
                    .register();

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
