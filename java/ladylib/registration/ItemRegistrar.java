package ladylib.registration;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import ladylib.LadyLib;
import ladylib.client.ICustomLocation;
import ladylib.misc.TemplateUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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

    private final LadyLib ladyLib;
    /**
     * A map tracking all registered items and whether they should be invisible in the creative and JEI tabs
     * (the value is true if the item is unlisted)
     */
    private Object2BooleanMap<Item> allItems = new Object2BooleanOpenHashMap<>();

    public ItemRegistrar(LadyLib ladyLib) {
        this.ladyLib = ladyLib;
    }

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
        allItems.forEach((item, unlisted) -> {
            event.getRegistry().register(item);
            if (!unlisted)
                item.setCreativeTab(ladyLib.getCreativeTab());
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

    /**
     * Call that anytime between item registration and model registration
     *
     * @param srcRoot the location of the <tt>resources</tt> directory in which the files will be generated
     */
    public void generateStubModels(String srcRoot) {
        TemplateUtil.srcRoot = srcRoot;
        List<String> createdModelFiles = allItems.keySet().stream()
                .filter(itemIn -> !(itemIn instanceof ItemBlock) && !(itemIn instanceof ICustomLocation))
                .map(Item::getRegistryName)
                .map(TemplateUtil::generateItemModel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!createdModelFiles.isEmpty())
            throw new TemplateUtil.ModelStubsCreatedPleaseRestartTheGameException(createdModelFiles); // Because stupid forge prevents System.exit()
    }

    public List<Item> getInvisibleItems() {
        return allItems.object2BooleanEntrySet().stream()
                .filter(Object2BooleanMap.Entry::getBooleanValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
