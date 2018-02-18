package ladylib.registration;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import ladylib.LadyLib;
import ladylib.client.ICustomLocation;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class in charge of item-specific registration behaviour
 */
public class ItemRegistrar {

    /**
     * A map tracking all registered items and whether they should be invisible in the creative and JEI tabs
     * (the value is true if the item is invisible)
     */
    private Object2BooleanMap<Item> allItems = new Object2BooleanOpenHashMap<>();

    /**
     * Adds an item to the list
     *
     * @param item   the item to be registered
     * @param listed whether this item will appear in the creative and JEI tabs
     */
    public void addItem(@Nonnull Item item, boolean listed) {
        allItems.put(item, !listed);
    }

    /**
     * Needs to be called after the main registrar has discovered all items
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void registerItems(RegistryEvent.Register<Item> event) {
        allItems.forEach((item, listed) -> {
            event.getRegistry().register(item);
            if (listed)
                item.setCreativeTab(LadyLib.getCreativeTab());
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
        allItems.keySet().stream()
                .filter(itemIn -> !(Block.getBlockFromItem(itemIn) instanceof BlockFluidBase))
                .forEach(ItemRegistrar::registerRender);
    }

    @SideOnly(Side.CLIENT)
    public static void registerRender(Item item) {
        if (item instanceof ICustomLocation)    // let the item handle its model registration
            ((ICustomLocation) item).registerRender();
        else    // use the standard procedure
            registerRender(item, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()).toString()));
    }

    @SideOnly(Side.CLIENT)
    public static void registerRender(Item item, ModelResourceLocation loc) {
        ModelLoader.setCustomModelResourceLocation(item, 0, loc);
    }

    public List<Item> getInvisibleItems() {
        return allItems.object2BooleanEntrySet().stream()
                .filter(Object2BooleanMap.Entry::getBooleanValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
