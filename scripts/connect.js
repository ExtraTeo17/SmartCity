const WebSocket = require('ws');
var ws = new WebSocket("ws://127.0.0.1:9000");
ws.on('open', function open() {
  ws.send("{\"type\":1,\"payload\":\"{\\\"latitude\\\":52.25808,\\\"longitude\\\":21.16241,\\\"radius\\\":610,\\\"generatePedestrians\\\":false}\"}");
});
   
var sent = false;
ws.on('message', function incoming(data) {
  console.log("Incoming data");
  if(sent){
    return;
  }
  
  sent = true;
  ws.send("{\"type\":3,\"payload\":\"{\\\"generateCars\\\":true,\\\"carsLimit\\\":5,\\\"testCarId\\\":5,\\\"generateBatchesForCars\\\":false,\\\"generateBikes\\\":false,\\\"bikesLimit\\\":8,\\\"testBikeId\\\":4,\\\"pedLimit\\\":20,\\\"testPedId\\\":5,\\\"generateTroublePoints\\\":true,\\\"timeBeforeTrouble\\\":5,\\\"generateBusFailures\\\":false,\\\"detectTrafficJams\\\":false,\\\"useFixedRoutes\\\":true,\\\"useFixedTroublePoints\\\":true,\\\"startTime\\\":\\\"2021-01-11T02:47:14.662Z\\\",\\\"timeScale\\\":10,\\\"lightStrategyActive\\\":false,\\\"extendLightTime\\\":20,\\\"stationStrategyActive\\\":false,\\\"extendWaitTime\\\":60,\\\"troublePointStrategyActive\\\":true,\\\"troublePointThresholdUntilIndexChange\\\":50,\\\"noTroublePointStrategyIndexFactor\\\":30,\\\"trafficJamStrategyActive\\\":false,\\\"transportChangeStrategyActive\\\":false}\"}");
  ws.close()
})
