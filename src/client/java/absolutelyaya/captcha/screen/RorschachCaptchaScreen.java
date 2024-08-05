package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.screen.widget.InputFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RorschachCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.rorschach.";
	final List<Splodge> splodges = new ArrayList<>();
	int instruction;
	
	protected RorschachCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		for (int i = 0; i < 16 + random.nextInt(32); i++)
			splodges.add(new Splodge(random.nextInt(getContainerHalfSize()), (int)(random.nextFloat() * getContainerHalfSize() * 2), random.nextBetween(3, 16)));
		instruction = random.nextInt(3) + (random.nextFloat() < 0.1 && difficulty > 25f ? 1 : 0);
	}
	
	@Override
	protected void init()
	{
		super.init();
		addInputField(new InputFieldWidget(textRenderer, 100, 20, this::onClickedProceed));
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0f);
		context.fill(0, 0, getContainerHalfSize() * 2, getContainerHalfSize() * 2, 0xffffffff);
		matrices.translate(getContainerHalfSize(), 0, 0f);
		for (int x = -1; x < getContainerHalfSize() + 1; x++)
		{
			for (int y = -1; y < getContainerHalfSize() * 2 + 1; y++)
			{
				for (Splodge i : splodges)
				{
					if(Math.abs(i.x - x) + Math.abs(i.y - y) < i.size)
					{
						context.fill(x, y, x + 1, y + 1, 0xff000000);
						context.fill(-x - 1, y, -x, y + 1, 0xff000000);
					}
				}
			}
		}
		matrices.pop();
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		onComplete();
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix.substring(0, prefix.length() - 1) + instruction);
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	record Splodge(int x, int y, int size)
	{
	
	}
}
