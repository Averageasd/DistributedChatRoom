import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageLogger implements Serializable {

    protected List<String> messages;

    public MessageLogger() {
        messages = new ArrayList<>();
    }
}
