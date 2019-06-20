## "How will you die in a horror movie?"
This project is a ["How will you die in a horror movie"](https://www.nyfa.edu/film-school-blog/take-nyfas-horror-movie-death-quiz/)
quiz implemented with [Clips JNI](http://clipsrules.sourceforge.net/).

### Build
To build the application you should first build Clips rules and message resources.

To do so simply go into the `./build` directory and run `Data.js` with NodeJS:
```bash
node ./Data.js
```
This will create `Horror.clp` and `Horror.properties` files inside `./resources`.

Afterwards you can compile all Java classes and package them all inside a JAR,
along with all resources you've generated with NodeJS.


### Run
To run the application use the following command:
```bash
java -Djava.library.path=lib -jar Horror.jar
```
This commands assumes `Horror.jar` is in you current working directory and Clips library files are under `lib` directory.
