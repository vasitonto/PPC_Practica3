package clienteBroker;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailBroker implements Runnable {


	@Override
	public void run() {
		while (true) {
			try {
				Properties props = new Properties();
				props.setProperty("mail.store.protocol", "imaps"); // o "pop3s"
				Session session = Session.getDefaultInstance(props, null);

				Store store = session.getStore("imaps");
				store.connect("imap.gmail.com", "damailbroka@gmail.com", "mailPPCP3w00h00");

				Folder inbox = store.getFolder("INBOX");
				inbox.open(Folder.READ_WRITE);

				Message[] mensajes = inbox.getMessages();
				for (Message msg : mensajes) {
					if (!msg.isSet(Flags.Flag.SEEN)) {
						procesarMensaje(msg);
						msg.setFlag(Flags.Flag.SEEN, true);
					}
				}

				inbox.close(false);
				store.close();

				Thread.sleep(15000); // espera antes de volver a revisar
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void procesarMensaje(Message msg) throws Exception {
		String asunto = msg.getSubject();
		String destinatario = ((InternetAddress) msg.getFrom()[0]).getAddress();

		if (asunto != null && asunto.equalsIgnoreCase("VALORES")) {
			String contenido = generarRespuestaMeteorologica();
			enviarCorreo(destinatario, contenido);
		}
	}

	private String generarRespuestaMeteorologica() {
		StringBuilder sb = new StringBuilder();
		broker.obtenerTodos().forEach((id, datos) -> {
			sb.append("[").append(id).append("]: ").append(datos).append("\n");
		});
		return sb.toString();
	}

	private void enviarCorreo(String to, String cuerpo) throws Exception {
		 Properties props = new Properties();
		    props.put("mail.smtp.auth", "true");
		    props.put("mail.smtp.starttls.enable", "true");
		    props.put("mail.smtp.host", "smtp.gmail.com");
		    props.put("mail.smtp.port", "587");

		    Session session = Session.getInstance(props,
		        new Authenticator() {
		            protected PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication("tucuenta@gmail.com", "tu_contraseña");
		            }
		        });

		    Message message = new MimeMessage(session);
		    message.setFrom(new InternetAddress("tucuenta@gmail.com"));
		    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		    message.setSubject("Respuesta meteorológica");

		    // Cuerpo principal
		    MimeBodyPart textoPlano = new MimeBodyPart();
		    textoPlano.setText(cuerpo); // text/plain

		    // (Opcional) adjunto con los mismos datos
		    MimeBodyPart adjunto = new MimeBodyPart();
		    adjunto.setText(cuerpo);
		    adjunto.setFileName("datos_meteo.txt");

		    // Multipart con ambas partes
		    Multipart multi = new MimeMultipart();
		    multi.addBodyPart(textoPlano);
		    multi.addBodyPart(adjunto);

		    message.setContent(multi); // esto hace que sea MIME multipart

		    Transport.send(message);
	}
}

