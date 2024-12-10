package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.entity.SlimerEntity;
import absolutelyaya.captcha.registry.EntityRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.OptionalLong;

public class SlimerCaptchaScreen extends AbstractCaptchaScreen
{
	public static final String TYPE = "slimer", TRANSLATION_KEY = "screen.captcha.slimer.";
	static final Vector2i[] directions = new Vector2i[] {new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0), new Vector2i(0, 1)};
	FakeWorld fakeWorld;
	MinecartEntity minecart;
	FurnaceMinecartEntity furnaceMinecart;
	FakeBoat boat;
	SlimerEntity slimer;
	float playerRot = 0, playerVisualRot;
	Vector2i playerPos = new Vector2i();
	Vector3f playerVisualPos = new Vector3f();
	
	protected SlimerCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		fakeWorld = new FakeWorld(MinecraftClient.getInstance());
		minecart = new MinecartEntity(EntityType.MINECART, fakeWorld);
		furnaceMinecart = new FurnaceMinecartEntity(EntityType.FURNACE_MINECART, fakeWorld);
		boat = new FakeBoat(EntityType.BOAT, fakeWorld);
		slimer = new SlimerEntity(EntityRegistry.SLIMER, fakeWorld);
	}
	
	@Override
	public String getType()
	{
		return "slimer";
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix);
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		slimer.age++;
		minecart.age++;
		boat.tick();;
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		matrices.push();
		matrices.translate(0, 0, 100);
		matrices.scale(16f, -16f, 16f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70));
		float tickDelta = client.getRenderTickCounter().getTickDelta(false);
		drawWorld(context, matrices, tickDelta);
		drawPlayer(context, matrices, tickDelta);
		matrices.pop();
		context.disableScissor();
	}
	
	void drawWorld(DrawContext context, MatrixStack matrices, float delta)
	{
	
	}
	
	void drawPlayer(DrawContext context, MatrixStack matrices, float delta)
	{
		drawEntity(boat, new Vec3d(5, Math.sin((boat.age + delta + 15) / 2.5f) * 0.05f - 0.5f, 0) , 0, matrices, context, delta);
		
		matrices.push();
		playerVisualPos = playerVisualPos.lerp(new Vector3f(playerPos.x, 0, playerPos.y), delta / 5f);
		matrices.translate(playerVisualPos.x, playerVisualPos.y, playerVisualPos.z);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(playerVisualRot = MathHelper.lerpAngleDegrees(delta / 5f, playerVisualRot, playerRot)));
		drawEntity(slimer, new Vec3d(0, 0, 0), 0, matrices, context, delta);
		matrices.pop();
		
		drawEntity(minecart, new Vec3d(-5, 0, -1.4), 90, matrices, context, delta);
		drawEntity(minecart, new Vec3d(-5, 0, 0), 90, matrices, context, delta);
		drawEntity(minecart, new Vec3d(-5, 0, 1.4), 90, matrices, context, delta);
		drawEntity(furnaceMinecart, new Vec3d(-5, 0, 2.8), 90, matrices, context, delta);
	}
	
	void drawEntity(Entity e, Vec3d pos, float yaw, MatrixStack matrices, DrawContext context, float delta)
	{
		RenderSystem.enableBlend();
		EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		matrices.push();
		DiffuseLighting.method_34742();
		dispatcher.setRenderShadows(false);
		dispatcher.render(e, pos.x, pos.y, pos.z, yaw, delta, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		context.draw();
		dispatcher.setRenderShadows(true);
		DiffuseLighting.enableGuiDepthLighting();
		matrices.pop();
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		if(chr == 'w')
			tryMove(0);
		else if(chr == 'a')
			tryMove(1);
		else if(chr == 's')
			tryMove(2);
		else if(chr == 'd')
			tryMove(3);
		return super.charTyped(chr, modifiers);
	}
	
	void tryMove(int dir)
	{
		playerPos = playerPos.add(directions[dir]);
		playerRot = 90f * dir + 90;
		slimer.hop();
	}
	
	@Override
	protected void onClickedProceed()
	{
		close();
	}
	
	@Override
	public void close()
	{
		super.close();
		slimer.discard();
		boat.discard();
		minecart.discard();
	}
	
	public static class FakeWorld extends ClientWorld
	{
		public FakeWorld(MinecraftClient client)
		{
			super(client.getNetworkHandler(), new Properties(Difficulty.EASY, false, true), null,
					RegistryEntry.of(new DimensionType(OptionalLong.of(0), true, false ,false ,false,
							1, false, true, 0, 32, 0, BlockTags.INFINIBURN_OVERWORLD,
							CAPTCHA.identifier("fakeworld"), 1, new DimensionType.MonsterSettings(false, false, ConstantIntProvider.create(1), 0))),
					1, 1, null, client.worldRenderer, false, 0);
		}
	}
	
	public static class FakeBoat extends BoatEntity
	{
		float paddle;
		
		public FakeBoat(EntityType<? extends BoatEntity> entityType, World world)
		{
			super(entityType, world);
		}
		
		@Override
		public boolean isPaddleMoving(int paddle)
		{
			return true;
		}
		
		@Override
		public float interpolatePaddlePhase(int paddle, float tickDelta)
		{
			return MathHelper.lerp(tickDelta, this.paddle - (float) (Math.PI / 8), this.paddle);
		}
		
		@Override
		public void tick()
		{
			paddle += (float)(Math.PI / 8);
			age++;
		}
	}
}
