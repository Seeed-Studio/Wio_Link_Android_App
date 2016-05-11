var HostApp = {
    getImei:function(callback) {
        return JSBridge.run("getImei",callback)
    },

    isLogin:function(callback) {
            return JSBridge.run("isLogin",callback)
    },

    getToken:function(callback) {
            return JSBridge.run("getToken",callback)
    },

    delayJsCallback:function(l,s,d,callback) {
        return JSBridge.run("delayJsCallback",l,s,d,callback)
    },

    nativePopTip:function(jsonObj) {
           return JSBridge.run("nativePopTip", jsonObj.text, jsonObj.time, jsonObj.callback)
      },

      nativePay:function(jsonObj) {
           return JSBridge.run("nativePay", jsonObj.payParams,jsonObj.onSuccess,jsonObj.onFail)
       },

    nativeAction:function(jsonObj) {
            return JSBridge.run("nativeAction",jsonObj.action,jsonObj.parameter);
       },

    toNativePage:function(jsonObj) {
           return JSBridge.run(jsonObj.action,jsonObj.parameter);
    },
     nativeEventReq:function(jsonObj) {
                return JSBridge.run("nativeEventReq",jsonObj.action);
           },

 }