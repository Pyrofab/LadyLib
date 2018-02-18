package ladylib.registration;

import ladylib.LadyLib;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class AutoRegistryRef {
    private Field field;
    private MethodHandle setUnlocalizedName;
    private boolean listed;
    private boolean makeItemBlock;

    AutoRegistryRef(Field field) {
        this.field = field;
        try {
            Method m;
            // Items and blocks have different obfuscated names for their setUnlocalizedName method
            if (Item.class.isAssignableFrom(field.getType()))
                m = ReflectionHelper.findMethod(field.getType(), "setUnlocalizedName", "func_77655_b", String.class);
            else if (Block.class.isAssignableFrom(field.getType()))
                m = ReflectionHelper.findMethod(field.getType(), "setUnlocalizedName", "func_149663_c", String.class);
            else    // If it has a setUnlocalizedName method, it is not from vanilla so not obfuscated
                m = field.getType().getMethod("setUnlocalizedName", String.class);
            if (m != null)
                setUnlocalizedName = MethodHandles.lookup().unreflect(m);
            listed = !field.isAnnotationPresent(AutoRegister.Unlisted.class);
            makeItemBlock = !field.isAnnotationPresent(AutoRegister.NoItem.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForRegistry(IForgeRegistry<?> registry) {
        return registry.getRegistrySuperType().isAssignableFrom(field.getType());
    }

    @SuppressWarnings("unchecked")
    <V extends IForgeRegistryEntry> V nameAndGet() {
        try {
            String name = field.getName();
            IForgeRegistryEntry value = ((IForgeRegistryEntry) field.get(null));
            value.setRegistryName(new ResourceLocation(LadyLib.getModId(), name));
            if (setUnlocalizedName != null)
                setUnlocalizedName.invoke(value, LadyLib.getModId() + "." + name);
            return (V) value;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    /**
     * @return true if the item should appear in the creative and JEI tabs
     */
    boolean isListed() {
        return listed;
    }

    /**
     * @return true if an item should be registered automatically for the underlying block field.
     * If the underlying field is not a block, this should be ignored.
     */
    boolean isMakeItemBlock() {
        return makeItemBlock;
    }
}
