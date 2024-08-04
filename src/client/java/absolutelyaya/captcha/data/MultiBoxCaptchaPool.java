package absolutelyaya.captcha.data;

import java.util.List;

public record MultiBoxCaptchaPool(String promptKey, float difficulty, List<String> textures)
{

}
