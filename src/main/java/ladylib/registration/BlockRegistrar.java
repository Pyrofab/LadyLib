package ladylib.registration;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import ladylib.LadyLib;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

public class BlockRegistrar {

    private ItemRegistrar itemRegistrar;
    /**
     * A map tracking all registered items and whether they should be invisible in the creative and JEI tabs
     * (the value is true if the item is invisible)
     */
    private Object2BooleanMap<Block> allBlocks = new Object2BooleanOpenHashMap<>();

    public BlockRegistrar(ItemRegistrar itemRegistrar) {
        this.itemRegistrar = itemRegistrar;
    }

    /**
     * Adds a block to register
     *
     * @param block         the block to add
     * @param listed        whether this block's item should appear in the creative and JEI tabs
     * @param makeItemBlock whether this block should get an associated ItemBlock created and registered automatically
     */
    public void addBlock(Block block, boolean listed, boolean makeItemBlock) {
        Function<Block, Item> itemGen;
        if (makeItemBlock)
            // a default function generating a default ItemBlock and giving it the block's own registry name
            itemGen = ((Function<Block, ItemBlock>) ItemBlock::new).andThen(item -> item.setRegistryName(Objects.requireNonNull(block.getRegistryName())));
        else
            itemGen = b -> Items.AIR;
        addBlock(block, listed, itemGen);
    }

    /**
     * @param block             the block to be registered
     * @param listed            if false, this block will not appear in the creative and JEI tabs
     * @param blockItemFunction a function to create an {@link ItemBlock} from the passed block
     *                          If the result is Items.AIR, no item will be registered for this block
     *                          <b>DO NOT</b> return a null Item
     * @return the generated ItemBlock
     */
    @SuppressWarnings("unchecked")
    public <T extends Item> T addBlock(Block block, boolean listed, Function<Block, T> blockItemFunction) {
        // adds the block to the list to be registered later
        allBlocks.put(block, listed);
        if (listed)
            block.setCreativeTab(LadyLib.getCreativeTab());
        // adds the corresponding item to the list of items to be registered as well
        T item = blockItemFunction.apply(block);
        if (item != Items.AIR)
            itemRegistrar.addItem(item, listed);
        // returns the obtained item in case I want to do something with it
        return item;
    }

    /**
     * Needs to be called after the main registrar has discovered all blocks
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        allBlocks.keySet().forEach(event.getRegistry()::register);
    }

    /**
     * Maps all states of a block to a custom {@link net.minecraft.client.renderer.block.model.IBakedModel}
     *
     * @param block the block to be mapped
     * @param rl    The model resource location for your custom baked model
     */
    @SideOnly(Side.CLIENT)
    public void registerSmartRender(Block block, ModelResourceLocation rl) {
        StateMapperBase ignoreState = new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState iBlockState) {
                return rl;
            }
        };
        ModelLoader.setCustomStateMapper(block, ignoreState);
    }
}
