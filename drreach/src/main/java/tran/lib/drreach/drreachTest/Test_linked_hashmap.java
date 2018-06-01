package tran.lib.drreach.drreachTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Test_linked_hashmap {

    public static void main(String[] arg){

        LinkedHashMap<Double, List<String>> reach = new LinkedHashMap<>();

        double key = 1;
        List<String> value = new ArrayList<>();
        value.add("cet");
        value.add("cet2");

        reach.put(key, value);
        //value.clear();
        key = 2;
        value = new ArrayList<>();
        value.add("cet4");
        reach.put(key, value);
        //value.clear();

        Set<Double> keys = reach.keySet();

        for (Double k:keys){
            System.out.println(k + "--\n");
            List<String> val = reach.get(k);
            System.out.println(val);
        }

    }

}
