package absolutelyaya.captcha.component;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public interface IConfigComponent extends ComponentV3, AutoSyncedComponent, ServerTickingComponent
{
	void setCurDifficulty(float val);
	
	float getCurDifficulty();
}
