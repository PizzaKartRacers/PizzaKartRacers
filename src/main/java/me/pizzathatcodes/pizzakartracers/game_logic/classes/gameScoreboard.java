package me.pizzathatcodes.pizzakartracers.game_logic.classes;

import fr.mrmicky.fastboard.FastBoard;
import me.pizzathatcodes.pizzakartracers.Main;
import me.pizzathatcodes.pizzakartracers.utils.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class gameScoreboard {

    public static void updateBoard(FastBoard board) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = formatter.format(date);

        GamePlayer gamePlayer = Main.getGame().getGamePlayer(board.getPlayer().getUniqueId());

        board.updateLines(
                util.translate("&7" + formattedDate),
                util.translate(""),
                util.translate("&b&lPosition:"),
                util.translate("&f1 "),
                util.translate("&f2 "),
                util.translate("&f3 "),
                util.translate("&f4 "),
                util.translate(""),
                util.translate(("&b&lItem:")),
                util.translate(gamePlayer.getItem()),
                util.translate(""),
                util.translate("&b&lMap:"),
                util.translate(Main.map.mapName),
                util.translate("")
        );


    }

}
