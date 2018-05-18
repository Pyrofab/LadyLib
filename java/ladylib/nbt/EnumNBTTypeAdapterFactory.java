package ladylib.nbt;

import com.google.gson.reflect.TypeToken;
import ladylib.LadyLib;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class EnumNBTTypeAdapterFactory implements NBTTypeAdapterFactory<Enum, NBTBase> {
    @SuppressWarnings("unchecked")
    @Override
    public NBTTypeAdapter<Enum, NBTBase> create(TypeToken type, boolean allowMutating) {
        Class enumClass = type.getRawType();
        if (!Enum.class.isAssignableFrom(enumClass)) {
            return null;
        }
        return new EnumNBTTypeAdapter(enumClass);
    }

    public static class EnumNBTTypeAdapter<E extends Enum<E>> implements NBTTypeAdapter<E, NBTTagString> {

        private final Class<E> enumClass;

        public EnumNBTTypeAdapter(Class<E> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public NBTTagString toNBT(E value) {
            return new NBTTagString(value.name());
        }

        @Override
        public E fromNBT(NBTBase nbtTagString) {
            try {
                return Enum.valueOf(enumClass, cast(nbtTagString, NBTTagString.class).getString());
            } catch (IllegalArgumentException e) {
                throw new MalformedNBTException("Failed to deserialize enum field", e);
            }
        }
    }
}
