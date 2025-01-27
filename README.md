## Introduction

Welcome in SmartCity!   
We hope, you will have a nice journey.  


![image](https://user-images.githubusercontent.com/33404585/102254512-fce3ae00-3f08-11eb-8b6e-897cd7f48b52.png)


## Build guide

To run `build.sh \ build.ps1` script following dependencies are required:  

1. Maven >= 3.6.0: 
Download link: https://maven.apache.org/download.cgi   
Download maven archive from provided website, unpack it and add its `bin` directory to system PATH variable.   
See also: https://maven.apache.org/install.html  
Check by `mvn --version`

2. Java >= 14   
Download link: https://jdk.java.net/java-se-ri/14
Check by `"$JAVA_HOME/bin/java" --version` for bash
Check by `& "$Env:JAVA_HOME\bin\java.exe" --version` for powershell

3. Node.js >= 14.15.1  
Download link: https://nodejs.org/en/download/  
Check by `node --version`

## Run guide

To run `run.sh \ run.ps1` script following dependencies are required:  

1. Java >= 14
2. Node.js >= 14.15.1  
3. Serve ( `npm install -g serve`)

See above for instructions.

Run `run.sh  \ run.ps1` script to boot the application.    
By default it will be present on address: `localhost:5000` - it should open in your browser automatically.   

### Browser
See https://create-react-app.dev/docs/supported-browsers-features/ for browser compatibility guide.
It is not guaranteed that app will work in IE 11, 10, 9 or older. 


## Docker
To use in docker: 
```
    docker build -t smart_city-image .
    docker run -dit -p 4000:4000 -p 9000:9000 -p 5000:5000 
       --name smartCity smart_city-image:latest
```
Then open your browser on `localhost:5000`.
