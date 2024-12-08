package absolutelyaya.captcha.component;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IPlayerComponent extends ComponentV3, AutoSyncedComponent
{
	void startCaptcha(String type, float difficulty);
	
	void finishCaptcha(boolean result, String type, float difficulty);
	
	float getLocalDifficulty();
	
	int getCurLives();
	
	void decrementLives();
}
