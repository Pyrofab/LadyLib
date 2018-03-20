package ladylib.client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Register your shaders when you receive this event
 */
public class ShaderRegistryEvent extends Event implements IContextSetter {
    private final Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> registeredShaders;

    public ShaderRegistryEvent(Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> registeredShaders) {
        this.registeredShaders = registeredShaders;
    }

    /**
     * Convenience method to register a shader with the fragment and vertex shaders having the same name <br/>
     * The corresponding program will be created and linked during the next ResourceManager reloading
     *
     * @param shaderName the common name or relative location of both shaders, minus the file extension
     */
    public void registerShader(String shaderName) {
        ModContainer mc = Loader.instance().activeModContainer();
        String prefix = mc == null || (mc instanceof InjectedModContainer && ((InjectedModContainer)mc).wrappedContainer instanceof FMLContainer) ? "minecraft" : mc.getModId().toLowerCase();
        ResourceLocation identifier = new ResourceLocation(prefix, shaderName);
        registeredShaders.put(identifier, Pair.of(
                new ResourceLocation(prefix, ShaderUtil.SHADER_LOCATION_PREFIX + shaderName + ".vsh"),
                new ResourceLocation(prefix, ShaderUtil.SHADER_LOCATION_PREFIX + shaderName + ".fsh")
        ));
    }

    /**
     * Registers a shader with two shaders having the same name
     * The corresponding program will be created and linked during the next ResourceManager reloading
     *
     * @param identifier a unique resource location that will be used to load this shader
     * @param vertex     the file name of the vertex shader, extension included
     * @param fragment   the file name of the fragment shader, extension included
     */
    public void registerShader(ResourceLocation identifier, ResourceLocation vertex, ResourceLocation fragment) {
        registeredShaders.put(identifier, Pair.of(vertex, fragment));
    }

}
