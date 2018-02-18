package ladylib.compat;

import ladylib.LadyLib;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.Item;

@JEIPlugin
public class JEICompat implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        blacklistInvisibleItems(registry.getJeiHelpers().getIngredientBlacklist());
    }

    private void blacklistInvisibleItems(IIngredientBlacklist blacklist) {
        for (Item blacklisted : LadyLib.getRegistrar().getInvisibleItems()) {
            blacklist.addIngredientToBlacklist(blacklisted);
        }
    }
}
