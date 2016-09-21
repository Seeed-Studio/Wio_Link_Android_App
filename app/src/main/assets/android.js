 function bridgeCall(params) {
     prompt(params,'call:');
 };

 function addJsFile(path) {
      var head = document.getElementsByTagName('head').item(0);
      var script = document.createElement('script');
      script.setAttribute('type', 'text/javascript');
      script.setAttribute('src', path);
      head.appendChild(script);
 };


