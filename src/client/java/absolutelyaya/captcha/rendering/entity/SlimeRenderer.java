package absolutelyaya.captcha.rendering.entity;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.CAPTCHAClient;
import absolutelyaya.captcha.entity.SlimerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SlimeRenderer extends MobEntityRenderer<SlimerEntity, SlimeModel>
{
	public SlimeRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new SlimeModel(ctx.getPart(CAPTCHAClient.SLIMER_LAYER)), 0.2f);
	}
	
	@Override
	public Identifier getTexture(SlimerEntity entity)
	{
		return CAPTCHA.texIdentifier("entity/slimer");
	}
	
	@Nullable
	@Override
	protected RenderLayer getRenderLayer(SlimerEntity entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		return RenderLayer.getEntityTranslucent(getTexture(entity));
	}
}
