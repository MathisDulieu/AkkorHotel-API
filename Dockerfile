FROM eclipse-temurin:21-jdk as build

COPY akkorhotel /AkkorHotel

WORKDIR /AkkorHotel

RUN chmod +x ./mvnw

RUN ./mvnw package -DskipTests

RUN mv -f target/*.jar /AkkorHotel.jar

FROM eclipse-temurin:21-jre

ARG PORT
ENV PORT=${PORT}

COPY --from=build /AkkorHotel.jar .

RUN useradd runtime
USER runtime

ENTRYPOINT [ "java", "-Dserver.port=${PORT}", "-jar", "AkkorHotel.jar" ]
