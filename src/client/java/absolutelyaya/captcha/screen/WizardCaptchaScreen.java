package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

public class WizardCaptchaScreen extends AbstractCaptchaScreen
{
	static final Identifier[] WIZARD_TEX = new Identifier[] {CAPTCHA.texIdentifier("gui/wizards/wzrd1"), CAPTCHA.texIdentifier("gui/wizards/wzrd2"),
			CAPTCHA.texIdentifier("gui/wizards/blizard"), CAPTCHA.texIdentifier("gui/wizards/wzrd-step")};
	static final String TRANSLATION_KEY = "screen.captcha.wizard.";
	final Identifier tex;
	Vector2f pos = new Vector2f(), movement = new Vector2f(), targetMovement = new Vector2f();
	float time;
	boolean jumpscare;
	
	protected WizardCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		tex = WIZARD_TEX[random.nextInt(WIZARD_TEX.length)];
		targetMovement = new Vector2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f).normalize();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		if(isAllowInput() || jumpscare)
			time += delta / 2.5f;
		if(!isAllowInput())
			return;
		if(random.nextFloat() < 0.05 + Math.min(difficulty / 1000f, 0.2))
			targetMovement = new Vector2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f).normalize();
		movement = movement.lerp(targetMovement, delta * difficulty / 100f).normalize();
		
		pos = pos.add(movement.mul(Math.max(difficulty / 25f, 0.75f)));
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
				jumpscare = random.nextFloat() < 0.01f;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate((int)pos.x, (int)pos.y, 0f);
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
