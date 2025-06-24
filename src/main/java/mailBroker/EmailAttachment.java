package mailBroker;

public class EmailAttachment {
	private String fileName;
    private String mimeType;
    private byte[] content;

    public EmailAttachment(String fileName, String mimeType, byte[] content) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.content = content;
    }

    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public byte[] getContent() { return content; }
}
