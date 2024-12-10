package absolutelyaya.captcha.component;

import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IPlayerComponent extends ComponentV3, AutoSyncedComponent
{
	void startCaptcha(String type, float difficulty);
	
	void finishCaptcha(boolean result, String type, float difficulty);
	
	float getLocalDifficulty();
	
	void setLocalDifficulty(float val);
	
	void resetLocalDifficulty();
	
	int getCurLives();
	
	void decrementLives();
	
	void storeLootContainer(BlockPos pos);
}
