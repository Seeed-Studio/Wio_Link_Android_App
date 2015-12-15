package cc.seeed.iot.util;

import android.util.Log;

import com.activeandroid.serializer.TypeSerializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
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
        Map<String, List<String>> map = (Map<String, List<String>>) o;
        String json = gson.toJson(map);
        return json;
    }

    @Override
    public Map<String, List<String>> deserialize(Object o) {
        if (o == null) {
            return null;
        }
        Gson gson = new Gson();
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        Type type = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        map = gson.fromJson((String) o, type);
        return map;
    }
}
