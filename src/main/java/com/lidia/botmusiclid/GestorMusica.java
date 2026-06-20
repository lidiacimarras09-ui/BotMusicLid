package com.lidia.botmusiclid;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.lavalink.youtube.YoutubeAudioSourceManager;

// Importamos los clientes que sí son compatibles con el Refresh Token de OAuth
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.AndroidTestsuiteWithThumbnail;
import dev.lavalink.youtube.clients.TvHtml5EmbeddedWithThumbnail;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import java.nio.ByteBuffer;

public class GestorMusica implements AudioSendHandler {

    public static final AudioPlayerManager playerManager =
            new DefaultAudioPlayerManager();

    static {
        String refreshToken = System.getenv("YOUTUBE_REFRESH_TOKEN");

        // Configuración con los clientes OAuth correctos para evitar el bloqueo anti-bot
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(
                true,
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new AndroidTestsuiteWithThumbnail(),
                new TvHtml5EmbeddedWithThumbnail()
        );

        if (refreshToken != null && !refreshToken.isEmpty()) {
            youtube.useOauth2(refreshToken, true);
            System.out.println("✅ OAuth con refresh token cargado");
        } else {
            youtube.useOauth2(null, false);
            System.out.println("⚠️ No hay refresh token");
        }

        playerManager.registerSourceManager(youtube);
        com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
                .registerLocalSource(playerManager);
    }

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final MutableAudioFrame frame = new MutableAudioFrame();
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);

    private static final byte[] SILENCE_FRAME = new byte[]{
            (byte) 0xF8, (byte) 0xFF, (byte) 0xFE
    };

    public GestorMusica() {
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        this.frame.setBuffer(buffer);
    }

    public AudioPlayer getPlayer() { return player; }
    public TrackScheduler getScheduler() { return scheduler; }
    public void pause() { player.setPaused(true); }
    public void resume() { player.setPaused(false); }
    public void stop() { scheduler.clear(); player.stopTrack(); }
    public void skip() { player.stopTrack(); }
    public AudioTrack getCurrentTrack() { return player.getPlayingTrack(); }

    @Override
    public boolean canProvide() { return true; }

    @Override
    public ByteBuffer provide20MsAudio() {
        if (player.provide(frame)) {
            buffer.flip();
            return buffer;
        }
        return ByteBuffer.wrap(SILENCE_FRAME);
    }

    @Override
    public boolean isOpus() { return true; }
}
