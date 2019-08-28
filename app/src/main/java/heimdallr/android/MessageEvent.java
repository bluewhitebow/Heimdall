package heimdallr.android;

public class MessageEvent {
    private String message;

    public MessageEvent(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }
    public void SetMessage(String message)
    {
        this.message = message;
    }

}
