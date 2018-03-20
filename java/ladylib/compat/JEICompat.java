package ladylib.compat;

import ladylib.LadyLib;
import ladylib.registration.AutoRegistrar;
import ladylib.registration.ItemRegistrar;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collection;

@JEIPlugin
public class JEICompat implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        blacklistInvisibleItems(registry.getJeiHelpers().getIngredientBlacklist());
    }

    private void blacklistInvisibleItems(IIngredientBlacklist blacklist) {
        LadyLib.getAllInstances().stream()
                .map(LadyLib::getRegistrar)
                .map(AutoRegistrar::getItemRegistrar)
                .map(ItemRegistrar::getInvisibleItems)
                .flatMap(Collection::stream)
                .map(ItemStack::new)
                .forEach(blacklist::addIngredientToBlacklist);
    }
}
