package ladylib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class has been adapted from embers' source code under GNU Lesser General Public License 2.1
 * https://github.com/RootsTeam/Embers/blob/master/src/main/java/teamroots/embers/particle/ParticleRenderer.java
 *
 * @author Elucent
 */
@SideOnly(Side.CLIENT)
public class ParticleManager {

    private final List<Particle> particles = new LinkedList<>();
    private final Set<ResourceLocation> particleTextures = new HashSet<>();
    private final Supplier<Integer> maxParticles;

    public ParticleManager(Supplier<Integer> maxParticles) {
        this.maxParticles = maxParticles;
    }

    public void registerParticleTexture(ResourceLocation location) {
        particleTextures.add(location);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        particleTextures.forEach(event.getMap()::registerSprite);
    }

    @SubscribeEvent
    public void onGameTick(TickEvent.ClientTickEvent event) {
        updateParticles();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        renderParticles(event.getPartialTicks());
    }

    private void updateParticles() {
        // particles cost a lot less to update than to render
        particles.stream().limit(3 * maxParticles.get()).forEach(Particle::onUpdate);
        particles.removeIf(p -> !p.isAlive());
    }

    private void renderParticles(float partialTicks) {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            Particle.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            Particle.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            Particle.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            Particle.cameraViewDir = player.getLook(partialTicks);

            GlStateManager.pushMatrix();

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.disableCull();

            GlStateManager.depthMask(false);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();

            // render normal particles
            {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                particles.stream()
                        .limit(maxParticles.get())
                        .forEach(p -> p.renderParticle(buffer, player, partialTicks, f, f4, f1, f2, f3));
                tess.draw();
            }
            // TODO add fancy particles back

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);

            GlStateManager.popMatrix();
        }
    }

    public void addParticle(Particle p) {
        // If we can't even tick them, don't add them
        if (particles.size() < maxParticles.get() * 3)
            particles.add(p);
    }

}