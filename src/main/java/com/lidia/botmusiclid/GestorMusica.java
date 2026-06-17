package com.lidia.botmusiclid;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.TvHtml5EmbeddedWithThumbnail;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import java.nio.ByteBuffer;

public class GestorMusica implements AudioSendHandler {

    public static final AudioPlayerManager playerManager =
            new DefaultAudioPlayerManager();

    static {
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(
                true,
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new TvHtml5EmbeddedWithThumbnail()
        );

        youtube.useOauth2(null, false);

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
