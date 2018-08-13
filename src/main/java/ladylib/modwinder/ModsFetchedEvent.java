package ladylib.modwinder;

import ladylib.modwinder.installer.ModEntry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;
import java.util.stream.Stream;

/**
 * This event is fired when the list of available mod entries for ModWinder's mod bar
 * has been retrieved from the remote API.
 * <p>
 * <em>Note: This event will not be fired on the main thread !</em>
 * Use scheduled tasks if you need to interact with the latter.
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class ModsFetchedEvent extends Event {
    private List<ModEntry> retrievedMods;

    public ModsFetchedEvent(List<ModEntry> retrievedMods) {
        this.retrievedMods = retrievedMods;
    }

    public Stream<ModEntry> readRetrievedMods() {
        return retrievedMods.stream();
    }

    public void addModEntry(ModEntry entry) {
        this.retrievedMods.add(entry);
    }
}