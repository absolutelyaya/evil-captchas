package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.CAPTCHAClient;
import absolutelyaya.captcha.entity.SlimerEntity;
import absolutelyaya.captcha.registry.EntityRegistry;
import absolutelyaya.captcha.rendering.FakeWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SlimerCaptchaScreen extends AbstractCaptchaScreen
{
	public static final String TYPE = "slimer", TRANSLATION_KEY = "screen.captcha.slimer.";
	static final Identifier[] TEXTURES = new Identifier[] {
			Identifier.of("textures/block/moss_block.png"), Identifier.of("textures/block/flowering_azalea_top.png"),
			Identifier.of("textures/block/cobblestone.png"), Identifier.of("textures/block/rail.png"),
			Identifier.of("textures/block/water_still.png"), Identifier.of("textures/block/water_flow.png"),
			Identifier.of("textures/block/dirt.png"),
			Identifier.of("textures/block/oak_log.png"), Identifier.of("textures/block/oak_log_top.png")};
	static final Vector2i[] directions = new Vector2i[] {new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0), new Vector2i(0, 1)};
	static final FakeWorld fakeWorld = CAPTCHAClient.FAKE_WORLD;
	MinecartEntity minecart;
	FakeFurnaceMinecart furnaceMinecart;
	SlimerEntity slimer;
	float playerRot = 0, playerVisualRot;
	Vector2i playerPos = new Vector2i(0, 4);
	Vector3f playerVisualPos = new Vector3f(), playerScale = new Vector3f(0.85f), worldVisualPos = new Vector3f();
	byte[][] map;
	List<Track<Train>> tracks = new ArrayList<>();
	List<Track<Log>> rivers = new ArrayList<>();
	boolean dead, squished;
	Log vehicle;
	int logOffset, time;
	
	protected SlimerCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		minecart = new MinecartEntity(EntityType.MINECART, fakeWorld);
		furnaceMinecart = new FakeFurnaceMinecart(EntityType.FURNACE_MINECART, fakeWorld);
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
				tracks.add(new Track<>(Train.class, y, random.nextBoolean(), speed, difficulty));
			}
			else //water
			{
				for (int x = 0; x < map.length; x++)
					map[x][y] = 3;
				float speed = random.nextFloat() * 0.1f + 0.025f + 0.05f * (1f + random.nextFloat() * Math.min(difficulty / 100f, 1f));
				rivers.add(new Track<>(Log.class, y, random.nextBoolean(), speed, difficulty));
			}
		}
		for (Track<Train> track : tracks)
			track.setSpawnDelay(10 + (int)Math.max(random.nextFloat() * 40 - difficulty / 100f, 0), 10 + (int)Math.max(random.nextFloat() * 100 - difficulty / 100f, 0));
		for (Track<Log> track : rivers)
			track.setSpawnDelay(5 + (int)Math.min(random.nextFloat() * difficulty / 100f, 40), 15 + (int)Math.min(random.nextFloat() * difficulty / 100f, 100));
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
	protected int getInstructionLines()
	{
		return 3;
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
		time++;
		
		tracks.forEach(track -> {
			track.tick();
			if(track.pos == playerPos.x)
			{
				track.objects.forEach(train -> {
					if(train.isOver(playerPos) && !dead)
						dead = squished = true;
				});
			}
		});
		rivers.forEach(Track::tick);
		if(!dead && vehicle != null && vehicle.removed)
		{
			slimer.splash();
			dead = true;
		}
		if(nextDelay == -1 && dead)
			onFail();
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		if(client == null)
		{
			close(); //shouldn't ever happen, I just want the compiler to stop complaining
			return;
		}
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		matrices.push();
		matrices.translate(0, 0, 500);
		matrices.scale(16f, -16f, 16f);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70));
		float delta = client.getRenderTickCounter().getTickDelta(false);
		
		if(vehicle != null)
		{
			Vector3f pos = new Vector3f(vehicle.pos).sub(0, 0, logOffset);
			worldVisualPos = worldVisualPos.lerp(new Vector3f(pos.x, 0, pos.z).mul(-1f), delta / 20f);
		}
		else
			worldVisualPos = worldVisualPos.lerp(new Vector3f(playerPos.x, 0, playerPos.y).mul(-1f), delta / 20f);
		matrices.translate(worldVisualPos.x, worldVisualPos.y, worldVisualPos.z);
		drawWorld(matrices);
		drawEntities(context, matrices, delta);
		
		matrices.pop();
		context.disableScissor();
	}
	
	void drawWorld(MatrixStack matrices)
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
		float offset = (float)Math.sin(time / 20f + y + x) * 0.05f;
		matrices.translate(0f, -0.1f + offset, 0f);
		
		RenderSystem.setShaderTexture(0, TEXTURES[4]);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		float div = 1f / 32f;
		float v = div * ((time / 2) % 32);
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
		rivers.forEach(i -> i.objects.forEach(t -> drawLog(t, matrices, delta)));
		
		matrices.push();
		if(squished)
			playerScale = playerScale.lerp(new Vector3f(1.2f, 0.05f, 1.2f), delta);
		if(vehicle != null)
		{
			Vector3f pos = new Vector3f(vehicle.pos);
			playerVisualPos = playerVisualPos.lerp(new Vector3f(pos.x, pos.y + vehicle.getHeight(delta), pos.z - logOffset), delta / 5f);
		}
		else
			playerVisualPos = playerVisualPos.lerp(new Vector3f(playerPos.x, 0f, playerPos.y), delta / 5f);
		matrices.translate(playerVisualPos.x, playerVisualPos.y, playerVisualPos.z);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(playerVisualRot = MathHelper.lerpAngleDegrees(delta / 5f, playerVisualRot, playerRot)));
		matrices.scale(playerScale.x, playerScale.y, playerScale.z);
		drawEntity(slimer, new Vec3d(0, 0, 0), 0, matrices, context, delta);
		matrices.pop();
		
		tracks.forEach(i -> i.objects.forEach(t -> drawTrain(t, context, matrices, delta)));
	}
	
	void drawTrain(Train train, DrawContext context, MatrixStack matrices, float delta)
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
	
	void drawLog(Log log, MatrixStack matrices, float delta)
	{
		matrices.push();
		Vector3f pos = new Vector3f(log.pos).sub(0, 0, log.dir ? log.speed : -log.speed).lerp(log.pos, delta);
		matrices.translate(pos.x, pos.y, pos.z);
		if(log instanceof Log realLog)
			matrices.translate(0f, realLog.getHeight(delta), 0f);
		RenderSystem.enableBlend();
		float c;
		RenderSystem.setShaderTexture(0, TEXTURES[7]);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		for (int i = 0; i < log.length; i++)
		{
			c = 1f;
			bufferBuilder.vertex(matrix, -0.5f, 0f, -0.5f + (log.dir ? -i : i)).texture(0f, 0f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, -0.5f, 0f, 0.5f + (log.dir ? -i : i)).texture(0f, 1f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, 0.5f, 0f, 0.5f + (log.dir ? -i : i)).texture(1f, 1f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, 0.5f, 0f, -0.5f + (log.dir ? -i : i)).texture(1f, 0f).color(c, c, c, 1f);
			c = 0.6f;
			bufferBuilder.vertex(matrix, -0.5f, -1f, -0.5f + (log.dir ? -i : i)).texture(0f, 0f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, -0.5f, -1f, 0.5f + (log.dir ? -i : i)).texture(0f, 1f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, -0.5f,  0f, 0.5f + (log.dir ? -i : i)).texture(1f, 1f).color(c, c, c, 1f);
			bufferBuilder.vertex(matrix, -0.5f,  0f, -0.5f + (log.dir ? -i : i)).texture(1f, 0f).color(c, c, c, 1f);
		}
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.setShaderTexture(0, TEXTURES[8]);
		bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		c = 0.5f;
		bufferBuilder.vertex(matrix, -0.5f, -1f, (log.dir ? 0.5f : log.length - 0.5f)).texture(0f, 0f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f, -1f, (log.dir ? 0.5f : log.length - 0.5f)).texture(0f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, 0.5f,  0f, (log.dir ? 0.5f : log.length - 0.5f)).texture(1f, 1f).color(c, c, c, 1f);
		bufferBuilder.vertex(matrix, -0.5f,  0f, (log.dir ? 0.5f : log.length - 0.5f)).texture(1f, 0f).color(c, c, c, 1f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
		if(dead || nextDelay >= 0)
			return;
		if(vehicle != null && (dir == 0 || dir == 2))
		{
			playerPos = new Vector2i((int)vehicle.pos.x, Math.round(vehicle.pos.z - logOffset));
			vehicle.boarded = false;
			vehicle = null;
		}
		if(vehicle != null)
			moveOnLog(dir);
		if(vehicle == null)
		{
			Vector2i targetPos = new Vector2i(playerPos).add(directions[dir]);
			if(targetPos.x < 0 || targetPos.x >= map[0].length || targetPos.y < 0 || targetPos.y >= map.length)
				return;
			playerPos = playerPos.add(directions[dir]);
		}
		slimer.hop();
		playerRot = 90f * dir + 90;
		if(vehicle != null)
			return;
		if(playerPos.x == map[0].length - 1 && nextDelay == -1)
		{
			onComplete();
			return;
		}
		rivers.forEach(track -> {
			if(track.pos == playerPos.x)
			{
				for (Log object : track.objects)
				{
					if(object instanceof Log log && log.isOver(playerPos))
					{
						logOffset = Math.abs(Math.round(playerPos.y - log.pos.z));
						if(Math.abs(logOffset) >= log.length)
							continue;
						vehicle = log;
						vehicle.boarded = true;
						if(!log.dir)
							logOffset *= -1;
						return;
					}
				}
				dead = true;
				slimer.splash();
			}
		});
	}
	
	void moveOnLog(int dir)
	{
		int lastLogOffset = logOffset;
		if(dir == 1)
			logOffset++;
		else if(dir == 3)
			logOffset--;
		if(Math.abs(logOffset) >= vehicle.length || (vehicle.dir ? logOffset < 0 : logOffset > 0))
		{
			playerPos = new Vector2i((int)vehicle.pos.x, Math.round(vehicle.pos.z - lastLogOffset));
			vehicle.boarded = false;
			vehicle = null;
		}
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	@Override
	public void close()
	{
		super.close();
		slimer.discard();
		minecart.discard();
	}
	
	static class Track<T extends GameObject>
	{
		private final Class<T> clazz;
		private final int pos;
		private final boolean dir;
		private final float speed, difficulty;
		private final List<T> objects = new ArrayList<>(), removedObjects = new ArrayList<>();
		private int minDelay = 20, maxDelay = 40, spawnTimer;
		
		public Track(Class<T> clazz, int pos, boolean dir, float speed, float difficulty)
		{
			this.clazz = clazz;
			this.pos = pos;
			this.dir = dir;
			this.speed = speed;
			this.difficulty = difficulty;
		}
		
		public void setSpawnDelay(int min, int maxDelta)
		{
			this.minDelay = min;
			this.maxDelay = min + maxDelta;
		}
		
		void tick()
		{
			boolean free = true;
			for (T object : objects)
			{
				if (object.hasPassed())
					removedObjects.add(object);
				else
				{
					object.tick();
					if (object.isOver(new Vector2i(pos, dir ? -6 : 16)))
						free = false;
				}
			}
			for (T object : removedObjects)
			{
				object.removed = true;
				objects.remove(object);
			}
			removedObjects.clear();
			if (free && spawnTimer-- <= 0)
			{
				try
				{
					T object = clazz.getDeclaredConstructor().newInstance();
					object.pos = new Vector3f(pos, 0, dir ? -6 : 16);
					object.speed = speed;
					object.dir = dir;
					object.length = Math.min(3 + Math.round((1f + (1f + random.nextFloat() * difficulty / 200f)) * difficulty / 50f), 16);
					objects.add(object);
				}
				catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
				{
					CAPTCHA.LOGGER.error("Failed to instantiate Child Object of Slimer Captcha Track", e);
				}
				spawnTimer = random.nextBetween(minDelay, maxDelay);
			}
		}
	}
	
	static class GameObject
	{
		protected Vector3f pos;
		int length;
		boolean dir, removed;
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
	
	static class Train extends GameObject
	{
	
	}
	
	static class Log extends GameObject
	{
		boolean boarded;
		int age;
		float sink;
		
		@Override
		public void tick()
		{
			super.tick();
			age++;
		}
		
		public float getHeight(float delta)
		{
			sink = MathHelper.lerp(delta / 20f, sink, boarded ? 0.1f : 0f);
			return 0.6f + (float)Math.sin((age + delta + 15) / 2.5f) * (boarded ? 0.02f : 0.0333f) - 0.5f - sink;
		}
	}
	
	static class FakeFurnaceMinecart extends FurnaceMinecartEntity
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
}
