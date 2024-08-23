package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IConfigComponent;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.networking.CaptchaResultPayload;
import absolutelyaya.captcha.registry.SoundRegistry;
import absolutelyaya.captcha.screen.widget.InputFieldWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractCaptchaScreen extends Screen
{
	static final Identifier HEARTS_TEX = CAPTCHA.texIdentifier("gui/hearts");
	static final Map<String, Pair<Integer, BiFunction<Float, String, AbstractCaptchaScreen>>> screens = new HashMap<>();
	static final List<String> easy = List.of("wizard", "butterflies", "puzzle-slide", "rorschach");
	protected static final Random random = Random.create();
	protected final String reason;
	protected IConfigComponent config;
	protected IPlayerComponent playerData;
	private boolean success;
	protected int nextDelay = -1;
	protected final float difficulty;
	ButtonWidget proceedButton;
	
	protected AbstractCaptchaScreen(Text title, float difficulty, String reason)
	{
		super(title);
		this.difficulty = difficulty;
		this.reason = reason;
	}
	
	@Override
	protected void init()
	{
		super.init();
		if(isHasProceedButton())
			addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.translatable("screen.captcha.generic.proceed"), button -> {
				if(isAllowInput())
					onClickedProceed();
			}).dimensions(width / 2 - 50, height / 2 + getContainerHalfSize() + 8, 100, 20).build());
		config = CaptchaComponents.CONFIG.get(client.world.getScoreboard());
		playerData = CaptchaComponents.PLAYER.get(client.player);
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
					openRandomCaptcha(client, Math.max(difficulty - 1f, 3f), reason);
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
				Text.translatable("screen.captcha.generic.instruction", Text.translatable("captcha.reason." + reason).getString()),
				0, 0, 0xffffff);
		matrices.pop();
		matrices.translate(0f, height / 2f - 16f, 0);
		drawContainer(context, matrices);
		matrices.translate(getContainerHalfSize() + 4, -getContainerHalfSize(), 0);
		drawInstructions(context, matrices);
		matrices.pop();
	}
	
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		if(config.isLethal())
			drawHealth(context, matrices);
		
		int boxSize = getContainerHalfSize();
		context.fill(-boxSize - 2, -boxSize - 2, boxSize + 2, boxSize + 2, 0x88000000);
		context.drawBorder(-boxSize - 1, -boxSize - 1, boxSize * 2 + 2, boxSize * 2 + 2, 0xffffffff);
	}
	
	public void drawHealth(DrawContext context, MatrixStack matrices)
	{
		matrices.push();
		int maxLives = config.getLives(), lives = playerData.getCurLives();
		matrices.translate(-maxLives * 22f / 2f, -32 - getContainerHalfSize(), 0);
		matrices.scale(2, 2, 2);
		for (int i = 0; i < maxLives; i++)
		{
			boolean b = i < lives;
			matrices.push();
			if(!isAllowInput() && !success)
				matrices.translate(random.nextFloat() * 1, random.nextFloat() * 1, random.nextFloat() * 1);
			context.drawTexture(HEARTS_TEX, i * 11, 0, b ? 0 : 11, config.isExplosive() ? 10 : 0, 11, 10, 22, 20);
			matrices.pop();
		}
		matrices.pop();
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
		ClientPlayNetworking.send(new CaptchaResultPayload(true));
	}
	
	protected void onFail()
	{
		success = false;
		nextDelay = 30;
		if(client != null && client.player != null)
			client.player.playSound(SoundRegistry.WRONG_BUZZER, 1f, 1f);
		ClientPlayNetworking.send(new CaptchaResultPayload(false));
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
	
	public static void openRandomCaptcha(MinecraftClient client, float difficulty, String reason)
	{
		IConfigComponent config = CaptchaComponents.CONFIG.get(client.world.getScoreboard());
		List<Pair<Integer, BiFunction<Float, String, AbstractCaptchaScreen>>> candidates = new ArrayList<>();
		for (Map.Entry<String, Pair<Integer, BiFunction<Float, String, AbstractCaptchaScreen>>> i : screens.entrySet())
			if(difficulty >= i.getValue().getLeft() && !(config.isNotEasy() && easy.contains(i.getKey())))
				candidates.add(i.getValue());
		for (int i = 0; i < 3; i++)
		{
			try
			{
				AbstractCaptchaScreen captcha;
				Pair<Integer, BiFunction<Float, String, AbstractCaptchaScreen>> pair = candidates.get(random.nextInt(candidates.size()));
				captcha = pair.getRight().apply(difficulty - pair.getLeft(), reason);
				client.setScreen(captcha);
				break;
			}
			catch (Exception ignored)
			{
			
			}
		}
	}
	
	public static void openSpecificCaptcha(MinecraftClient client, String type, float difficulty, String reason)
	{
		if(!screens.containsKey(type))
		{
			CAPTCHA.LOGGER.error("captcha type '{}' doesn't exist", type);
			return;
		}
		for (int i = 0; i < 3; i++)
		{
			try
			{
				AbstractCaptchaScreen captcha;
				Pair<Integer, BiFunction<Float, String, AbstractCaptchaScreen>> pair = screens.get(type);
				captcha = pair.getRight().apply(difficulty - pair.getLeft(), reason);
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
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	static {
		screens.put("single-boxes", new Pair<>(0, SingleBoxCaptchaScreen::new));
		screens.put("multi-boxes", new Pair<>(0, MultiBoxCaptchaScreen::new));
		screens.put("wonky-text", new Pair<>(0, WonkyTextCaptchaScreen::new));
		screens.put("puzzle-slide", new Pair<>(3, (i, r) -> new PuzzleSlideCaptchaScreen(Math.max(i, 1), r)));
		screens.put("simple-comprehension", new Pair<>(5, (i, r) -> new ComprehensionTestCaptchaScreen(Math.max(i, 1), r)));
		screens.put("image-search", new Pair<>(5, (i, r) -> new ImageSearchCaptchaScreen(Math.max(i, 1), r)));
		screens.put("math", new Pair<>(5, (i, r) -> new MathCaptchaScreen(Math.max(i, 1), r)));
		screens.put("rorschach", new Pair<>(10, (i, r) -> new RorschachCaptchaScreen(Math.max(i, 1), r)));
		screens.put("wimmelbild", new Pair<>(10, (i, r) -> new WimmelbildCaptchaScreen(Math.max(i, 1), r)));
		screens.put("wizard", new Pair<>(15, (i, r) -> new WizardCaptchaScreen(Math.max(i, 1), r)));
		screens.put("amongus", new Pair<>(15, (i, r) -> new AmongusCaptchaScreen(Math.max(i, 1), r)));
		screens.put("advanced-comprehension", new Pair<>(20, (i, r) -> new AdvancedComprehensionTestCaptchaScreen(Math.max(i, 1), r)));
		screens.put("gambling", new Pair<>(20, (i, r) -> new GamblingCaptchaScreen(Math.max(i, 1), r)));
		screens.put("butterflies", new Pair<>(20, (i, r) -> new ButterflyCaptchaScreen(Math.max(i, 1), r)));
	}
}
