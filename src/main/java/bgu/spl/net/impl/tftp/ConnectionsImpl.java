package bgu.spl.net.impl.tftp;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.Pair;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, Pair<ConnectionHandler<T>, Boolean>> concurrentHashMap;


    public ConnectionsImpl() {
        concurrentHashMap = new ConcurrentHashMap<>();
    }
    @Override
    public boolean connect(int connectionId, ConnectionHandler<T> handler){
        return concurrentHashMap.putIfAbsent(connectionId, new Pair<>(handler, false)) == null;
    }

    @Override
    public void logIn(int connectionId){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.get(connectionId).setSecond(true);
            return;
        }
        throw new IllegalArgumentException("No such connectionId");
    }
    
    @Override
    public boolean isConnected(int connectionId){
        if(!concurrentHashMap.containsKey(connectionId)){
            return false;
        }
        return concurrentHashMap.get(connectionId).getSecond();
    }

    @Override
    public boolean send(int connectionId, T msg){
        if(concurrentHashMap.containsKey(connectionId)){
            concurrentHashMap.get(connectionId).getFirst().send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId){
        if(concurrentHashMap.containsKey(connectionId)){
            try{
                concurrentHashMap.get(connectionId).getFirst().close();
                concurrentHashMap.remove(connectionId);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return;
        }
        throw new IllegalArgumentException("No such connectionId");
    }
    @Override
    public boolean sendBrodcast(T msg) {
        boolean ans = true;
        for (Integer connectionId : concurrentHashMap.keySet()) {

            if(!send(connectionId, msg)){
                ans = false;
            }
        }
        return ans;
    }
    
}
