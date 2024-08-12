package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.data.ComprehensionAdjectiveData;
import absolutelyaya.captcha.data.ComprehensionObjectData;
import absolutelyaya.captcha.data.ComprehensionTestManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class ComprehensionTestCaptchaScreen extends AbstractCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.comprehension.";
	static final Identifier GLOW_TEX = CAPTCHA.texIdentifier("gui/comprehension/glow");
	protected final List<ObjectInstance> objects = new ArrayList<>();
	protected Pair<ComprehensionAdjectiveData, ComprehensionObjectData> prompt;
	
	protected ComprehensionTestCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		for (int i = 0; i < 7; i++)
		{
			ObjectInstance object = new ObjectInstance(
					(int)Math.min((random.nextFloat() - 0.5f) * 2f * getContainerHalfSize(), getContainerHalfSize() * 2 - 16),
					(int)Math.min((random.nextFloat() - 0.5f) * 2f * getContainerHalfSize(), getContainerHalfSize() * 2 - 16),
					ComprehensionTestManager.getRandomObject(difficulty));
			for (int j = 0; j < 8; j++)
			{
				ComprehensionAdjectiveData colorAdj = ComprehensionTestManager.getRandomColor(difficulty);
				if(!isObjectPresent(object.object, colorAdj))
				{
					object.adjectives.add(colorAdj);
					break;
				}
			}
			if(random.nextFloat() < 0.7f)
			{
				for (int j = 0; j < 8; j++)
				{
					ComprehensionAdjectiveData adjective = ComprehensionTestManager.getRandomNonColorAdjective(difficulty);
					if(!isObjectPresent(object.object, adjective))
					{
						object.adjectives.add(adjective);
						break;
					}
				}
			}
			if(!isObjectPresent(object) && object.hasColor())
				objects.add(object);
		}
		ObjectInstance object = objects.get(random.nextInt(objects.size()));
		prompt = new Pair<>(object.adjectives.get(random.nextInt(object.adjectives.size())), object.object);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		RenderSystem.enableBlend();
		context.enableScissor(width / 2 - getContainerHalfSize(), height / 2 - getContainerHalfSize(),
				(width / 2) + getContainerHalfSize(), (height / 2) + getContainerHalfSize());
		matrices.push();
		for (ObjectInstance obj : objects)
		{
			if(!isAllowInput() && (!obj.adjectives.contains(prompt.getLeft()) || !obj.object.equals(prompt.getRight())))
				continue;
			matrices.push();
			int size = (int)(16f * obj.getScale());
			int tint = obj.getColor().tint();
			matrices.translate(obj.x, obj.y, 0f);
			if(obj.isGlowing())
			{
				int glowSize = (int)(18f * obj.getScale());
				int glowColor = obj.getGlowColor();
				RenderSystem.setShaderColor(ColorHelper.Argb.getRed(glowColor) / 255f, ColorHelper.Argb.getGreen(glowColor) / 255f, ColorHelper.Argb.getBlue(glowColor) / 255f, 1f);
				context.drawTexture(GLOW_TEX, -glowSize / 2, -glowSize / 2, 0, 0, glowSize, glowSize, glowSize, glowSize);
			}
			if(obj.isShaking())
				matrices.translate((int)((random.nextFloat() - 0.5f) * 2.5f), (int)((random.nextFloat() - 0.5f) * 2.5f), 0);
			RenderSystem.setShaderColor(ColorHelper.Argb.getRed(tint) / 255f, ColorHelper.Argb.getGreen(tint) / 255f, ColorHelper.Argb.getBlue(tint) / 255f, 1f);
			context.drawTexture(CAPTCHA.texIdentifier(obj.object.texture()), -size / 2, -size / 2, 0, 0, size, size, size, size);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			matrices.pop();
		}
		matrices.pop();
		context.disableScissor();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!isAllowInput())
			return false;
		boolean hit = false;
		for(ObjectInstance obj : objects)
		{
			int x = width / 2 + obj.x - 8, y = height / 2 + obj.y - 8;
			{
				if(mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16)
				{
					hit = true;
					if(checkSolution(obj))
					{
						onComplete();
						return true;
					}
				}
			}
		}
		if(hit)
		{
			onFail();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	protected boolean checkSolution(ObjectInstance obj)
	{
		return obj.object.equals(prompt.getRight()) && obj.adjectives.contains(prompt.getLeft());
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(TRANSLATION_KEY + "instruction.simple", Text.translatable(prompt.getLeft().name()), Text.translatable(prompt.getRight().name()));
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	boolean isObjectPresent(ObjectInstance instance)
	{
		ComprehensionObjectData objectData = instance.object;
		List<ComprehensionAdjectiveData> instanceAdjectives = instance.adjectives;
		for (ObjectInstance i : objects)
		{
			if(i.object.equals(objectData))
				for (ComprehensionAdjectiveData adj : instanceAdjectives)
					if(i.adjectives.contains(adj))
						return true;
		}
		return false;
	}
	
	boolean isObjectPresent(ComprehensionObjectData objectData, ComprehensionAdjectiveData colorAdj)
	{
		for (ObjectInstance i : objects)
		{
			if(i.object.equals(objectData) && i.adjectives.contains(colorAdj))
				return true;
		}
		return false;
	}
	
	@Override
	protected boolean isHasProceedButton()
	{
		return false;
	}
	
	protected static class ObjectInstance
	{
		public List<ComprehensionAdjectiveData> adjectives = new ArrayList<>();
		public ComprehensionObjectData object;
		public int x, y;
		
		public ObjectInstance(int x, int y, ComprehensionObjectData object)
		{
			this.x = x;
			this.y = y;
			this.object = object;
		}
		
		public boolean hasColor()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(adjective.isColor())
					return true;
			return false;
		}
		
		public ComprehensionAdjectiveData getColor()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(adjective.isColor())
					return adjective;
			return null;
		}
		
		public float getScale()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(Math.abs(adjective.scale() - 1f) > 0.1f)
					return adjective.scale();
			return 1f;
		}
		
		public boolean isShaking()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(adjective.shaking())
					return true;
			return false;
		}
		
		public boolean isGlowing()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(adjective.glowColor() != -1)
					return true;
			return false;
		}
		
		public int getGlowColor()
		{
			for (ComprehensionAdjectiveData adjective : adjectives)
				if(adjective.glowColor() != -1)
					return adjective.glowColor();
			return -1;
		}
	}
}
