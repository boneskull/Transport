package xyz.brassgoggledcoders.transport.model.item;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
import xyz.brassgoggledcoders.transport.util.CachedValue;

import java.util.List;

public class EntityItemModelCache {
    private final static List<CachedValue<IBakedModel>> CACHED_VALUES = Lists.newArrayList();

    public static CachedValue<IBakedModel> getBakedModelCacheFor(Item item) {
        CachedValue<IBakedModel> cachedValue = new CachedValue<>(() ->
                Minecraft.getInstance()
                        .getItemRenderer()
                        .getItemModelMesher()
                        .getItemModel(item)
        );
        CACHED_VALUES.add(cachedValue);
        return cachedValue;
    }

    public static void dirtyCaches() {
        CACHED_VALUES.forEach(CachedValue::invalidate);
    }
}
