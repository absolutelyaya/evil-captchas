# Don't you love captchas ?
This mod adds 14 silly captchas that appear at inopportune moments; for example when opening a loot chest. They get progressively more difficult, with most not having a max difficulty; so if you play long enough, they'll get almost impossible at some point.

This mod is obviously not serious btw. Not a safety measure, just a small funi

## As Seen on TV! (<- actually just youtube)
[insert video here]

## Features
### Captcha-like minigames (surprise!)
<details>
<summary>Spoilers! It's much more fun if you don't know what's coming for you :)</summary>

- Classic Single Image Boxes
    - Shows a single cohesive image; "click all boxes containing X"
    - Difficulty increases the accuracy required to pass
    - Difficulty also influences the Image Pool
- Classic Multi Image Boxes
    - Shows multible boxes with small images; "click all boxes containing X"
    - Difficulty increases the amount of boxes and slows down the fading animation
    - Difficulty also influences the Image Pool
- Classic puzzle slide
    - Shows a cohesive image with one small part of it on the side. You need to slide it into its rightful place
    - Difficulty influences the accuracy required to pass
- Classic Wonky Text
    - You need to transcribe the increasingly obscured letters shown to you
    - Difficulty adds more and more layers of.. obscuration ? dunno if that's a word
- Classic Image Search
    - You're shown an array of Objects scattered over a background image; You need to click a specific one to pass.
    - While this sounds similar to Comprehension tests (next bullet point), there's no Adjectives involved here
- Simple Comprehension Test
    - You're shown several Objects with different Attributes; you then need to (for example) "Click the Green Apple", or "Click the Nervous Delta"
    - Difficulty influences the Object and Adjective Pools
- ADVANCED Comprehension Test
    - You're once again shown several Objects with different Attributes; the prompts are a bit more complicated though (for example) "Click the Circle in the color of the Huge Mystery"
    - Difficulty influences the Object and Adjective Pools
    - An incident of me not getting past one of these irl was probably the main inspiration behind this entire mod. It's a deep seated Trauma. Yes, this Mod is Vent Art (<- not really (I want to clarify that this is a joke))
- Math
    - Solve a Math Equation to pass (yea, that's it)
    - Difficulty influences how big the numbers are and what Operators may be used
    - Caution: Order of Operations is taken into account
- Wimmelbild
    - A harder version of Classic Image Search; an absurd amount of Objects is shown, making it much more difficult to find the right one
    - Wimmelbild is german and can be translated to "Busy Picture". It's that type of game where you gotta find a bunch of stuff in images, usually to obtain an item and ultimately solve a puzzle
    - Difficulty increases the amount of objects further
- Rorschach Test (now we're getting into the sillies)
    - What do you see in this image ? How does it make you feel ? Maybe you'll find an answer in these random ink splodges
    - Not influenced by difficulty
- Gambling Addiction
    - You need to get your balance above a given quota by playing on a Slot Machine
    - You can in-//decrease your bet as you wish to possibly WIN BIG!! or lose it all (much more likely)
    - Difficulty increases the Quota
- AMONGUS
    - You're shown an array of Images; one of them is not like the others.. it must be up to something! THROW IT INTO SPACE
    - Difficulty influences the Image Pool
- Wizard
    - catch it
    - **quick**
    - Difficulty influences the Wizards might
- Butterflies!
    - A bunch of Butterflies are set loose on your screen; click all of them
    - Difficulty influences how fast and erratic they move

I have some more ideas as well, those were too ambitious for a quick joke side project though. Maybe I'll add them in the future
There is a Trick to some of them

</details>

There is Global and Local Difficulty; The Global Difficulty is shared between all players. When a Player passes a Captcha, their Local Difficulty increases. The Final Difficulty is calculated using `global + local`

## Configurable Evil
- Lethal (default: false)
    - You have a limited amount of tries; failing too many Captchas kills you.
- Explosive (default: false)
    - You don't just die, you explode (it's a bit of a big explosion, be vary of that)
    - I'd recommend only enabling this in Worlds you don't intend on keeping, or are fine with them
    - Doesn't have any effect if `Lethal` is disabled being destroyed a little
- Validation Expiration (default: true)
    - Triggers a Captcha when there wasn't one for a certain random range of time
- Not Easy (default: false)
    - Makes it so that Captchas that can't be failed or are very easy, won't appear
- Lives (default: 3)
    - The Amount of Tries you have to do Captchas before you die
    - Doesn't have any effect if `Lethal` is disabled
- Expiration Delay Min (default: 120)
    - The minimum Amount of Seconds before a new random Captcha is triggered
    - Doesn't have any effect if `Validation Expiration` is disabled
- Expiration Delay Range (default: 120)
    - The random Range of Seconds before a new random Captcha is triggered
    - To better explain it, the delay before a new captcha is triggered is calculated by `min + random(range)`
    - Doesn't have any effect if `Validation Expiration` is disabled
- Constant Increase Rate (default: 0.002)
    - The Global Difficulty is increased by this amount every Second. I wouldn't recommend setting much higher numbers than the default, stuff escalates VERY quickly
    - If you don't want the difficulty ot increase over time, set this to 0
        - Passing Captchas will still increase the local difficulty

## What Triggers Captchas ?
<details>
<summary>Spoiler</summary>

- Joining a World
- Respawning
- Opening a Loot Chest that hasn't been opened before
- Mining Ores (chance based on Rarity of the Ore)
- Validation Expiration (if enabled)

</details>

## Datapack support
You can make your own Captcha Variants using Datapacks. If you want to add unique images, you'll also need a Resourcepack tho

# Other info
- I largely made this in a week as a joke, so it wasn't very thuroughly tested. There may be bugs; please report Issues on github
- uuh;, have a nice day