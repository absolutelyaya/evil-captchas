package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.registry.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class AbstractCaptchaScreen extends Screen
{
	private boolean success;
	private int nextDelay = -1;
	private final float difficulty;
	ButtonWidget proceedButton;
	
	protected AbstractCaptchaScreen(Text title, float difficulty)
	{
		super(title);
		this.difficulty = difficulty;
	}
	
	@Override
	protected void init()
	{
		super.init();
		addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.translatable("screen.captcha.generic.proceed"), button -> onPressedProceed())
								 .dimensions(width / 2 - 50, height / 2 + getContainerHalfSize() + 8, 100, 20).build());
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(nextDelay > 0)
		{
			nextDelay--;
			if(nextDelay == 0)
			{
				if(success)
					close();
				else
					openRandomCaptcha(client, difficulty - 1f);
			}
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(width / 2f, 16f, 0);
		matrices.push();
		matrices.scale(2f, 2f, 2f);
		context.drawCenteredTextWithShadow(textRenderer, title, 0, 0, 0xffffff);
		matrices.pop();
		matrices.push();
		matrices.translate(0, 19f, 0);
		context.drawCenteredTextWithShadow(textRenderer,
				Text.translatable("screen.captcha.generic.instruction"), 0, 0, 0xffffff);
		matrices.pop();
		matrices.translate(0f, height / 2f - 16f, 0);
		drawContainer(context, matrices);
		matrices.pop();
		drawInstructions(context, matrices);
	}
	
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		int boxSize = getContainerHalfSize();
		context.fill(-boxSize - 1, -boxSize - 1, boxSize + 1, boxSize + 1, 0x88000000);
		context.drawBorder(-boxSize, -boxSize, boxSize * 2, boxSize * 2, 0xffffffff);
	}
	
	public void drawInstructions(DrawContext context, MatrixStack matrices)
	{
		for (int i = 0; i < getInstructionLines(); i++)
		{
			String key = getTranslationKey() + "instruction" + i;
		}
	}
	
	public int getContainerHalfSize()
	{
		return 64;
	}
	
	protected void onComplete()
	{
		success = true;
		nextDelay = 20;
	}
	
	protected void onFail()
	{
		success = false;
		nextDelay = 30;
		if(client != null && client.player != null)
			client.player.playSound(SoundRegistry.WRONG_BUZZER, 1f, 1f);
	}
	
	protected void onPressedProceed()
	{
		proceedButton.active = false;
		onFail();
	}
	
	protected int getInstructionLines()
	{
		return 1;
	}
	
	public static void openRandomCaptcha(MinecraftClient client, float difficulty)
	{
		AbstractCaptchaScreen captcha;
		captcha = new SingleBoxCaptchaScreen(difficulty);
		client.setScreen(captcha);
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	abstract String getTranslationKey();
	
	protected boolean allowInput()
	{
		return nextDelay == -1;
	}
}
