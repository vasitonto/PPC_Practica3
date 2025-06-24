package mailBroker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import clienteBroker.DataBroker;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.*;

public class MailBroker implements Runnable {

	private static final String APPLICATION_NAME = "Estacion de datos meteorologicos PPC";
	private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String CREDENTIALS_FILE_PATH = "/credentials/credentials.json";

	private final NetHttpTransport httpTransport; // Se inyecta en el constructor
	private Gmail gmailService; // El servicio de Gmail autenticado
	private final static int CHECK_INTERVAL = 20000;
	private volatile boolean running = true; // Para controlar el ciclo de ejecución

	/**
	 * Constructor para MailBroker.
	 * @param httpTransport Instancia de NetHttpTransport.
	 * @throws IOException Si ocurre un error de E/S.
	 * @throws GeneralSecurityException Si ocurre un error de seguridad.
	 */
	public MailBroker() throws IOException, GeneralSecurityException {
		this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		initializeGmailService();
	}

	/**
	 * Creates an authorized Credential object and initializes the Gmail service.
	 * @throws IOException If the credentials.json file cannot be found.
	 * @throws GeneralSecurityException If a security error occurs during transport initialization.
	 */
	private void initializeGmailService(){
		// getCredentials ahora usa la instancia de HTTP_TRANSPORT del atributo de clase
		Credential credential = null;
		try {
			credential = getCredentials(this.httpTransport);
		} catch (IOException e) {
			System.err.println("Error al cargar las credenciales de Gmail: " + e.getMessage());
			e.printStackTrace();
		}
		
		gmailService = new Gmail.Builder(this.httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
			throws IOException {
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader( MailBroker.class.getResourceAsStream(CREDENTIALS_FILE_PATH)));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Set.of(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY))
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		//returns an authorized Credential object.
		return credential;
	}

	public void checkEmails(String userId) throws IOException {
        try {
            ListMessagesResponse response = gmailService.users().messages().list(userId)
                    .setQ("is:unread") // Solo mensajes no leídos
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                System.out.println("No se han encontrado mensajes nuevos.");
            } else {
                for (Message message : messages) {
                    processEmail(userId, message.getId());
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

	
	private void processEmail(String userId, String messageId) throws IOException {
        try {
            // Get the full message content (including headers and body parts)
            Message fullMessage = gmailService.users().messages().get(userId, messageId)
                    .setFormat("full") // Get full message content
                    .execute();

            String subject = getHeader(fullMessage, "Subject");
            String from = getHeader(fullMessage, "From");

            if (subject != null && subject.toLowerCase().contains("valores")) {

                // Simulate sending XML data (you can choose JSON or both)
                String responseBody = "Hola,\n\nEl adjunto de este mensaje contiene los últimos datos meteorológicos.\n"
                		+ "A continuación se muestran los datos de las estaciones:\n"
                		+ DataBroker.obtenerTodos().toString(); 

                List<EmailAttachment> adjuntos = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    String rawData = DataBroker.obtenerRaw(i); // raw original (XML o JSON)
                    if (rawData != null) {
                        String mimeType = rawData.trim().startsWith("<") ? "application/xml" : "application/json";
                        String ext = mimeType.equals("application/xml") ? "xml" : "json";
                        adjuntos.add(new EmailAttachment("Servidor" + (i+1) + "." + ext, mimeType,  rawData.getBytes()));
                    }
                }
                // Create the response email
                MimeMessage responseMessage = createMimeMessage(
                    from, // To the sender of the original email
                    userId, // From your email address
                    "Re: " + subject, // Subject for the reply
                    responseBody, adjuntos
                );

                // Send the response
                sendMessage(userId, responseMessage);

                // Optionally, mark the original message as read
                markMessageAsRead(userId, messageId);

            } else {
            	markMessageAsRead(userId, messageId);
            	// Optionally, mark messages as read even if not processed to avoid re-checking them.
                // markMessageAsRead(userId, messageId);
            }
        } catch (MessagingException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
            throw e; // Re-lanzar para que el bucle principal maneje el error
        }
    }

	private String getHeader(Message message, String name) {
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            for (com.google.api.services.gmail.model.MessagePartHeader header : message.getPayload().getHeaders()) {
                if (header.getName().equalsIgnoreCase(name)) {
                    return header.getValue();
                }
            }
        }
        return null;
    }
	
	private MimeMessage createMimeMessage(String to, String from, String subject, String bodyText,
			List<EmailAttachment> adjuntos)
					throws MessagingException, IOException {

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(bodyText, "text/plain");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);
	   

	    // Añadir todos los adjuntos
	    for (EmailAttachment adj : adjuntos) {
	        MimeBodyPart adjunto = new MimeBodyPart();
	        DataSource source = new ByteArrayDataSource(adj.getContent(), adj.getMimeType());
	        adjunto.setDataHandler(new DataHandler(source));
	        adjunto.setFileName(adj.getFileName());
	        multipart.addBodyPart(adjunto);
	    }

		// Add attachment
		email.setContent(multipart);

		return email;
	}
	
	private Message sendMessage(String userId, MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());

        Message message = new Message();
        message.setRaw(encodedEmail);

        return gmailService.users().messages().send(userId, message).execute();
    }
	
	private void markMessageAsRead(String userId, String messageId) throws IOException {
        ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(Arrays.asList("UNREAD"));
        gmailService.users().messages().modify(userId, messageId, mods).execute();
    }

	
	@Override
	public void run() {
		while (running) {
			try {
				// Aquí usamos "me" porque damailbroker@gmail.com es la cuenta que se autentica.
				checkEmails("me");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(CHECK_INTERVAL);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				running = false;
			}
		}
	}

	/**
	 * Method to gracefully stop the MailBroker thread.
	 */
	public void stopRunning() {
		this.running = false;
	}	

	//	@Override
	//	public void run() {
	//		while (true) {
	//			try {
	//				Properties props = new Properties();
	//				props.setProperty("mail.store.protocol", "imaps"); // o "pop3s"
	//				Session session = Session.getDefaultInstance(props, null);
	//
	//				Store store = session.getStore("imaps");
	//				store.connect("imap.gmail.com", "damailbroka@gmail.com", "mailPPCP3w00h00");
	//
	//				Folder inbox = store.getFolder("INBOX");
	//				inbox.open(Folder.READ_WRITE);
	//
	//				Message[] mensajes = inbox.getMessages();
	//				for (Message msg : mensajes) {
	//					if (!msg.isSet(Flags.Flag.SEEN)) {
	//						procesarMensaje(msg);
	//						msg.setFlag(Flags.Flag.SEEN, true);
	//					}
	//				}
	//
	//				inbox.close(false);
	//				store.close();
	//
	//				Thread.sleep(20000); // espera antes de volver a revisar
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}
	//
	//	private void procesarMensaje(Message msg) throws Exception {
	//		String asunto = msg.getSubject();
	//		String destinatario = ((InternetAddress) msg.getFrom()[0]).getAddress();
	//
	//		if (asunto != null && asunto.equalsIgnoreCase("VALORES")) {
	//			String contenido = generarRespuestaMeteorologica();
	//			enviarCorreo(destinatario, contenido);
	//		}
	//	}
	//
	//	private String generarRespuestaMeteorologica() {
	//		StringBuilder sb = new StringBuilder();
	//		DataBroker.obtenerTodos().forEach((id, datos) -> {
	//			sb.append("[").append(id).append("]: ").append(datos).append("\n");
	//		});
	//		return sb.toString();
	//	}
	//
	//	private void enviarCorreo(String to, String cuerpo) throws Exception {
	//		 Properties props = new Properties();
	//		    props.put("mail.smtp.auth", "true");
	//		    props.put("mail.smtp.starttls.enable", "true");
	//		    props.put("mail.smtp.host", "smtp.gmail.com");
	//		    props.put("mail.smtp.port", "587");
	//
	//		    Session session = Session.getInstance(props,
	//		        new Authenticator() {
	//		            protected PasswordAuthentication getPasswordAuthentication() {
	//		                return new PasswordAuthentication("damailbroka@gmail.com", "tu_contraseña");
	//		            }
	//		        });
	//
	//		    Message message = new MimeMessage(session);
	//		    message.setFrom(new InternetAddress("damailbroka@gmail.com"));
	//		    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	//		    message.setSubject("Respuesta meteorológica");
	//
	//		    // Cuerpo principal
	//		    MimeBodyPart textoPlano = new MimeBodyPart();
	//		    textoPlano.setText(cuerpo); // text/plain
	//
	//		    // (Opcional) adjunto con los mismos datos
	//		    MimeBodyPart adjunto = new MimeBodyPart();
	//		    adjunto.setText(cuerpo);
	//		    adjunto.setFileName("datos_meteo.txt");
	//
	//		    // Multipart con ambas partes
	//		    Multipart multi = new MimeMultipart();
	//		    multi.addBodyPart(textoPlano);
	//		    multi.addBodyPart(adjunto);
	//
	//		    message.setContent(multi); // esto hace que sea MIME multipart
	//
	//		    Transport.send(message);
	//	}
}

