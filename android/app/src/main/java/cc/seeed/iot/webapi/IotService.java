package cc.seeed.iot.webapi;

import java.util.Map;

import cc.seeed.iot.webapi.model.CommonResponse;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.NodeConfigResponse;
import cc.seeed.iot.webapi.model.NodeJson;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.NodeResponse;
import cc.seeed.iot.webapi.model.OtaStatusResponse;
import cc.seeed.iot.webapi.model.PropertyResponse;
import cc.seeed.iot.webapi.model.UserResponse;
import cc.seeed.iot.webapi.model.WellKnownResponse;
import cc.seeed.iot.yaml.NodeConfig;
import retrofit.Callback;
import retrofit.http.Body;
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

    @POST("/v1/user/create")
    public void userCreate(@Query("email") String email, @Query("password") String password,
                           Callback<UserResponse> callback);

    @POST("/v1/user/login")
    public void userLogin(@Query("email") String email, @Query("password") String password,
                          Callback<UserResponse> callback);

    @POST("/v1/user/changepassword")
    public void userChangePassword(@Query("password") String newPwd,
                                   @Query("access_token") String user_token,
                                   Callback<UserResponse> callback);

    @POST("/v1/user/retrievepassword")
    public void userRetrievePassword(@Query("email") String email, Callback<CommonResponse> callback);

    /**
     * Node manage APIs
     * Before call, set Hearer's Authorization, user_key
     */

    @POST("/v1/nodes/create")
    public void nodesCreate(@Query("name") String node_name, Callback<NodeResponse> callback);

    @GET("/v1/nodes/list")
    public void nodesList(Callback<NodeListResponse> callback);

    @POST("/v1/nodes/rename")
    public void nodesRename(@Query("name") String node_name,
                            @Query("node_sn") String node_sn,
                            Callback<NodeResponse> callback);

    @POST("/v1/node/setting/dataxserver/{ip}")
    public void nodeSettingDataxserver(@Path("ip") String property, Callback<CommonResponse> callback);

    @POST("/v1/nodes/delete")
    public void nodesDelete(@Query("node_sn") String node_sn, Callback<NodeResponse> callback);

    @GET("/v1/node/config")
    public void nodeConfig(Callback<NodeConfigResponse> callback);

    @POST("/v1/user/download")
    public void userDownload(@Body NodeJson nodeJson,Callback<OtaStatusResponse> callback);

    @POST("/v1/ota/status")
    public void otaStatus(@Query("access_token") String node_key,
                          Callback<OtaStatusResponse> callback);

    /**
     * Grove driver scanning APIs
     */

    @GET("/v1/scan/drivers")
    public void scanDrivers(Callback<GroveDriverListResponse> callback);

    @GET("/v1/scan/status")
    public void scanStatus(Callback<CommonResponse> callback);


    /**
     * Node property call APIs
     * Before call, send node_key Authorization, node_key
     */
    @GET("/v1/node/.well-known")
    public void nodeWellKnown(Callback<WellKnownResponse> callback);


    //  node/Grove_Example1/temp
    //  node/Grove_Example1/with_arg/90
    @GET("/v1/node/{propertyArgs}")
    public void readPropertyArg1(@Path("propertyArgs") String property,
                                 Callback<PropertyResponse> callback);


    //  ...-d "a=10&b=10.5&c=445566" https://iot.yuzhe.me/v1/node/Grove_Example1/multi_value
    @POST("/v1/node/{property}")
    public void writePropert(@Path("property") String property, @QueryMap Map<String, Object> option,
                             Callback<CommonResponse> callback);
}
