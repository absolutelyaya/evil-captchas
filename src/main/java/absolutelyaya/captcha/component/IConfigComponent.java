package absolutelyaya.captcha.component;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public interface IConfigComponent extends ComponentV3, AutoSyncedComponent, ServerTickingComponent
{
	void setLethal(boolean b);
	
	boolean isLethal();
	
	void setExplosive(boolean b);
	
	boolean isExplosive();
	
	void setLives(int val);
	
	int getLives();
	
	void setCurDifficulty(float val);
	
	float getCurDifficulty();
	
	void setConstantIncreaseRate(float val);
	
	float getConstantIncreaseRate();
	
	void setValidationExpiration(boolean b);
	
	boolean isValidationExpiration();
	
	void setMinExpirationDelay(int val);
	
	int getMinExpirationDelay();
	
	void setMaxExpirationDelay(int val);
	
	int getMaxExpirationDelay();
	
	void setNotEasy(boolean b);
	
	boolean isNotEasy();
}
