package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ButterflyCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.butterflies.";
	List<Butterfly> butterflies = new ArrayList<>();
	List<Butterfly> eaten = new ArrayList<>();
	float time;
	
	protected ButterflyCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		for (int i = 0; i < 8 + difficulty / 20f; i++)
		{
			Butterfly bf = new Butterfly(random.nextInt(10) + 1, random.nextFloat());
			bf.pos = new Vector2f(random.nextFloat() * getContainerHalfSize() * 2, random.nextFloat() * getContainerHalfSize() * 2);
			bf.movement = bf.targetMovement = new Vector2f(random.nextFloat(), random.nextFloat()).normalize();
			butterflies.add(bf);
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		time += delta / 2.5f;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(!isAllowInput())
			return;
		for (Butterfly bf : butterflies)
		{
			bf.pos = bf.pos.add(bf.movement.mul(1f + difficulty / 50f));
			bf.movement = bf.movement.lerp(bf.targetMovement, Math.min(0.1f + difficulty / 200f, 0.25f)).normalize();
			bf.yaw = (float)Math.toDegrees(Math.atan2(bf.movement.y, bf.movement.x)) + 90;
			if(random.nextFloat() < 0.01f)
				bf.targetMovement = new Vector2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f).normalize();
			if(bf.pos.x - 4 > getContainerHalfSize() * 2 || bf.pos.x + 4 < 0 ||
					   bf.pos.y - 4 > getContainerHalfSize() * 2 || bf.pos.y + 4 < 0)
				bf.targetMovement = bf.movement = new Vector2f(bf.pos).mul(-1).add(getContainerHalfSize(), getContainerHalfSize()).normalize();
		}
		for (Butterfly bf : eaten)
			butterflies.remove(bf);
		eaten.clear();
		if(butterflies.isEmpty())
			onComplete();
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		matrices.push();
		matrices.translate(0f, 0f, -25f);
		super.drawContainer(context, matrices);
		matrices.pop();
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0);
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		for (Butterfly bf : butterflies)
		{
			matrices.push();
			matrices.translate(bf.pos.x, bf.pos.y, 5f);
			matrices.scale(1.5f, 1.5f, 1.5f);
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(bf.yaw));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)Math.sin(time + bf.wingOffset) * 50f));
			context.drawTexture(CAPTCHA.texIdentifier("gui/butterfly/butterfly" + bf.tex), 0, -6, 5, 0, 6, 11, 11, 11);
			matrices.pop();
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(bf.yaw));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)Math.sin(time + bf.wingOffset) * -50f));
			context.drawTexture(CAPTCHA.texIdentifier("gui/butterfly/butterfly" + bf.tex), -5, -6, 0, 0, 6, 11, 11, 11);
			matrices.pop();
			matrices.pop();
		}
		context.disableScissor();
		matrices.pop();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput())
			return false;
		for(Butterfly bf : butterflies)
		{
			int x = (int)(width / 2 - getContainerHalfSize() + bf.pos.x) - 8, y = (int)(height / 2 - getContainerHalfSize() + bf.pos.y - 8);
			{
				if(mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16)
				{
					eaten.add(bf);
					if(client != null && client.player != null)
						client.player.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1f, 0.85f + random.nextFloat() * 0.3f);
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
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
	
	static class Butterfly
	{
		public final int tex;
		public final float wingOffset;
		public Vector2f pos, movement, targetMovement;
		public float yaw;
		
		public Butterfly(int tex, float wingOffset)
		{
			this.tex = tex;
			this.wingOffset = wingOffset;
		}
	}
}
