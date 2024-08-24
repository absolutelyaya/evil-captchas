package absolutelyaya.captcha.data;

import net.minecraft.util.Identifier;

import java.util.List;

public record SingleBoxCaptchaData(Identifier texture, float difficulty, int subdivisions, List<List<String>> values, List<String> prompts)
{

}
