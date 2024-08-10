package absolutelyaya.captcha.data;

import net.minecraft.util.Identifier;

import java.util.List;

public record AmongusPool(float difficulty, List<Identifier> crewmates, List<Identifier> impostors)
{

}
