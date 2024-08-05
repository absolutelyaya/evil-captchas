package absolutelyaya.captcha.screen.widget;

import net.minecraft.client.font.TextRenderer;

public class NumberFieldWidget extends InputFieldWidget
{
	public NumberFieldWidget(TextRenderer textRenderer, int width, int height, Runnable onConfirm)
	{
		super(textRenderer, width, height, onConfirm);
	}
	
	public boolean charTyped(char chr, int modifiers)
	{
		if (isActive() && isValidChar(chr))
		{
			write(Character.toString(chr));
			return true;
		}
		return false;
	}
	
	protected boolean isValidChar(char c)
	{
		return c == '-' || Character.isDigit(c);
	}
}
