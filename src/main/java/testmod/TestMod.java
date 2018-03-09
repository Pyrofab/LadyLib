package testmod;

import ladylib.LadyLib;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TestMod.MODID)
public class TestMod {

    public static final String MODID = "test_mod";
    private LadyLib lib;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        lib = LadyLib.newLibInstance(event);
        lib.clientInit(() -> Configuration.client.maxParticles);
        lib.makeCreativeTab(() -> new ItemStack(ModItems.TEST));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        lib.getRegistrar().getItemRegistrar().generateStubModels("../src/main/resources");
    }

}
