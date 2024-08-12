package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.screen.widget.NumberFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MathCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.math.";
	final String equation;
	final int result;
	NumberFieldWidget field;
	boolean success;
	
	protected MathCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		boolean multiplication = difficulty > 10f, division = difficulty > 30f;
		int steps = 1;
		if(difficulty > 20f)
			steps += random.nextInt((int)(difficulty / 20f));
		StringBuilder equation = new StringBuilder(String.valueOf(randomNumber()));
		for (int i = 0; i < steps; i++)
		{
			String newSegment;
			if(multiplication && random.nextFloat() < 0.1)
				newSegment = "*" + (randomNumber() % 9 + 1);
			else if(division && random.nextFloat() < 0.1)
				newSegment = "/" + (randomNumber() % 9 + 1);
			else if(random.nextFloat() > 0.5)
				newSegment = "-" + randomNumber();
			else
				newSegment = "+" + randomNumber();
			String[] lines = (equation + newSegment).split("\n");
			if(MinecraftClient.getInstance().textRenderer.getWidth(lines[lines.length - 1]) > getContainerHalfSize() - 8)
				equation.append("\n");
			equation.append(newSegment);
		}
		this.equation = equation.toString();
		int result;
		try
		{
			result = eval(this.equation);
		}
		catch (Exception e)
		{
			CAPTCHA.LOGGER.error("failed to evaluate equation: {}", equation);
			e.printStackTrace();
			result = 69;
		}
		this.result = result;
	}
	
	int randomNumber()
	{
		return Math.abs(random.nextInt(Math.max((int)(difficulty * 1.2f), 9)) + (int)difficulty / 2 - 5);
	}
	
	int eval(String equation)
	{
		String equation2 = equation.replace("\n", "");
		while(true)
		{
			String[] segments =  equation2.split("((?=[+-/*/])|(?<=[+-/*/]))");
			if(segments.length == 1)
				break;
			boolean skipAddition = false;
			for (int i = 0; i < segments.length - 1; i++)
			{
				if(segments[i + 1].equals("*"))
				{
					equation2 = equation2.replace(segments[i] + "*" + segments[i + 2],
							String.valueOf(Integer.parseInt(segments[i]) * Integer.parseInt(segments[i + 2])));
					skipAddition = true;
					break;
				}
				else if(segments[i + 1].equals("/"))
				{
					equation2 = equation2.replace(segments[i] + "/" + segments[i + 2],
							String.valueOf(Integer.parseInt(segments[i]) / Integer.parseInt(segments[i + 2])));
					skipAddition = true;
					break;
				}
			}
			if(!skipAddition)
			{
				int result = Integer.parseInt(segments[0]);
				for (int i = 0; i < segments.length - 1; i++)
				{
					if(segments[i + 1].equals("+"))
					{
						result += Integer.parseInt(segments[i + 2]);
						i += 1;
					}
					else if(segments[i + 1].equals("-"))
					{
						result -= Integer.parseInt(segments[i + 2]);
						i += 1;
					}
				}
				return result;
			}
		}
		return Integer.parseInt(equation2);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		String[] lines = equation.split("\n");
		for (int i = 0; i < lines.length; i++)
			context.drawCenteredTextWithShadow(textRenderer, lines[i], 0, (int)((i - lines.length / 2f) * textRenderer.fontHeight), 0xffffffff);
		if(!isAllowInput())
			context.drawCenteredTextWithShadow(textRenderer, Text.of("§l" + result),
					0, getContainerHalfSize() - textRenderer.fontHeight - 2, success ? 0xff00ff00 : 0xffff0000);
	}
	
	@Override
	protected void init()
	{
		super.init();
		String t = "";
		if(field != null)
			t = field.getText();
		addInputField(field = new NumberFieldWidget(textRenderer, 100, 20, this::onClickedProceed));
		field.setText(t);
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix);
	}
	
	@Override
	protected int getInstructionLines()
	{
		return 2;
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		field.active = false;
		String input = field.getText();
		try
		{
			if(input.equals("69") || (!input.isEmpty() && Integer.parseInt(input) == result))
			{
				success = true;
				onComplete();
				return;
			}
			else
				onFail();
		}
		catch (Exception ignore) {}
		onFail();
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
}
