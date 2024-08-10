package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.data.AmongusPool;
import absolutelyaya.captcha.data.AmongusPoolManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AmongusCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.amongus.";
	static final Identifier RESULT_BG = CAPTCHA.texIdentifier("gui/amongus/result_bg");
	final List<Identifier> textures = new ArrayList<>();
	final Identifier impostor;
	int selection = -1;
	float resultSequenceTime;
	boolean finished;
	
	protected AmongusCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		AmongusPool pool = AmongusPoolManager.getRandomPool(difficulty);
		textures.add(impostor = pool.impostors().get(random.nextInt(pool.impostors().size())));
		for (int i = 0; i < 4; i++)
		{
			if(!pool.crewmates().isEmpty())
				textures.add(pool.crewmates().get(random.nextInt(pool.crewmates().size())));
			else
				textures.add(impostor);
		}
		Collections.shuffle(textures);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		if(selection >= 0)
			resultSequenceTime += delta / 20f;
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		if(selection >= 0)
		{
			matrices.push();
			matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 10);
			drawResultSequence(context, matrices);
			matrices.pop();
		}
		matrices.push();
		int width = getContainerHalfSize() * 2 - 32;
		for (int i = 0; i < textures.size(); i++)
		{
			matrices.push();
			matrices.translate((i - (int)((float)textures.size() / 2)) * (int)(width / (float)textures.size() + 4), 0, 0);
			context.drawTexture(textures.get(i), -12, -12, 0, 0, 24, 24, 24, 24);
			matrices.pop();
		}
		matrices.pop();
		context.disableScissor();
	}
	
	void drawResultSequence(DrawContext context, MatrixStack matrices)
	{
		float bgVis = Math.min(resultSequenceTime / 0.25f, 1f);
		matrices.push();
		int size = getContainerHalfSize() * 2;
		matrices.translate((int)(size * (1f - bgVis)), 0f, 0f);
		context.drawTexture(RESULT_BG, 0, 0, (int)(size * (1f - bgVis)), 0, (int)(size * bgVis), size, size, size);
		matrices.pop();
		float ejectedX = (resultSequenceTime / 3f - 0.3f) * getContainerHalfSize();
		matrices.push();
		matrices.translate(ejectedX, getContainerHalfSize(), 0);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float)Math.toRadians(ejectedX / 3f - 18f)));
		context.drawTexture(textures.get(selection), -24, -24, 0, 0, 48, 48, 48, 48);
		matrices.pop();
		boolean correct = textures.get(selection).equals(impostor);
		matrices.push();
		matrices.translate(getContainerHalfSize(), getContainerHalfSize() * 2 - 16, 0);
		matrices.scale(0.9f, 0.9f, 0.9f);
		float textVis = Math.min(resultSequenceTime - 2f, 1f);
		String text = Text.translatable("screen.captcha.amongus." + (correct ? "" : "in") + "correct").getString();
		context.drawCenteredTextWithShadow(textRenderer, text.substring(0, (int)Math.max(textVis * text.length(), 0)), 0, 0, 0xffffffff);
		matrices.pop();
		
		if(resultSequenceTime >= 3 && !finished)
		{
			if(correct)
				onComplete();
			else
				onFail();
			finished = true;
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput())
			return false;
		int width = getContainerHalfSize() * 2 - 32;
		for(int i = 0; i < textures.size(); i++)
		{
			int x = this.width / 2 + (i - (int)((float)textures.size() / 2)) * (int)(width / (float)textures.size() + 4) - 12, y = height / 2 - 12;
			{
				if(mouseX > x && mouseX < x + 24 && mouseY > y && mouseY < y + 24)
				{
					selection = i;
					setAllowInput(false);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
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
}
