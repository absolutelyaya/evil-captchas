package absolutelyaya.captcha.screen.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class ScreenSaverElement
{
	static final String[] ITEMS = new String[] {"slime_ball", "fire_charge", "magma_cream", "ender_pearl", "ender_eye"};
	static final Random random = Random.create();
	static int item;
	static float x, y, moveX = 0.5f, moveY = 0.5f;
	
	public static void render(DrawContext context, float delta)
	{
		Identifier texture = Identifier.of("textures/item/" + ITEMS[item] + ".png");
		x += moveX;
		y += moveY;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x, y, 420);
		context.drawTexture(texture, 0, 0, 0, 0, 32, 32, 32, 32);
		matrices.pop();
		if(x < -4 || x > context.getScaledWindowWidth() - (item < 3 ? 14 : 15) * 2)
		{
			moveX = -moveX;
			item = random.nextInt(ITEMS.length);
		}
		if(y < -4 || y > context.getScaledWindowHeight() - (item < 3 ? 14 : 15) * 2)
		{
			moveY = -moveY;
			item = random.nextInt(ITEMS.length);
		}
	}
}
