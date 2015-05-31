package es.phyl.phyllis.helper;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Libra on 2015-05-31.
 */
public class GroupItem {
    //{"Id":1,"Name":"Phyles"}
    @SerializedName("Id")
    public String id;
    @SerializedName("Name")
    public String name;

    @Override
    public String toString() {
        return "GroupItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
