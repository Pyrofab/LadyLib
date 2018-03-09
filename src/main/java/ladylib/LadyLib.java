package ladylib;

import ladylib.client.ParticleManager;
import ladylib.client.ShaderUtil;
import ladylib.registration.AutoRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
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

    @SideOnly(Side.CLIENT)
    private ParticleManager particleManager;

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
    private void preInit(FMLPreInitializationEvent event) {
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
     * Initializes client-only helpers like {@link ShaderUtil} or {@link ParticleManager} <br/>
     * Call this in your client proxy PreInitialization method
     */
    public void clientInit(Supplier<Integer> maxParticles) {
        // safety checks can't hurt
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) return;

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ShaderUtil::loadShaders);
        particleManager = new ParticleManager(maxParticles);
        MinecraftForge.EVENT_BUS.register(particleManager);
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

    @SideOnly(Side.CLIENT)
    public ParticleManager getParticleManager() {
        return particleManager;
    }
}
