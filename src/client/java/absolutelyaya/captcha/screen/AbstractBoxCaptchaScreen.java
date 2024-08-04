package absolutelyaya.captcha.screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBoxCaptchaScreen extends AbstractCaptchaScreen
{
	protected List<ButtonWidget> boxButtons = new ArrayList<>();
	
	protected AbstractBoxCaptchaScreen(Text title, float difficulty)
	{
		super(title, difficulty);
	}
}
