package ladylib.registration;

import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegister {

    /**
     * The mod id for which this class should automatically register stuff
     */
    String value();

    /**
     * The type of registry entry to automatically register
     */
    Class<? extends IForgeRegistryEntry> type();

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ignore {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Invisible {
    }

}
