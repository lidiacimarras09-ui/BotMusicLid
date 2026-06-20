package com.lidia.botmusiclid;

import moe.kyokobot.libdave.NativeDaveFactory;
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main extends ListenerAdapter {

    private GestorMusica gestor = null;

    public static void main(String[] args) throws Exception {
        String token = System.getenv("DISCORD_TOKEN");

        // Configuración para forzar una conexión más robusta
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        NativeDaveFactory daveFactory = new NativeDaveFactory();
        LDJDADaveSessionFactory daveSessionFactory = new LDJDADaveSessionFactory(daveFactory);

        AudioModuleConfig audioConfig = new AudioModuleConfig()
                .withDaveSessionFactory(daveSessionFactory);

        JDABuilder.createDefault(
                token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES)
                .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .setAudioModuleConfig(audioConfig)
                .addEventListeners(new Main())
                .build()
                .awaitReady();

        System.out.println("Bot conectado y listo");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isWebhookMessage()) return;
        if (!event.isFromGuild()) return;

        String mensaje = event.getMessage().getContentRaw().trim();
        if (!mensaje.startsWith("!")) return;

        System.out.println(">>> " + mensaje);

        // JOIN
        if (mensaje.equals("!join")) {
            if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inAudioChannel()) {
                event.getChannel().sendMessage("❌ Debes estar en un canal de voz").queue();
                return;
            }
            VoiceChannel canal = (VoiceChannel) event.getMember().getVoiceState().getChannel();
            AudioManager audioManager = event.getGuild().getAudioManager();
            if (gestor == null) gestor = new GestorMusica();
            audioManager.setSendingHandler(gestor);
            audioManager.openAudioConnection(canal);
            event.getChannel().sendMessage("🔊 Conectado a **" + canal.getName() + "**").queue();
            return;
        }

        // PLAY
        if (mensaje.startsWith("!play ")) {
            if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inAudioChannel()) {
                event.getChannel().sendMessage("❌ Debes estar en un canal de voz").queue();
                return;
            }
            
            VoiceChannel canal = (VoiceChannel) event.getMember().getVoiceState().getChannel();
            AudioManager audioManager = event.getGuild().getAudioManager();
            if (gestor == null) gestor = new GestorMusica();
            
            if (!audioManager.isConnected()) {
                audioManager.setSendingHandler(gestor);
                audioManager.openAudioConnection(canal);
            }

            String busqueda = mensaje.substring(6).trim();
            event.getChannel().sendMessage("🎶 Buscando: " + busqueda).queue();

            String consulta = busqueda.startsWith("http") ? busqueda : "ytsearch:" + busqueda;

            GestorMusica.playerManager.loadItemOrdered(gestor, consulta, new CargadorCanciones(gestor, event.getChannel()));
            return;
        }

        // COMANDOS BASICOS (PAUSE, RESUME, SKIP, STOP, LEAVE) - Mismo código que tenías
        if (mensaje.equals("!pause") && gestor != null) gestor.pause();
        if (mensaje.equals("!resume") && gestor != null) gestor.resume();
        if (mensaje.equals("!skip") && gestor != null) gestor.skip();
        if (mensaje.equals("!stop") && gestor != null) gestor.stop();
        if (mensaje.equals("!leave")) {
            event.getGuild().getAudioManager().setSendingHandler(null);
            event.getGuild().getAudioManager().closeAudioConnection();
            event.getChannel().sendMessage("👋 Desconectado").queue();
        }
    }
}
