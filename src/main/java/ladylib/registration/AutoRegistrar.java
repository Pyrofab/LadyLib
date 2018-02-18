package ladylib.registration;

import ladylib.LadyLib;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class handling most of the registration work automatically
 */
public class AutoRegistrar {

    private ItemRegistrar itemRegistrar;
    private BlockRegistrar blockRegistrar;

    private List<AutoRegistryRef> references = new ArrayList<>();

    public AutoRegistrar(ASMDataTable asmData) {
        this.itemRegistrar = new ItemRegistrar();
        this.blockRegistrar = new BlockRegistrar(itemRegistrar);
        // find all classes that will be handled by this registrar
        Set<ASMDataTable.ASMData> allRegistryHandlers = asmData.getAll(AutoRegister.class.getName());
        for (ASMDataTable.ASMData data : allRegistryHandlers) {
            // each mod using this library is shading it so we must only affect the shading mod
            boolean isShadingModProperty = data.getAnnotationInfo().get("value").equals(LadyLib.getModId());
            if (isShadingModProperty) {
                try {
                    scanClassForFields(Class.forName(data.getClassName(), false, getClass().getClassLoader()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanClassForFields(Class<?> autoRegisterClass) {
        for (Field f : autoRegisterClass.getFields()) {
            int mods = f.getModifiers();
            // use the same criteria as ObjectHolderRegistry to detect candidates
            boolean isMatch = Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods);
            // No point in trying to automatically register non registrable fields
            if (isMatch && IForgeRegistryEntry.class.isAssignableFrom(f.getType()) && !f.isAnnotationPresent(AutoRegister.Ignore.class)) {
                references.add(new AutoRegistryRef(f));
            }
        }
    }

    @SubscribeEvent
    public <T extends IForgeRegistryEntry<T>> void onRegistryRegister(RegistryEvent.Register<T> event) {
        references.stream()
                // Only register for the right event, incidentally filters out entries with no corresponding registry
                .filter(ref -> ref.isValidForRegistry(event.getRegistry()))
                .forEach(ref -> {
                    T value = ref.nameAndGet();
                    // items and blocks have additional registration behaviours
                    if (value instanceof Item) {
                        itemRegistrar.addItem((Item) value, ref.isListed());
                    } else if (value instanceof Block) {
                        blockRegistrar.addBlock((Block) value, ref.isListed(), ref.isMakeItemBlock());
                    } else {
                        event.getRegistry().register(value);
                    }
                });
    }

    public BlockRegistrar getBlockRegistrar() {
        return blockRegistrar;
    }

    public ItemRegistrar getItemRegistrar() {
        return itemRegistrar;
    }
}
