# Docker modern-cljs development environment

Here we will be going over just enough material to get a 
docker container up and running that has what you would end 
[tutorial 18](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md) with.  

At the end you will be able to start the `boot tdd` or `boot repl -c` process 
within [Docker](https://en.wikipedia.org/wiki/Docker_%28software%29) containers.

> NOTE: This is not currently complete and does not provide instructions 
on how to get `boot repl -c` running in docker.

For a more though covering of Docker please see the 
[Get Started with Docker Guide](https://docs.docker.com/mac/) for your operating system.  
You can also read the [Docker documentation](https://docs.docker.com/).

## Install docker

To get started let's install docker.

See the instructions on installing docker on:
- [Linux](https://docs.docker.com/linux/step_one/)
- [Mac OS X](https://docs.docker.com/mac/step_one/)
- [Windows](https://docs.docker.com/windows/step_one/)

## Start the Docker daemon

You can start a docker dameon with:

```bash
sudo docker daemon
```

## Create the Dockerfile

Now let's create a Dockerfile which is what we will build a Docker image from.

```bash
mkdir docker
cd docker
touch Dockerfile
```

Now let's put following in your Dockerfile and save it.

```
# Dockerfile for modern-cljs development environment

FROM  debian:stable

ENV  BOOT_AS_ROOT=yes

RUN  apt-get update -y && \

apt-get install bzip2 git default-jdk curl -y && \

git clone https://github.com/magomimmo/modern-cljs.git && \
cd modern-cljs && \
git checkout se-tutorial-18 && \

cd /usr/local/bin && \
curl -fsSLo boot https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh && \
chmod 755 boot && \
./boot  && \

curl -fsSLo phantomjs.tar.bz2 http://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
tar -jxvf phantomjs.tar.bz2 && \
mv phantomjs-2.1.1-linux-x86_64/bin/phantomjs phantomjs && \
rm -r phantomjs-2.1.1-linux-x86_64/ phantomjs.tar.bz2

WORKDIR  /modern-cljs

ENTRYPOINT  ["boot"]
      
CMD  ["tdd"]
```

## Breaking down the Dockerfile

Let's take a look at what is going on in the Dockerfile.

```
# Dockerfile for modern-cljs development environment
```
This is just some comments at the top to explain what the Dockerfile if about.

```
FROM  debian:stable
```
This is the foundation of our Dockerfile.  Everything else will 
be building on top of a stable version of `Debian`.

```
ENV   BOOT_AS_ROOT=yes
```
This is setting a environment variable that will be required 
for `Boot` to work in a Docker container.

> NOTE: For each `RUN` in the `Dockerfile` an addition image 
layer is created.  So we are using `&&` to combine many commands 
into one `RUN` so only one image layer is created.


> NOTE: The `-y` in the commands are to automate permissions 
for software installations for the container image.

```bash
apt-get update
```
This updates the base operating system for the container.

```bash
apt-get install bzip2 git default-jdk curl
```
This installs `bzip2`, `git`, `OpenJDK` and  `curl`.

```bash
git clone https://github.com/magomimmo/modern-cljs.git && \
cd modern-cljs && \
git checkout se-tutorial-18
```
This is to clone `modern-cljs` and checkout `se-tutorial-18`.

```bash
cd /usr/local/bin && \
curl -fsSLo boot https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh && \
chmod 755 boot && \
./boot
```
This is to install `Boot`.

```bash
curl -fsSLo phantomjs.tar.bz2 http://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
tar -jxvf phantomjs.tar.bz2 && \
mv phantomjs-2.1.1-linux-x86_64/bin/phantomjs phantomjs && \
rm -r phantomjs-2.1.1-linux-x86_64/ phantomjs.tar.bz2
```
This is to install `phantomjs` and do some cleanup afterwards.

```
WORKDIR  /modern-cljs
```
This is to set the working directory to `modern-cljs` so 
that we do not have to change into the directory manually.

```
ENTRYPOINT  ["boot"]
```
This is the default process for a container based on this image.

```
CMD  ["tdd"]
```
`tdd` is the default arguments that will be passed to the default process of `boot`.

## Create docker image

Now that we have our Dockerfile created let's make a Docker image from that Dockerfile.

```bash
sudo docker build -t moderncljs/0 .
```

In this case we have given our Docker image the tag of `moderncljs/0` with the `-t` option.

We used the `.` at the end because we are in the directory where our `Dockerfile` is located.

Let's take a look at the Docker image we just created.

```bash
sudo docker images
```

This will show us something like the following:

```
REPOSITORY    	    TAG		    IMAGE ID		    CREATED		      SIZE
moderncljs/0		latest		4fa780d9c938		2 minutes ago		659.6 MB
debian			    stable		3c5f9479c5c1		4 days ago		  125.1 MB
```

> NOTE: The moderncljs/0 is the docker image we just created.  The debian image 
is what the image is based on and comes from the `FROM  debian:stable` in the `Dockerfile`

## Start the container's default process

Let's start the container container's default process `boot tdd`.

```bash
sudo docker run -it -p 3000:3000 moderncljs/0
```
We are passing run the options `-it` so we can attach and work in the container.
With `-p 3000:3000` we are setting the host port to 3000 and the container port to 3000.
We are using the image with the tag `moderncljs/0`.

This is the expected result:
```bash
Starting reload server on ws://localhost:51336
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2016-04-09 15:33:20.121:INFO::clojure-agent-send-off-pool-0: Logging initialized @94653ms
2016-04-09 15:34:19.508:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2016-04-09 15:34:19.654:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@1fe21544{HTTP/1.1}{0.0.0.0:3000}
2016-04-09 15:34:19.655:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @154187ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

nREPL server started on port 49448 on host 127.0.0.1 - nrepl://127.0.0.1:49448
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
? main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 61 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 4 tests containing 62 assertions.
0 failures, 0 errors.
Writing target dir(s)...
Elapsed time: 171.654 sec
```
> NOTE: The first time through it also includes many lines 
about retrieve jar files which have been excluded here.

You should now be able to connect to [shopping.html](http://localhost:3000/shopping.html) 
and it should be working as expected.

## Save container state

Let's save the container state so that we don't have to wait for 
it to retrieve the jar files again.

Let's end the `boot tdd` process by pressing the `control` and `c` keys.

Now let's save the containers state to an image:
```bash
sudo docker commit 8e4fff31aabf moderncljs/1
```

> NOTE: Substitute in your container ID that you get from 
`sudo docker ps -a` for `8e4fff31aabf`.

Let's see that a new image with the container sate was created with:
```bash
sudo docker images
```

The results should look like:

```bash
REPOSITORY	        TAG		    IMAGE ID		    CREATED		          SIZE
moderncljs/1		latest		836f7be518c4		20 seconds ago	    719.6 MB
moderncljs/0		latest		4fa780d9c938		About an hour ago	  659.6 MB
debian			    stable		3c5f9479c5c1		4 days ago		      125.1 MB
```

##  Test container state was saved

Let's see if the state of the container really was saved.

Let's start the container:
```bash
sudo docker run -it -p 3000:3000 moderncljs/1
```

The `boot tdd` process and [shopping.html](http://localhost:3000/shopping.html) should be working.

## Remove docker containers

You can see a list of all the containers and their IDs with:
```bash
sudo docker ps -a
```

You can remove a Docker container with:
```bash
sudo docker rm b249b8b919f8
```
Where `b249b8b919f8` is the container ID.


## Remove docker images

You can see a list of all the images and their IDs with:
```bash
sudo docker images
```

You can remove a Docker image with:
```bash
sudo docker rmi 5324261f9218
```
Where `5324261f9218` is the image ID.

> NOTE: If you try to remove a image that is being used 
> by a container you will get an error message like this:
> ```bash
> Failed to remove image (5324261f9218): Error response from daemon: conflict: unable to delete
> 5324261f9218 (must be forced) - image is being used by stopped container b249b8b919f8
> ```
> You will need to remove the container first.
