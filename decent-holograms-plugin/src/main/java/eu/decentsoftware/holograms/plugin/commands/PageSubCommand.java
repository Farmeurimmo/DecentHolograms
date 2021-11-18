package eu.decentsoftware.holograms.plugin.commands;

import com.google.common.collect.Lists;
import eu.decentsoftware.holograms.api.Lang;
import eu.decentsoftware.holograms.api.Settings;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.commands.*;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.api.utils.Common;
import eu.decentsoftware.holograms.api.utils.entity.DecentEntityType;
import eu.decentsoftware.holograms.api.utils.items.DecentMaterial;
import eu.decentsoftware.holograms.api.utils.message.Message;
import eu.decentsoftware.holograms.plugin.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandInfo(
        permission = "",
        usage = "/dh pages help",
        description = "All commands for editting hologram pages.",
        aliases = {"pages", "p"}
)
public class PageSubCommand extends DecentCommand {

    public PageSubCommand() {
        super("page");

        addSubCommand(new PageHelpSub());
        addSubCommand(new PageAddSub());
        addSubCommand(new PageInsertSub());
        addSubCommand(new PageRemoveSub());
        addSubCommand(new PageSwapSub());
        addSubCommand(new PageSwitchSub());
        addSubCommand(new PageAddActionSub());
        addSubCommand(new PageRemoveActionSub());
        addSubCommand(new PageClearActionsSub());
        addSubCommand(new PageActionsSub());
    }

    @Override
    public CommandHandler getCommandHandler() {
        return (sender, args) -> {
            if (args.length == 0) {
                Lang.USE_HELP.send(sender);
                return true;
            }
            Lang.UNKNOWN_SUB_COMMAND.send(sender);
            Lang.USE_HELP.send(sender);
            return true;
        };
    }

    @Override
    public TabCompleteHandler getTabCompleteHandler() {
        return null;
    }

    /*
     *  SubCommands
     */

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page ?",
            description = "All commands for editting pages.",
            aliases = {"?"}
    )
    static class PageHelpSub extends DecentCommand {

        public PageHelpSub() {
            super("help");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                sender.sendMessage("");
                Common.tell(sender, " &3&lHOLOGRAM PAGES HELP");
                Common.tell(sender, " All page commands.");
                sender.sendMessage("");
                CommandBase command = PLUGIN.getCommandManager().getMainCommand().getSubCommand("page");
                List<CommandBase> subCommands = Lists.newArrayList(command.getSubCommands());
                for (CommandBase subCommand : subCommands) {
                    Common.tell(sender, " &8• &b%s &8- &7%s", subCommand.getUsage(), subCommand.getDescription());
                }
                sender.sendMessage("");
                Common.tell(sender, " &7Aliases: &b%s%s",
                        command.getName(),
                        command.getAliases().size() > 1
                                ? ", " + String.join(", ", command.getAliases())
                                : ""
                );
                sender.sendMessage("");
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }
    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page add <hologram> [content]",
            description = "Add a page to Hologram.",
            aliases = {"append"},
            minArgs = 1
    )
    static class PageAddSub extends DecentCommand {

        public PageAddSub() {
            super("add");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.addPage();
                if (page != null) {
                    String content = Settings.DEFAULT_TEXT.getValue();
                    if (args.length > 1) {
                        content = sender instanceof Player ? Validator.getLineContent((Player) sender, args, 1) : Validator.getLineContent(args, 1);
                    }
                    page.addLine(new HologramLine(page, page.getNextLineLocation(), content));
                    hologram.save();
                    Lang.PAGE_ADDED.send(sender);
                } else {
                    Lang.PAGE_ADD_FAILED.send(sender);
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return (sender, args) -> {
                List<String> matches = Lists.newArrayList();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], PLUGIN.getHologramManager().getHologramNames(), matches);
                } else if (args.length == 3 && (args[1].startsWith("#ICON:") || args[1].startsWith("#HEAD:") || args[1].startsWith("#SMALLHEAD:"))) {
                    StringUtil.copyPartialMatches(
                            args[2],
                            Arrays.stream(Material.values())
                                    .filter(DecentMaterial::isItem)
                                    .map(Material::name)
                                    .collect(Collectors.toList()),
                            matches
                    );
                } else if (args.length == 3 && args[1].startsWith("#ENTITY:")) {
                    StringUtil.copyPartialMatches(args[2], DecentEntityType.getAllowedEntityTypeNames(), matches);
                }
                return matches;
            };
        }
    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page insert <hologram> <page> [content]",
            description = "Insert a page into Hologram.",
            minArgs = 2
    )
    static class PageInsertSub extends DecentCommand {

        public PageInsertSub() {
            super("insert");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.insertPage(Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1);
                if (page != null) {
                    String content = Settings.DEFAULT_TEXT.getValue();
                    if (args.length > 2) {
                        content = Validator.getLineContent(args, 1);
                    }
                    page.addLine(new HologramLine(page, page.getNextLineLocation(), content));
                    hologram.save();
                    Lang.PAGE_INSERTED.send(sender);
                } else {
                    Lang.PAGE_INSERT_FAILED.send(sender);
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return (sender, args) -> {
                List<String> matches = Lists.newArrayList();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], PLUGIN.getHologramManager().getHologramNames(), matches);
                } else if (args.length == 2) {
                    Hologram hologram = PLUGIN.getHologramManager().getHologram(args[0]);
                    if (hologram != null) {
                        StringUtil.copyPartialMatches(args[1], IntStream
                                .rangeClosed(1, hologram.size())
                                .boxed().map(String::valueOf)
                                .collect(Collectors.toList()), matches);
                    }
                } else if (args.length == 4 && (args[2].startsWith("#ICON:") || args[2].startsWith("#HEAD:") || args[2].startsWith("#SMALLHEAD:"))) {
                    StringUtil.copyPartialMatches(
                            args[3],
                            Arrays.stream(Material.values())
                                    .filter(DecentMaterial::isItem)
                                    .map(Material::name)
                                    .collect(Collectors.toList()),
                            matches
                    );
                } else if (args.length == 4 && args[2].startsWith("#ENTITY:")) {
                    StringUtil.copyPartialMatches(args[3], DecentEntityType.getAllowedEntityTypeNames(), matches);
                }
                return matches;
            };
        }

    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page remove <hologram> <page>",
            description = "Remove a page from Hologram.",
            aliases = {"rem", "del", "delete"},
            minArgs = 2
    )
    static class PageRemoveSub extends DecentCommand {

        public PageRemoveSub() {
            super("remove");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                int index = Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue());
                hologram.removePage(index - 1);
                if (hologram.size() == 0) {
                    hologram.delete();
                    Lang.PAGE_DELETED.send(sender);
                    Lang.HOLOGRAM_DELETED.send(sender);
                } else {
                    hologram.save();
                    Lang.PAGE_DELETED.send(sender);
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES;
        }

    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page swap <hologram> <page1> <page2>",
            description = "Swap two pages in a Hologram.",
            minArgs = 3
    )
    static class PageSwapSub extends DecentCommand {

        public PageSwapSub() {
            super("swap");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                final int index1 = Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue());
                final int index2 = Validator.getInteger(args[2], Lang.PAGE_DOES_NOT_EXIST.getValue());
                if (index1 == index2) {
                    Lang.PAGE_SWAP_SELF.send(sender);
                    return true;
                }
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                if (hologram.swapPages(index1 - 1, index2 - 1)) {
                    hologram.save();
                    Lang.PAGE_SWAPPED.send(sender);
                } else {
                    Lang.PAGE_SWAP_FAILED.send(sender);
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return (sender, args) -> {
                List<String> matches = Lists.newArrayList();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], PLUGIN.getHologramManager().getHologramNames(), matches);
                } else if (args.length == 2) {
                    Hologram hologram = PLUGIN.getHologramManager().getHologram(args[0]);
                    if (hologram != null) {
                        StringUtil.copyPartialMatches(args[1], IntStream
                                .rangeClosed(1, hologram.size())
                                .boxed().map(String::valueOf)
                                .collect(Collectors.toList()), matches);
                    }
                } else if (args.length == 3) {
                    Hologram hologram = PLUGIN.getHologramManager().getHologram(args[0]);
                    if (hologram != null) {
                        StringUtil.copyPartialMatches(args[2], IntStream
                                .rangeClosed(1, hologram.size())
                                .boxed().map(String::valueOf)
                                .collect(Collectors.toList()), matches);
                    }
                }
                return matches;
            };
        }
    }

    @CommandInfo(
            permission = "",
            usage = "/dh page switch <hologram> <page> [player]",
            description = "Switch to a page in hologram.",
            aliases = {"go", "view"},
            minArgs = 2
    )
    static class PageSwitchSub extends DecentCommand {

        public PageSwitchSub() {
            super("switch");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                int index = Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1;
                if (hologram.getPage(index) == null) {
                    Lang.PAGE_DOES_NOT_EXIST.send(sender);
                    return true;
                }
                if (args.length > 2 && sender.hasPermission("dh.admin")) {
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player != null && player.isOnline()) {
                        hologram.show(player, index);
                        return true;
                    }
                }
                hologram.show((Player) sender, index);
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES;
        }

    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page actions <hologram> <page> <clickType> [page]",
            description = "List of click actions.",
            playerOnly = true,
            minArgs = 3
    )
    static class PageActionsSub extends DecentCommand {

        public PageActionsSub() {
            super("actions");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.getPage(Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1);
                if (page == null) {
                    Lang.PAGE_DOES_NOT_EXIST.send(sender);
                    return true;
                }
                ClickType clickType = ClickType.fromString(args[2]);
                if (clickType == null) {
                    Lang.CLICK_TYPE_DOES_NOT_EXIST.send(sender, args[2]);
                    return true;
                }
                List<Action> actions = page.getActions(clickType);
                if (actions == null || actions.isEmpty()) {
                    Lang.ACTION_NO_ACTIONS.send(sender);
                    return true;
                }
                int currentPage = args.length >= 4 ? Validator.getInteger(args[3], "Page must be a valid integer.") - 1 : 0;
                List<String> header = Lists.newArrayList("", " &3&lACTIONS LIST - #{page}", " &fList of all actions on a page.", "");
                Function<Action, String> parseItem = action -> String.format(" &8• &b%s", action.toString());
                String commandFormat = "/dh actions " + args[0] + " " + args[1] + " " + args[2] + " %d";
                Message.sendPaginatedMessage((Player) sender, currentPage, commandFormat, 15, header, null, actions, parseItem);
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES_CLICK_TYPES;
        }
    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page clearactions <hologram> <page> <clickType>",
            description = "Clear all click actions.",
            minArgs = 3
    )
    static class PageClearActionsSub extends DecentCommand {

        public PageClearActionsSub() {
            super("clearactions");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.getPage(Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1);
                if (page == null) {
                    Lang.PAGE_DOES_NOT_EXIST.send(sender);
                    return true;
                }
                ClickType clickType = ClickType.fromString(args[2]);
                if (clickType == null) {
                    Lang.CLICK_TYPE_DOES_NOT_EXIST.send(sender, args[2]);
                    return true;
                }
                List<Action> actions = page.getActions(clickType);
                if (actions == null || actions.isEmpty()) {
                    Lang.ACTION_NO_ACTIONS.send(sender);
                    return true;
                }
                page.clearActions(clickType);
                hologram.save();
                Lang.ACTION_CLEARED.send(sender);
                if (!page.isClickable()) {
                    hologram.hideAll();
                    hologram.showAll();
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES_CLICK_TYPES;
        }
    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page addaction <hologram> <page> <clickType> <action>",
            description = "Add a click action.",
            minArgs = 4
    )
    static class PageAddActionSub extends DecentCommand {

        public PageAddActionSub() {
            super("addaction");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.getPage(Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1);
                if (page == null) {
                    Lang.PAGE_DOES_NOT_EXIST.send(sender);
                    return true;
                }
                boolean wasClickable = page.isClickable();
                ClickType clickType = ClickType.fromString(args[2]);
                if (clickType == null) {
                    Lang.CLICK_TYPE_DOES_NOT_EXIST.send(sender, args[2]);
                    return true;
                }
                String actionString = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                Action action = new Action(actionString);
                if (!action.isValid()) {
                    Lang.ACTION_DOES_NOT_EXIST.send(sender);
                    return true;
                } else if (action.getType().getName().contains("_PAGE") && action.getData() == null) {
                    action.setData(hologram.getName());
                }
                page.addAction(clickType, action);
                hologram.save();
                Lang.ACTION_ADDED.send(sender);
                if (!wasClickable) {
                    hologram.hideAll();
                    hologram.showAll();
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES_ACTIONS;
        }

    }

    @CommandInfo(
            permission = "dh.admin",
            usage = "/dh page removeaction <hologram> <page> <clickType> <index>",
            description = "Add a click action.",
            aliases = {"remaction"},
            minArgs = 4
    )
    static class PageRemoveActionSub extends DecentCommand {

        public PageRemoveActionSub() {
            super("removeaction");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Hologram hologram = Validator.getHologram(args[0], Lang.HOLOGRAM_DOES_NOT_EXIST.getValue());
                HologramPage page = hologram.getPage(Validator.getInteger(args[1], Lang.PAGE_DOES_NOT_EXIST.getValue()) - 1);
                if (page == null) {
                    Lang.PAGE_DOES_NOT_EXIST.send(sender);
                    return true;
                }
                ClickType clickType = ClickType.fromString(args[2]);
                if (clickType == null) {
                    Lang.CLICK_TYPE_DOES_NOT_EXIST.send(sender, args[2]);
                    return true;
                }
                int index = Validator.getInteger(args[3], 1, page.getActions(clickType).size(), Lang.ACTION_DOES_NOT_EXIST.getValue());
                page.removeAction(clickType, index - 1);
                hologram.save();
                Lang.ACTION_REMOVED.send(sender);
                if (!page.isClickable()) {
                    hologram.hideAll();
                    hologram.showAll();
                }
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return TabCompleteHandler.HOLOGRAM_PAGES_ACTION_INDEXES;
        }

    }

}
