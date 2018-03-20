package ladylib.client;

import ladylib.registration.ItemRegistrar;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Items that implement this interface can control their rendering registration
 */
public interface ICustomLocation {

    /**
     * @return a path to the model json file for this item
     */
    ModelResourceLocation getModelLocation();

    /**
     * Called by {@link ladylib.registration.ItemRegistrar#registerRender(Item)}
     */
    @SideOnly(Side.CLIENT)
    default void registerRender() {
        ItemRegistrar.registerRender((Item) this, getModelLocation());
    }

}