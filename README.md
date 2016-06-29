# truesight-meter-client

A java library for interacting with the TrueSight meter.

# Motivation

Primarily this library was developed to provide a way to ship dropwizard metrics from a java process into the Pulse backend.

# Installation

## Prerequisites

By default it is expected that a meter process is running locally.

## Maven

```xml

    <dependency>
        <groupId>com.bmc.truesight.saas</groupId>
        <artifactId>meter-client</artifactId>
        <version>0.8</version>
    </dependency>
```

# Usage

## Create the client

```java
        TruesightMeterRpcClientConfig config = new TruesightMeterRpcClientConfig();
        // set config values if you are running the meter on non-standard host/port
        TruesightMeterRpcClient client = new TruesightMeterRpcClient(config);
        client.connect();
```

## Add Measurements

```java
        Measure measure = Measure.of("my_measurement", 2.3);
        client.addMeasure(measure);
```


## Add Events

```java
        Event myEvent = Event.of("Test Event", Severity.warn);
        client.addEvent(myEvent);
```

## Close the client

```java
        client.close();
```

# Tests

To run the tests, clone the repo and run `mvn test` from the parent directory.

# LICENSE

&copy; Copyright 2015-2016 BMC SOFTWARE, INC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


![BMC Open Source](badge-os.png)
