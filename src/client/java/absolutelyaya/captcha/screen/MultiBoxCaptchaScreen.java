package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.data.MultiBoxCaptchaPool;
import absolutelyaya.captcha.data.MultiBoxCaptchaPoolManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MultiBoxCaptchaScreen extends AbstractCaptchaScreen
{
	final static String TRANSLATION_KEY = "screen.captcha.boxes.multi.";
	final MultiBoxCaptchaPool pool;
	final List<String> allPossibleTextures = new ArrayList<>();
	final int subdivisions;
	final Box[][] boxes;
	protected List<ButtonWidget> boxButtons = new ArrayList<>();
	
	protected MultiBoxCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
		pool = MultiBoxCaptchaPoolManager.getRandom(difficulty);
		boolean canFinish = false, finished = false;
		while(!finished)
		{
			MultiBoxCaptchaPoolManager.getRandom(difficulty).textures().forEach(t -> {
				if(!(allPossibleTextures.contains(t) || pool.textures().contains(t)))
					allPossibleTextures.add(t);
			});
			if(!canFinish)
			{
				for (String tex : allPossibleTextures)
				{
					if(!pool.textures().contains(tex))
					{
						canFinish = true;
						break;
					}
				}
			}
			else if(random.nextFloat() > 0.4 - (difficulty / 200f))
				finished = true;
		}
		subdivisions = Math.min(4 + (int)(difficulty / 40f), 6);
		boxes = new Box[subdivisions][subdivisions];
		populateAllBoxes();
	}
	
	@Override
	protected void init()
	{
		super.init();
		int startX = width / 2 - getContainerHalfSize(), startY = height / 2 - getContainerHalfSize();
		int size = getContainerHalfSize() * 2;
		int boxSize = size / subdivisions;
		for (int x = 0; x < subdivisions; x++)
		{
			for (int y = 0; y < subdivisions; y++)
			{
				int finalX = x;
				int finalY = y;
				boxButtons.add(addDrawableChild(new ButtonWidget.Builder(Text.empty(), button -> {
					if(isAllowInput())
						onClickBox(finalX, finalY);
				}).dimensions(startX + x * boxSize, startY + y * boxSize, boxSize, boxSize).build()));
			}
		}
	}
	
	void populateAllBoxes()
	{
		for (int x = 0; x < subdivisions; x++)
			for (int y = 0; y < subdivisions; y++)
				randomizeBox(x, y);
		
		for (int x = 0; x < subdivisions; x++)
			for (int y = 0; y < subdivisions; y++)
				if(pool.textures().contains(boxes[x][y].value))
					return;
		populateAllBoxes();
	}
	
	void randomizeBox(int x, int y)
	{
		if (boxes[x][y] == null)
			boxes[x][y] = new Box();
		if(random.nextFloat() < 0.33f)
			boxes[x][y].value = pool.textures().get(random.nextInt(pool.textures().size()));
		else
			boxes[x][y].value = allPossibleTextures.get(random.nextInt(allPossibleTextures.size()));
	}
	
	void onClickBox(int x, int y)
	{
		if (pool.textures().contains(boxes[x][y].value))
			boxes[x][y].fading = true;
		else
		{
			boxes[x][y].fail = true;
			onFail();
		}
	}
	
	@Override
	protected void onClickedProceed()
	{
		super.onClickedProceed();
		for (int x = 0; x < subdivisions; x++)
		{
			for (int y = 0; y < subdivisions; y++)
			{
				if(pool.textures().contains(boxes[x][y].value))
				{
					onFail();
					return;
				}
			}
		}
		onComplete();
	}
	
	@Override
	protected void onFail()
	{
		for (int x = 0; x < subdivisions; x++)
			for (int y = 0; y < subdivisions; y++)
				if(pool.textures().contains(boxes[x][y].value))
					boxes[x][y].fail = true;
		super.onFail();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		float fadeSpeed = delta / (2.5f + difficulty / 5f);
		for (int x = 0; x < subdivisions; x++)
		{
			for (int y = 0; y < subdivisions; y++)
			{
				Box box = boxes[x][y];
				if(box.fading)
				{
					if(box.alpha > 0f)
						box.alpha = Math.max(box.alpha - fadeSpeed, 0f);
					else if(box.alpha == 0f)
					{
						randomizeBox(x, y);
						box.fading = false;
					}
				}
				else if(boxes[x][y].alpha < 1f)
					boxes[x][y].alpha = Math.min(boxes[x][y].alpha + fadeSpeed, 1f);
			}
		}
	}
	
	@Override
	public void drawContainer(DrawContext context, MatrixStack matrices)
	{
		super.drawContainer(context, matrices);
		matrices.push();
		matrices.translate(-getContainerHalfSize(), -getContainerHalfSize(), 0);
		int size = getContainerHalfSize() * 2;
		context.fill(0, 0, size, size, 0xff554488);
		int boxSize = size / subdivisions;
		RenderSystem.enableBlend();
		for (int x = 0; x < subdivisions; x++)
		{
			for (int y = 0; y < subdivisions; y++)
			{
				Identifier texture = boxes[x][y].fail ? CAPTCHA.texIdentifier("minecraft:item/barrier") : CAPTCHA.texIdentifier(boxes[x][y].value);
				RenderSystem.setShaderColor(1f, 1f, 1f, boxes[x][y].alpha);
				context.drawTexture(texture, x * boxSize, y * boxSize, 0, 0, boxSize, boxSize, boxSize, boxSize);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			}
		}
		matrices.pop();
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(prefix, Text.translatable(pool.promptKey()));
	}
	
	@Override
	protected int getInstructionLines()
	{
		return 2;
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
	
	static private class Box
	{
		public String value;
		public float alpha = 1f;
		public boolean fading = false, fail = false;
	}
}
