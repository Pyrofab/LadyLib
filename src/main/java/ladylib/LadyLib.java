package ladylib;

import ladylib.client.ShaderUtil;
import ladylib.registration.AutoRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class LadyLib {

    public static Logger LOGGER = LogManager.getLogger("LadyLib");
    private static final List<LadyLib> allInstances = new ArrayList<>();

    private ModContainer shadingMod;
    private String shadingModId;
    private CreativeTabs creativeTab;
    private AutoRegistrar registrar;

    public static LadyLib newLibInstance(FMLPreInitializationEvent event) {
        LadyLib ret = new LadyLib();
        ret.preInit(event);
        allInstances.add(ret);
        return ret;
    }

    public static boolean isDevEnv() {
        return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    public static List<LadyLib> getAllInstances() {
        return allInstances;
    }

    private LadyLib() {}

    /**
     * Call this during {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent}
     */
    public void preInit(FMLPreInitializationEvent event) {
        // automatically gets the calling mod container
        shadingMod = Loader.instance().activeModContainer();
        if (shadingMod == null)
            throw new IllegalStateException("LadyLib initialization was done at the wrong time");
        shadingModId = shadingMod.getModId();
        LOGGER = LogManager.getLogger(shadingMod.getName() + ":lib");
        registrar = new AutoRegistrar(this, event.getAsmData());
        MinecraftForge.EVENT_BUS.register(registrar);
        MinecraftForge.EVENT_BUS.register(registrar.getItemRegistrar());
        MinecraftForge.EVENT_BUS.register(registrar.getBlockRegistrar());
    }

    /**
     * Initializes client-only helpers like {@link ShaderUtil} <br/>
     * Call this in your client proxy PreInitialization method
     */
    @SideOnly(Side.CLIENT)
    public static void clientInit() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ShaderUtil::loadShaders);
    }

    public void makeCreativeTab(Supplier<ItemStack> icon) {
        creativeTab = new CreativeTabs(shadingMod.getName()) {
            @Nonnull
            @Override
            public ItemStack getTabIconItem() {
                return icon.get();
            }
        };
    }

    public CreativeTabs getCreativeTab() {
        return creativeTab;
    }

    public AutoRegistrar getRegistrar() {
        return registrar;
    }

    public String getModId() {
        return Objects.requireNonNull(shadingModId, "The enclosing mod's id was not set before calling the library");
    }

}
