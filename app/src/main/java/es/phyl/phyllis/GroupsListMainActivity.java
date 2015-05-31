package es.phyl.phyllis;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import es.phyl.phyllis.helper.GroupList;
import es.phyl.phyllis.helper.HubConnectionFactory;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.hubs.HubProxy;


public class GroupsListMainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups_list_main);
        getGroups();

    }

    private void getGroups() {
        final HubConnectionFactory hcf = HubConnectionFactory.getInstance();
        HubProxy hub = hcf.getChatHub();
        hcf.getHubConnection().received(new MessageReceivedHandler() {
            @Override
            public void onMessageReceived(JsonElement jsonElement) {
                Gson gson = new GsonBuilder().create();
                GroupList result = gson.fromJson(jsonElement, GroupList.class);
                Log.d(GroupsListMainActivity.class.getSimpleName(), result.items.toString());
            }
        });
        hub.invoke(HubConnectionFactory.GET_GROUPS);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groups_list_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
