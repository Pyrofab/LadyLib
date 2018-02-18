package ladylib.registration;

import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AutoRegister can be used to automatically populate appropriate registries from the class' public static final fields.
 * It is effectively the counterpart to {@link net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder}
 *
 * Will also add items to the mod's creative tab
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegister {

    /**
     * The mod id for which this class should automatically register stuff
     */
    String value();

    /**
     * If put on a field inside a class annotated with AutoRegister, that field will be excluded from automatic registration
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ignore {
    }

    /**
     * If put on an Item field, the corresponding item will not be added to the creative tab.
     * If JEI is installed, will also prevent the item from being displayed in its ingredient list.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Invisible {
    }

}
