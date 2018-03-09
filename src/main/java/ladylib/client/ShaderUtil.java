package ladylib.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladylib.LadyLib;
import ladylib.misc.MatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * A class offering several utility methods to create, use and configure shaders
 */
@SideOnly(Side.CLIENT)
public class ShaderUtil {


    private static int prevProgram = 0, currentProgram = 0;
    private static final String SHADER_LOCATION_PREFIX = "shaders/";

    private static final Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> registeredShaders = new HashMap<>();
    private static final Object2IntMap<ResourceLocation> linkedShaders = new Object2IntOpenHashMap<>();

    private static boolean shouldNotUseShaders() {
        return !OpenGlHelper.shadersSupported;
    }

    /**
     * Convenience method to register a shader with the fragment and vertex shaders having the same name <br/>
     * The corresponding program will be created and linked during the next ResourceManager reloading
     *
     * @param identifier the unique identifier for this shader. The resource domain is also used to get the file
     * @param shaderName the common name or relative location of both shaders, minus the file extension
     */
    public static void registerShader(ResourceLocation identifier, String shaderName) {
        registeredShaders.put(identifier, Pair.of(
                new ResourceLocation(identifier.getResourceDomain(), SHADER_LOCATION_PREFIX + shaderName + ".vsh"),
                new ResourceLocation(identifier.getResourceDomain(), SHADER_LOCATION_PREFIX + shaderName + ".fsh")
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
    public static void registerShader(ResourceLocation identifier, ResourceLocation vertex, ResourceLocation fragment) {
        registeredShaders.put(identifier, Pair.of(vertex, fragment));
    }

    /**
     * Loads and links all registered shaders
     *
     * @param resourceManager Minecraft's resource manager
     */
    public static void loadShaders(IResourceManager resourceManager) {
        if (shouldNotUseShaders())
            return;
        registeredShaders.forEach((rl, sh) -> linkedShaders.put(rl, loadShader(resourceManager, sh.getLeft(), sh.getRight())));
    }

    /**
     * Initializes a program with one or two shaders
     *
     * @param vertexLocation   the name or relative location of the vertex shader
     * @param fragmentLocation the name or relative location of the fragment shader
     * @return the reference to the initialized program
     */
    private static int loadShader(IResourceManager resourceManager, ResourceLocation vertexLocation, ResourceLocation fragmentLocation) {

        // program creation
        int program = OpenGlHelper.glCreateProgram();

        // vertex shader creation
        if (vertexLocation != null) {
            int vertexShader = OpenGlHelper.glCreateShader(OpenGlHelper.GL_VERTEX_SHADER);
            ARBShaderObjects.glShaderSourceARB(vertexShader, fromFile(resourceManager, vertexLocation));
            OpenGlHelper.glCompileShader(vertexShader);
            OpenGlHelper.glAttachShader(program, vertexShader);
        }

        // fragment shader creation
        if (fragmentLocation != null) {
            int fragmentShader = OpenGlHelper.glCreateShader(OpenGlHelper.GL_FRAGMENT_SHADER);
            ARBShaderObjects.glShaderSourceARB(fragmentShader, fromFile(resourceManager, fragmentLocation));
            OpenGlHelper.glCompileShader(fragmentShader);
            OpenGlHelper.glAttachShader(program, fragmentShader);
        }

        OpenGlHelper.glLinkProgram(program);

        return program;
    }

    /**
     * Sets the currently used program to a previously registered shader
     *
     * @param id the resource location used to register the shader
     */
    public static void useShader(ResourceLocation id) {
        useShader(linkedShaders.getInt(id));
    }

    /**
     * Sets the currently used program
     *
     * @param program the reference to the desired shader (0 to remove any current shader)
     */
    public static void useShader(int program) {
        if (shouldNotUseShaders())
            return;

        prevProgram = GlStateManager.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        OpenGlHelper.glUseProgram(program);

        currentProgram = program;
    }

    /**
     * Sets the value of an int uniform from the current shader program
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param value       an int value for this uniform
     */
    public static void setUniform(String uniformName, int value) {
        if (shouldNotUseShaders() || currentProgram == 0)
            return;

        int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
        if (uniform != -1)
            GL20.glUniform1i(uniform, value);
    }

    /**
     * Sets the value of an uniform from the current shader program
     * If exactly 1 value is supplied, will set the value of a float uniform field
     * If between 2 and 4 values are supplied, will set the value of a vec uniform of corresponding length
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param values      between 1 and 4 float values
     */
    public static void setUniform(String uniformName, float... values) {
        if (shouldNotUseShaders())
            return;

        int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
        if (uniform != -1) {
            switch (values.length) {
                case 1:
                    GL20.glUniform1f(uniform, values[0]);
                case 2:
                    GL20.glUniform2f(uniform, values[0], values[1]);
                    break;
                case 3:
                    GL20.glUniform3f(uniform, values[0], values[1], values[2]);
                    break;
                case 4:
                    GL20.glUniform4f(uniform, values[0], values[1], values[2], values[3]);
                    break;
            }
        }
    }

    /**
     * Sets the value of a mat4 uniform in the current shader
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param mat4        a raw array of float values
     */
    public static void setUniform(String uniformName, FloatBuffer mat4) {
        if (shouldNotUseShaders())
            return;

        int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
        if (uniform != -1) {
            GL20.glUniformMatrix4(uniform, true, mat4);
        }
    }

    /**
     * Binds any number of additional textures to be used by the current shader
     */
    public static void bindAdditionalTextures(ResourceLocation... textures) {
        for (int i = 0; i < textures.length; i++) {
            ResourceLocation texture = textures[i];
            // don't mess with the lightmap (1) nor the default texture (0)
            GlStateManager.setActiveTexture(i + OpenGlHelper.defaultTexUnit + 2);
            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            // start texture uniforms at 1, as 0 would be the default texture which doesn't require any special operation
            setUniform("texture" + (i + 1), i + 2);
        }
    }

    public static FloatBuffer getProjectionMatrix() {
        ByteBuffer projection = ByteBuffer.allocateDirect(16 * Float.BYTES);
        projection.order(ByteOrder.nativeOrder());
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, (FloatBuffer) projection.asFloatBuffer().position(0));
        return projection.asFloatBuffer();
    }

    public static FloatBuffer getProjectionMatrixInverse() {
        FloatBuffer projection = ShaderUtil.getProjectionMatrix();
        FloatBuffer projectionInverse = ByteBuffer.allocateDirect(16 * Float.BYTES).asFloatBuffer();
        MatUtil.invertMat4FBFA((FloatBuffer) projectionInverse.position(0), (FloatBuffer) projection.position(0));
        projection.position(0);
        projectionInverse.position(0);
        return projectionInverse;
    }

    /**
     * This one is actually broken for some reason
     */
    public static FloatBuffer getModelViewMatrix() {
        ByteBuffer modelView = ByteBuffer.allocateDirect(16 * Float.BYTES);
        modelView.order(ByteOrder.nativeOrder());
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, (FloatBuffer) modelView.asFloatBuffer().position(0));
        return modelView.asFloatBuffer();
    }

    public static FloatBuffer getModelViewMatrixInverse() {
        FloatBuffer modelView = ShaderUtil.getModelViewMatrix();
        FloatBuffer modelViewInverse = ByteBuffer.allocateDirect(16 * Float.BYTES).asFloatBuffer();
        MatUtil.invertMat4FBFA((FloatBuffer) modelViewInverse.position(0), (FloatBuffer) modelView.position(0));
        modelView.position(0);
        modelViewInverse.position(0);
        return modelViewInverse;
    }

    /**
     * Reverts to the previous shader used
     */
    public static void revert() {
        useShader(prevProgram);
    }

    /**
     * Reads a text file into a single String
     *
     * @param fileLocation the path to the file to read
     * @return a string with the content of the file
     */
    private static String fromFile(IResourceManager resourceManager, ResourceLocation fileLocation) {
        StringBuilder source = new StringBuilder();

        try (InputStream in = resourceManager.getResource(fileLocation).getInputStream();/* ShaderUtil.class.getResourceAsStream(JAR_LOCATION_PREFIX + filename*/
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null)
                source.append(line).append('\n');
        } catch (IOException exc) {
            LadyLib.LOGGER.error(exc);
        } catch (NullPointerException e) {
            LadyLib.LOGGER.error(e + " : " + fileLocation + " does not exist");
        }

        return source.toString();
    }

}
