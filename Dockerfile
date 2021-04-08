FROM openjdk:8-jre

RUN apt-get update -y

WORKDIR .
# RUN mvn clean install

# Copy sample-maven-app to user directory
COPY ["target/large-file-processor-1.0-SNAPSHOT.jar", "large-file-processor-1.0-SNAPSHOT.jar"]
COPY ["conf/products.csv", "conf/products.csv"]


# Execute app
CMD [ "java", "-jar", "large-file-processor-1.0-SNAPSHOT.jar" ]

