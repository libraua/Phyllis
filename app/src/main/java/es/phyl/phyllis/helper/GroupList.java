package es.phyl.phyllis.helper;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Libra on 2015-05-31.
 */
public class GroupList {
    //{"H":"MainHub","M":"receiveGroup","A":[{"Id":1,"Name":"Phyles"}]}
    @SerializedName("H")
    public String hubName;
    @SerializedName("M")
    public String methodName;
    @SerializedName("A")
    public List<GroupItem> items;
}
