package com.codetaylor.mc.dropt.modules.dropt.rule.parser;

import com.codetaylor.mc.dropt.modules.dropt.rule.data.Rule;
import com.codetaylor.mc.dropt.modules.dropt.rule.data.RuleDrop;
import com.codetaylor.mc.dropt.modules.dropt.rule.data.RuleList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class ParserRuleDropItem
    implements IRuleListParser {

  @Override
  public void parse(
      RecipeItemParser parser, RuleList ruleList, Rule rule, ILogger logger
  ) {

    for (RuleDrop drop : rule.drops) {

      if (drop.item == null || drop.item.items == null) {
        return;
      }

      for (String itemString : drop.item.items) {
        ParseResult parse;

        try {
          parse = parser.parse(itemString);

        } catch (MalformedRecipeItemException e) {
          logger.error("Unable to parse item drop <" + itemString + "> in file: " + ruleList._filename, e);
          continue;
        }

        if ("ore".equals(parse.getDomain())) {
          NonNullList<ItemStack> ores = OreDictionary.getOres(parse.getPath());

          if (ores.isEmpty()) {
            logger.warn("No entries found for oreDict entry <" + "ore:" + parse.getPath() + "> in file: " + ruleList._filename);
          }

          for (ItemStack ore : ores) {

            if (ore.getItem().getHasSubtypes()) {
              ParserUtil.addSubItemsToList(ore.getItem(), drop.item._items);

            } else {
              drop.item._items.add(new ItemStack(ore.getItem(), 1, ore.getMetadata()));
            }
          }

        } else { // not an ore dict entry
          Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parse.getDomain(), parse.getPath()));

          if (item == null) {
            logger.error("Unable to find registered item <" + parse.toString() + "> in file: " + ruleList._filename);
            continue;
          }

          if (parse.getMeta() == OreDictionary.WILDCARD_VALUE) {

            if (!item.getHasSubtypes()) {
              logger.error("Wildcard used for item <" + parse.toString() + ">, but item has no subtypes: " + ruleList._filename);

            } else {
              ParserUtil.addSubItemsToList(item, drop.item._items);
            }

          } else {
            drop.item._items.add(new ItemStack(item, 1, parse.getMeta()));
          }
        }
      }
    }
  }
}