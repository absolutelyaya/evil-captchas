package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GamblingCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.gambling.";
	static final Identifier BG_TEX = CAPTCHA.texIdentifier("gui/gambling/slotmachine");
	static final Identifier KNOB_TEX = CAPTCHA.texIdentifier("gui/gambling/knob");
	static final Identifier LEVER_TEX = CAPTCHA.texIdentifier("gui/gambling/lever");
	static final Identifier COIN_TEX = CAPTCHA.texIdentifier("gui/gambling/coin");
	static final Identifier[] SYMBOLS = new Identifier[] {CAPTCHA.texIdentifier("gui/gambling/frown"), CAPTCHA.texIdentifier("gui/gambling/hat"), CAPTCHA.texIdentifier("gui/gambling/ruby"), CAPTCHA.texIdentifier("gui/gambling/seven")};
	static final SoundEvent[] RESULT_SOUNDS = new SoundEvent[] {SoundEvents.ENTITY_VILLAGER_NO, SoundEvents.ENTITY_VILLAGER_YES, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundEvents.ENTITY_GENERIC_EXPLODE.value()};
	static final int leverX = 114, leverY = 21;
	boolean dragging, completed, outcomePending;
	float leverOffset, spinning, lastSpinning, time, coinTimer;
	int[] outcome = new int[3];
	boolean[] sound = new boolean[3];
	int realBalance = 200, realBet = 10, scheduledCoins, targetBalance, pulls;
	float displayBalance, displayBet;
	List<Coin> coins = new ArrayList<>(), remove = new ArrayList<>();
	List<ButtonWidget> buttons = new ArrayList<>();
	
	protected GamblingCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		targetBalance = 300 + (int)((difficulty - 1) * 25) + random.nextInt((int)(difficulty * 66));
		sound[0] = sound[1] = sound[2] = false;
	}
	
	@Override
	protected void init()
	{
		super.init();
		buttons.clear();
		int width = getContainerHalfSize() / 2;
		String t = Text.translatable("screen.captcha.gambling.bet").getString();
		buttons.add(addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.of(t + "--"), button -> realBet = Math.max((realBet - 100) / 10, 1) * 10)
												 .dimensions(this.width / 2 - width * 2, height / 2 + getContainerHalfSize() + 8, width, 20).build()));
		buttons.add(addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.of(t + "-"), button -> realBet = Math.max((realBet - 10) / 10, 1) * 10)
												 .dimensions(this.width / 2 - width, height / 2 + getContainerHalfSize() + 8, width, 20).build()));
		buttons.add(addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.of(t + "+"), button -> realBet = realBet + 10)
												 .dimensions(this.width / 2, height / 2 + getContainerHalfSize() + 8, width, 20).build()));
		buttons.add(addDrawableChild(proceedButton = new ButtonWidget.Builder(Text.of(t + "++"), button -> realBet = realBet + 100)
												 .dimensions(this.width / 2 + width, height / 2 + getContainerHalfSize() + 8, width, 20).build()));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		time += delta;
		coinTimer -= delta / 5f;
		displayBalance = MathHelper.lerp(delta / 5f, displayBalance, (float)realBalance);
		if(realBet > realBalance && !outcomePending && spinning == 0)
			realBet = realBalance;
		displayBet = MathHelper.lerp(delta / 5f, displayBet, (float)realBet);
		if(Math.abs(displayBalance - realBalance) < 0.2)
			displayBalance = realBalance;
		if(Math.abs(displayBet - realBet) < 0.2)
			displayBet = realBet;
		if(scheduledCoins > 0 && coinTimer <= 0f)
		{
			coins.add(new Coin(new Vector2f(random.nextInt(65), -32), new Vector2f((random.nextFloat() - 0.5f) * 0.2f, 1 + random.nextFloat() * 0.5f)));
			scheduledCoins--;
			coinTimer = 1f / Math.max(scheduledCoins / 5f, 1f);
			if(client != null && client.player != null)
				client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.05f, 0.85f + random.nextFloat() * 0.3f);
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
		if(!completed && spinning == 0f && !outcomePending)
		{
			if(realBalance <= 0)
				onFail();
			else if(realBalance > targetBalance)
				onComplete();
		}
		else if((realBalance != displayBalance || scheduledCoins > 0) && nextDelay > 0)
			nextDelay = 20;
		
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
					case 1 -> realBet * 8;
					case 2 -> realBet * 16;
					case 3 -> realBet * 32;
					default -> 0;
				};
				realBalance += win;
				scheduledCoins += (int)Math.ceil(win / 5f);
				if(client != null && client.player != null)
					client.player.playSound(RESULT_SOUNDS[outcome[0] % RESULT_SOUNDS.length], 1f, 1f);
			}
			outcomePending = false;
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
		Text t = Text.of(String.valueOf((int)Math.ceil(displayBalance)));
		context.drawText(textRenderer, t, 81 - textRenderer.getWidth(t), 116, 0x00ff00, false);
		t = Text.of(String.valueOf((int)Math.ceil(displayBet)));
		context.drawText(textRenderer, t, 129 - textRenderer.getWidth(t), 116, 0x00ff00, false);
		matrices.push();
		matrices.translate(120, 30 + (int)leverOffset, 0);
		context.drawTexture(LEVER_TEX, 0, -(int)Math.max(leverOffset - 24, 0), 0, 0,
				4, Math.abs(24 - (int)leverOffset), 4, 24 - (int)leverOffset);
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
				if(offset < 0.5f && !sound[i - 1])
				{
					if(client != null && client.player != null)
						client.player.playSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f);
					sound[i - 1] = true;
				}
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
		if(!isAllowInput() || (spinning > 0f && !dragging))
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		int pieceX = width / 2 - getContainerHalfSize() + leverX + (int)leverOffset, pieceY = height / 2 - getContainerHalfSize() + leverY;
		if((mouseX > pieceX && mouseX < pieceX + 16 && mouseY > pieceY && mouseY < pieceY + 16) || dragging)
		{
			if(!dragging && client != null && client.player != null)
					client.player.playSound(SoundEvents.ITEM_CROSSBOW_LOADING_START.value(), 1f, 0.5f);
			dragging = true;
			if(deltaY > 0f)
				leverOffset = MathHelper.clamp(leverOffset + (float)deltaY, 0, 53);
			if(leverOffset > 32 && spinning <= 0)
			{
				spinning = 0.99f;
				realBalance -= realBet;
				sound[0] = sound[1] = sound[2] = false;
				if(client != null && client.player != null)
					client.player.playSound(SoundEvents.ITEM_CROSSBOW_LOADING_END.value(), 1f, 1f);
				pulls++;
			}
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput() || outcomePending)
			return super.mouseReleased(mouseX, mouseY, button);
		dragging = false;
		if(spinning > 0f)
		{
			if(pulls % 7 == 0 && pulls > 0)
			{
				int outcome = 0;
				float r = random.nextFloat();
				if(r < 0.55) //55%
					outcome = 1;
				else if(r < 0.99) //34%
					outcome = 2;
				else if(r <= 1) //1%
					outcome = 3;
				this.outcome[0] = this.outcome[1] = this.outcome[2] = outcome;
			}
			else
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
					else if(r <= 1) //15%
						outcome[i] = 3;
				}
			}
			outcomePending = true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	protected void onFail()
	{
		super.onFail();
		completed = true;
		buttons.forEach(i -> i.active = false);
	}
	
	@Override
	protected void onComplete()
	{
		super.onComplete();
		completed = true;
		buttons.forEach(i -> i.active = false);
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix, targetBalance);
	}
	
	@Override
	protected int getInstructionLines()
	{
		return 3;
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
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
