package bgu.spl.net.impl.tftp;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;


public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> concurrentHashMap;

    public ConnectionsImpl() {
        concurrentHashMap = new ConcurrentHashMap<>();
    }
    @Override
    public boolean connect(int connectionId, ConnectionHandler<T> handler){
        return concurrentHashMap.putIfAbsent(connectionId, handler) == null;
    }

    @Override
    public boolean send(int connectionId, T msg){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.remove(connectionId);
            return;
        }
        throw new IllegalArgumentException("No such connectionId");
    }

}
