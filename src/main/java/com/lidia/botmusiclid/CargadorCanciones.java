package com.lidia.botmusiclid;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CargadorCanciones implements AudioLoadResultHandler {

    private final GestorMusica gestor;
    private final MessageChannel canal;

    public CargadorCanciones(
            GestorMusica gestor,
            MessageChannel canal) {

        this.gestor = gestor;
        this.canal = canal;
    }

    @Override
    public void trackLoaded(AudioTrack track) {

        gestor.getScheduler().queue(track);

        canal.sendMessage(
                "🎵 **Añadida a la cola**\n\n" +
                "**" + track.getInfo().title + "**\n\n" +
                track.getInfo().uri
        ).queue();

        System.out.println(
                "AÑADIDA A LA COLA: "
                        + track.getInfo().title);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        if (playlist.getTracks().isEmpty()) {

            canal.sendMessage(
                    "❌ La playlist está vacía"
            ).queue();

            return;
        }

        int canciones = 0;

        for (AudioTrack track : playlist.getTracks()) {

            gestor.getScheduler().queue(track);

            canciones++;
        }

        canal.sendMessage(
                "🎶 **Playlist cargada**\n\n" +
                "**" + playlist.getName() + "**\n" +
                "Canciones añadidas: **" + canciones + "**"
        ).queue();

        System.out.println(
                "PLAYLIST CARGADA: "
                        + playlist.getName()
                        + " (" + canciones + " canciones)"
        );
    }

    @Override
    public void noMatches() {

        canal.sendMessage(
                "❌ No se encontró ninguna canción"
        ).queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {

        canal.sendMessage(
                "❌ Error al cargar la canción"
        ).queue();

        exception.printStackTrace();
    }
}