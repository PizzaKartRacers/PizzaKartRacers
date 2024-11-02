package me.pizzathatcodes.pizzakartracers.queue_logic.classes;

import fr.mrmicky.fastboard.FastBoard;
import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.utils.util;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class queueScoreboard {

    public static void updateBoard(FastBoard board) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = formatter.format(date);

            if(Main.getQueue().getPlayers().size() < 4) {
                int neededAmountOfPlayers = 4 - Main.getQueue().getPlayers().size();



                board.updateLines(
                        util.translate("&7" + formattedDate + " &8" + Main.map.map),
                        util.translate(""),
                        util.translate("&fMap: &a" + Main.map.mapName),
                        util.translate("&fPlayers: &a" + Main.getQueue().getPlayers().size() + "/12"),
                        util.translate(""),
                        util.translate("&fStarting in &a" + ( (Main.getQueue().timeWaitLeft / 60) < 10 ? "0" + (Main.getQueue().timeWaitLeft/60) : (Main.getQueue().timeWaitLeft/60) ) + ":" + ( (Main.getQueue().timeWaitLeft%60) < 10 ? "0" + (Main.getQueue().timeWaitLeft%60) : (Main.getQueue().timeWaitLeft%60) ) + " &fif "),
                        util.translate("&a" + neededAmountOfPlayers + " &fmore " + (neededAmountOfPlayers > 1 ? "players join" : "player joins")),
                        util.translate(""),
                        util.translate("&eslimeworks.net")
                );

            } else {

                board.updateLines(
                        util.translate("&7" + formattedDate + " &8" + Main.map.map),
                        util.translate(""),
                        util.translate("&fMap: &7" + Main.map.mapName),
                        util.translate("&fPlayers: &7" + Main.getQueue().getPlayers().size() + "/8"),
                        util.translate(""),
                        util.translate("&fStarting in: &a" + ( (Main.getQueue().timeWaitLeft / 60) < 10 ? "0" + (Main.getQueue().timeWaitLeft/60) : (Main.getQueue().timeWaitLeft/60) ) + ":" + ( (Main.getQueue().timeWaitLeft%60) < 10 ? "0" + (Main.getQueue().timeWaitLeft%60) : (Main.getQueue().timeWaitLeft%60) ) + " &fto"),
                        util.translate("&fallow time for"),
                        util.translate("&fadditional playe&frs"),
                        util.translate(""),
                        util.translate("&eslimeworks.net")
                );

            }
    }

}
