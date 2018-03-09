package testmod;

import ladylib.registration.AutoRegister;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.GameRegistry;

@AutoRegister(TestMod.MODID)
@GameRegistry.ObjectHolder(TestMod.MODID)
public class ModBlocks {
    public static final Block TEST_BLOCK = new Block(Material.ANVIL);
    @AutoRegister.Ignore
    public static final Block IGNORE = Blocks.AIR;

}
