package com.lidia.botmusiclid;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.LinkedList;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Queue<AudioTrack> cola = new LinkedList<>();

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void queue(AudioTrack track) {

        System.out.println("QUEUE: " + track.getInfo().title);

        if (!player.startTrack(track, true)) {
            cola.offer(track);
            System.out.println("METIDA EN COLA");
        } else {
            System.out.println("EMPEZANDO A SONAR");
        }
    }

    public void nextTrack() {

        AudioTrack siguiente = cola.poll();

        System.out.println("NEXT TRACK");

        if (siguiente != null) {

            System.out.println("SIGUIENTE: "
                    + siguiente.getInfo().title);

            player.startTrack(siguiente, false);

        } else {

            System.out.println("COLA VACIA");
        }
    }

    public void clear() {

        cola.clear();
    }

    @Override
    public void onTrackEnd(
            AudioPlayer player,
            AudioTrack track,
            AudioTrackEndReason endReason) {

        System.out.println(
                "TRACK END -> " + endReason.name());

        nextTrack();
    }
}