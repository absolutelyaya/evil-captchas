package absolutelyaya.captcha.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

public class InputFieldWidget extends TextFieldWidget
{
	final Runnable onConfirm;
	
	public InputFieldWidget(TextRenderer textRenderer, int width, int height, Runnable onConfirm)
	{
		super(textRenderer, width, height, Text.empty());
		this.onConfirm = onConfirm;
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
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(isFocused() && isActive() && keyCode == 257)
		{
			onConfirm.run();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	protected boolean isValidChar(char c)
	{
		return StringHelper.isValidChar(c);
	}
}
