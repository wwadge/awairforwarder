# Intro 

This app will move PLANET funds from the wallets you specify to a target wallet.

# How to run

1. Create a config file that looks like this

```yaml
   planets:
     from:
       - name: Sensor 1    
         seed: xxx your seed words here space separated 
         percentageToTransfer: 1.0   # Transfer 100% of the funds here
       - name: Some other fancy name for your reference
         seed: xxx xxx your seed words here space separated
         percentageToTransfer: 0.5   # Transfer 50% of the funds 
     to: AJW...    # Your target wallet address 
     # For example, "0 * * * * MON-FRI" means once per minute on weekdays
     # (at the top of the minute - the 0th second).
     # The fields read from left to right are interpreted as follows.
     # second
     # minute
     # hour
     # day of month
     # month
     # day of week
     # The special value "-" indicates a disabled cron i.e. a 1 shot
     schedule: "-"

```

2. Run it:

a) Via docker: 

`docker run -it -v /path/to/your/configfile.yaml:/tmp/application.yaml  wwadge/planetsforwarder --spring.config.location=/tmp/application.yaml`

What we're doing here is mounting your file external to docker ("/path/to/your/configfile.yaml") to /tmp/application.yaml and telling the app to use that config internally.

b) Or build the code: `./gradlew build` and run using java: `java -jar build/libs/planetsforwarder-0.0.1-SNAPSHOT.jar`

c) Or build and run: `./gradlew bootRun`


3. Scheduling

You can either run this app via its in-built scheduler or as a 1-shot (do the transfer and quit). 

Example: You can keep it running and the scheduler will fire at 1pm or
you can drive the schedule externally (eg a cloud build) where the code does its work and quits.

Please look in the sample config file for details.


# Useful?

Found this app useful? Donate PLANET/ALGO to this address: 4QHBG2ZT5W7I6ULG4OTSZMAUB7MDGBDNKVG4DJIUTKA2STQNV7YMH4IVFQ 

