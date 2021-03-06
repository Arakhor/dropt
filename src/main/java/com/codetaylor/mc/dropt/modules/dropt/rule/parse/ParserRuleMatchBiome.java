package com.codetaylor.mc.dropt.modules.dropt.rule.parse;

import com.codetaylor.mc.dropt.modules.dropt.rule.log.ILogger;
import com.codetaylor.mc.dropt.modules.dropt.rule.log.LogFileWrapper;
import com.codetaylor.mc.dropt.modules.dropt.rule.data.Rule;
import com.codetaylor.mc.dropt.modules.dropt.rule.data.RuleList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ParserRuleMatchBiome
    implements IRuleListParser {

  @Override
  public void parse(
      RecipeItemParser parser, RuleList ruleList, Rule rule, ILogger logger, LogFileWrapper logFileWrapper
  ) {

    if (rule.match == null) {

      if (rule.debug) {
        logFileWrapper.debug("[PARSE] Match object not defined, skipped parsing biome match");
      }
      return;
    }

    if (rule.match.biomes == null) {

      if (rule.debug) {
        logFileWrapper.debug("[PARSE] Match object not defined, skipped parsing biome match");
      }
      return;
    }

    for (String id : rule.match.biomes.ids) {
      ParseResult parse;

      try {
        parse = parser.parse(id);

      } catch (MalformedRecipeItemException e) {
        logger.error("[PARSE] Unable to parse biome <" + id + "> in file: " + ruleList._filename, e);
        continue;
      }

      if (rule.debug) {
        logFileWrapper.debug("[PARSE] Parsed biome match: " + parse);
      }

      Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(parse.getDomain(), parse.getPath()));

      if (biome == null) {
        logger.error("[PARSE] Unable to find registered biome: " + parse.toString());
        continue;
      }

      if (rule.debug) {
        logFileWrapper.debug("[PARSE] Found registered biome: " + biome);
      }

      rule.match.biomes._biomes.add(biome);

      if (rule.debug) {
        logFileWrapper.debug("[PARSE] Added biome match: " + biome);
      }
    }
  }
}
