package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.data.ImageSearchCaptchaPool;
import absolutelyaya.captcha.data.ImageSearchCaptchaPoolManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageSearchCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.image-search.";
	final ImageSearchCaptchaPool pool;
	final String prompt;
	final Identifier background, overlay;
	final List<Element> elements = new ArrayList<>();
	final Element promptElement;
	boolean failed;
	
	protected ImageSearchCaptchaScreen(Text text, float difficulty, String reason)
	{
		super(text, difficulty, reason);
		pool = ImageSearchCaptchaPoolManager.getRandom(difficulty);
		String[] prompts = pool.objects().keySet().toArray(String[]::new);
		prompt = prompts[random.nextInt(prompts.length)];
		int size = 16 + random.nextInt(16);
		elements.add(promptElement = new Element(
				random.nextBetween(0, getContainerHalfSize() * 2) - size / 2,
				random.nextBetween(0, getContainerHalfSize() * 2) - size / 2, prompt, size));
		for (int i = 0; i < getImageCount(); i++)
		{
			String id = null;
			for (int j = 0; j < 16; j++)
			{
				id = prompts[random.nextInt(prompts.length)];
				if(!id.isEmpty() && !id.equals(prompt))
					break;
			}
			size = 16 + random.nextInt(16);
			elements.add(new Element(random.nextBetween(0, getContainerHalfSize() * 2 - size / 2), random.nextBetween(0, getContainerHalfSize() * 2 - size / 2), id,
					Math.max(random.nextInt(32), size)));
		}
		if(!pool.backgrounds().isEmpty())
			background = CAPTCHA.texIdentifier(pool.backgrounds().get(random.nextInt(pool.backgrounds().size())));
		else
			background = null;
		if(!pool.overlays().isEmpty())
			overlay = CAPTCHA.texIdentifier(pool.overlays().get(random.nextInt(pool.overlays().size())));
		else
			overlay = null;
		Collections.shuffle(elements);
	}
	
	public ImageSearchCaptchaScreen(float difficulty, String reason)
	{
		this(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput())
			return false;
		int x = width / 2 - getContainerHalfSize(), y = height / 2 - getContainerHalfSize();
		if(mouseX > x && mouseY > y && mouseX < x + getContainerHalfSize() * 2 && mouseY < y + getContainerHalfSize() * 2)
		{
			int promptX = promptElement.x, promptY = promptElement.y, promptSize = promptElement.size;
			if(mouseX > x + promptX && mouseX < x + promptX + promptSize && mouseY > y + promptY && mouseY < y + promptY + promptSize)
				onComplete();
			else
				onFail();
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	protected int getImageCount()
	{
		return 3 + (int)(difficulty / 30f);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0);
		context.enableScissor((int)(width / 2f) - getContainerHalfSize(), (int)(height / 2f) - getContainerHalfSize(),
				(int)(width / 2f) - getContainerHalfSize() + getContainerHalfSize() * 2,
				(int)(height / 2f) - getContainerHalfSize() + getContainerHalfSize() * 2);
		int size = getContainerHalfSize() * 2;
		if(background != null)
			context.drawTexture(background, 0, 0, 0, 0, size, size, size, size);
		for (Element image : elements)
		{
			int s = image.size;
			if(!failed || image.prompt.equals(prompt))
				context.drawTexture(CAPTCHA.texIdentifier(pool.objects().get(image.prompt)), image.x, image.y, 0, 0, s, s, s, s);
		}
		if(overlay != null)
			context.drawTexture(overlay, 0, 0, 0, 0, size, size, size, size);
		context.disableScissor();
		matrices.pop();
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		onComplete();
	}
	
	@Override
	protected void onFail()
	{
		super.onFail();
		failed = true;
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix, Text.translatable(prompt));
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	record Element(int x, int y, String prompt, int size)
	{
	
	}
}
