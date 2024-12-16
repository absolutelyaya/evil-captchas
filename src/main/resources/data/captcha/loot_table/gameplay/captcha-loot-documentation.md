# Captcha Reward Loot
Change the loot table `captcha:gameplay/captcha/reward.json` using a Datapack; it's empty per default.<br>
When a Player completes a Captcha successfully, they'll be given items from that loot table.
## loot type
Evil Captchas adds the Loot Type `captcha:reward`. It won't work properly in non-captcha uses, like entity drops and whatever, since it requires specific data from the completed captcha; namely the captchas type and difficulty.
## loot conditions
### captcha:difficulty
Fulfilled when the solved captchas difficulty is equal to or higher than the `difficulty` argument (float)<br>
This example will give players a gold block for completing a captcha with a difficulty equal to or higher than 100:
```
{
  "type": "captcha:reward",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:gold_block"
        }
      ],
      "conditions": [
        {
          "condition": "captcha:difficulty",
          "difficulty": 100
        }
      ]
    }
  ]
}
```
### captcha:type
Fulfilled when the solved captchas type is equal to the `type` argument (string)<br>
This example will give players a diamond for completing a captcha of the type `single-boxes`:
```
{
  "type": "captcha:reward",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:diamond"
        }
      ],
      "conditions": [
        {
          "condition": "captcha:type",
          "type": "single-boxes"
        }
      ]
    }
  ]
}
```
<br><br>
And that's already all there is. The rest is same as in vanilla. Check the file `captcha-example.json` for a combination of both examples.