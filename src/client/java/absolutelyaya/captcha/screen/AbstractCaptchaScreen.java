package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.registry.SoundRegistry;
import absolutelyaya.captcha.screen.widget.InputFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractCaptchaScreen extends Screen
{
	static final List<Pair<Integer, Function<Float, AbstractCaptchaScreen>>> screens = new ArrayList<>();
	protected static Random random = Random.create();
	private boolean success;
	protected int nextDelay = -1;
	protected final float difficulty;
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
		if(isHasProceedButton())
			addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.translatable("screen.captcha.generic.proceed"), button -> onClickedProceed())
									 .dimensions(width / 2 - 50, height / 2 + getContainerHalfSize() + 8, 100, 20).build());
	}
	
	protected void addInputField(InputFieldWidget field)
	{
		addDrawableChild(field);
		field.setX(width / 2 - 50);
		field.setY(height / 2 + getContainerHalfSize() + 8);
		setFocused(field);
		
		if(isHasProceedButton())
			proceedButton.setY(proceedButton.getY() + 24);
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
					openRandomCaptcha(client, Math.max(difficulty - 1f, 3f));
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
		matrices.translate(getContainerHalfSize() + 4, -getContainerHalfSize(), 0);
		drawInstructions(context, matrices);
		matrices.pop();
	}
	
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		int boxSize = getContainerHalfSize();
		context.fill(-boxSize - 2, -boxSize - 2, boxSize + 2, boxSize + 2, 0x88000000);
		context.drawBorder(-boxSize - 1, -boxSize - 1, boxSize * 2 + 2, boxSize * 2 + 2, 0xffffffff);
	}
	
	public void drawInstructions(DrawContext context, MatrixStack matrices)
	{
		int totalLines = 0;
		for (int i = 0; i < getInstructionLines(); i++)
		{
			String key = getTranslationKey() + "instruction" + i;
			Text t = Text.of("- " + getInstructionText(i, key).getString());
			List<OrderedText> lines = textRenderer.wrapLines(t, width / 2 - getContainerHalfSize() - 8);
			for (int j = 0; j < lines.size(); j++)
				context.drawText(textRenderer, lines.get(j), j > 0 ? 10 : 0, (totalLines++) * textRenderer.fontHeight, 0xffffffff, true);
		}
	}
	
	public int getContainerHalfSize()
	{
		return 70;
	}
	
	protected void onComplete()
	{
		success = true;
		nextDelay = 20;
		if(client != null && client.player != null)
			client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
	}
	
	protected void onFail()
	{
		success = false;
		nextDelay = 30;
		if(client != null && client.player != null)
			client.player.playSound(SoundRegistry.WRONG_BUZZER, 1f, 1f);
	}
	
	protected void onClickedProceed()
	{
		if(isHasProceedButton())
			proceedButton.active = false;
	}
	
	protected int getInstructionLines()
	{
		return 1;
	}
	
	protected abstract Text getInstructionText(int i, String prefix);
	
	public static void openRandomCaptcha(MinecraftClient client, float difficulty)
	{
		List<Function<Float, AbstractCaptchaScreen>> candidates = new ArrayList<>();
		for (Pair<Integer, Function<Float, AbstractCaptchaScreen>> i : screens)
			if(difficulty >= i.getLeft())
				candidates.add(i.getRight());
		for (int i = 0; i < 3; i++)
		{
			try
			{
				AbstractCaptchaScreen captcha;
				captcha = candidates.get(random.nextInt(candidates.size())).apply(difficulty);
				client.setScreen(captcha);
				break;
			}
			catch (Exception ignored)
			{
			
			}
		}
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	abstract String getTranslationKey();
	
	protected boolean isAllowInput()
	{
		return nextDelay == -1;
	}
	
	protected void setAllowInput(boolean b)
	{
		nextDelay = b ? -1 : Integer.MAX_VALUE;
	}
	
	protected boolean isHasProceedButton()
	{
		return true;
	}
	
	static {
		screens.add(new Pair<>(0, SingleBoxCaptchaScreen::new));
		screens.add(new Pair<>(0, MultiBoxCaptchaScreen::new));
		screens.add(new Pair<>(0, WonkyTextCaptchaScreen::new));
		screens.add(new Pair<>(3, i -> new PuzzleSlideCaptchaScreen(Math.max(i - 3, 1))));
		screens.add(new Pair<>(5, i -> new ComprehensionTestCaptchaScreen(Math.max(i - 5, 1))));
		screens.add(new Pair<>(5, i -> new ImageSearchCaptchaScreen(Math.max(i - 5, 1))));
		screens.add(new Pair<>(5, i -> new MathCaptchaScreen(Math.max(i - 5, 1))));
		screens.add(new Pair<>(10, i -> new RorschachCaptchaScreen(Math.max(i - 10, 1))));
		screens.add(new Pair<>(10, i -> new WimmelbildCaptchaScreen(Math.max(i - 10, 1))));
		screens.add(new Pair<>(15, i -> new WizardCaptchaScreen(Math.max(i - 15, 1))));
		screens.add(new Pair<>(15, i -> new AmongusCaptchaScreen(Math.max(i - 15, 1))));
		screens.add(new Pair<>(20, i -> new AdvancedComprehensionTestCaptchaScreen(Math.max(i - 20, 1))));
		screens.add(new Pair<>(20, i -> new GamblingCaptchaScreen(Math.max(i - 20, 1))));
		screens.add(new Pair<>(20, i -> new ButterflyCaptchaScreen(Math.max(i - 20, 1))));
	}
}
