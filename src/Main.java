import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;
import java.io.*;
import java.net.InetAddress;
import java.util.List;

public class Main extends ReceiverAdapter{
    private JChannel channel;
    private final DistributedMap t=new DistributedMap();

    public void start() throws Exception{
        channel=new JChannel(false);
        channel.setReceiver(this);
        ProtocolStack stack=new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("234.128.64.10")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FRAG2());

        stack.init();
        channel.connect("comm");
        channel.getState(null,5000);
    }

    public void viewAccepted(View view){
        if(view instanceof MergeView){
            MergeView tmp=(MergeView)view;
            List<View> subgroups=tmp.getSubgroups();
        }

        System.out.println("\nCurrent instances: "+view);
    }

    private static void handleView(JChannel ch,View v){
        if(v instanceof MergeView){
            ViewHandler handler=new ViewHandler(ch,(MergeView)v);
            handler.start();
        }
    }

    public void getState(OutputStream output) throws Exception{
        synchronized(t){
            Util.objectToStream(t,new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception{
        DistributedMap dm=(DistributedMap)Util.objectFromStream(new DataInputStream(input));
        synchronized(t){
            t.clear();
            t.addAll(dm);
        }
    }

    public void receive(Message msg){
        String m=msg.getObject().toString();
        System.out.println("\nFrom: "+msg.getSrc()+", message: "+m);
        if (m.startsWith("put")){
            String kv=m.substring(4);
            String[] spl=kv.split(" ");
            String key=spl[0];
            Integer value=Integer.parseInt(spl[1]);
            synchronized(t){
                t.put(key,value);
            }
        }
        if (m.startsWith("remove")){
            String key=m.substring(7);
            Integer x;
            synchronized(t){
                x=t.remove(key);
            }
            if (x==-1) System.out.println("\nObject with provided key doesn't exist");
            else System.out.println("\nRemoved value: "+x);
        }
    }

    private void eventLoop(){
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true){
            try{
                System.out.print("\nEnter message: ");
                System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit")){
                    channel.close();
                    break;
                }
                Message msg=new Message(null,null,line);
                if (line.startsWith("get")){
                    String key=line.substring(4);
                    Integer x=t.get(key);
                    if (x==-1) System.out.println("\nObject with provided key doesn't exist");
                    else System.out.println("\nReturned value: "+x);
                }
                else channel.send(msg);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String Args[]) throws Exception{
        System.setProperty("java.net.preferIPv4Stack","true");
        Main m=new Main();
        m.start();
        m.eventLoop();
    }
}