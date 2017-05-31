# Decentralised Self-Driving / Self-Organising Train Simulator (Ferromone Trails)

Ferromone Trails is a concept of using the existing train infrastructure such that trains can self-organise safely, without having to rely on a centralised network. This project is a Java-based simulator created from scratch which was built to investigate the feasibility of this concept.

# Development Status

The simulator application was built under the Department for Transport T-TRIG grant, and since the objectives of the study have already been investigated, the project is no longer actively maintained. However, contributions are more than welcome!

# How to Get It
## Download Release

Download Latest Release: 1.0.0

## Run it!

Start by executing the program (Notice: Java 1.8 is required):

    java -jar train-simulator.jar

Navigate to http://localhost:8080 and follow the intstructions on the page to run your desired simulation.

## Runtime Arguments

### Custom Maps

Maps can be created in YAML format. You can pass custom maps to the program using the argument: 

    java -jar train-simulator.jar --maps=path/to/my_map.yaml,path/to/map/folder,...

### Output folder

By default, the results will be exported to the `./results` directory in the current working directory. This can be changed by passing the runtime argument `--output=my/custom/path`.

# License
This project is released under GPL License. Please review [License file](LICENSE) for more details.
