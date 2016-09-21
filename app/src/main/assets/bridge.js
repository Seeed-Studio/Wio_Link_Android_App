 var JSBridge = {
     queue: [],
     callback: function() {
         var args = Array.prototype.slice.call(arguments, 0);
         var index = args.shift();
         console.log("onCallbck "+args)
         this.queue[index].apply(this,args);
         delete this.queue[index];
     },
     run:function() {
         var args = Array.prototype.slice.call(arguments, 0);
         console.log("args "+args)
         if (args.length < 1) {
             throw "call error, message:miss method name";
         }
         var aTypes = [];
         for (var i = 1; i < args.length; i++) {
             var arg = args[i];
             var type = typeof arg;
             aTypes[aTypes.length] = type;
             if (type == "function") {
                 var index = this.queue.length;
                 this.queue[index] = arg;
                 args[i] = index;
             }
         }
         var params = JSON.stringify({
             method: args.shift(),
             types: aTypes,
             args: args
         });
         bridgeCall(params);
    }
 }

 