package bgu.spl.net.srv;

public interface Connections<T> {

    boolean connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    boolean sendBrodcast(T msg);

    boolean isConnected(int connectionId);

    void disconnect(int connectionId);
    void logIn(int connectionId);
}
