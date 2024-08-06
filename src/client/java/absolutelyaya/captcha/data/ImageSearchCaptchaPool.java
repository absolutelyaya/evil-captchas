package absolutelyaya.captcha.data;

import java.util.List;
import java.util.Map;

public record ImageSearchCaptchaPool(float difficulty, List<String> backgrounds, List<String> overlays, Map<String, String> objects)
{
}
