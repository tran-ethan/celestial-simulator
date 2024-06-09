# Celestial Simulator
A fully interactive 3D n-body simulation using Barnes-Hut algorithm with gravitational vector fields

## Getting started
### Prerequisites
Before you can build and run this project, ensure you have the following software installed on your system:
1. Oracle OpenJDK 21.0.2 - You can download the JDK [here](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html).
2. Gradle 8.5

### Installation
Make sure Git is installed on your system before continuing with the installation
1. Clone the repository
```shell
git clone https://github.com/exisodd/celestial-simulator.git
```
2. Navigate to the project directory
```shell
cd celestial-simulator
```
3. Build the project
```shell
./gradlew build
```

## Usage
### Using Gradle Wrapper
To run the application using the Gradle Wrapper, execute the following command:
```shell
./gradlew run
```

### Running MainApp
You can execute the program by executing the `main` method in the `MainApp` class directly from within your IDE. Ensure your IDE is configured to use Oracle OpenJDK 21.0.2 and has the necessary dependencies in `build.gradle` installed.

### Executing the JAR file
To run the compiled JAR file, execute the following command:
```shell
java -jar ?
```

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for further details. Planet textures were sourced from [Solar System Scope](https://www.solarsystemscope.com/textures/) in accordance with the Attribution 4.0 International License.