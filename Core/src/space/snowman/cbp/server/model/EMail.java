package space.snowman.cbp.server.model;

public class EMail {
    private String mime;
    private String from;
    private String to;
    private String date;
    private String subject;
    private String contentType;
    private String contentTransferEncoding;
    private String dataText = "";
    private String dataBase64 = "";

    public String getMime() {
        return mime;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentTransferEncoding() {
        return contentTransferEncoding;
    }

    public String getDataText() {
        return dataText;
    }

    public String getDataBase64() {
        return dataBase64;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    public void setDataText(String dataText) {
        this.dataText = dataText;
    }

    public void setDataBase64(String dataBase64) {
        this.dataBase64 = dataBase64;
    }

    @Override
    public String toString() {
        return "EMail{" +
                "mime='" + mime + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", date='" + date + '\'' +
                ", subject='" + subject + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentTransferEncoding='" + contentTransferEncoding + '\'' +
                ", dataText='" + dataText + '\'' +
                ", dataBase64='" + dataBase64 + '\'' +
                '}';
    }
}
