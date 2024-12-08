package absolutelyaya.captcha.config;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.yayconfig.config.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ServerConfig extends Config
{
	static final Identifier BG_TEX = Identifier.of("textures/block/dirt.png");
	public final BooleanEntry lethal = new BooleanEntry("lethal", false);
	public final BooleanEntry explosive = new BooleanEntry("explosive", false);
	public final BooleanEntry validationExpiration = new BooleanEntry("validation_expiration", false);
	public final IntegerEntry expirationDelayMin = new IntegerEntry("expiration_delay_min", 120);
	public final IntegerEntry expirationDelayMax = new IntegerEntry("expiration_delay_max", 120);
	public final BooleanEntry notEasy = new BooleanEntry("not_easy", false);
	public final IntegerEntry lives = new IntegerEntry("lives", 3);
	public final IntegerEntry constantIncreaseInterval = new IntegerEntry("constant_increase_interval", 0);
	public final FloatEntry constantIncreaseRate = new FloatEntry("constant_increase_rate", 0.0005f);
	public final BooleanEntry loginCaptcha = new BooleanEntry("login_captcha", true);
	public final BooleanEntry respawnCaptcha = new BooleanEntry("respawn_captcha", true);
	public final BooleanEntry miningCaptcha = new BooleanEntry("mining_captcha", true);
	public final BooleanEntry lootCaptcha = new BooleanEntry("loot_captcha", true);
	
	public ServerConfig()
	{
		super(CAPTCHA.identifier("world"));
		addEntry(new Comment(" ## ############################# ##  #"));
		addEntry(new Comment("     Welcome to Config Zone"));
		addEntry(new Comment(" ## ############################# ##  #"));
		addEntry(lethal);
		addEntry(lives);
		addEntry(explosive);
		addEntry(validationExpiration);
		addEntry(expirationDelayMin);
		addEntry(expirationDelayMax);
		addEntry(notEasy);
		addEntry(constantIncreaseInterval);
		addEntry(constantIncreaseRate);
		
		addEntry(loginCaptcha);
		addEntry(respawnCaptcha);
		addEntry(miningCaptcha);
		addEntry(lootCaptcha);
	}
	
	@Override
	protected String getFileName()
	{
		return "world.properties";
	}
	
	@Override
	public Text getTitle()
	{
		return Text.translatable("screen.captcha.config.title");
	}
	
	@Override
	public Identifier getBackgroundTexture()
	{
		return BG_TEX;
	}
	
	Identifier getIconPath(String name)
	{
		return CAPTCHA.identifier("textures/gui/rule_icons/" + name + ".png");
	}
}
