FROM openjdk
MAINTAINER 	Coxy "coxy@not_my_real_email.com"
ENV JAVA_TOOL_OPTIONS="-Xmx1024m -XX:MaxPermSize=512m -Xms512m"
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV BOOT_AS_ROOT yes

RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists
RUN bash -c "cd /usr/local/bin && curl -fsSLo boot https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh && chmod 755 boot"

