package cc.seeed.iot.util;

import android.util.Log;

import com.activeandroid.serializer.TypeSerializer;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tenwong on 15/11/23.
 */
final public class UtilMapSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Map.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object o) {
        if (o == null) {
            return null;
        }
        Gson gson = new Gson();
        Map<String, Object> map = (Map<String, Object>) o;
        String json = gson.toJson(map);
//        Log.e("Map", json);
        return json;
    }

    @Override
    public Map<String, Object> deserialize(Object o) {
        if (o == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();

        try {
            JSONObject json = new JSONObject((String) o);
            for (Iterator it = json.keys(); it.hasNext(); ) {
                String key = (String) it.next();
                map.put(key, json.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return map;
    }
}
