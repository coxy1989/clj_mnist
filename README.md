# clj_mnist

MNIST handwritten digit classification with clojure.

## Walkthrough

1. Train a network with `boot train`

![Alt Text](./README/output.gif)

2. Drop the `network.json` spat out by the previous process into the `web_resources` folder. Then run a single page web app on `localhost:3000` with `boot run-web`, draw some digits:

|![Alt Text](./README/1.gif) |![Alt Text](./README/2.gif)|![Alt Text](./README/3.gif)|
|---|---|---|
| ![Alt Text](./README/4.gif)|![Alt Text](./README/5.gif)|![Alt Text](./README/6.gif)|
| ![Alt Text](./README/7.gif)|![Alt Text](./README/8.gif)|![Alt Text](./README/9.gif)|


## Usage

You must have docker installed on your system.

1. `docker build -t mnist .`

2. `docker run -it -P --entrypoint='bash' --name mnist -v ${PWD}:/mnist mnist`

3. `cd mnist`

4. `boot train` or `boot run-web`

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
