package testmod;

import ladylib.registration.AutoRegister;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

@AutoRegister(TestMod.MODID)
@GameRegistry.ObjectHolder(TestMod.MODID)
public class ModItems {
    public static final Item TEST = new Item();
    @AutoRegister.Unlisted
    public static final Item HIDDEN = new Item();
}
