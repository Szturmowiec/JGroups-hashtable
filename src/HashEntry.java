import java.io.Serializable;

public class HashEntry implements Serializable{
    private String key;
    private Integer value;

    HashEntry(String key,Integer value){
        this.key=key;
        this.value=value;
    }

    public String getKey(){
        return key;
    }

    public Integer getValue(){
        return value;
    }
}