package cc.seeed.iot.ui_main;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    Button bt_userCreate;
    TextView tv_status, tv_msg;
    ListView li_nodes;
    Context context;
    SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_node_list, container, false);
//        bt_userCreate = (Button) view.findViewById(R.id.bt_userCreate);
//        tv_status = (TextView) view.findViewById(R.id.tv_status);
//        tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        li_nodes = (ListView) view.findViewById(R.id.list_nodes);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        sp = context.getSharedPreferences("IOT", Context.MODE_PRIVATE);

        IotApi api = new IotApi();
        api.setAccessToken(sp.getString("userToken", ""));
        final IotService iot = api.getService();
        bt_userCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                iot.nodesList(new Callback<NodeListResponse>() {
                    @Override
                    public void success(NodeListResponse nodeListResponse, Response response) {
                        Log.d("iot", "success");
                        tv_status.setText(nodeListResponse.status);
                        tv_msg.setText(nodeListResponse.msg);

//                        ArrayList<Node> nodes = new ArrayList<Node>();
                        ArrayList<Node> nodes = (ArrayList) nodeListResponse.nodes;

                        NodeListAdapter adapter = new NodeListAdapter(context, nodes);

                        li_nodes.setAdapter(adapter);
//                        Log.e("iot", nodeListResponse.nodes.get(0).name);
//                        saveData(user);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("iot", "fail");
                        Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_LONG).show();
                    }
                });


            }
        });


    }

}