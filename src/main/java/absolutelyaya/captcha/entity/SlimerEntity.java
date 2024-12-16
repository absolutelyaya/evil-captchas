package absolutelyaya.captcha.entity;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class SlimerEntity extends MobEntity
{
	public AnimationState hopAnimationState = new AnimationState();
	public AnimationState splashAnimationState = new AnimationState();
	
	public SlimerEntity(EntityType<? extends MobEntity> type, World world)
	{
		super(type, world);
		hopAnimationState.start(-100);
	}
	
	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
	}
	
	public void hop()
	{
		if(hopAnimationState.isRunning())
			hopAnimationState.stop();
		hopAnimationState.start(age);
	}
	
	public void splash()
	{
		if(hopAnimationState.isRunning())
			hopAnimationState.stop();
		splashAnimationState.start(age);
	}
}
