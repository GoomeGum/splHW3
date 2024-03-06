import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.util.Pair;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> concurrentHashMap;

    public ConnectionsImpl() {
        concurrentHashMap = new ConcurrentHashMap<>();
    }
    @Override
    boolean connect(int connectionId, ConnectionHandler<T> handler){
        if(concurrentHashMap.putIfAbsent(connectionId, handler) == null)
            return true;
        return false;
    }

    @Override
    boolean send(int connectionId, T msg){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    void disconnect(int connectionId){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.remove(connectionId);
            return;
        }
        throw new IllegalArgumentException("No such connectionId");
    }
}
