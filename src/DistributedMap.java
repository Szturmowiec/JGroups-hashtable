import java.io.Serializable;

public class DistributedMap implements SimpleStringMap,Serializable{
    private int size=100;
    HashEntry[] t=new HashEntry[size];

    public DistributedMap(){
        for (int i=0; i<size; i++) t[i]=null;
    }

    public Integer get(String key){
        char[] ch=key.toCharArray();
        int hash=0;
        for (char s : ch) hash+=(int)s;
        hash=hash%size;

        if (!this.containsKey(key)) return -1;
        while (!t[hash].getKey().equals(key)) hash=(hash+1)%size;
        return t[hash].getValue();
    }

    public void put(String key,Integer value){
        char[] ch=key.toCharArray();
        int hash=0;
        for (char s : ch) hash+=(int)s;
        hash=hash%size;

        if (this.containsKey(key)) while (!t[hash].getKey().equals(key)) hash=(hash+1)%size;
        else while (t[hash]!=null) hash=(hash+1)%size;
        t[hash]=new HashEntry(key,value);
    }

    public boolean containsKey(String key){
        for (int i=0; i<size; i++){
            if (t[i]!=null && t[i].getKey().equals(key)) return true;
        }
        return false;
    }

    public Integer remove(String key){
        for (int i=0; i<size; i++){
            if (t[i]!=null && t[i].getKey().equals(key)){
                Integer x=t[i].getValue();
                t[i]=null;
                return x;
            }
        }
        return -1;
    }

    public HashEntry[] getTable(){
        return t;
    }

    public void setTable(HashEntry[] t){
        this.t=t;
    }

    public void clear(){
        this.t=new HashEntry[size];
    }

    public void addAll(DistributedMap t){
        this.setTable(t.getTable());
    }
}