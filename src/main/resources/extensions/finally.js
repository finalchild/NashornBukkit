var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();

var logger = script.logger;

Function.prototype.on = function(event, eventPriority) {
  if (eventPriority == undefined) {
    script.on(event, this);
  } else {
    script.on(event, this, eventPriority)
  }
}

Function.prototype.onCommand = function(name) {
  script.onCommand(name, this);
}
