package absolutelyaya.captcha.component;

import absolutelyaya.captcha.CAPTCHA;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;

public class CaptchaComponents implements EntityComponentInitializer, ScoreboardComponentInitializer
{
	public static final ComponentKey<IConfigComponent> CONFIG = ComponentRegistry.getOrCreate(CAPTCHA.identifier("config"), IConfigComponent.class);
	public static final ComponentKey<IPlayerComponent> PLAYER = ComponentRegistry.getOrCreate(CAPTCHA.identifier("player"), IPlayerComponent.class);
	
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerForPlayers(PLAYER, PlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
	
	@Override
	public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry)
	{
		registry.registerScoreboardComponent(CONFIG, ConfigComponent::new);
	}
}
