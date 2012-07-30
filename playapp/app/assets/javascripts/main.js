(function(){

  var VOLUME_SIZE = 60;
  
  var uniqueId = Math.random(); // OK this is a hacky!

  function syncVolumeControl (node) {
    var v = parseFloat(node.attr("data-value"));
    var min = parseFloat(node.attr("data-min")||0);
    var max = parseFloat(node.attr("data-max")||1);
    v = (v-min)/(max-min);
    var rotate = "rotate("+Math.round(280*(v-0.5))+"deg)";
    node.find(".volume-handle").css({
      "-webkit-transform": rotate,
      "-moz-transform": rotate,
      "-ms-transform": rotate,
      "-o-transform": rotate,
      "transform": rotate
    });
  }

  $(".volume-control").each(function(){
    syncVolumeControl($(this));
  });

  function getRelativePosition (e, node) {
    var position = node.offset();
    var x = e.clientX-(position.left+node.width()/2);
    var y = e.clientY-(position.top+node.height()/2);
    return { x: x, y: y };
  }

  function updateVolumeControl (e, node) {
    var p = getRelativePosition(e, node);
    var clickP = node.data("clickPosition");
    var clickV = node.data("clickValue");
    var min = parseFloat(node.attr("data-min")||0);
    var max = parseFloat(node.attr("data-max")||1);
    var a = (p.x-clickP.x)/VOLUME_SIZE;
    a = a*(max-min);
    var v = clickV+a;
    v = Math.max(min, Math.min(v, max));
    node.attr("data-value", v).trigger("change");
    syncVolumeControl(node);
  }

  $(".volume-control").live("mousedown", function (e) {
    e.preventDefault();
    var node = $(this);
    node.data("clickValue", parseFloat(node.attr("data-value")));
    node.addClass("mousedown").data("clickPosition", getRelativePosition(e, node));
  });
  $(document).on("mouseup", function (e) {
    var node = $(".volume-control.mousedown").removeClass("mousedown");
    if (node.size())
      updateVolumeControl(e, node);
  });
  $(document).on("mousemove", function (e) {
    var node = $(".volume-control.mousedown:first");
    if (node.size()) {
      e.preventDefault();
      updateVolumeControl(e, node);
    }
  });

  (function(){
   $("#stream").one("play", main);

   var status = $("#status");
   var error = $("#error");
   $("#stream").on("error pause ended waiting", function() {
     status.toggleClass("enabled", false);
     });
   $("#stream").on("play", function() {
     status.toggleClass("enabled", true);
   });

   var actions = {
     "osc-freq": function (o) {
      var node = $("#channel"+o.osc+" .sound-pitch .volume-control");
      if (!node.hasClass("mousedown")) {
        node.attr("data-value", o.value);
        syncVolumeControl(node);
      }
    },
    "osc-volume": function (o) {
      var node = $("#channel"+o.osc+" .sound-volume .volume-control");
      if (!node.hasClass("mousedown")) {
        node.attr("data-value", o.value);
        syncVolumeControl(node);
      }
    },
    "osc-wave": function (o) {
      $("#channel"+o.osc+" .wave."+o.value).click();
    }
   }

   var sock = new WebSocket(WEBSOCKET_CONTROLS);
   sock.onmessage = function (o) { 
    handleAction(JSON.parse(o.data)); 
   }
   sock.onerror = function () {
    error.text("An error occured with the WebSocket connection...");
   }

   function send (o) {
     sock.send(JSON.stringify(o));
   }

   function handleAction (o) {
     if (o.myId == uniqueId) return;
     if (actions[o.type])
       actions[o.type](o);
   }

   function getChannel (node) {
     return parseInt(node.parents("div.channel:first").attr("data-channel"));
   }

   function main () {
     $(".sound-wave input").live("change", function(e){
         var $this = $(this),
         chan = getChannel($this),
         wave = $this.val();
         send({ type: "osc-wave", osc: chan, value: wave });
         })

     $(".sound-volume .volume-control").live("change", function(e){
         var $this = $(this),
         chan = getChannel($this),
         volume = parseFloat($this.attr("data-value"));
         send({ type: "osc-volume", osc: chan, value: volume });
     });
     
     $(".sound-pitch .volume-control").live("change", function(e){
         var $this = $(this),
         chan = parseInt($this.parents("div.channel:first").attr("id").replace(/[^0-9]/g, ""), 10),
         pitch = parseFloat($this.attr("data-value"));
         send({ type: "osc-freq", osc: chan, value: pitch });
     });

     send({ type: "connect", msg: "ready", myId: uniqueId });
     animatingTitle();
   }



  }());

  $(".sound-wave input").change(function(){
    var checked = $(this).is(":checked");
    var label = $(this).parent();
    label.toggleClass("enabled", checked);
    if (checked)
      label.siblings().toggleClass("enabled", false);
  }).change();

  function animatingTitle () {
    (function(i, letters){
      setInterval(function(){
        ++i;
        var mod30 = i%50;
        if (mod30 < 14) {
          for (var j=0; j<letters.size(); ++j) {
            letters.eq(j).toggleClass("enabled", mod30 > 2)
        mod30 -= 2;
          }
        }
        else {
          if (mod30 < 25) {
            letters.toggleClass("enabled", mod30 % 2 == 0);
          }
        }
      }, 200);
    }(0, $("header h1 .letter")));
  }

}());
