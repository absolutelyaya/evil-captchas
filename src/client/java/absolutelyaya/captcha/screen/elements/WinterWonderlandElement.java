package absolutelyaya.captcha.screen.elements;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public class WinterWonderlandElement
{
	static final Identifier[] TEXTURES = new Identifier[] {CAPTCHA.texIdentifier("gui/sponsor/snow/1"), CAPTCHA.texIdentifier("gui/sponsor/snow/2"),
			CAPTCHA.texIdentifier("gui/sponsor/snow/3"), CAPTCHA.texIdentifier("gui/sponsor/snow/4"), CAPTCHA.texIdentifier("gui/sponsor/snow/5")};
	static final Identifier ICICLES = CAPTCHA.texIdentifier("gui/sponsor/snow/icicles");
	static final Random random = Random.create();
	static final List<Snowflake> snowflakes = new ArrayList<>(), removedFlakes = new ArrayList<>();
	static float time;
	static long lastActive;
	
	public static void render(DrawContext context, float delta)
	{
		if(System.currentTimeMillis() - lastActive > 5 * 1000) //reset if last frame was over 5 seconds ago
		{
			snowflakes.clear();
			time = 0f;
		}
		MatrixStack matrices = context.getMatrices();
		for (Snowflake flake : snowflakes)
		{
			flake.update(delta);
			matrices.push();
			matrices.translate(flake.x, flake.y, 100);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flake.rotation));
			matrices.translate(3, 3, 0);
			context.drawTexture(TEXTURES[flake.variant], -3, -3, 0, 0, 7, 7, 7, 7);
			matrices.pop();
			if(flake.removed)
				removedFlakes.add(flake);
		}
		matrices.push();
		matrices.scale(2, 2, 2);
		matrices.translate(0, Math.min(time / 10, 16), 0);
		context.drawTexture(ICICLES, 0, -16, 0, 0, context.getScaledWindowWidth() / 2, 13, 64, 13);
		matrices.pop();
		if(snowflakes.size() < 128 && random.nextFloat() < 0.05f)
			snowflakes.add(new Snowflake(random, context.getScaledWindowWidth(), context.getScaledWindowHeight()));
		snowflakes.removeAll(removedFlakes);
		removedFlakes.clear();
		time += delta / 20f;
		lastActive = System.currentTimeMillis();
	}
	
	static class Snowflake
	{
		float rotation, rotationSpeed;
		float x, y, velocityX, velocityY, maxY;
		boolean removed;
		int variant;
		
		public Snowflake(Random random, float screenWidth, float screenHeight)
		{
			rotationSpeed = (random.nextFloat() - 0.5f) * 3.5f;
			velocityX = (random.nextFloat() - 0.5f) * 0.5f;
			velocityY = random.nextFloat() * 0.65f + 0.1f;
			x = random.nextFloat() * screenWidth;
			y = -10;
			maxY = screenHeight;
			variant = random.nextInt(TEXTURES.length);
		}
		
		void update(float delta)
		{
			x += velocityX * delta;
			y += velocityY * delta;
			rotation += rotationSpeed * delta;
			if(y > maxY + 5 || x < -5)
				removed = true;
		}
	}
}
