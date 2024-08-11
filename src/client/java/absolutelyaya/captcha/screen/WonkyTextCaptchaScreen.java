package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.screen.widget.InputFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Vector2i;

public class WonkyTextCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.wonky-text.";
	static final String[] ALL_LETTER_IDS = new String[] { "a1", "a2", "b1", "c1", "d1", "d2", "e1", "e2", "f1" },
						  ALL_LETTERS = new String[] { "A", "a", "B", "C", "d", "D", "e", "E", "F" };
	final String[] letters;
	final int[] obscura;
	final Vector2i[] offset;
	final String solution;
	InputFieldWidget field;
	
	protected WonkyTextCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		StringBuilder sb = new StringBuilder();
		letters = new String[7];
		offset = new Vector2i[7];
		for (int i = 0; i < 7; i++)
		{
			if(random.nextBoolean())
			{
				int r = random.nextInt(ALL_LETTER_IDS.length);
				letters[i] = ALL_LETTER_IDS[r];
				sb.append(ALL_LETTERS[r]);
			}
			else
			{
				int r = random.nextInt(9) + 1;
				letters[i] = String.valueOf(r);
				sb.append(r);
			}
			offset[i] = new Vector2i((int)((random.nextFloat() - 0.5f) * 10), (int)((random.nextFloat() - 0.5f) * 16));
		}
		int obscuraLayers = random.nextInt((int)(difficulty / 5f));
		obscura = new int[obscuraLayers];
		for (int i = 0; i < obscuraLayers; i++)
			obscura[i] = random.nextInt(8) + 1;
		solution = sb.toString();
	}
	
	@Override
	protected void init()
	{
		super.init();
		addInputField(field = new InputFieldWidget(textRenderer, 100, 20, this::onClickedProceed));
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -16, 0);
		context.fill(0, 0, 140, 48, 0xffffffff);
		matrices.push();
		for (int i = 0; i < letters.length; i++)
		{
			context.drawTexture(CAPTCHA.texIdentifier("gui/wonky-text/" + letters[i]), offset[i].x, offset[i].y, 0, 0,
					20, 32, 20, 32);
			matrices.translate(20, 0, 0);
		}
		matrices.pop();
		for (int i : obscura)
			context.drawTexture(CAPTCHA.texIdentifier("gui/wonky-text/obscura" + i), 0, 0, 0, 0,
					140, 48, 140, 48);
		matrices.pop();
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		field.active = false;
		if(field.getText().equals(solution))
			onComplete();
		else
			onFail();
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
