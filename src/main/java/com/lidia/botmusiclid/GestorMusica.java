package com.lidia.botmusiclid;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import java.nio.ByteBuffer;

public class GestorMusica implements AudioSendHandler {

    public static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    static {
        // Al usar un nodo externo de Lavalink, no necesitamos configurar el 
        // YoutubeAudioSourceManager localmente. Registramos fuentes básicas locales:
        AudioSourceManagers.registerLocalSource(playerManager);
        // Si necesitas fuentes adicionales (como Bandcamp, Vimeo, etc), se añaden aquí:
        AudioSourceManagers.registerRemoteSources(playerManager);
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
