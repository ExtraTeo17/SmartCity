## Introduction
The long_runner.sh script is used for strategy testing.
It runs the simulation **x** times, where **x** is parameter provided by the user.
It does not need, nor use the GUI, so to avoid running it many times you might want to
**comment lines** starting with `serve` and `start` in `run.sh` script.

## Usage

To use long_runner.sh add following lines to appropriate test-object handle
in SmartCityAgent class:

```java
if (resultTime != null) {
    logger.info("$---$");
    logger.info(resultTime.toString());
    Codec codec = new SLCodec();
    var jmo = JADEManagementOntology.getInstance();
    getContentManager().registerLanguage(codec);
    getContentManager().registerOntology(jmo);
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.addReceiver(getAMS());
    msg.setLanguage(codec.getName());
    msg.setOntology(jmo.getName());
    try {
        getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
        send(msg);
    }
    catch (Exception e) {}
}
```
You may also need to add some imports.
To change parameters of simulation, go to connect.js script.

Script usage
```
./long_runner.sh 10
```


To find the results in logs, search for '$--$'.



