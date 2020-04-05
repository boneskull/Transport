package xyz.brassgoggledcoders.transport.routing.instruction;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import xyz.brassgoggledcoders.transport.api.routing.instruction.Routing;

import javax.annotation.Nonnull;
import java.util.List;

public class AndRouting extends Routing {
    private final List<Routing> routingList;

    public AndRouting(List<Routing> routingList) {
        this.routingList = routingList;
    }

    @Override
    public boolean matches(@Nonnull AbstractMinecartEntity minecartEntity) {
        for (Routing routing : routingList) {
            if (!routing.matches(minecartEntity)) {
                return false;
            }
        }
        return true;
    }
}