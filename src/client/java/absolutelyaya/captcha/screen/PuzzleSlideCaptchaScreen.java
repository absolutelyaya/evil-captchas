package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.data.PuzzleSlideDataManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class PuzzleSlideCaptchaScreen extends AbstractCaptchaScreen
{
	final static String TRANSLATION_KEY = "screen.captcha.puzzle.";
	final int x, y;
	final Identifier image;
	float pieceOffset, time;
	boolean dragging, untouched = true;
	
	protected PuzzleSlideCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		x = random.nextInt(getContainerHalfSize() * 2 - 16);
		y = random.nextInt(getContainerHalfSize() * 2 - 16);
		image = PuzzleSlideDataManager.getRandomTexture();
		pieceOffset = (50 + random.nextInt(50)) * (random.nextBoolean() ? 1 : -1);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		time += delta / 5f;
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0);
		int size = getContainerHalfSize() * 2;
		context.drawTexture(image, 0, 0, 0, 0, size, size, size, size);
		matrices.translate(x, y, 0);
		context.fill(0, 0, 15, 15, 0x88000000);
		context.fill(15, 0, 16, 15, 0x88ffffff);
		context.fill(0, 15, 16, 16, 0x88ffffff);
		matrices.translate((int)pieceOffset, 0, 0);
		if(!untouched)
		{
			if(isAllowInput())
			{
				context.fill(-1, -1, 16, 16, 0x66ffffff);
				context.fill(0, 0, 17, 17, 0x66000000);
			}
		}
		else
			context.fill(-1, -1, 17, 17, ColorHelper.Argb.fromFloats((float)((Math.sin(time) + 1) / 2f), 1f, 1f, 1f));
		context.drawTexture(image, 0, 0, x, y, 16, 16, size, size);
		matrices.pop();
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		int pieceX = width / 2 - getContainerHalfSize() + x + (int)pieceOffset, pieceY = height / 2 - getContainerHalfSize() + y;
		if((mouseX > pieceX && mouseX < pieceX + 16 && mouseY > pieceY && mouseY < pieceY + 16) || dragging)
		{
			dragging = true;
			pieceOffset += (float)deltaX;
			untouched = false;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		dragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		if(Math.abs(pieceOffset) <= 4)
		{
			pieceOffset = 0;
			onComplete();
		}
		else
			onFail();
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
}
