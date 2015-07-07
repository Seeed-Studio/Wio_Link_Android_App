package cc.seeed.iot.ui_main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tenwong on 15/6/24.
 */
public class NodeListFragment extends Fragment {
    View view;
    Context context;
    TextView txtvName;
    ListView li_nodes;
    SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_node_list, container, false);
        txtvName = (TextView) view.findViewById(R.id.txtvName);
        li_nodes = (ListView) view.findViewById(R.id.list_nodes);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        sp = context.getSharedPreferences("IOT", Context.MODE_PRIVATE);

        IotApi api = new IotApi();
        api.setAccessToken("sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");
        final IotService iot = api.getService();

        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                Log.d("iot", "success");
                ArrayList<Node> nodes = (ArrayList) nodeListResponse.nodes;
                NodeListAdapter adapter = new NodeListAdapter(context, nodes);
                li_nodes.setAdapter(adapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("iot", "fail");
                Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_LONG).show();
            }
        });
    }

}