package ladylib.registration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

class AutoRegistryRef {
    private static final LoadingCache<Class<?>, MethodHandle> unlocalizedNamesCache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(type -> {
                if (type == null) return null;
                try {
                    Method m;
                    // Items and blocks have different obfuscated names for their setUnlocalizedName method
                    if (Item.class.isAssignableFrom(type))
                        m = ReflectionHelper.findMethod(type, "setUnlocalizedName", "func_77655_b", String.class);
                    else if (Block.class.isAssignableFrom(type))
                        m = ReflectionHelper.findMethod(type, "setUnlocalizedName", "func_149663_c", String.class);
                    else    // If it has a setUnlocalizedName method, it is not from vanilla so not obfuscated
                        m = type.getMethod("setUnlocalizedName", String.class);
                    if (m != null)
                        return MethodHandles.lookup().unreflect(m);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
    }));

    private Field field;
    private MethodHandle setUnlocalizedName;
    private boolean listed;
    private boolean makeItemBlock;
    private String modId;

    AutoRegistryRef(String modId, Field field) {
        this.modId = modId;
        this.field = field;
        try {
            setUnlocalizedName = unlocalizedNamesCache.get(field.getType());
            listed = !field.isAnnotationPresent(AutoRegister.Unlisted.class);
            makeItemBlock = !field.isAnnotationPresent(AutoRegister.NoItem.class);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForRegistry(IForgeRegistry<?> registry) {
        return registry.getRegistrySuperType().isAssignableFrom(field.getType());
    }

    @SuppressWarnings("unchecked")
    <V extends IForgeRegistryEntry> V nameAndGet() {
        try {
            String name = field.getName().toLowerCase(Locale.ENGLISH);
            IForgeRegistryEntry value = ((IForgeRegistryEntry) field.get(null));
            value.setRegistryName(new ResourceLocation(modId, name));
            if (setUnlocalizedName != null)
                setUnlocalizedName.invoke(value, modId + "." + name);
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
