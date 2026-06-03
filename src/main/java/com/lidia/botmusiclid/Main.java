//String token = "XXXXXXXXX";
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

public class Main extends ListenerAdapter {

    private GestorMusica gestor = null;

    public static void main(String[] args) throws Exception {

    	String token = System.getenv("TOKEN");

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

        // HELP
        if (mensaje.equals("!help")) {

            event.getChannel().sendMessage("""
🎵 **Bot MusicLid**

━━━━━━━━━━━━━━━━━━

🎶 **Comandos de música**

🎵 `!play <url>/título`
Reproduce una canción o la añade a la cola.

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