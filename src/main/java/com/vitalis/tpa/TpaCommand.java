package com.vitalis.tpa;

import com.vitalis.tpa.Requests.Kind;
import com.vitalis.tpa.Requests.Req;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * /tpa, /tpahere, /tpaccept, /tpdeny — one class, mode by command name.
 * Target gets clickable [Принять] [Отклонить] buttons in chat.
 */
public class TpaCommand implements BasicCommand {
    private final HeavenTPA plugin;
    private final String mode;

    public TpaCommand(HeavenTPA plugin, String mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender s = stack.getSender();
        if (mode.equals("reload")) {
            if (!s.hasPermission("heaventpa.admin") && !s.isOp()) {
                s.sendMessage(color("&cНет доступа."));
                return;
            }
            plugin.cfg().load(plugin);
            s.sendMessage(color("&aКонфиг перезагружен."));
            return;
        }
        if (!(s instanceof Player p)) {
            s.sendMessage("Only players.");
            return;
        }
        if (!p.hasPermission("heaventpa.use")) {
            p.sendMessage(color("&cНет доступа."));
            return;
        }
        switch (mode) {
            case "tpa" -> send(p, args);
            case "tpaccept" -> answer(p, args, true);
            case "tpdeny" -> answer(p, args, false);
            default -> p.sendMessage(color("&cНеизвестная команда."));
        }
    }

    private void send(Player p, String[] args) {
        if (args.length < 1) {
            p.sendMessage(color("&eИспользование: &f/tpa <ник>"));
            return;
        }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) {
            p.sendMessage(color("&cИгрок &f" + args[0] + " &cне в сети."));
            return;
        }
        if (t.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage(color("&cК себе нельзя."));
            return;
        }
        long now = System.currentTimeMillis();
        plugin.requests().add(t.getUniqueId(), p.getUniqueId(), p.getName(), Kind.TO,
                now, plugin.cfg().expireSeconds * 1000L);
        p.sendMessage(color("&aЗапрос отправлен &f" + t.getName()
                + " &7(сгорит через " + plugin.cfg().expireSeconds + " сек)"));
        // target message with buttons
        t.sendMessage(color("&f" + p.getName() + " &7хочет телепортироваться &fк тебе"));
        Component row = Component.empty()
                .append(btn("&a[Принять]", "/tpaccept " + p.getName(), "&7Клик — принять"))
                .append(Component.text("   "))
                .append(btn("&c[Отклонить]", "/tpdeny " + p.getName(), "&7Клик — отклонить"));
        t.sendMessage(row);
        if (plugin.cfg().sounds) {
            t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.4f);
        }
    }

    private void answer(Player p, String[] args, boolean accept) {
        String nick = args.length >= 1 ? args[0] : "";
        Req r = plugin.requests().take(p.getUniqueId(), nick, System.currentTimeMillis());
        if (r == null) {
            p.sendMessage(color("&7Нет входящих запросов"
                    + (nick.isEmpty() ? "." : " от &f" + nick + "&7.")));
            return;
        }
        Player from = Bukkit.getPlayer(r.from);
        if (from == null || !from.isOnline()) {
            p.sendMessage(color("&cИгрок &f" + r.fromName + " &cвышел."));
            return;
        }
        if (!accept) {
            p.sendMessage(color("&7Ты отклонил запрос &f" + r.fromName + "&7."));
            from.sendMessage(color("&f" + p.getName() + " &cотклонил твой запрос."));
            if (plugin.cfg().sounds) {
                from.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
            }
            return;
        }
        // teleport the requester to the accepter (commands already run on main thread)
        try {
            from.teleport(p.getLocation());
            from.sendMessage(color("&aТелепортация!"));
            if (plugin.cfg().sounds) {
                from.playSound(from.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            }
            p.sendMessage(color("&f" + from.getName() + " &aтелепортировался."));
        } catch (Exception e) {
            from.sendMessage(color("&cНе вышло телепортироваться."));
            plugin.getLogger().warning("Teleport failed: " + e.getMessage());
        }
    }

    private Component btn(String text, String cmd, String hover) {
        String plain = text.replaceAll("&[0-9a-f]", "");
        NamedTextColor c = text.startsWith("&a") ? NamedTextColor.GREEN : NamedTextColor.RED;
        return Component.text(plain).color(c)
                .clickEvent(ClickEvent.runCommand(cmd))
                .hoverEvent(HoverEvent.showText(Component.text(hover.replaceAll("&[0-9a-f]", ""))));
    }

    private Component color(String text) {
        return com.vitalis.tpa.Msg.color(plugin.cfg().prefix + text);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        if (args.length <= 1) {
            String q = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            List<String> out = new ArrayList<>();
            if (mode.equals("tpa")) {
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (o.getName().toLowerCase(Locale.ROOT).startsWith(q)
                            && !o.equals(stack.getSender())) out.add(o.getName());
                }
            } else {
                for (Player o : Bukkit.getOnlinePlayers()) {
                    if (o.getName().toLowerCase(Locale.ROOT).startsWith(q)) out.add(o.getName());
                }
            }
            return out;
        }
        return List.of();
    }
}
