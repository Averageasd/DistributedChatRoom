import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerLocks {

    protected static final Lock MESSAGE_LOGGER_LOCK = new ReentrantLock();
    protected static final Lock SOCKET_LIST_LOCK = new ReentrantLock();

}
