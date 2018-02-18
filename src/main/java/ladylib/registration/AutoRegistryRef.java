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
    private boolean invisible;

    AutoRegistryRef(Field field) {
        this.field = field;
        try {
            Method m;
            // Items and blocks have different obfuscated names for their setUnlocalizedName method
            if (Item.class.isAssignableFrom(field.getDeclaringClass()))
                m = ReflectionHelper.findMethod(field.getDeclaringClass(), "setUnlocalizedName", "func_77655_b", String.class);
            else if (Block.class.isAssignableFrom(field.getDeclaringClass()))
                m = ReflectionHelper.findMethod(field.getDeclaringClass(), "setUnlocalizedName", "func_149663_c", String.class);
            else    // If it has a setUnlocalizedName method, it is not from vanilla so not obfuscated
                m = field.getDeclaringClass().getMethod("setUnlocalizedName", String.class);
            if (m != null)
                setUnlocalizedName = MethodHandles.lookup().unreflect(m);
            invisible = field.isAnnotationPresent(AutoRegister.Invisible.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForRegistry(IForgeRegistry<?> registry) {
        return registry.getRegistrySuperType().isAssignableFrom(field.getDeclaringClass());
    }

    @SuppressWarnings("unchecked")
    <V extends IForgeRegistryEntry> V nameAndGet() {
        try {
            String name = field.getName();
            IForgeRegistryEntry value = ((IForgeRegistryEntry)field.get(null));
            value.setRegistryName(new ResourceLocation(LadyLib.getModId(), name));
            if (setUnlocalizedName != null)
                setUnlocalizedName.invoke(value, name);
            return (V) value;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    boolean isInvisible() {
        return invisible;
    }
}
