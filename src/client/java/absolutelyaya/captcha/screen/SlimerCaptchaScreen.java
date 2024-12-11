package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.entity.SlimerEntity;
import absolutelyaya.captcha.registry.EntityRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

public class SlimerCaptchaScreen extends AbstractCaptchaScreen
{
	public static final String TYPE = "slimer", TRANSLATION_KEY = "screen.captcha.slimer.";
	static final Identifier[] TEXTURES = new Identifier[] {
			Identifier.of("textures/block/moss_block.png"), Identifier.of("textures/block/obsidian.png"),
			Identifier.of("textures/block/cobblestone.png"), Identifier.of("textures/block/rail.png"),
			Identifier.of("textures/block/water_still.png"), Identifier.of("textures/block/water_flow.png"),
			Identifier.of("textures/block/dirt.png")};
	static final Vector2i[] directions = new Vector2i[] {new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0), new Vector2i(0, 1)};
	FakeWorld fakeWorld;
	MinecartEntity minecart;
	FakeFurnaceMinecart furnaceMinecart;
	FakeBoat boat;
	SlimerEntity slimer;
	float playerRot = 0, playerVisualRot;
	Vector2i playerPos = new Vector2i(0, 4);
	Vector3f playerVisualPos = new Vector3f(), playerScale = new Vector3f(0.85f), worldVisualPos = new Vector3f();
	byte[][] map;
	List<Track> tracks = new ArrayList<>(), rivers = new ArrayList<>();
	boolean dead;
	
	protected SlimerCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		fakeWorld = new FakeWorld(MinecraftClient.getInstance());
		minecart = new MinecartEntity(EntityType.MINECART, fakeWorld);
		furnaceMinecart = new FakeFurnaceMinecart(EntityType.FURNACE_MINECART, fakeWorld);
		boat = new FakeBoat(EntityType.BOAT, fakeWorld);
		slimer = new SlimerEntity(EntityRegistry.SLIMER, fakeWorld);
		map = new byte[9][10 + (int)(difficulty / 50) * 3];
		generateWorld();
		playerVisualPos = new Vector3f(playerPos.x, 0, playerPos.y);
		worldVisualPos = new Vector3f(playerVisualPos).mul(-1f);
	}
	
	void generateWorld()
	{
		float difficultyMod = difficulty / 50f;
		for (int y = 0; y < map[0].length; y++)
		{
			boolean safe = (y == 0 || y == map[0].length - 1);
			float r = safe ? 0f : random.nextFloat() + difficultyMod;
			if(r < 0.65f) //ground
				for (int x = 0; x < map.length; x++)
					map[x][y] = safe ? (byte) 0 : (byte)(random.nextFloat() < 0.2f ? 1 : 0);
			else if(r < 0.85 + difficultyMod) //tracks
			{
				for (int x = 0; x < map.length; x++)
					map[x][y] = 2;
				float speed = random.nextFloat() * 0.2f + 0.1f + 0.2f * (1f + random.nextFloat() * Math.min(difficulty / 100f, 1f));
				tracks.add(new Track(y, random.nextBoolean(), speed, difficulty, () -> Train.supply(difficulty)));
			}
			else //water
			{
				for (int x = 0; x < map.length; x++)
					map[x][y] = 3;
				float speed = random.nextFloat() * 0.1f + 0.025f + 0.05f * (1f + random.nextFloat() * Math.min(difficulty / 100f, 1f));
				rivers.add(new Track(y, random.nextBoolean(), speed, difficulty, () -> Log.supply(difficulty)));
			}
		}
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
		boat.tick();
		
		tracks.forEach(track -> {
			track.tick();
			track.objects[0].forEach(train -> {
				if(train.isOver(playerPos))
					dead = true;
			});
		});
		rivers.forEach(Track::tick);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		matrices.push();
		matrices.translate(0, 0, 500);
		matrices.scale(16f, -16f, 16f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70));
		float tickDelta = client.getRenderTickCounter().getTickDelta(false);
		
		worldVisualPos = worldVisualPos.lerp(new Vector3f(playerPos.x, 0, playerPos.y).mul(-1f), tickDelta / 20f);
		matrices.translate(worldVisualPos.x, worldVisualPos.y, worldVisualPos.z);
		drawWorld(matrices, tickDelta);
		drawEntities(context, matrices, tickDelta);
		
		matrices.pop();
		context.disableScissor();
	}
	
	void drawWorld(MatrixStack matrices, float delta)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		float c = 0f;
		bufferBuilder.vertex(matrix, -200, -2f, -200).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, -200, -2f, 200).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 200, -2f, 200).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 200, -2f, -200).color(c, c, c, 1f);
		
		bufferBuilder.vertex(matrix, -200, 200f, -5).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, -200, -2f, -5).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 200, -2f, -5).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 200, 200f, -5).color(c, c, c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		
		for (int x = map[0].length - 1; x >= 0; x--)
		{
			for (int y = map.length - 1; y >= 0; y--)
			{
				switch(map[y][x])
				{
					case 0 -> drawGround(TEXTURES[0], x, y, matrices);
					case 1 -> drawGround(TEXTURES[1], x, y, matrices);
					case 2 -> drawTrack(x, y, matrices);
					case 3 -> drawWater(x, y, matrices, y == map.length - 1);
				}
			}
		}
	}
	
	void drawTrack(int x, int y, MatrixStack matrices)
	{
		drawGround(TEXTURES[2], x, y, matrices);
		matrices.push();
		matrices.translate(0f, 0.1f, 0f);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(00f));
		drawGroundPlane(TEXTURES[3], x, y, matrices);
		matrices.pop();
	}
	
	void drawGround(Identifier texture, int x, int y, MatrixStack matrices)
	{
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		drawGroundPlane(texture, x, y, matrices);
		float c = 0.6f;
		bufferBuilder.vertex(matrix, x - 0.5f, -1f, -0.5f + y).texture(0f, 0f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f, -1f, 0.5f + y).texture(0f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f,  0f, 0.5f + y).texture(1f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f,  0f, -0.5f + y).texture(1f, 0f).color(c, c, c, 1f);
		c = 0.5f;
		bufferBuilder.vertex(matrix, -0.5f + x, -1f, y + 0.5f).texture(0f, 0f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, -1f, y + 0.5f).texture(0f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x,  0f, y + 0.5f).texture(1f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, -0.5f + x,  0f, y + 0.5f).texture(1f, 0f).color(c, c, c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
	
	void drawGroundPlane(Identifier texture, int x, int y, MatrixStack matrices)
	{
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		float c = 1f;
		bufferBuilder.vertex(matrix, -0.5f + x, 0f, -0.5f + y).texture(0f, 0f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, -0.5f + x, 0f, 0.5f + y).texture(0f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, 0f, 0.5f + y).texture(1f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, 0f, -0.5f + y).texture(1f, 0f).color(c, c, c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
	
	void drawWater(int x, int y, MatrixStack matrices, boolean full)
	{
		matrices.push();
		matrices.translate(0, -1, 0);
		drawGroundPlane(TEXTURES[6], x, y, matrices);
		matrices.pop();
		matrices.push();
		float offset = (float)Math.sin(boat.age / 20f + y + x) * 0.05f;
		matrices.translate(0f, -0.1f + offset, 0f);
		
		RenderSystem.setShaderTexture(0, TEXTURES[4]);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		float div = 1f / 32f;
		float v = div * ((boat.age / 2) % 32);
		float c = 1f;
		float r = 0.4f, g = 0.6f, b = 1f;
		bufferBuilder.vertex(matrix, -0.5f + x, 0f, -0.5f + y).texture(1, v).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, -0.5f + x, 0f, 0.5f + y).texture(0, v).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, 0f, 0.5f + y).texture(0, v + div).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, 0f, -0.5f + y).texture(1, v + div).color(r * c, g * c, b * c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.setShaderTexture(0, TEXTURES[5]);
		bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		c = 0.9f;
		bufferBuilder.vertex(matrix, x - 0.5f, -0.1f + offset, -0.5f + y).texture(1, v + div).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f, -0.1f + offset, 0.5f + y).texture(0, v + div).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f,  0f, 0.5f + y).texture(0, v + (0.1f + offset) * div ).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, x - 0.5f,  0f, -0.5f + y).texture(1, v + (0.1f + offset) * div ).color(r * c, g * c, b * c, 1f);
		c = 0.75f;
		bufferBuilder.vertex(matrix, -0.5f + x, -(full ? 0.9f : 0.1f) - offset, y + 0.5f).texture(0, v + div).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x, -(full ? 0.9f : 0.1f) - offset, y + 0.5f).texture(1, v + div).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, 0.5f + x,  0f, y + 0.5f).texture(1, v + (0.1f + offset) * div ).color(r * c, g * c, b * c, 1f);
		bufferBuilder.vertex(matrix, -0.5f + x,  0f, y + 0.5f).texture(0, v + (0.1f + offset) * div ).color(r * c, g * c, b * c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		matrices.pop();
	}
	
	void drawEntities(DrawContext context, MatrixStack matrices, float delta)
	{
		rivers.forEach(i -> i.objects[0].forEach(t -> drawLog(t, context, matrices, delta)));
		
		matrices.push();
		if(dead)
			playerScale = playerScale.lerp(new Vector3f(1.2f, 0.05f, 1.2f), delta);
		playerVisualPos = playerVisualPos.lerp(new Vector3f(playerPos.x, 0f, playerPos.y), delta / 5f);
		matrices.translate(playerVisualPos.x, playerVisualPos.y, playerVisualPos.z);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(playerVisualRot = MathHelper.lerpAngleDegrees(delta / 5f, playerVisualRot, playerRot)));
		matrices.scale(playerScale.x, playerScale.y, playerScale.z);
		drawEntity(slimer, new Vec3d(0, 0, 0), 0, matrices, context, delta);
		matrices.pop();
		
		tracks.forEach(i -> i.objects[0].forEach(t -> drawTrain(t, context, matrices, delta)));
	}
	
	void drawTrain(GameObject train, DrawContext context, MatrixStack matrices, float delta)
	{
		matrices.push();
		Vector3f pos = new Vector3f(train.pos).sub(0, 0, train.dir ? train.speed : -train.speed).lerp(train.pos, delta);
		matrices.translate(pos.x, pos.y, pos.z);
		for (int i = 0; i < train.length; i++)
		{
			matrices.push();
			matrices.translate(0, 0, train.dir ? -i : i);
			matrices.scale(0.8f, 0.8f, 0.8f);
			drawEntity(i == 0 ? furnaceMinecart : minecart, new Vec3d(0, 0, 0),
					90 * (train.dir ? 1 : -1), matrices, context, delta);
			matrices.pop();
		}
		matrices.pop();
	}
	
	void drawLog(GameObject log, DrawContext context, MatrixStack matrices, float delta)
	{
		matrices.push();
		Vector3f pos = new Vector3f(log.pos).sub(0, 0, log.dir ? log.speed : -log.speed).lerp(log.pos, delta);
		matrices.translate(pos.x, pos.y, pos.z);
		if(log instanceof Log realLog)
			matrices.translate(0f, realLog.getHeight(delta), 0f);
		for (int i = 0; i < log.length; i++)
			drawGround(TEXTURES[1], 0, log.dir ? -i : i, matrices);
		matrices.pop();
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
		if(dead)
			return;
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
	
	record Track(int pos, boolean dir, float speed, float difficulty, List<GameObject>[] objects, Supplier<GameObject> objectSupplier)
	{
		public Track(int pos, boolean dir, float speed, float difficulty, Supplier<GameObject> objectSupplier)
		{
			this(pos, dir, speed, difficulty, new ArrayList[2], objectSupplier);
			for (int i = 0; i < objects.length; i++)
				objects[i] = new ArrayList<>();
		}
		
		void tick()
		{
			boolean free = true;
			for (GameObject object : objects[0])
			{
				if(object.hasPassed())
					objects[1].add(object);
				else
				{
					object.tick();
					if(object.isOver(new Vector2i(pos, dir ? -6 : 16)))
						free = false;
				}
			}
			objects[0].removeAll(objects[1]);
			objects[1].clear();
			if(free)
			{
				GameObject g = objectSupplier.get();
				if(g == null)
					return;
				g.pos = new Vector3f(pos, 0, dir ? -6 : 16);
				g.speed = speed;
				g.dir = dir;
				g.length = Math.min(3 + Math.round((1f + (1f + random.nextFloat() * difficulty / 200f)) * difficulty / 50f), 16);
				objects[0].add(g);
			}
		}
	}
	
	public static class GameObject
	{
		protected Vector3f pos;
		int length;
		boolean dir;
		float speed;
		
		public void tick()
		{
			pos = pos.add(0, 0, (dir ? speed : -speed));
		}
		
		public boolean hasPassed()
		{
			return dir ? Math.abs(pos.z) - length > 16 : pos.z + length < -4;
		}
		
		public boolean isOver(Vector2i pos)
		{
			if(this.pos.x != pos.x)
				return false;
			return (pos.y <= this.pos.z + (dir ? 0.8f : length) && pos.y >= this.pos.z + (dir ? -length : -0.8f));
		}
	}
	
	public static class Train extends GameObject
	{
		public static GameObject supply(float difficulty)
		{
			if(random.nextFloat() < 0.001f + Math.min(difficulty / 2000f, 0.1f))
				return new Train();
			else
				return null;
		}
	}
	
	public static class Log extends GameObject
	{
		float heightOverride;
		int age;
		
		public static GameObject supply(float difficulty)
		{
			if(random.nextFloat() < (0.001f + Math.min(difficulty / 2000f, 0.1f)) * 2f)
				return new Log();
			else
				return null;
		}
		
		@Override
		public void tick()
		{
			super.tick();
			age++;
		}
		
		public float getHeight(float delta)
		{
			heightOverride += delta / 30f;
			return Math.min((float)Math.sin((age + delta + 15) / 2.5f) * 0.05f - 0.5f, heightOverride);
		}
		
		public void impact()
		{
			heightOverride = -0.25f;
		}
	}
	
	public static class FakeFurnaceMinecart extends FurnaceMinecartEntity
	{
		
		public FakeFurnaceMinecart(EntityType<? extends FurnaceMinecartEntity> entityType, World world)
		{
			super(entityType, world);
		}
		
		@Override
		protected boolean isLit()
		{
			return true;
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
