package ladylib.misc;

import com.google.common.annotations.Beta;
import ladylib.LadyLib;
import ladylib.client.ItemRenderRegistrationHandler;
import ladylib.registration.BlockRegistrar;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A class to help generating base json files for registered blocks and items. <br/>
 * Methods in this class will do nothing outside of a development environment.
 */
@Beta
public class TemplateUtil {
    private TemplateUtil() { }

    private static final ResourceLocation STUB_ITEM_MODEL = new ResourceLocation("ladylib", "models/item/sample_item.json");
    private static final ResourceLocation STUB_BLOCKSTATE = new ResourceLocation("ladylib", "blockstates/sample_blockstate.json");
    private static final String NAME_TOKEN = "@NAME@";
    private static final String DOMAIN_TOKEN = "@DOMAIN@";
    private static String srcRoot;

    /**
     * Call that anytime between item registration and model registration
     *
     * @param srcRoot the location of the <tt>resources</tt> directory in which the files will be generated
     */
    public static void generateStubModels(String modid, @Nullable String srcRoot) {
        if (!LadyLib.isDevEnv()) {
            return;
        }
        if (srcRoot == null) {
            srcRoot = "../src/main/resources";
        }

        TemplateUtil.srcRoot = srcRoot;
        List<String> createdModelFiles = LadyLib.INSTANCE.getItemRegistrar().getAllItems().stream()
                .filter(item -> modid.equals(item.getRegistryName().getNamespace()))
                .filter(itemIn -> !(itemIn instanceof ItemBlock) && !(itemIn instanceof ItemRenderRegistrationHandler))
                .map(Item::getRegistryName)
                .map(TemplateUtil::generateItemModel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!createdModelFiles.isEmpty()) {
            throw new ModelStubsCreatedPleaseRestartTheGameException(createdModelFiles); // Because forge prevents System.exit()
        }
    }

    /**
     * Call that anytime between item registration and model registration. <br>
     * If srcRoot is <code>null</code>, the path <tt>../src/main/resources</tt>
     * will be used by default.
     *
     * @param srcRoot the location of the <tt>resources</tt> directory in which the files will be generated
     */
    public static void generateStubBlockstates(BlockRegistrar blockRegistrar, @Nullable String srcRoot) {
        if (!LadyLib.isDevEnv()) {
            return;
        }
        if (srcRoot == null) {
            srcRoot = "../src/main/resources";
        }

        TemplateUtil.srcRoot = srcRoot;
        List<String> createdModelFiles = blockRegistrar.getAllBlocks().stream()
                .map(Block::getRegistryName)
                .filter(Objects::nonNull)
                .map(TemplateUtil::generateBlockState)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!createdModelFiles.isEmpty()) {
            throw new ModelStubsCreatedPleaseRestartTheGameException(createdModelFiles); // Because stupid forge prevents System.exit()
        }
    }

    @Nullable
    private static String generateItemModel(ResourceLocation loc) {
        // it would be bad if stubs were generated in a random minecraft folder in prod
        if (!LadyLib.isDevEnv()) {
            return null;
        }

        String domain = loc.getNamespace();
        String fileName = loc.getPath() + ".json";
        String textureName = loc.getNamespace() + ":items/" + loc.getPath();
        Path modelPath = Paths.get(srcRoot, "assets", domain, "models", "item", fileName);
        return getStubModel(STUB_ITEM_MODEL, fileName, textureName, modelPath);
    }

    @Nullable
    private static String generateBlockState(ResourceLocation loc) {
        // it would be bad if stubs were generated in a random minecraft folder in prod
        if (!LadyLib.isDevEnv()) {
            return null;
        }

        String domain = loc.getNamespace();
        String fileName = loc.getPath() + ".json";
        String textureName = loc.getNamespace() + ":blocks/" + loc.getPath();
        Path modelPath = Paths.get(srcRoot, "assets", domain, "blockstates", fileName);
        return getStubModel(STUB_BLOCKSTATE, fileName, textureName, modelPath);
    }

    @Nullable
    private static String getStubModel(ResourceLocation stubModel, String fileName, String textureName, Path modelPath) {
        InputStream in;
        try {
            in = Minecraft.getMinecraft().getResourceManager().getResource(stubModel).getInputStream();
        } catch (IOException e) {
            in = TemplateUtil.class.getResourceAsStream("/assets/ladylib/" + stubModel.getPath());
        }
        if (in == null) {
            LadyLib.LOGGER.error("The model stub file {} could not be found.", stubModel);
            return null;
        }
        try (Scanner scan = new Scanner(in)) {
            if (modelPath.getParent().toFile().mkdirs()) {
                LadyLib.LOGGER.info("Created directories for " + modelPath.getParent());
            }
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
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LadyLib.LOGGER.error("Error while generating stub item model", e);
            }
        }
        return null;
    }

    public static class ModelStubsCreatedPleaseRestartTheGameException extends CustomModLoadingErrorDisplayException {
        private final List<String> createdModelFiles;

        ModelStubsCreatedPleaseRestartTheGameException(List<String> createdModelFiles) {
            this.createdModelFiles = createdModelFiles;
        }

        @Override
        public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
            // Nothing to do here
        }

        @Override
        public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            fontRenderer.drawString("The following model stub files have been generated:", 30, 10, 0xFFFFFFFF);
            int i = 0;
            for (; i < createdModelFiles.size(); i++) {
                String s = createdModelFiles.get(i);
                fontRenderer.drawString(s, 5, 20 * (i+2), 0xFFFFFFFF);
            }
            fontRenderer.drawString("The game should now be restarted", 30, 30*(i+1), 0xFFFFFFFF);
        }
    }
}
