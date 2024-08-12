package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.data.ComprehensionAdjectiveData;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedComprehensionTestCaptchaScreen extends ComprehensionTestCaptchaScreen
{
	Pair<ComprehensionAdjectiveData, ObjectInstance> secondaryPrompt;
	
	protected AdvancedComprehensionTestCaptchaScreen(float difficulty, String reason)
	{
		super(difficulty, reason);
		List<Pair<ComprehensionAdjectiveData, ObjectInstance>> candidates = new ArrayList<>();
		for (ObjectInstance object : objects)
		{
			if(object.adjectives.size() < 2 || object.object.equals(prompt.getRight()))
				continue;
			for (int i = 0; i < object.adjectives.size(); i++)
			{
				if(!object.adjectives.get(i).isColor())
				{
					candidates.add(new Pair<>(object.adjectives.get(i), object));
					break;
				}
			}
		}
		secondaryPrompt = candidates.get(random.nextInt(candidates.size()));
		boolean present = false;
		for (ObjectInstance instance : objects)
		{
			if(instance.object.equals(prompt.getRight()) && instance.getColor().equals(secondaryPrompt.getRight().getColor()))
			{
				present = true;
				break;
			}
		}
		if(!present)
		{
			ObjectInstance target = new ObjectInstance(
					(int)Math.min((random.nextFloat() - 0.5f) * 2f * getContainerHalfSize(), getContainerHalfSize() * 2 - 16),
					(int)Math.min((random.nextFloat() - 0.5f) * 2f * getContainerHalfSize(), getContainerHalfSize() * 2 - 16),
					prompt.getRight());
			target.adjectives.add(secondaryPrompt.getRight().getColor());
			objects.add(target);
		}
		prompt = new Pair<>(secondaryPrompt.getRight().getColor(), prompt.getRight());
	}
	
	@Override
	protected boolean checkSolution(ObjectInstance obj)
	{
		return obj.object.equals(prompt.getRight()) && obj.getColor().equals(secondaryPrompt.getRight().getColor());
	}
	
	@Override
	protected Text getInstructionText(int i, String prefix)
	{
		return Text.translatable(TRANSLATION_KEY + "instruction.advanced", Text.translatable(prompt.getRight().name()),
				Text.translatable(secondaryPrompt.getLeft().name()), Text.translatable(secondaryPrompt.getRight().object.name()));
	}
}
