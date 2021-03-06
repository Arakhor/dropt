package com.codetaylor.mc.dropt.modules.dropt.command;

import com.codetaylor.mc.dropt.modules.dropt.ModuleDropt;
import com.codetaylor.mc.dropt.modules.dropt.Util;
import com.codetaylor.mc.dropt.modules.dropt.rule.log.LogFileWrapper;
import com.codetaylor.mc.dropt.modules.dropt.rule.RuleLoader;
import com.codetaylor.mc.dropt.modules.dropt.rule.log.LoggerWrapper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;

public class Command
    extends CommandBase {

  @Override
  public String getName() {

    return "dropt";
  }

  @Override
  public String getUsage(ICommandSender sender) {

    return "dropt <reload>";
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

    if (args.length > 0 && "reload".equals(args[0])) {
      sender.sendMessage(new TextComponentString("Starting rule list reload..."));

      FileWriter logFileWriter = ModuleDropt.LOG_FILE_WRITER_PROVIDER.createLogFileWriter();
      CommandLoggerWrapper wrapper = new CommandLoggerWrapper(ModuleDropt.LOGGER, sender, logFileWriter);
      ModuleDropt.RULE_LISTS.clear();
      RuleLoader.loadRuleLists(ModuleDropt.RULE_PATH, ModuleDropt.RULE_LISTS, wrapper,
          new LogFileWrapper(logFileWriter)
      );
      RuleLoader.parseRuleLists(ModuleDropt.RULE_LISTS, wrapper, new LogFileWrapper(logFileWriter));
      Util.closeSilently(logFileWriter);
      sender.sendMessage(new TextComponentString("[" + ModuleDropt.RULE_LISTS.size() + "] files processed"));
    }
  }

  @Override
  public boolean checkPermission(MinecraftServer server, ICommandSender sender) {

    if (sender instanceof EntityPlayer) {
      return server.getPlayerList().canSendCommands(((EntityPlayer) sender).getGameProfile());
    }

    return false;
  }

  private static class CommandLoggerWrapper
      extends LoggerWrapper {

    private final ICommandSender sender;

    public CommandLoggerWrapper(Logger logger, ICommandSender sender, FileWriter fileWriter) {

      super(logger, fileWriter);
      this.sender = sender;
    }

    @Override
    public void warn(String message) {

      super.warn(message);
      sender.sendMessage(new TextComponentString(message));
    }

    @Override
    public void error(String message) {

      super.error(message);
      sender.sendMessage(new TextComponentString(message));
    }

    @Override
    public void error(String message, Throwable error) {

      super.error(message, error);
      sender.sendMessage(new TextComponentString(message));
      sender.sendMessage(new TextComponentString(error.getMessage()));
    }
  }
}
