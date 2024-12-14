package absolutelyaya.captcha.component;

import absolutelyaya.captcha.data.InvoluntaryAddon;
import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.List;
import java.util.Optional;

public interface IPlayerComponent extends ComponentV3, AutoSyncedComponent, CommonTickingComponent
{
	void startCaptcha(String type, float difficulty);
	
	void finishCaptcha(boolean result, String type, float difficulty);
	
	float getLocalDifficulty();
	
	void setLocalDifficulty(float val);
	
	void resetLocalDifficulty();
	
	int getCurLives();
	
	void decrementLives();
	
	void storeLootContainer(BlockPos pos);
	
	void addInvoluntaryAddon(InvoluntaryAddon addon);
	
	void addInvoluntaryAddon(String type);
	
	void removeInvoluntaryAddon(InvoluntaryAddon addon);
	
	List<InvoluntaryAddon> getAddons();
	
	boolean hasAddon(String type);
	
	InvoluntaryAddon getAddon(String type);
}
