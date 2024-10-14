# Celestial Simulator
A fully interactive 3D **n-body** simulation using Barnes-Hut algorithm with gravitational vector fields

https://github.com/exisodd/celestial-simulator/assets/96459404/30d4bb50-aad8-489f-a7cc-1052034a7dfe

## Features
- **N-Body simulation**: Predicting the trajectory of celestial objects under the influence gravity according to Newton's Law of Universal Gravitation
- **Gravitational fields**: Toggle on/off vector fields for gravity
- **Algorithms**: Switch between Direct Sum $O(n^2)$ and Barnes-Hut algorithm $O(n \log n)$ for computing gravitational forces. Option to change Barnes-Hut Criterion (threshold for estimation) and toggling visualization for Barnes-Hut quadrants
- **Camera Controls**: Panning, zooming (SCROLL), rotating (RMB), position resetting, locking in 2D view, follow planets, automatic rotation around axis
- **Interacting with bodies**: Create bodies with custom radius, mass, textures/colors, positions, velocity. Select/delete bodies, and remove all bodies
- **Simulation parameters**: Pausing/playing, simulation speed, gravitational constant, toggling grid/axes
- **Saving and Loading**: Save current state and layout of planets into files that can be loaded later after the application is closed
- **Presets**: Comes with many presets including a 3-body simulation, a solar system preset and a randomly generated layout

## Getting started
### Prerequisites
Before you can build and run this project, ensure you have the following software installed on your system:
- [Oracle OpenJDK 21.0.2](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

### Installation
Make sure Git is installed on your system before continuing with the installation
1. Clone the repository
```shell
git clone https://github.com/tran-ethan/celestial-simulator.git
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
Make sure Oracle OpenJDK 21.0.2 is properly configured in the `JAVA_HOME` environment variable before trying this method. To run the application using the Gradle Wrapper, execute the following command:
```shell
./gradlew run
```

### Using IDE
You can run the program by directly executing the `main` method in the `MainApp` class directly from within your IDE. Ensure your IDE is configured to use the JDK 21 and has the necessary dependencies in `build.gradle` installed.

## License
This project is licensed under the [MIT License](LICENSE)
