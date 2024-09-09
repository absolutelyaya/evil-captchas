package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

public class WizardCaptchaScreen extends AbstractCaptchaScreen
{
	static final Identifier[] WIZARD_TEX = new Identifier[] {CAPTCHA.texIdentifier("gui/wizards/wzrd1"), CAPTCHA.texIdentifier("gui/wizards/wzrd2"),
			CAPTCHA.texIdentifier("gui/wizards/blizard"), CAPTCHA.texIdentifier("gui/wizards/wzrd-step"), CAPTCHA.texIdentifier("gui/wizards/blue"),
			CAPTCHA.texIdentifier("gui/wizards/nekomancer")};
	static final String TRANSLATION_KEY = "screen.captcha.wizard.";
	final Identifier tex;
	Vector2f pos = new Vector2f(), movement = new Vector2f(), targetMovement = new Vector2f();
	float time;
	boolean jumpscare;
	float failTimer, tickingSoundTimer;
	int tickSoundCounter;
	
	protected WizardCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		tex = WIZARD_TEX[random.nextInt(WIZARD_TEX.length)];
		targetMovement = new Vector2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f).normalize();
		failTimer = 15f - Math.min(difficulty / 50f, 5f);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		if(isAllowInput() || jumpscare)
			time += delta / 2.5f;
		if(isAllowInput())
		{
			failTimer = Math.max(failTimer - delta / 20f, 0f);
			if(failTimer <= 0f)
			{
				onFail();
				time = 0f;
				jumpscare = true;
			}
			if((tickingSoundTimer -= delta / 20f) < 0f)
			{
				boolean fast = failTimer < 5f;
				float pitch = fast ? 1.3f : 1f;
				if(tickSoundCounter++ % 2 == 1)
					pitch -= 0.15f;
				if(client != null && client.player != null)
					client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), 1f, pitch);
				tickingSoundTimer = fast ? 0.25f : 0.5f;
			}
		}
		if(!isAllowInput())
			return;
		if(random.nextFloat() < 0.05 + Math.min(difficulty / 1000f, 0.4))
			targetMovement = new Vector2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f).normalize();
		movement = movement.lerp(targetMovement, delta * Math.min(difficulty / 100f, 0.75f)).normalize();
		
		pos = pos.add(movement.mul(MathHelper.clamp(difficulty / 25f, 0.75f, 10f)));
		if(pos.x + 16> getContainerHalfSize() || pos.x - 16 < -getContainerHalfSize() ||
				   pos.y + 16> getContainerHalfSize() || pos.y - 16 < -getContainerHalfSize())
			targetMovement = movement = new Vector2f(pos).mul(-1).normalize();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput())
			return false;
		int x = width / 2 - getContainerHalfSize(), y = height / 2 - getContainerHalfSize();
		if(mouseX > x && mouseY > y && mouseX < x + getContainerHalfSize() * 2 && mouseY < y + getContainerHalfSize() * 2)
		{
			int wizardX = (int)pos.x + getContainerHalfSize() - 16, wizardY = (int)pos.y + getContainerHalfSize() - 16, size = 32;
			if(mouseX > x + wizardX && mouseX < x + wizardX + size && mouseY > y + wizardY && mouseY < y + wizardY + size)
			{
				onComplete();
				time = 0f;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		
		matrices.push();
		matrices.translate(getContainerHalfSize() + 8, 0, 0);
		float tension = Math.abs(MathHelper.clamp((failTimer - 7.5f) / 7.5f, -1f, 0f));
		matrices.scale(1f + tension, 1f + tension, 1f + tension);
		matrices.translate(0f, -textRenderer.fontHeight / 2f, 0f);
		matrices.translate(random.nextFloat() * tension * 1.5f, random.nextFloat() * tension * 1.5f, random.nextFloat() * tension * 1.5f);
		context.drawText(textRenderer, Text.of((int)Math.floor(failTimer) + ":" + (int)Math.floor(((failTimer - (int)Math.floor(failTimer)) * 100f))),
				0, 0, ColorHelper.Argb.fromFloats(1f, 1f, Math.max(1f - tension * 2f, 0f), Math.max(1f - tension * 2f, 0f)), true);
		matrices.pop();
		
		matrices.push();
		matrices.translate((int)pos.x, (int)pos.y, 10f);
		int size = 32;
		if(!isAllowInput() && jumpscare)
			size = (int)(size + time * 75);
		context.drawTexture(tex, -size / 2, -size / 2, 0, 0, size, size, time % 2f > 1f ? -size : size, size);
		matrices.pop();
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
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
	public int getContainerHalfSize()
	{
		return (int)(super.getContainerHalfSize() * MathHelper.clamp(1f + (difficulty - 20f) / 10f, 1f, 1.2f));
	}
}
