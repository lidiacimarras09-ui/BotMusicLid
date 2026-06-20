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
import net.dv8tion.jda.api.utils.cache.CacheFlag; // Importación necesaria para deshabilitar las alertas de caché

public class Main extends ListenerAdapter {

    private GestorMusica gestor = null;

    public static void main(String[] args) throws Exception {

        String token = System.getenv("DISCORD_TOKEN");

        NativeDaveFactory daveFactory = new NativeDaveFactory();
        LDJDADaveSessionFactory daveSessionFactory =
                new LDJDADaveSessionFactory(daveFactory);

        AudioModuleConfig audioConfig =
                new AudioModuleConfig()
                        .withDaveSessionFactory(daveSessionFactory);

        JDABuilder.createDefault(
                token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES)
                // Deshabilitamos manualmente las cachés que no usas para limpiar la consola de advertencias
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

            if (event.getMember() == null
                    || event.getMember().getVoiceState() == null
                    || !event.getMember().getVoiceState().inAudioChannel()) {

                event.getChannel()
                        .sendMessage("❌ Debes estar en un canal de voz")
                        .queue();
                return;
            }

            VoiceChannel canal =
                    (VoiceChannel) event.getMember()
                            .getVoiceState()
                            .getChannel();

            AudioManager audioManager =
                    event.getGuild().getAudioManager();

            if (gestor == null) {
                gestor = new GestorMusica();
            }

            audioManager.setSendingHandler(gestor);
            audioManager.openAudioConnection(canal);

            event.getChannel()
                    .sendMessage("🔊 Conectado a **" + canal.getName() + "**")
                    .queue();

            return;
        }

        // PLAY
        if (mensaje.startsWith("!play ")) {

            if (event.getMember() == null
                    || event.getMember().getVoiceState() == null
                    || !event.getMember().getVoiceState().inAudioChannel()) {

                event.getChannel()
                        .sendMessage("❌ Debes estar en un canal de voz")
                        .queue();
                return;
            }

            VoiceChannel canal =
                    (VoiceChannel) event.getMember()
                            .getVoiceState()
                            .getChannel();

            AudioManager audioManager =
                    event.getGuild().getAudioManager();

            if (gestor == null) {
                gestor = new GestorMusica();
            }

            if (!audioManager.isConnected()) {

                audioManager.setSendingHandler(gestor);
                audioManager.openAudioConnection(canal);

                event.getChannel()
                        .sendMessage("🔊 Conectado a **" + canal.getName() + "**")
                        .queue();
            }

            String busqueda = mensaje.substring(6).trim();

            event.getChannel()
                    .sendMessage("🎶 Buscando...")
                    .queue();

            String consulta;

            if (busqueda.startsWith("http")) {
                consulta = busqueda;
            } else {
                consulta = "ytsearch:" + busqueda;
            }

            GestorMusica.playerManager.loadItemOrdered(
                    gestor,
                    consulta,
                    new CargadorCanciones(
                            gestor,
                            event.getChannel()
                    )
            );

            return;
        }

        // PAUSE
        if (mensaje.equals("!pause")) {

            if (gestor != null) {
                gestor.pause();
            }

            event.getChannel()
                    .sendMessage("⏸️ Reproducción pausada")
                    .queue();

            return;
        }

        // RESUME
        if (mensaje.equals("!resume")) {

            if (gestor != null) {
                gestor.resume();
            }

            event.getChannel()
                    .sendMessage("▶️ Reproducción reanudada")
                    .queue();

            return;
        }

        // SKIP
        if (mensaje.equals("!skip")) {

            if (gestor != null) {
                gestor.skip();
            }

            event.getChannel()
                    .sendMessage("⏭️ Saltando canción...")
                    .queue();

            return;
        }
        
        // CLEAR
        if (mensaje.equals("!clear")) {

            if (gestor != null) {
                gestor.getScheduler().clear();
            }

            event.getChannel()
                    .sendMessage("🗑️ Cola vaciada")
                    .queue();

            return;
        }
        
        // STOP
        if (mensaje.equals("!stop")) {

            if (gestor != null) {
                gestor.stop();
            }

            event.getChannel()
                    .sendMessage("⏹️ Reproducción detenida")
                    .queue();

            return;
        }

        // NOW PLAYING
        if (mensaje.equals("!np")) {

            if (gestor == null
                    || gestor.getCurrentTrack() == null) {

                event.getChannel()
                        .sendMessage("❌ No hay ninguna canción sonando")
                        .queue();

                return;
            }

            event.getChannel()
                    .sendMessage(
                            "🎵 Sonando: **"
                                    + gestor.getCurrentTrack()
                                    .getInfo().title
                                    + "**")
                    .queue();

            return;
        }

        // QUEUE
        if (mensaje.equals("!qn")) {

            if (gestor == null) {

                event.getChannel()
                        .sendMessage("❌ No hay música")
                        .queue();

                return;
            }

            StringBuilder sb = new StringBuilder();

            if (gestor.getCurrentTrack() != null) {

                sb.append("🎵 Sonando ahora:\n");
                sb.append("**")
                        .append(gestor.getCurrentTrack().getInfo().title)
                        .append("**\n\n");
            }

            sb.append("📜 Cola:\n");

            int i = 1;

            for (var track : gestor.getScheduler().getQueue()) {

                sb.append(i)
                        .append(". ")
                        .append(track.getInfo().title)
                        .append("\n");

                i++;

                if (i > 20) {
                    sb.append("\n... y más canciones");
                    break;
                }
            }

            if (i == 1) {
                sb.append("Vacía");
            }

            event.getChannel()
                    .sendMessage(sb.toString())
                    .queue();

            return;
        }
        
        // HELP
        if (mensaje.equals("!help")) {

            event.getChannel().sendMessage("""
🎵 **Bot MusicLid**

━━━━━━━━━━━━━━━━━━

🎶 **Comandos de música**

🎵 `!play <url>/título`
Reproduce una canción o la añade a la cola.

🎶 **Mixes y Playlists**
Pega la URL de un Mix o Playlist de YouTube y el bot cargará todas las canciones automáticamente.

⏭️ `!skip`
Salta a la siguiente canción.

⏸️ `!pause`
Pausa la reproducción.

▶️ `!resume`
Reanuda la reproducción.

⏹️ `!stop`
Detiene la música y vacía la cola.

🎶 `!np`
Muestra la canción que está sonando.

📜 !qn
Muestra la cola de reproducción.

🗑️ !clear
Vacía la cola sin detener la canción actual.

━━━━━━━━━━━━━━━━━━

🔊 **Canal de voz**

🔗 `!join`
Conecta el bot a tu canal de voz.

👋 `!leave`
Desconecta el bot del canal de voz.

━━━━━━━━━━━━━━━━━━

💡 Usa enlaces o pon directamente el título que aparece en YouTube.
""").queue();

            return;
        }

        // LEAVE
        if (mensaje.equals("!leave")) {

            event.getGuild()
                    .getAudioManager()
                    .setSendingHandler(null);

            event.getGuild()
                    .getAudioManager()
                    .closeAudioConnection();

            event.getChannel()
                    .sendMessage("👋 Desconectado del canal de voz")
                    .queue();

            return;
        }
    }
}
