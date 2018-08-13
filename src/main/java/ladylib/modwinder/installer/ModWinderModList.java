package ladylib.modwinder.installer;

import com.google.gson.reflect.TypeToken;
import ladylib.LadyLib;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.libraries.Artifact;
import net.minecraftforge.fml.relauncher.libraries.Repository;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModWinderModList {
    private static final Map<Path, ModWinderModList> CACHE = new HashMap<>();

    public static ModWinderModList create(Path modList) {
        return CACHE.computeIfAbsent(modList, ml -> {
            ModWinderModList ret = new ModWinderModList();
            try {
                if (ml.toFile().exists()) {
                    Type type = new TypeToken<Map<String, LocalModEntry>>(){}.getType();
                    ret.allMods = ModEntry.GSON.fromJson(Files.newBufferedReader(ml), type);
                }
            } catch (IOException e) {
                LadyLib.LOGGER.error("Unable to read json mod list {}", ml, e);
            }
            ret.saveFile = modList;
            return ret;
        });
    }

    private Map<String, LocalModEntry> allMods = new HashMap<>();
    private transient Path saveFile;

    public void add(ModEntry modEntry, Artifact artifact) {
        // If it just got installed, assume it is the latest version
        String version = modEntry.isInstalled() ? modEntry.getInstalledVersion() : modEntry.getLatestVersion();
        allMods.put(modEntry.getModId(), new LocalModEntry(artifact.toString(), artifact.isSnapshot() ? artifact.getTimestamp() : null, version));
    }

    @Nullable
    public Artifact getArtifact(ModEntry modEntry, Repository repo) {
        ComparableVersion latest = new ComparableVersion(modEntry.getLatestVersion());
        @Nullable LocalModEntry disabled = allMods.get(modEntry.getModId());
        if (disabled == null) {
            return null;
        }
        ComparableVersion local = new ComparableVersion(disabled.version);
        if (local.compareTo(latest) < 0) {
            return null;
        }
        return new Artifact(repo, disabled.modRef, disabled.timeStamp);
    }

    public void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(this.saveFile)) {
            ModEntry.GSON.toJson(this.allMods, writer);
        }
    }

    private static class LocalModEntry {
        private String modRef;
        private String timeStamp;
        private String version;

        private LocalModEntry() {
            super();
        }

        public LocalModEntry(String modRef, String timeStamp, String version) {
            this();
            this.modRef = modRef;
            this.timeStamp = timeStamp;
            this.version = version;
        }
    }
}
