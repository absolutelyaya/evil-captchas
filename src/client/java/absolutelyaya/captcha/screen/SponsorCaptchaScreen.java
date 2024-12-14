package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.data.InvoluntaryAddon;
import absolutelyaya.captcha.registry.SoundRegistry;
import absolutelyaya.captcha.screen.elements.SpinningPigElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SponsorCaptchaScreen extends AbstractCaptchaScreen
{
	static final Identifier TEXTURE = CAPTCHA.identifier("textures/gui/sponsor/");
	static final String TYPE = "sponsor", TRANSLATION_KEY = "screen.captcha.sponsor.";
	static final String[] ALL_SPONSORS = new String[]{"spinning-pig", "live-reaction", "winter-wonderland", "custom-cursor"};
	String sponsor;
	int timer;
	
	protected SponsorCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		List<String> options = new ArrayList<>();
		if(MinecraftClient.getInstance().player instanceof PlayerEntity player)
		{
			IPlayerComponent comp = CaptchaComponents.PLAYER.get(player);
			for (String option : ALL_SPONSORS)
			{
				InvoluntaryAddon addon = comp.getAddon(option);
				if(addon == null || addon.isAllowsMultiple())
					options.add(option);
			}
		}
		else
			options = List.of(ALL_SPONSORS);
		sponsor = options.get(random.nextInt(options.size()));
		timer = Math.round(10 + random.nextFloat() * 5 + Math.min(difficulty / 1000, 30)) * 20;
	}
	
	@Override
	public String getType()
	{
		return TYPE;
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
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		context.drawTexture(TEXTURE.withSuffixedPath(sponsor + ".png"), -getContainerHalfSize(), -getContainerHalfSize(), 0, 0,
				getContainerHalfSize() * 2, getContainerHalfSize() * 2,
				getContainerHalfSize() * 2, getContainerHalfSize() * 2);
		float delta = client.getRenderTickCounter().getTickDelta(true);
		if(sponsor.equals("spinning-pig"))
		{
			matrices.push();
			matrices.translate(0, 38, 0);
			SpinningPigElement.render(context, delta + client.world.getTime());
			matrices.pop();
		}
		matrices.push();
		matrices.translate(getContainerHalfSize() - 1.25, getContainerHalfSize() - 1.25, 100);
		Text t;
		if(timer > 0)
			t = Text.of(String.valueOf(timer / 20));
		else
			t = Text.translatable("screen.captcha.sponsor.skip");
		int width = textRenderer.getWidth(t);
		context.fill(-width - 7, -14, -2, -2, 0x88000000);
		context.drawBorder(-width - 7, -15, width + 6, 14, timer > 0 ? 0xff888888 : 0xffffffff);
		context.drawText(textRenderer, t, -width - 4, - 12,  timer > 0 ? 0xff888888 : 0xffffffff, false);
		matrices.pop();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(super.mouseClicked(mouseX, mouseY, button))
			return true;
		if(mouseX > width / 2f - getContainerHalfSize() && mouseY > height / 2f - getContainerHalfSize() &&
			mouseX < width / 2f + getContainerHalfSize() && mouseY < height / 2f + getContainerHalfSize())
		{
			playerData.addInvoluntaryAddon(new InvoluntaryAddon(sponsor,
					System.currentTimeMillis() + (int)(180f + random.nextFloat() * 120f + random.nextFloat() * difficulty / 100f) * 1000,
					random.nextFloat(), random.nextFloat()));
			if(client != null && client.player != null) //technically a fail, *but* the consequences are worse than needing to do another captcha lmao
				client.player.playSound(SoundRegistry.WRONG_BUZZER, 1f, 1f);
			close();
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		timer--;
	}
}
