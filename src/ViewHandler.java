import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;
import java.util.List;

public class ViewHandler extends Thread {
    JChannel ch;
    MergeView view;

    public ViewHandler(JChannel ch, MergeView view) {
        this.ch=ch;
        this.view=view;
    }

    public void run() {
        List<View> subgroups=view.getSubgroups();
        View tmp_view=subgroups.get(0);
        Address addr=ch.getAddress();
        if(!tmp_view.getMembers().contains(addr)) {
            System.out.println("Out of the merged partition ("+tmp_view+"), will re-acquire the state");
            try {
                ch.getState(null,30000);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else System.out.println("No member with given address");
    }
}