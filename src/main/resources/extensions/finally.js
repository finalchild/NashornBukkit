var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();

var logger = script.logger;

function on(event, listener, eventPriority) {
  var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();
  var pluginManager = nashornBukkit.getServer().getPluginManager();
  var NBListener = Java.type("me.finalchild.nashornbukkit.event.NBListener");
  var NBEventExecutor = Java.type("me.finalchild.nashornbukkit.event.NBEventExecutor");

  if (typeof event === "function") {
    event = event.class;
  }
  if (eventPriority == undefined) {
    eventPriority = EventPriority.NORMAL;
  }

  var func = function(event) {
    (listener.bind(event))(event);
  };

  pluginManager.registerEvent(event, NBListener.INSTANCE, EventPriority.NORMAL, new NBEventExecutor(func), nashornBukkit);
}

Function.prototype.on = function(event, eventPriority) {
  var listener = this;
  var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();
  var pluginManager = nashornBukkit.getServer().getPluginManager();
  var NBListener = Java.type("me.finalchild.nashornbukkit.event.NBListener");
  var NBEventExecutor = Java.type("me.finalchild.nashornbukkit.event.NBEventExecutor");

  if (typeof event === "function") {
    event = event.class;
  }
  if (eventPriority == undefined) {
    eventPriority = EventPriority.NORMAL;
  }

  var func = function(event) {
    (listener.bind(event))(event);
  };

  pluginManager.registerEvent(event, NBListener.INSTANCE, EventPriority.NORMAL, new NBEventExecutor(func), nashornBukkit);
}

function onCommand(name, executor) {
  var NBCommandUtil = Java.type("me.finalchild.nashornbukkit.command.NBCommandUtil");
  NBCommandUtil.register(name, executor);
}

Function.prototype.onCommand = function(name) {
  var executor = this;
  var NBCommandUtil = Java.type("me.finalchild.nashornbukkit.command.NBCommandUtil");
  NBCommandUtil.register(name, executor);
}
