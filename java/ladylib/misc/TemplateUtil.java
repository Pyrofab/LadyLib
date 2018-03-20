package ladylib.misc;

import ladylib.LadyLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;

public class TemplateUtil {

    public static final ResourceLocation STUB_ITEM_MODEL = new ResourceLocation("ladylib", "models/item/sample_item.json");
    public static final String NAME_TOKEN = "@NAME@";
    public static final String DOMAIN_TOKEN = "@DOMAIN@";
    public static String srcRoot;

    @Nullable
    public static String generateItemModel(ResourceLocation loc) {
        // it would be bad if stubs were generated in a random minecraft folder
        if (!LadyLib.isDevEnv()) return null;

        String domain = loc.getResourceDomain();
        String fileName = loc.getResourcePath() + ".json";
        String textureName = loc.getResourceDomain() + ":items/" + loc.getResourcePath();
        Path modelPath = Paths.get(srcRoot, "assets", domain, "models", "item", fileName);
        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(STUB_ITEM_MODEL).getInputStream();
             Scanner scan = new Scanner(in)) {
            if (modelPath.getParent().toFile().mkdirs())
                LadyLib.LOGGER.info("Created directories for " + modelPath.getParent());
            try (BufferedWriter out = Files.newBufferedWriter(modelPath, StandardOpenOption.CREATE_NEW)) {
                while (scan.hasNextLine()) {
                    out.append(scan.nextLine().replaceAll(DOMAIN_TOKEN, textureName).replaceAll(NAME_TOKEN, textureName)).append('\n');
                }
                LadyLib.LOGGER.info("Created {} stub", modelPath);
                return modelPath.toString();
            }
        } catch (FileAlreadyExistsException ignored) {
            LadyLib.LOGGER.trace("{} already exists, skipping", fileName);
        } catch (IOException e) {
            LadyLib.LOGGER.error("Error while generating stub item model", e);
        }
        return null;
    }

    public static class ModelStubsCreatedPleaseRestartTheGameException extends CustomModLoadingErrorDisplayException {
        private final List<String> createdModelFiles;

        public ModelStubsCreatedPleaseRestartTheGameException(List<String> createdModelFiles) {
            this.createdModelFiles = createdModelFiles;
        }

        @Override
        public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
        }

        @Override
        public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            fontRenderer.drawString("The following model stub files have been generated:", 30, 10, 0xFFFFFFFF);
            int i = 0;
            for (; i < createdModelFiles.size(); i++) {
                String s = createdModelFiles.get(i);
                fontRenderer.drawString(s, 5, 30 * (i+1), 0xFFFFFFFF);
            }
            fontRenderer.drawString("The game should now be restarted", 30, 30*(i+1), 0xFFFFFFFF);
        }
    }
}
