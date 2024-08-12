package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.data.SingleBoxCaptchaData;
import absolutelyaya.captcha.data.SingleBoxCaptchaDataManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SingleBoxCaptchaScreen extends AbstractCaptchaScreen
{
	final static String TRANSLATION_KEY = "screen.captcha.boxes.single.";
	final SingleBoxCaptchaData data;
	final boolean[][] selection;
	final String prompt;
	protected List<ButtonWidget> boxButtons = new ArrayList<>();
	
	protected SingleBoxCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		data = SingleBoxCaptchaDataManager.getRandom(difficulty);
		selection = new boolean[data.subdivisions()][data.subdivisions()];
		prompt = data.prompts().get(random.nextInt(data.prompts().size()));
	}
	
	@Override
	protected void init()
	{
		super.init();
		int startX = width / 2 - getContainerHalfSize(), startY = height / 2 - getContainerHalfSize();
		int size = getContainerHalfSize() * 2;
		int boxSize = size / data.subdivisions();
		for (int x = 0; x < data.subdivisions(); x++)
		{
			for (int y = 0; y < data.subdivisions(); y++)
			{
				int finalX = x;
				int finalY = y;
				boxButtons.add(addDrawableChild(new ButtonWidget.Builder(Text.empty(), button -> {
					if(isAllowInput())
						selection[finalX][finalY] = !selection[finalX][finalY];
				}).dimensions(startX + x * boxSize, startY + y * boxSize, boxSize, boxSize).build()));
			}
		}
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		if(data == null)
			return;
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0);
		int size = getContainerHalfSize() * 2;
		context.drawTexture(data.texture(), 0, 0, 0, 0, size, size, size, size);
		int boxSize = size / data.subdivisions();
		for (int x = 0; x < data.subdivisions(); x++)
		{
			for (int y = 0; y < data.subdivisions(); y++)
			{
				boolean selected = selection[x][y];
				if(selected)
					context.fill(x * boxSize, y * boxSize, (x + 1) * boxSize, (y + 1) * boxSize, 0x4400ff00);
				context.fill(x * boxSize, y * boxSize, x * boxSize + 1, (y + 1) * boxSize, selected ? 0xff4444ff : 0x44ffffff);
				context.fill(x * boxSize, y * boxSize, (x + 1) * boxSize, y * boxSize + 1, selected ? 0xff4444ff : 0x44ffffff);
				context.fill((x + 1) * boxSize, y * boxSize, (x + 1) * boxSize - 1, (y + 1) * boxSize, selected ? 0xff333388 : 0x44000000);
				context.fill(x * boxSize, (y + 1) * boxSize, (x + 1) * boxSize, (y + 1) * boxSize - 1, selected ? 0xff333388 : 0x44000000);
			}
		}
		matrices.pop();
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		int accuracy = 0, maxAccuracy = 0;
		for (int x = 0; x < data.subdivisions(); x++)
		{
			for (int y = 0; y < data.subdivisions(); y++)
			{
				if(selection[x][y])
				{
					if(data.values().get(x + y * data.subdivisions()).contains(prompt))
						accuracy++;
					else
						accuracy--;
				}
				if(data.values().get(x + y * data.subdivisions()).contains(prompt))
					maxAccuracy++;
			}
		}
		if((float)accuracy / (float)maxAccuracy >= Math.min(0.5f + (difficulty - 5f) / 250f, 1f))
			onComplete();
		else
			onFail();
	}
	
	@Override
	protected int getInstructionLines()
	{
		return 2;
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return switch(i)
		{
			case 0 -> Text.translatable(prefix, prompt);
			case 1 -> Text.translatable(prefix, (int)(Math.min(0.5f + (difficulty - 5f) / 250f, 1f) * 100f));
			default -> Text.empty();
		};
	}
}
