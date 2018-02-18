package ladylib;

import ladylib.client.ShaderUtil;
import ladylib.registration.AutoRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Objects;

public class LadyLib {

    public static Logger LOGGER = LogManager.getLogger("LadyLib");

    private static ModContainer shadingMod;
    private static String shadingModId;
    private static CreativeTabs creativeTab;
    private static AutoRegistrar registrar;

    /**
     * Call this during {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent}
     */
    public static void preInit(FMLPreInitializationEvent event) {
        shadingMod = Loader.instance().activeModContainer();
        if (shadingMod == null)
            throw new IllegalStateException("LadyLib initialization was done at the wrong time");
        shadingModId = shadingMod.getModId();
        LOGGER = LogManager.getLogger(shadingMod.getName());
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ShaderUtil::loadShaders);
        registrar = new AutoRegistrar(event.getAsmData());
        MinecraftForge.EVENT_BUS.register(registrar);
    }

    public static void makeCreativeTab(ItemStack icon) {
        creativeTab = new CreativeTabs(shadingMod.getName()) {
            @Nonnull
            @Override
            public ItemStack getTabIconItem() {
                return icon;
            }
        };
    }

    public static CreativeTabs getCreativeTab() {
        return creativeTab;
    }

    public static AutoRegistrar getRegistrar() {
        return registrar;
    }

    public static String getModId() {
        return Objects.requireNonNull(shadingModId, "The enclosing mod's id was not set before calling the library");
    }
}
