package cc.seeed.ap.webapi;

import java.util.List;
import java.util.Map;

import cc.seeed.ap.webapi.model.OtaStatusResponse;
import cc.seeed.ap.webapi.model.PropertyResponse;
import cc.seeed.ap.webapi.model.GroverDriver;
import cc.seeed.ap.webapi.model.NodeListResponse;
import cc.seeed.ap.webapi.model.NodeResponse;
import cc.seeed.ap.webapi.model.Response;
import cc.seeed.ap.webapi.model.WellKnownResponse;
import cc.seeed.ap.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by tenwong on 15/6/23.
 */
public interface IotService {
    /**
     * User manage APIs
     */

    @POST("/user/create")
    public void userCreate(@Query("email") String email, @Query("password") String password,
                           Callback<UserResponse> callback);

    @POST("/user/login")
    public void userLogin(@Query("email") String email, @Query("password") String password,
                          Callback<UserResponse> callback);

    @POST("/user/changepassword")
    public void changePassword(@Query("password") String newPwd,
                               @Query("access_token") String user_token,
                               Callback<UserResponse> callback);


    /**
     * Node manage APIs
     * Before call, set Hearer's Authorization, user_key
     */

    @POST("/nodes/create")
    public void nodesCreate(@Query("name") String node_name, Callback<NodeResponse> callback);

    @GET("/nodes/list")
    public void nodesList(Callback<NodeListResponse> callback);

    @POST("/nodes/rename") //Todo add another value, such as node_sn! Test it
    public void nodesRename(@Query("name") String node_name,
                            @Query("node_sn") String node_sn,
                            Callback<NodeResponse> callback);

    @POST("/nodes/delete")
    public void nodesDelete(@Query("node_sn") String node_sn, Callback<NodeResponse> callback);

    @POST("/user/download")
    public void userDownload(@Query("access_token") String node_key,
                             @Query("yaml") String yaml,
                             Callback<OtaStatusResponse> callback);


    @POST("/ota/status")
    public void otaStatus(@Query("access_token") String node_key,
                             Callback<OtaStatusResponse> callback);
    /**
     * Grove driver scanning APIs
     */

    @GET("/scan/drivers")
    public void scanDrivers(Callback<List<GroverDriver>> callback);

    @GET("/scan/status")
    public void scanStatus(Callback<Response> callback);


    /**
     * Node property call APIs
     * Before call, send node_key Authorization, node_key
     */
    @GET("/node/.well-known")
    public void nodeWellKnown(Callback<WellKnownResponse> callback);


    //  node/Grove_Example1/temp
    //  node/Grove_Example1/with_arg/90
    @GET("/node/{propertyArgs}")
    public void readPropertyArg1(@Path("propertyArgs") String property,
                                 Callback<PropertyResponse> callback);


    //  ...-d "a=10&b=10.5&c=445566" https://iot.yuzhe.me/v1/node/Grove_Example1/multi_value
    @POST("/node/{property}")
    public void writePropert(@Path("property") String property, @QueryMap Map<String, Object> option,
                             Callback<Response> callback);
}
