package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GamblingCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_STRING = "screen.captcha.gambling.";
	static final Identifier BG_TEX = CAPTCHA.texIdentifier("gui/gambling/slotmachine");
	static final Identifier KNOB_TEX = CAPTCHA.texIdentifier("gui/gambling/knob");
	static final Identifier LEVER_TEX = CAPTCHA.texIdentifier("gui/gambling/lever");
	static final Identifier COIN_TEX = CAPTCHA.texIdentifier("gui/gambling/coin");
	static final Identifier[] SYMBOLS = new Identifier[] {CAPTCHA.texIdentifier("gui/gambling/frown"), CAPTCHA.texIdentifier("gui/gambling/hat"), CAPTCHA.texIdentifier("gui/gambling/ruby"), CAPTCHA.texIdentifier("gui/gambling/seven")};
	static final int leverX = 114, leverY = 21;
	boolean dragging;
	float leverOffset, spinning, lastSpinning, time, coinTimer;
	int[] outcome = new int[3];
	int balance = 200, bet = 10, scheduledCoins;
	List<Coin> coins = new ArrayList<>(), remove = new ArrayList<>();
	
	protected GamblingCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_STRING + "title"), difficulty);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		time += delta;
		coinTimer -= delta;
		if(scheduledCoins > 0 && coinTimer <= 0f)
		{
			coins.add(new Coin(new Vector2f(random.nextInt(65), -32), new Vector2f((random.nextFloat() - 0.5f), 1 + random.nextFloat() * 0.5f)));
			scheduledCoins--;
			coinTimer = 1f;
		}
		if(!coins.isEmpty())
		{
			for (Coin coin : coins)
			{
				coin.pos = coin.pos.add(coin.vel);
				coin.vel = coin.vel.add(0, 0.05f * delta);
				if(coin.pos.y > 120)
					remove.add(coin);
			}
			coins.removeAll(remove);
			remove.clear();
		}
		if(dragging)
			return;
		leverOffset = (int)MathHelper.lerp(delta / 2f, leverOffset, 0f);
		lastSpinning = spinning;
		spinning = Math.max(spinning - delta / 20f, 0f);
		if(lastSpinning > 0 && spinning == 0)
		{
			if(outcome[0] == outcome[1] && outcome[0] == outcome[2])
			{
				int win = switch(outcome[0])
				{
					case 1 -> bet * 2;
					case 2 -> bet * 4;
					case 3 -> bet * 16;
					default -> 0;
				};
				balance += win;
				scheduledCoins += (int)Math.ceil(win / 10f);
			}
		}
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		int size = getContainerHalfSize() * 2;
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0f);
		context.drawTexture(BG_TEX, 0, 0, 0, 0, size, size, size, size);
		matrices.push();
		matrices.translate(120, 30 + (int)leverOffset, 0);
		context.drawTexture(LEVER_TEX, 0, -(int)Math.max(leverOffset - 24, 0), 0, 0,
				4, (int)Math.abs(24 - leverOffset), 4, (int)(24 - leverOffset));
		matrices.pop();
		matrices.push();
		matrices.translate(114, 20 + (int)leverOffset, 0);
		context.drawTexture(KNOB_TEX, 0, 0, 0, 0, 16, 16, 16,16);
		matrices.pop();
		//draw slots
		matrices.push();
		context.enableScissor(width / 2 - getContainerHalfSize() + 8, height / 2 - getContainerHalfSize() + 33,
				width / 2 + getContainerHalfSize() - 34, height / 2 + 9);
		matrices.translate(12, 44, 0);
		for (int i = 1; i <= 3; i++)
		{
			matrices.push();
			matrices.translate((24 + 9) * (i - 1), 0, 0);
			if(spinning > 0.99f - (i * 0.33f) + 0.16f)
			{
				context.drawTexture(SYMBOLS[((int)((i * 49 + time * 32) / 80f)) % SYMBOLS.length],
						0, (int)((time * 32 + i * 49) % 80) - 45, 0, 0, 24, 24, 24, 24);
			}
			else
			{
				float offset = Math.max((spinning - (3 - i) * 0.33f) % 0.33f, 0f) * 128;
				context.drawTexture(SYMBOLS[outcome[i - 1] % SYMBOLS.length], 0, (int)-offset, 0, 0, 24, 24, 24, 24);
			}
			matrices.pop();
		}
		context.disableScissor();
		matrices.pop();
		//draw coins
		matrices.push();
		context.enableScissor(width / 2 - getContainerHalfSize() + 19, height / 2 - getContainerHalfSize() + 85,
				width / 2 + 95, height / 2 + getContainerHalfSize());
		matrices.translate(19, 85, 0);
		for (Coin coin : coins)
		{
			matrices.push();
			matrices.translate(coin.pos.x, coin.pos.y, 0f);
			context.drawTexture(COIN_TEX, 0, 0, 0, 0, 11, 11, 11, 11);
			matrices.pop();
		}
		context.disableScissor();
		matrices.pop();
		matrices.pop();
		context.disableScissor();
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		if(spinning > 0f && !dragging)
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		int pieceX = width / 2 - getContainerHalfSize() + leverX + (int)leverOffset, pieceY = height / 2 - getContainerHalfSize() + leverY;
		if((mouseX > pieceX && mouseX < pieceX + 16 && mouseY > pieceY && mouseY < pieceY + 16) || dragging)
		{
			dragging = true;
			if(deltaY > 0f)
				leverOffset = MathHelper.clamp(leverOffset + (float)deltaY, 0, 53);
			if(leverOffset > 32 && spinning <= 0)
				spinning = 0.99f;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		dragging = false;
		if(spinning > 0f)
		{
			for (int i = 0; i < 3; i++)
			{
				float r = random.nextFloat();
				if(r < 0.05) //5%
					outcome[i] = 0;
				else if(r < 0.55) //50%
					outcome[i] = 1;
				else if(r < 0.85) //20%
					outcome[i] = 2;
				else if(r < 1) //15%
					outcome[i] = 3;
			}
		}
		return super.mouseReleased(mouseX, mouseY, button);
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
		return TRANSLATION_STRING;
	}
	
	static class Coin
	{
		public Vector2f pos, vel;
		
		public Coin(Vector2f pos, Vector2f vel)
		{
			this.pos = pos;
			this.vel = vel;
		}
	}
}
