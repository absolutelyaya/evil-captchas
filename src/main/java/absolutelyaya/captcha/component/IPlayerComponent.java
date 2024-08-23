package absolutelyaya.captcha.component;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IPlayerComponent extends ComponentV3, AutoSyncedComponent
{
	void startCaptcha();
	
	void finishCaptcha(boolean result);
	
	float getLocalDifficulty();
	
	int getCurLives();
	
	void decrementLives();
}
